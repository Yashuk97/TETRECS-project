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

    Text title = new Text(isMultiplayer ? "Multiplayer Results" : "High Scores");
    title.getStyleClass().add("scores-title");
    BorderPane.setAlignment(title, Pos.CENTER);
    mainPane.setTop(title);

    RotateTransition rotate = new RotateTransition(Duration.seconds(2), title);
    rotate.play();

    var scoresContainer = new HBox(20);
    scoresContainer.setAlignment(Pos.TOP_CENTER);
    mainPane.setCenter(scoresContainer);

    var localScoresList = new ScoresList();
    localScoresList.setId("localScores");
    var localTitle = new Text(isMultiplayer ? "Final Scores" : "Local Scores");
    localTitle.getStyleClass().add("heading");
    var localVBox = new VBox(10, localTitle, localScoresList);
    localVBox.setAlignment(Pos.TOP_CENTER);
    scoresContainer.getChildren().add(localVBox);

    VBox bottomPane = new VBox(10);
    bottomPane.setAlignment(Pos.CENTER);
    mainPane.setBottom(bottomPane);

    var statsVBox = buildStatsPanel();
    bottomPane.getChildren().add(statsVBox);

    if (isMultiplayer) {
      title.setText("Multiplayer Results");
      localScoresList.setId("multiplayerResults");
      observableScores = FXCollections.observableArrayList(game.finalScores);
      localScoresList.scoresProperty().bind(new SimpleListProperty<>(observableScores));
    } else {
      var remoteScoresList = new ScoresList();
      remoteScoresList.setId("remoteScores");
      var remoteTitle = new Text("Online Scores");
      remoteTitle.getStyleClass().add("heading");
      var remoteVBox = new VBox(10, remoteTitle, remoteScoresList);
      remoteVBox.setAlignment(Pos.TOP_CENTER);
      scoresContainer.getChildren().add(remoteVBox);

      ArrayList<Pair<String, Integer>> loadedScores = loadScores();
      observableScores = FXCollections.observableArrayList(loadedScores);
      localScoresList.scoresProperty().bind(new SimpleListProperty<>(observableScores));

      remoteScores = FXCollections.observableArrayList();
      remoteScoresList.scoresProperty().bind(new SimpleListProperty<>(remoteScores));

      communicator.send("HISCORES");
      checkAndPromptForScore();
    }

    Statistics.saveStats();
  }

  // Extracted stats panel building to its own method for clarity
  private VBox buildStatsPanel() {
    VBox statsVBox = new VBox(5);
    statsVBox.setAlignment(Pos.CENTER);
    statsVBox.setPadding(new Insets(10));

    var statsTitle = new Text("Your Statistics");
    statsTitle.getStyleClass().add("heading");

    HBox gamesPlayedBox = new HBox(10);
    // ... build HBox for gamesPlayed ...

    HBox linesClearedBox = new HBox(10);
    // ... build HBox for linesCleared ...

    HBox highestScoreBox = new HBox(10);
    // ... build HBox for highestScore ...

    HBox highestMultiplierBox = new HBox(10);
    // ... build HBox for highestMultiplier ...

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
    // Clear the previous remote scores
    remoteScores.clear();

    // Remove the "HISCORES " prefix
    String scoresData = message.substring(9);

    // Split the data into individual lines
    String[] lines = scoresData.split("\n");

    // Process each score line
    for (String line : lines) {
      String[] parts = line.split(":");
      if (parts.length == 2) {
        String name = parts[0];
        int score = Integer.parseInt(parts[1]);
        remoteScores.add(new Pair<>(name, score));
      }
    }
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