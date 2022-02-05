import auth.InMemoryAuthService;
import server.Server;

public class App {

    public static void main(String[] args) {
      new Server(new InMemoryAuthService()).start();
    }
  }

