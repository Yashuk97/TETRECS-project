package uk.ac.soton.comp1206.scene;

import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.multimedia.Multimedia;
import uk.ac.soton.comp1206.event.CommunicationsListener;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

public class LobbyScene extends BaseScene {

  private static final Logger logger = LogManager.getLogger(LobbyScene.class);

  private Timer channelTimer;
  private ObservableList<String> channels;
  private StackPane lobbyPane;
  private VBox channelBrowser;
  private BorderPane inChannelView;
  private ObservableList<String> userList;
  private Button startButton;
  private VBox chatMessages;
  private boolean isHost = false;

  private CommunicationsListener listener; // Field to hold our listener

  public LobbyScene(GameWindow gameWindow) {
    super(gameWindow);
    logger.info("Creating Lobby Scene");
  }

  @Override
  public void initialise() {
    // Create our listener. We receive the entire message as one string.
    listener = new CommunicationsListener() {
      @Override
      public void receiveCommunication(String message) {
        Platform.runLater(() -> receiveCommunication(message));
      }
    };

    // Start a timer to request the channel list periodically
    startChannelListTimer();

    scene.setOnKeyPressed(event -> {
      if (event.getCode() == KeyCode.ESCAPE) {
        shutdown(); // Clean up before leaving
        gameWindow.startMenu();
      }
    });
  }

  @Override
  public void receiveCommunication(String message) {
    logger.info("Lobby received message: {}", message);
    String[] parts = message.split(" ", 2);
    String command = parts[0];
    String data = (parts.length > 1) ? parts[1] : "";

    switch (command) {
      case "CHANNELS":
        updateChannelList(data);
        break;
      case "JOIN":
        switchToInChannelView();
        break;
      case "USERS":
        updateUserList(data);
        break;
      case "MSG":
        addChatMessage(data);
        Multimedia.playSound("message.wav");
        break;
      case "PARTED":
        switchToChannelBrowser();
        break;
      case "HOST": // Player is the host
        isHost = true;
        startButton.setDisable(false); // Enable the start button
        break;
      case "START": // The game is starting for everyone
        startGame();
        break;
      case "ERROR":
        showError("Server Error", data);
        break;
    }
  }
  private void showError(String title, String message) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle("Error");
    alert.setHeaderText(title);
    alert.setContentText(message);
    alert.showAndWait();
  }

  private void startGame() {
    logger.info("Starting multiplayer game!");
    shutdown();
    gameWindow.startMultiplayerGame();
  }

  private void startChannelListTimer() {
    channelTimer = new Timer();
    channelTimer.schedule(new TimerTask() {
      @Override
      public void run() {
        communicator.send("LIST");
      }
    }, 0, 3000);
  }

  private void switchToInChannelView() {
    // Successfully joined a channel, switch views
    if (channelTimer != null) {
      channelTimer.cancel(); // Stop polling for channels
    }
    channelBrowser.setVisible(false);
    inChannelView.setVisible(true);
    communicator.send("USERS"); // Ask for the list of users
  }

  private void switchToChannelBrowser() {
    // Left the channel, switch back to browser
    inChannelView.setVisible(false);
    channelBrowser.setVisible(true);
    startChannelListTimer(); // Restart the channel polling
  }

  private void updateUserList(String data) {
    Platform.runLater(() -> {
      userList.clear();
      String[] users = data.split("\n");
      userList.addAll(users);
    });
  }



  private void addChatMessage(String data) {
    Platform.runLater(() -> {
      // The server sends messages in the format "PlayerName:The message"
      // We split the string at the first colon to separate the name and message.
      String[] parts = data.split(":", 2);
      String playerName = parts[0];
      String messageContent = (parts.length > 1) ? parts[1] : "";

      // Create an HBox to hold the new message
      HBox messageBox = new HBox(5); // 5 is the spacing between name and message

      // Create a Text node for the player's name
      Text playerNameText = new Text(playerName + ":");
      playerNameText.getStyleClass().add("player-name"); // Assign a specific style class

      // Create a Text node for the message content
      Text messageContentText = new Text(messageContent);
      messageContentText.getStyleClass().add("player-message"); // Assign another style class

      // Add both Text nodes to the HBox
      messageBox.getChildren().addAll(playerNameText, messageContentText);

      // Add the complete messageBox to our list of chat messages
      chatMessages.getChildren().add(messageBox);
    });
  }
  private void updateChannelList(String data) {
    Platform.runLater(() -> {
      channels.clear();
      String[] channelNames = data.split("\n");
      for (String name : channelNames) {
        if (!name.isBlank()) {
          channels.add(name);
        }
      }
    });
  }

  @Override
  public void build() {
    logger.info("Building " + this.getClass().getName());
    Multimedia.playMusic("multiplayer.mp3");
    root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

    lobbyPane = new StackPane();
    lobbyPane.setMaxWidth(gameWindow.getWidth());
    lobbyPane.setMaxHeight(gameWindow.getHeight());
    lobbyPane.getStyleClass().add("menu-background");
    root.getChildren().add(lobbyPane);

    // --- View 1: Channel Browser ---
    channelBrowser = new VBox(10);
    channelBrowser.setAlignment(Pos.CENTER);
    channelBrowser.setPadding(new Insets(20));
    // FIX: Constrain the max width of the entire browser pane to prevent overflow
    channelBrowser.setMaxWidth(gameWindow.getWidth() * 0.7); // Use 70% of the window width

    var listTitle = new Text("Available Channels");
    listTitle.getStyleClass().add("lobby-title"); // FIX: Add a specific style class for coloring

    channels = FXCollections.observableArrayList();
    ListView<String> channelListView = new ListView<>(channels);
    // FIX: Make the ListView grow vertically to fill the space
    VBox.setVgrow(channelListView, Priority.ALWAYS);

    HBox controlsHBox = new HBox(10);
    controlsHBox.setAlignment(Pos.CENTER);
    TextField channelNameField = new TextField();
    channelNameField.setPromptText("Enter Channel Name");
    Button joinButton = new Button("Join");
    Button createButton = new Button("Create");
    controlsHBox.getChildren().addAll(channelNameField, joinButton, createButton);

    channelBrowser.getChildren().addAll(listTitle, channelListView, controlsHBox);
    lobbyPane.getChildren().add(channelBrowser);

    // --- View 2: In-Channel View (initially hidden) ---
    inChannelView = new BorderPane();
    inChannelView.setVisible(false);
    lobbyPane.getChildren().add(inChannelView);
    buildInChannelUI();

    // --- Actions ---
    joinButton.setOnAction(e -> {
      String selectedChannel = channelListView.getSelectionModel().getSelectedItem();
      if (selectedChannel != null) {
        communicator.send("JOIN " + selectedChannel);
      }
    });
    createButton.setOnAction(e -> {
      String channelName = channelNameField.getText();
      if (!channelName.isBlank()) {
        communicator.send("CREATE " + channelName);
      }
    });
  }

  private void buildInChannelUI() {
    var topBar = new HBox();
    topBar.setAlignment(Pos.CENTER);
    topBar.setPadding(new Insets(10));
    var channelTitle = new Text("Current Channel");
    channelTitle.getStyleClass().add("lobby-title");
    topBar.getChildren().add(channelTitle);
    inChannelView.setTop(topBar);

    VBox chatArea = new VBox(10);
    chatArea.setPadding(new Insets(10));
    inChannelView.setCenter(chatArea);

    chatMessages = new VBox(5);
    ScrollPane chatScroller = new ScrollPane(chatMessages);
    chatScroller.setFitToWidth(true);
    chatScroller.getStyleClass().add("scroller");
    VBox.setVgrow(chatScroller, Priority.ALWAYS);

    HBox messageInputBox = new HBox(10);
    TextField messageField = new TextField();
    messageField.setPromptText("Send a message...");
    HBox.setHgrow(messageField, Priority.ALWAYS);
    Button sendButton = new Button("Send");
    messageInputBox.getChildren().addAll(messageField, sendButton);
    chatArea.getChildren().addAll(chatScroller, messageInputBox);

    VBox sidebar = new VBox(20);
    sidebar.setPadding(new Insets(10));
    sidebar.setAlignment(Pos.CENTER);
    sidebar.setPrefWidth(250); // Constrain width
    inChannelView.setRight(sidebar);

    var usersTitle = new Text("Users");
    usersTitle.getStyleClass().add("heading");

    userList = FXCollections.observableArrayList();
    ListView<String> usersListView = new ListView<>(userList);

    startButton = new Button("Start Game");
    startButton.setDisable(true);

    Button leaveButton = new Button("Leave Channel");
    sidebar.getChildren().addAll(usersTitle, usersListView, startButton, leaveButton);

    sendButton.setOnAction(e -> sendMessage(messageField));
    messageField.setOnAction(e -> sendMessage(messageField));
    startButton.setOnAction(e -> communicator.send("START"));
    leaveButton.setOnAction(e -> communicator.send("PART"));

  }

  private void sendMessage(TextField field) {
    String message = field.getText();
    if (!message.isBlank()) {
      communicator.send("MSG " + message);
      field.clear();
    }

  }
  // In LobbyScene.java
  @Override
  public void shutdown() {
    if (channelTimer != null) {
      channelTimer.cancel();
      channelTimer = null;
    }
    if (listener != null) {
      communicator.removeListener(listener);
    }
    Multimedia.stopMusic(); // <-- ADD THIS LINE
    logger.info("Lobby has been shut down.");
  }



}