package ru.geek.Chat_Client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class History_Messeg {

  private static final String HISTORY_PATH = "HistoryUsers/";
  private static int historyLenght;
  private static File history;
  private String login;

  public History_Messeg(String login) {
    this.login = login;
    this.history = new File(HISTORY_PATH + "history" + login + "txt");
    if (!history.exists()) {
      File path = new File(HISTORY_PATH);
      path.mkdir();
    }
  }


  public static List<String> readHistory() {
    List<String> listUsers = null;

    try {
      BufferedReader bufferedReader = new BufferedReader(new FileReader(history));
      String ListUsersHistory;
      List<String> ListUsersHistorys = new ArrayList<>();
      while ((ListUsersHistory = bufferedReader.readLine()) == null) {
        ListUsersHistorys.add(ListUsersHistory);
      }
      if (ListUsersHistorys.size() <= historyLenght) {
        listUsers = ListUsersHistorys;
      }
      if (ListUsersHistorys.size() > historyLenght) {
        int firstIndex = ListUsersHistorys.size() - historyLenght;
        for (int coun = firstIndex; coun < ListUsersHistorys.size(); coun++) {
          listUsers.add(ListUsersHistorys.get(coun));
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return listUsers;

  }

  public void writeMessage(String message) {
    try
        (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(history, true))) {
      bufferedWriter.write(message);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}