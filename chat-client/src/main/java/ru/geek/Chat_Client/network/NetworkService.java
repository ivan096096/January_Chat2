package ru.geek.Chat_Client.network;


  import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

  public class NetworkService {
    private static final String HOST = "127.0.0.1";
    private static final int PORT = 8189;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private MessageProcessor messageProcessor;

    public NetworkService(MessageProcessor messageProcessor)  {
      this.messageProcessor = messageProcessor;
    }

    public void connect() throws IOException {
      this.socket = new Socket(HOST, PORT);
      this.in = new DataInputStream(socket.getInputStream());
      this.out = new DataOutputStream(socket.getOutputStream());
      readMessages();
    }

    public void readMessages() {
      var thread = new Thread(() -> {
        try {
          while (!Thread.currentThread().isInterrupted() && !socket.isClosed()) {
            var message = in.readUTF();
            messageProcessor.processMessage(message);
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
      });
      thread.setDaemon(true);
      thread.start();
    }

    public void sendMessage(String message) {
      try {
        out.writeUTF(message);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    public boolean isConnected() {
      return socket != null && !socket.isClosed();
    }

    public void close() {
      try {
        if (socket != null && !socket.isClosed()) {
          socket.close();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }


  }


