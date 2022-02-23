package entity;

import error.ChangeNickExeption;
import error.WrongCredentialsException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDataDaseService {


  private static final String DRIVER = "org.sqlite.JDBC";
  private static final String CONNECTION = "jdbc:sqlite:db/dbUsers.db";
  private static final String Login = "select username from clients where login = ? and password = ?;";
  private static final String CHANGE_Login = "update clients set username = ? where login = ?;";
  private static UserDataDaseService insuserDataDaseServiceance;
  private Connection connection;
  PreparedStatement getClientStatement;
  PreparedStatement changeNickStatement;

  public UserDataDaseService() {

    try {
      connect();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    } catch (SQLException e) {
      e.printStackTrace();
    }


  }

  public static UserDataDaseService getInstance() {
    if (insuserDataDaseServiceance != null) {
      return insuserDataDaseServiceance;
    }
    insuserDataDaseServiceance = new UserDataDaseService();
    return insuserDataDaseServiceance;
  }

  public String changeNick(String login, String newNick) {
    try {
      changeNickStatement.setString(1, newNick);
      changeNickStatement.setString(2, login);
      if (changeNickStatement.executeUpdate() > 0) {
        return newNick;
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    throw new ChangeNickExeption("Something went wrong with nickname change");
  }

  public String getClientNameByLoginPass(String login, String password) {
    try {
      getClientStatement.setString(1, login);
      getClientStatement.setString(2, password);
      ResultSet rs = getClientStatement.executeQuery();
      if (rs.next()) {
        String result = rs.getString("username");
        rs.close();
        System.out.printf("Логин - это: %s\n", result);
        return result;
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    throw new WrongCredentialsException("Пользователь не найден");
  }

  private void connect() throws ClassNotFoundException, SQLException {
    Class.forName(DRIVER);
    connection = DriverManager.getConnection(CONNECTION);
    System.out.println("Подключение к базе данных!");
    getClientStatement = connection.prepareStatement(Login);
    changeNickStatement = connection.prepareStatement(CHANGE_Login);
  }

  public void closeConnection() {
    try {
      if (getClientStatement != null) {
        getClientStatement.close();
      }
      if (changeNickStatement != null) {
        changeNickStatement.close();
      }
      if (connection != null) {
        connection.close();
      }
      System.out.println("Отключение от базы данных!");
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
}



