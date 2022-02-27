package ru.geek.Chat_Client;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import ru.geek.Chat_Client.network.MessageProcessor;
import ru.geek.Chat_Client.network.NetworkService;


import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class MainChatController implements Initializable, MessageProcessor {


  @FXML
  private VBox changeNickPanel;

  @FXML
  private TextField newNickField;

  @FXML
  private VBox changePasswordPanel;

  @FXML
  private PasswordField oldPassField;

  @FXML
  private PasswordField newPasswordField;

  @FXML
  private VBox loginPanel;

  @FXML
  private TextField loginField;

  @FXML

  public TextArea mainChatArea;
  @FXML

  public ListView contactList;
  @FXML

  public TextField inputField;
  @FXML

  public Button btnSend;


  @FXML
  public PasswordField passwordField;

  @FXML
  public VBox mainChatPanel;

  private History_Messeg historyMesseg;
  private String nick;
  private NetworkService networkService;
  public static final String REGEX = "%!%";


  public void sendMessage(ActionEvent actionEvent) {
    var message = inputField.getText();
    if (message.isBlank()) {
      return;
    }
    var recipient = contactList.getSelectionModel().getSelectedItem();
    if (recipient.equals("ALL")) {
      networkService.sendMessage("/broadcast" + REGEX + message);
    } else {
      networkService.sendMessage("/p" + REGEX + recipient + REGEX + message);
    }
    historyMesseg.writeMessage(String.format("[ME] %s\n", message));
    inputField.clear();
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    this.networkService = new NetworkService(this);
  }

  @Override
  public void processMessage(String message) {
    Platform.runLater(() -> parseIncomingMessage(message));
  }

  private void parseIncomingMessage(String message) {
    var splitMessage = message.split(REGEX);
    switch (splitMessage[0]) {
      case "/auth_ok":
        this.nick = splitMessage[1];
        loginPanel.setVisible(false);
        mainChatPanel.setVisible(true);
        this.historyMesseg = new History_Messeg(nick);
        var history = historyMesseg.readHistory();
        for (String s : history) {
          mainChatArea.appendText(s + System.lineSeparator());
        }
        break;
      case "/error":
        showError(splitMessage[1]);
        System.out.println("got error " + splitMessage[1]);
        break;
      case "/list":
        var contacts = new ArrayList<String>();
        contacts.add("ALL");
        for (int i = 1; i < splitMessage.length; i++) {
          contacts.add(splitMessage[i]);
        }
        contactList.setItems(FXCollections.observableList(contacts));
        contactList.getSelectionModel().selectFirst();
        break;
      case "/change_pass_ok":
        changePasswordPanel.setVisible(false);
        mainChatPanel.setVisible(true);
        break;
      default:
        mainChatArea.appendText(splitMessage[0] + System.lineSeparator());
        historyMesseg.writeMessage(splitMessage[0] + System.lineSeparator());
        break;
    }
  }

  private void showError(String message) {
    var alert = new Alert(Alert.AlertType.ERROR,
        "An error occured: " + message,
        ButtonType.OK);
    alert.showAndWait();
  }

  public void sendAuth(ActionEvent actionEvent) {
    var login = loginField.getText();
    var password = passwordField.getText();
    if (login.isBlank() || password.isBlank()) {
      return;
    }

    var message = "/auth" + REGEX + login + REGEX + password;

    if (!networkService.isConnected()) {
      try {
        networkService.connect();
      } catch (IOException e) {
        e.printStackTrace();
        showError(e.getMessage());

      }
    }

    networkService.sendMessage(message);
  }

  public void sendChangeNick(ActionEvent actionEvent) {
    if (newNickField.getText().isBlank()) {
      return;
    }
    networkService.sendMessage("/change_nick" + REGEX + newNickField.getText());
  }

  public void sendChangePass(ActionEvent actionEvent) {
    if (newPasswordField.getText().isBlank() || oldPassField.getText().isBlank()) {
      return;
    }
    networkService.sendMessage(
        "/change_pass" + REGEX + oldPassField.getText() + REGEX + newPasswordField.getText());
  }

  public void sendEternalLogout(ActionEvent actionEvent) {
    networkService.sendMessage("/remove");
  }
  public void returnToChat(ActionEvent actionEvent) {
    changeNickPanel.setVisible(false);
    changePasswordPanel.setVisible(false);
    mainChatPanel.setVisible(true);
  }

  public void showChangeNick(ActionEvent actionEvent) {
    mainChatPanel.setVisible(false);
    changeNickPanel.setVisible(true);
  }

  public void showChangePass(ActionEvent actionEvent) {
    mainChatPanel.setVisible(false);
    changePasswordPanel.setVisible(true);
  }

  public void connectToServer(ActionEvent actionEvent) {
  }

  public void disconnectFromServer(ActionEvent actionEvent) {

  }

  public void mockAction(ActionEvent actionEvent) {
  }

  public void exit(ActionEvent actionEvent) {
  }

  public void showHelp(ActionEvent actionEvent) {
  }

  public void showAbout(ActionEvent actionEvent) {
  }

}