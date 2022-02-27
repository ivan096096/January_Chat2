package entity;

import error.ChangeNickExeption;
import error.WrongCredentialsException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class UserDataDaseService {

  private static final String DRIVER = "org.sqlite.JDBC";
  private static final String CONNECTION = "jdbc:sqlite:db/dbUsers.db";
  private static final String GET_USERNAME = "select username from clients where login = ? and password = ?;";
  private static final String CHANGE_USERNAME = "update clients set username = ? where login = ?;";
  private static final String CREATE_DB = "create table if not exists clients (id integer primary key autoincrement," +
      " login text unique not null, password text not null, username text unique not null);";
  private static final String INIT_DB = "insert into clients (login, password, username) " +
      "values ('log1', 'pass1', 'user1'), ('log2', 'pass2', 'user2'), ('log3', 'pass3', 'user3');";
  private static UserDataDaseService instance;

  private Connection connection;
  PreparedStatement getClientStatement;
  PreparedStatement changeNickStatement;

  private UserDataDaseService() {
    try {
      connect();
    } catch (ClassNotFoundException | SQLException e) {
      e.printStackTrace();
    }
//        createDb();
  }

  public static UserDataDaseService getInstance() {
    if (instance != null) return instance;
    instance = new UserDataDaseService();
    return instance;
  }

  public String changeNick(String login, String newNick)  {
    try  {
      changeNickStatement.setString(1, newNick);
      changeNickStatement.setString(2, login);
      if (changeNickStatement.executeUpdate() > 0) return newNick;
    } catch (SQLException e) {
      e.printStackTrace();
    }
    throw new ChangeNickExeption("Something went wrong with nickname change");
  }

  public String getClientNameByLoginPass(String login, String pass) {
    try {
      getClientStatement.setString(1, login);
      getClientStatement.setString(2, pass);
      ResultSet rs = getClientStatement.executeQuery();
      if (rs.next()) {
        String result = rs.getString("username");
        rs.close();
        System.out.printf("login is: %s\n", result);
        return result;
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    throw new WrongCredentialsException("User not found");
  }

  private void createDb() {
    try (Statement st = connection.createStatement()) {
      st.execute(CREATE_DB);
      st.execute(INIT_DB);
    } catch (SQLException throwables) {
      throwables.printStackTrace();
    }
  }

  private void connect() throws ClassNotFoundException, SQLException {
    Class.forName(DRIVER);
    connection = DriverManager.getConnection(CONNECTION);
    System.out.println("Connected to db!");
    getClientStatement = connection.prepareStatement(GET_USERNAME);
    changeNickStatement = connection.prepareStatement(CHANGE_USERNAME);
  }

  public void closeConnection() {
    try {
      if (getClientStatement != null) getClientStatement.close();
      if (changeNickStatement != null) changeNickStatement.close();
      if (connection != null) connection.close();
      System.out.println("Disconnected from db!");
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
}

