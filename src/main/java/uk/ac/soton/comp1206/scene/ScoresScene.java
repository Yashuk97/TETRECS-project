package uk.ac.soton.comp1206.scene;

import javafx.animation.PauseTransition;
import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.ScoresList;
import uk.ac.soton.comp1206.event.CommunicationsListener;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.MultiplayerGame;
import uk.ac.soton.comp1206.multimedia.Multimedia;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.utility.Statistics;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import javafx.scene.control.ScrollPane;


public class ScoresScene extends BaseScene {

  private static final Logger logger = LogManager.getLogger(ScoresScene.class);
  private final Game game;
  private final boolean isMultiplayer;

  private ObservableList<Pair<String, Integer>> observableScores;
  private ObservableList<Pair<String, Integer>> remoteScores;
  private ListChangeListener<Pair<String, Integer>> scoreListener;
  private boolean onlineScoresReceived = false;
  private BorderPane mainPane;

  public ScoresScene(GameWindow gameWindow, Game game) {
    super(gameWindow);
    this.game = game;
    this.isMultiplayer = (game instanceof MultiplayerGame);
  }

  @Override
  public void initialise() {
    scene.setOnKeyPressed(event -> {
      if (event.getCode() == KeyCode.ESCAPE) {
        gameWindow.startMenu();
      }
    });

    // Only listen for online scores if it's a single player game
    if (!isMultiplayer) {
      communicator.addListener(this);
    }
  }

  @Override
  public void shutdown() {
    // Remove listener when scene is shut down
    communicator.removeListener(this);
  }

  @Override
  public void build() {
    logger.info("Building " + this.getClass().getName());
    Multimedia.playMusic("end.wav");

    root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

    var scoresPane = new StackPane();
    scoresPane.setMaxWidth(gameWindow.getWidth());
    scoresPane.setMaxHeight(gameWindow.getHeight());
    scoresPane.getStyleClass().add("menu-background");
    root.getChildren().add(scoresPane);

    var scroller = new ScrollPane();
    scroller.setFitToWidth(true);
    scroller.setFitToHeight(true);
    scroller.getStyleClass().add("scroller");
    scoresPane.getChildren().add(scroller);

    mainPane = new BorderPane();
    scroller.setContent(mainPane);

    Text title = new Text(); // Title text will be set below
    title.getStyleClass().add("scores-title");
    BorderPane.setAlignment(title, Pos.CENTER);
    mainPane.setTop(title);

    RotateTransition rotate = new RotateTransition(Duration.seconds(2), title);
    rotate.play();

    // --- Build and place the bottom panel (stats and maybe name prompt) ---
    VBox bottomPane = new VBox(10);
    bottomPane.setAlignment(Pos.CENTER);
    mainPane.setBottom(bottomPane);

    var statsVBox = buildStatsPanel();
    bottomPane.getChildren().add(statsVBox);

    // --- Handle Multiplayer vs Single Player Layout and Logic ---
    if (isMultiplayer) {
      // --- MULTIPLAYER LAYOUT ---
      title.setText("Multiplayer Results");

      var finalScoresList = new ScoresList();
      finalScoresList.setId("multiplayerResults");
      var finalScoresTitle = new Text("Final Scores");
      finalScoresTitle.getStyleClass().add("heading");

      var finalScoresVBox = new VBox(10, finalScoresTitle, finalScoresList);
      finalScoresVBox.setAlignment(Pos.CENTER);
      finalScoresVBox.setPadding(new Insets(20));

      mainPane.setCenter(finalScoresVBox); // Place in the CENTER

      observableScores = FXCollections.observableArrayList(game.finalScores);
      finalScoresList.setScores(observableScores);

    } else {
      // --- SINGLE-PLAYER LAYOUT ---
      title.setText("High Scores");

      var localScoresList = new ScoresList();
      localScoresList.setId("localScores");
      var localTitle = new Text("Local Scores");
      localTitle.getStyleClass().add("heading");
      var localVBox = new VBox(10, localTitle, localScoresList);
      localVBox.setAlignment(Pos.CENTER);
      localVBox.setPadding(new Insets(20));
      mainPane.setLeft(localVBox); // Place on the LEFT

      var remoteScoresList = new ScoresList();
      remoteScoresList.setId("remoteScores");
      var remoteTitle = new Text("Online Scores");
      remoteTitle.getStyleClass().add("heading");
      var remoteVBox = new VBox(10, remoteTitle, remoteScoresList);
      remoteVBox.setAlignment(Pos.CENTER);
      remoteVBox.setPadding(new Insets(20));
      mainPane.setRight(remoteVBox); // Place on the RIGHT

      // Load and set data for both lists
      ArrayList<Pair<String, Integer>> loadedScores = loadScores();
      observableScores = FXCollections.observableArrayList(loadedScores);
      localScoresList.setScores(observableScores);

      remoteScores = FXCollections.observableArrayList();
      remoteScoresList.setScores(remoteScores);

      communicator.send("HISCORES");
      checkAndPromptForScore();
    }

    Statistics.saveStats();
  }

  // Extracted stats panel building to its own method for clarity
  /**
   * Creates and returns a VBox containing the full statistics display panel.
   * @return A VBox with all the statistics UI elements.
   */
  private VBox buildStatsPanel() {
    // Main container for the entire stats section
    VBox statsVBox = new VBox(10); // Spacing between title and individual stat lines
    statsVBox.setAlignment(Pos.CENTER);
    statsVBox.setPadding(new Insets(20));

    // Title for the panel
    var statsTitle = new Text("Your Statistics");
    statsTitle.getStyleClass().add("heading");

    // -- Games Played Stat --
    HBox gamesPlayedBox = new HBox(10); // Spacing between heading and value
    gamesPlayedBox.setAlignment(Pos.CENTER);
    Label gamesPlayedHeading = new Label("Games Played:");
    gamesPlayedHeading.getStyleClass().add("stats-heading");
    Label gamesPlayedValue = new Label();
    gamesPlayedValue.getStyleClass().add("stats-item");
    gamesPlayedValue.textProperty().bind(Statistics.gamesPlayed.asString());
    gamesPlayedBox.getChildren().addAll(gamesPlayedHeading, gamesPlayedValue);

    // -- Lines Cleared Stat --
    HBox linesClearedBox = new HBox(10);
    linesClearedBox.setAlignment(Pos.CENTER);
    Label linesClearedHeading = new Label("Total Lines Cleared:");
    linesClearedHeading.getStyleClass().add("stats-heading");
    Label linesClearedValue = new Label();
    linesClearedValue.getStyleClass().add("stats-item");
    linesClearedValue.textProperty().bind(Statistics.linesCleared.asString());
    linesClearedBox.getChildren().addAll(linesClearedHeading, linesClearedValue);

    // -- Highest Score Stat --
    HBox highestScoreBox = new HBox(10);
    highestScoreBox.setAlignment(Pos.CENTER);
    Label highestScoreHeading = new Label("Highest Score:");
    highestScoreHeading.getStyleClass().add("stats-heading");
    Label highestScoreValue = new Label();
    highestScoreValue.getStyleClass().add("stats-item");
    highestScoreValue.textProperty().bind(Statistics.highestScore.asString());
    highestScoreBox.getChildren().addAll(highestScoreHeading, highestScoreValue);

    // -- Highest Multiplier Stat --
    HBox highestMultiplierBox = new HBox(10);
    highestMultiplierBox.setAlignment(Pos.CENTER);
    Label highestMultiplierHeading = new Label("Highest Multiplier:");
    highestMultiplierHeading.getStyleClass().add("stats-heading");
    Label highestMultiplierValue = new Label();
    highestMultiplierValue.getStyleClass().add("stats-item");
    highestMultiplierValue.textProperty().bind(Bindings.format("%dx", Statistics.highestMultiplier));
    highestMultiplierBox.getChildren().addAll(highestMultiplierHeading, highestMultiplierValue);

    // Add all the individual stat HBoxes to the main VBox container
    statsVBox.getChildren().addAll(statsTitle, gamesPlayedBox, linesClearedBox, highestScoreBox, highestMultiplierBox);

    return statsVBox;
  }
  /**
   * Displays the name input field and submit button.
   * @param isOnlineScore true if this score will be submitted online
   */

  private void promptForName(boolean isOnlineScore) {
    // Get the bottom container VBox from the mainPane
    VBox bottomPane = (VBox) mainPane.getBottom();

    // Create the name input box
    VBox inputBox = new VBox(10);
    inputBox.setAlignment(Pos.CENTER);
    inputBox.setPadding(new Insets(20));

    Text promptText = new Text("Save your score by entering your name.");
    promptText.getStyleClass().add("heading");

    TextField nameField = new TextField();
    nameField.setPromptText("Your Name");
    nameField.setMaxWidth(300);

    inputBox.getChildren().addAll(promptText, nameField);

    // Add the input box to the bottom container (below the stats)
    bottomPane.getChildren().add(inputBox);

    // Add handler for the ENTER key
    nameField.setOnKeyPressed(event -> {
      if (event.getCode() == KeyCode.ENTER) {
        Multimedia.playSound("save.wav"); // Use a suitable sound
        String name = nameField.getText().isBlank() ? "Player" : nameField.getText();
        addNewScore(name, game.scoreProperty().get(), isOnlineScore);

        // Remove the input box, leaving the stats panel
        bottomPane.getChildren().remove(inputBox);
      }
    });

    Platform.runLater(nameField::requestFocus);
  }
  /**
   * Handles messages received from the server.
   * @param message the message from the server
   */
  @Override
  public void receiveCommunication(String message) {
    logger.info("Received from server: {}", message);

    // Check if the message is a high scores list
    if (message.startsWith("HISCORES")) { // Correct
      parseScores(message);
    }
  }

  /**
   * Parses a HISCORES message and updates the remote scores list.
   * @param message the HISCORES message
   */
  private void parseScores(String message) {
    // Create a temporary list to hold the parsed scores.
    ArrayList<Pair<String, Integer>> newScores = new ArrayList<>();

    String scoresData = message.substring(9);
    String[] lines = scoresData.split("\n");

    for (String line : lines) {
      String[] parts = line.split(":");
      if (parts.length == 2) {
        String name = parts[0];
        int score = Integer.parseInt(parts[1].trim()); // Use trim() for safety
        newScores.add(new Pair<>(name, score));
      }
    }

    // Now, update the main observable list in a single, atomic operation.
    // This will fire the listeners only ONCE, after all data is ready.
    Platform.runLater(() -> {
      remoteScores.setAll(newScores);
    });
  }
  /**
   * Checks if the player's score is a new high score and shows a prompt if it is.
   * Waits for remote scores to be loaded first.
   */
  private void checkAndPromptForScore() {
    // --- LOCAL SCORE CHECK (IMMEDIATE) ---
    int lowestLocalScore = 0;
    if (observableScores.size() >= 10) {
      lowestLocalScore = observableScores.get(9).getValue();
    }
    boolean isLocalHighScore = (game.scoreProperty().get() > lowestLocalScore || observableScores.size() < 10);

    // --- ONLINE SCORE LISTENER ---
    scoreListener = c -> {
      onlineScoresReceived = true; // Set the flag to true
      c.getList().removeListener(scoreListener); // Still remove the listener to prevent re-entry

      int lowestOnlineScore = 0;
      if (!remoteScores.isEmpty()) {
        lowestOnlineScore = remoteScores.get(remoteScores.size() - 1).getValue();
      }
      boolean isOnlineHighScore = (game.scoreProperty().get() > lowestOnlineScore || remoteScores.size() < 10);

      if (isOnlineHighScore) {
        logger.info("New ONLINE high score detected!");
        promptForName(true);
      } else if (isLocalHighScore) {
        logger.info("New LOCAL high score detected (after online check)!");
        promptForName(false);
      }
    };
    remoteScores.addListener(scoreListener);

    // --- FAILSAFE TIMER ---
    PauseTransition failsafe = new PauseTransition(Duration.seconds(2));
    failsafe.setOnFinished(e -> {
      // After 2 seconds, check the flag.
      if (!onlineScoresReceived) {
        logger.warn("Online scores did not arrive in time. Checking local scores only.");
        remoteScores.removeListener(scoreListener); // Clean up the listener
        if (isLocalHighScore) {
          promptForName(false);
        }
      }
    });
    failsafe.play();
  }

  /**
   * Adds a new score to the list(s), saves, and optionally submits online.
   * @param name the player's name
   * @param score the player's score
   * @param isOnlineScore true if the score should be submitted to the server
   */
  private void addNewScore(String name, int score, boolean isOnlineScore) {
    // Add to local scores regardless
    observableScores.add(new Pair<>(name, score));
    observableScores.sort((a, b) -> b.getValue().compareTo(a.getValue()));
    if (observableScores.size() > 10) observableScores.remove(10);
    saveScores(new ArrayList<>(observableScores));

    if (isOnlineScore) {
      // Add to remote scores visually and submit to server
      remoteScores.add(new Pair<>(name, score));
      remoteScores.sort((a, b) -> b.getValue().compareTo(a.getValue()));
      if (remoteScores.size() > 10) remoteScores.remove(10);

      communicator.send("HISCORE " + name + ":" + score);
    }
  }
  /**
   * Loads scores from the local scores.txt file.
   * @return An ArrayList of Pairs, where each Pair is a name and a score.
   */
  private ArrayList<Pair<String, Integer>> loadScores() {
    ArrayList<Pair<String, Integer>> loadedScores = new ArrayList<>();
    try (BufferedReader reader = new BufferedReader(new FileReader("scores.txt"))) {
      String line;
      while ((line = reader.readLine()) != null) {
        String[] parts = line.split(":");
        if (parts.length == 2) {
          String name = parts[0];
          int score = Integer.parseInt(parts[1]);
          loadedScores.add(new Pair<>(name, score));
        }
      }
    } catch (IOException e) {
      logger.error("Failed to load scores: {}", e.getMessage());
      // If the file doesn't exist, we'll just return an empty list.
    }
    // Sort scores in descending order
    loadedScores.sort((a, b) -> b.getValue().compareTo(a.getValue()));
    return loadedScores;
  }

  /**
   * Saves the provided list of scores to the scores.txt file.
   * @param scores The list of scores to save.
   */
  private void saveScores(ArrayList<Pair<String, Integer>> scores) {
    try (FileWriter writer = new FileWriter("scores.txt")) {
      for (Pair<String, Integer> scoreEntry : scores) {
        writer.write(scoreEntry.getKey() + ":" + scoreEntry.getValue() + "\n");
      }
    } catch (IOException e) {
      logger.error("Failed to save scores: {}", e.getMessage());
    }
  }

}