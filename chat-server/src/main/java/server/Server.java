package server;


import auth.AuthService;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {

  public static final String REGEX = "%!%";
  private static final int PORT = 8189;
  private AuthService authService;
  private List<ClientHandler> clientHandlers;

  public Server(AuthService authService) {
    this.clientHandlers = new ArrayList<>();
    this.authService = authService;
  }

  public void start() {
      try (ServerSocket serverSocket = new ServerSocket(PORT)) {
        System.out.println("Server start!");
        while (true) {
          System.out.println("Waiting for connection......");
          Socket socket = serverSocket.accept();
          System.out.println("Client connected");
          ClientHandler clientHandler = new ClientHandler(socket, this);
          clientHandler.handle();

        }
      } catch (IOException e) {
        e.printStackTrace();
      } finally {
        authService.stop();
        shutdown();
      }

  }

  public void privateMessage(String sender, String recipient, String message, ClientHandler senderHandler) {
    var handler = getHandlerByUser(recipient);
    if (handler == null) {
      senderHandler.send(String.format("/error%s recipient not found: %s", REGEX, recipient));
      return;
    }
    message = String.format("[PRIVATE] [%s] -> [%s]: %s", sender, recipient, message);
    handler.send(message);
    senderHandler.send(message);
  }

  private ClientHandler getHandlerByUser(String username) {
    for (ClientHandler clientHandler : clientHandlers) {
      if (clientHandler.getUserNick().equals(username)) {
        return clientHandler;
      }
    }
    return null;
  }


  public void broadcastMessage(String from, String message) {
    message = "/broadcast" + REGEX + from + REGEX + message;
    for (ClientHandler clientHandler : clientHandlers) {
      clientHandler.send(message);
    }
  }

  public synchronized void addAuthorizedClientToList(ClientHandler clientHandler) {
    clientHandlers.add(clientHandler);
    sendOnlineClients();
  }

  public synchronized void removeAuthorizedClientToList(ClientHandler clientHandler) {
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
}