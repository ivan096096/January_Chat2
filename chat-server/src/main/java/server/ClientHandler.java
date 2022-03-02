package server;

import Property.PropertyReader;
import error.WrongCredentialsException;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ClientHandler {
  private final long authTimeout;
  private Socket socket;
  private DataOutputStream out;
  private DataInputStream in;
  private Thread handlerThread;
  private ExecutorService cachedService;
  private Server server;
  private String user;
  public ClientHandler(Socket socket, Server server) {
    authTimeout = PropertyReader.getInstance().getAuthTimeout();
    try {
      this.cachedService = Executors.newCachedThreadPool();
      this.server = server;
      this.socket = socket;
      this.in = new DataInputStream(socket.getInputStream());
      this.out = new DataOutputStream(socket.getOutputStream());
      System.out.println("Handler created");
    } catch (IOException e) {
      System.out.println("Connection broken with user " + user);
    }
  }


  public void handle() {
    cachedService.execute(()->{
      authorize();
      while (!Thread.currentThread().isInterrupted() && !socket.isClosed()) {
        try {
          var message = in.readUTF();
          handleMessage(message);
        } catch (IOException e) {
          System.out.println("Connection broken with user " + user);
          server.removeAuthorizedClientFromList(this);
        }
      }
    });
    cachedService.shutdown();
  }

  private void handleMessage(String message) {
    var splitMessage = message.split(Server.REGEX);
    try {
      switch (splitMessage[0]) {
        case "/w":
          server.privateMessage(this.user, splitMessage[1], splitMessage[2], this);
          break;
        case "/broadcast":
          server.broadcastMessage(user, splitMessage[1]);
          break;
        case "/change_nick":
          String nick = server.getAuthService().changeNick(this.user, splitMessage[1]);
          server.removeAuthorizedClientFromList(this);
          this.user = nick;
          server.addAuthorizedClientToList(this);
          send("/change_nick_ok");
          break;
        case "/change_pass":
          server.getAuthService().changePassword(this.user, splitMessage[1], splitMessage[2]);
          send("/change_pass_ok");
          break;
        case "/remove":
          server.getAuthService().deleteUser(splitMessage[1], splitMessage[2]);
          this.socket.close();
          break;
        case "/register":
          server.getAuthService().createNewUser(splitMessage[1], splitMessage[2], splitMessage[3]);
          send("register_ok:");
          break;
      }
    } catch (IOException e) {
      send("/error" + Server.REGEX + e.getMessage());
    }
  }
  private void authorize() {
    System.out.println("Authorizing");
    var timer = new Timer(true);
    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        try {
          if (user == null) {
            send("/error" + Server.REGEX + "Authentication timeout!\nPlease, try again later!");
            Thread.sleep(50);
            socket.close();
            System.out.println("Connection with client closed");
          }
        } catch (InterruptedException | IOException e) {
          e.getStackTrace();
        }
      }
    }, authTimeout);
    try {
      while (!socket.isClosed()) {
        var message = in.readUTF();
        if (message.startsWith("/auth")) {
          var parsedAuthMessage = message.split(Server.REGEX);
          var response = "";
          String nickname = null;
          try {
            nickname = server.getAuthService().authorizeUserByLoginAndPassword(parsedAuthMessage[1], parsedAuthMessage[2]);
          } catch (WrongCredentialsException e) {
            response = "/error" + Server.REGEX + e.getMessage();
            System.out.println("Wrong credentials, nick " + parsedAuthMessage[1]);
          }
          if (server.isNickBusy(nickname)) {
            response = "/error" + Server.REGEX + "this client already connected";
            System.out.println("Nick busy " + nickname);
          }
          if (!response.equals("")) {
            send(response);
          } else {
            this.user = nickname;
            server.addAuthorizedClientToList(this);
            send("/auth_ok" + Server.REGEX + nickname);
            break;
          }
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  public void send(String msg) {
    try {
      out.writeUTF(msg);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  public Thread getHandlerThread() {
    return handlerThread;
  }
  public String getUserNick() {
    return this.user;
  }
}

