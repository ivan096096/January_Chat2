import auth.DataBaseService;
import server.Server;

public class App {

  public static void main(String[] args) {
    new Server(new DataBaseService()).start();
  }
}

