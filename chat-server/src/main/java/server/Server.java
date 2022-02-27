package server;


import Property.PropertyReader;
import auth.AuthService;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;



  public class Server {

    public static final String REGEX = "%!%";
    private final int port;
    private final AuthService authService;
    private final List<ClientHandler> clientHandlers;

    public Server(AuthService authService) {
      port = PropertyReader.getInstance().getPort();
      this.clientHandlers = new ArrayList<>();
      this.authService = authService;
    }

    public void start() {
      try (ServerSocket serverSocket = new ServerSocket(port)) {
        System.out.println("Server start!");
        authService.start();
        while (true) {
          System.out.println("Waiting for connection......");
          var socket = serverSocket.accept();
          System.out.println("Client connected");
          var clientHandler = new ClientHandler(socket, this);
          clientHandler.handle();
        }
      } catch (IOException e) {
        e.printStackTrace();
      } finally {
        authService.stop();
        shutdown();
      }
    }

    public void privateMessage(String sender, String recipient, String message,
        ClientHandler senderHandler) {
      var handler = getHandlerByUser(recipient);
      if (handler == null) {
        senderHandler.send(String.format("/error%s recipient not found: %s", REGEX, recipient));
        return;
      }
      message = String.format("[PRIVATE] [%s] -> [%s]: %s", sender, recipient, message);
      handler.send(message);
      senderHandler.send(message);
    }

    public void broadcastMessage(String from, String message) {
      message = String.format("[%s]: %s", from, message);
      for (ClientHandler clientHandler : clientHandlers) {
        clientHandler.send(message);
      }
    }

    public synchronized void addAuthorizedClientToList(ClientHandler clientHandler) {
      clientHandlers.add(clientHandler);
      sendOnlineClients();
    }

    public synchronized void removeAuthorizedClientFromList(ClientHandler clientHandler) {
      clientHandlers.remove(clientHandler);
      sendOnlineClients();
    }

    public void sendOnlineClients() {
      var sb = new StringBuilder("/list");
      sb.append(REGEX);
      for (ClientHandler clientHandler : clientHandlers) {
        sb.append(clientHandler.getUserNick());
        sb.append(REGEX);
      }
      var message = sb.toString();
      for (ClientHandler clientHandler : clientHandlers) {
        clientHandler.send(message);
      }
    }

    public synchronized boolean isNickBusy(String nick) {
      for (ClientHandler clientHandler : clientHandlers) {
        if (clientHandler.getUserNick().equals(nick)) {
          return true;
        }
      }
      return false;
    }

    private void shutdown() {

    }

    public AuthService getAuthService() {
      return authService;
    }

    private ClientHandler getHandlerByUser(String username) {
      for (ClientHandler clientHandler : clientHandlers) {
        if (clientHandler.getUserNick().equals(username)) {
          return clientHandler;
        }
      }
      return null;
    }
  }
