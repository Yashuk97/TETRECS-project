package uk.ac.soton.comp1206.scene;

import java.io.File;
import javafx.animation.PauseTransition;
import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
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

public class ScoresScene extends BaseScene {

  private static final Logger logger = LogManager.getLogger(ScoresScene.class);
  private final Game game;
  private final boolean isMultiplayer;

  private ObservableList<Pair<String, Integer>> observableScores;
  private ObservableList<Pair<String, Integer>> remoteScores;
  private ListChangeListener<Pair<String, Integer>> scoreListener;
  private boolean onlineScoresReceived = false;
  private BorderPane mainPane;
  private final String scoresFilePath;


  public ScoresScene(GameWindow gameWindow, Game game) {
    super(gameWindow);
    this.game = game;
    this.isMultiplayer = (game instanceof MultiplayerGame);

    String gameDataFolder = System.getProperty("user.home") + "/.TetrECS";
    this.scoresFilePath = gameDataFolder + "/scores.txt";
    new File(gameDataFolder).mkdirs(); // Ensure the directory exists
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

    VBox bottomPane = new VBox(10);
    bottomPane.setAlignment(Pos.CENTER);
    mainPane.setBottom(bottomPane);

    var statsVBox = buildStatsPanel();
    bottomPane.getChildren().add(statsVBox);

    if (isMultiplayer) {
      var finalScoresList = new ScoresList();
      finalScoresList.setId("multiplayerResults");
      var finalScoresTitle = new Text("Final Scores");
      finalScoresTitle.getStyleClass().add("heading");
      var finalScoresVBox = new VBox(10, finalScoresTitle, finalScoresList);
      finalScoresVBox.setAlignment(Pos.CENTER);
      finalScoresVBox.setPadding(new Insets(20));
      mainPane.setCenter(finalScoresVBox);

      observableScores = FXCollections.observableArrayList(game.finalScores);
      finalScoresList.setScores(observableScores);
    } else {
      var localScoresList = new ScoresList();
      localScoresList.setId("localScores");
      var localTitle = new Text("Local Scores");
      localTitle.getStyleClass().add("heading");
      var localVBox = new VBox(10, localTitle, localScoresList);
      localVBox.setAlignment(Pos.CENTER);
      localVBox.setPadding(new Insets(20));
      mainPane.setLeft(localVBox);

      var remoteScoresList = new ScoresList();
      remoteScoresList.setId("remoteScores");
      var remoteTitle = new Text("Online Scores");
      remoteTitle.getStyleClass().add("heading");
      var remoteVBox = new VBox(10, remoteTitle, remoteScoresList);
      remoteVBox.setAlignment(Pos.CENTER);
      remoteVBox.setPadding(new Insets(20));
      mainPane.setRight(remoteVBox);

      // *** THE PERMANENT FIX IS HERE ***
      // Load and display local scores on the next UI pulse
      Platform.runLater(() -> {
        ArrayList<Pair<String, Integer>> loadedScores = loadScores();
        observableScores = FXCollections.observableArrayList(loadedScores);
        localScoresList.setScores(observableScores);
      });

      remoteScores = FXCollections.observableArrayList();
      remoteScoresList.setScores(remoteScores);

      communicator.send("HISCORES");
      checkAndPromptForScore();
    }

    Statistics.saveStats();
  }

  private VBox buildStatsPanel() {
    VBox statsVBox = new VBox(10);
    statsVBox.setAlignment(Pos.CENTER);
    statsVBox.setPadding(new Insets(20));
    var statsTitle = new Text("Your Statistics");
    statsTitle.getStyleClass().add("heading");
    HBox gamesPlayedBox = new HBox(10);
    gamesPlayedBox.setAlignment(Pos.CENTER);
    Label gamesPlayedHeading = new Label("Games Played:");
    gamesPlayedHeading.getStyleClass().add("stats-heading");
    Label gamesPlayedValue = new Label();
    gamesPlayedValue.getStyleClass().add("stats-item");
    gamesPlayedValue.textProperty().bind(Statistics.gamesPlayed.asString());
    gamesPlayedBox.getChildren().addAll(gamesPlayedHeading, gamesPlayedValue);
    HBox linesClearedBox = new HBox(10);
    linesClearedBox.setAlignment(Pos.CENTER);
    Label linesClearedHeading = new Label("Total Lines Cleared:");
    linesClearedHeading.getStyleClass().add("stats-heading");
    Label linesClearedValue = new Label();
    linesClearedValue.getStyleClass().add("stats-item");
    linesClearedValue.textProperty().bind(Statistics.linesCleared.asString());
    linesClearedBox.getChildren().addAll(linesClearedHeading, linesClearedValue);
    HBox highestScoreBox = new HBox(10);
    highestScoreBox.setAlignment(Pos.CENTER);
    Label highestScoreHeading = new Label("Highest Score:");
    highestScoreHeading.getStyleClass().add("stats-heading");
    Label highestScoreValue = new Label();
    highestScoreValue.getStyleClass().add("stats-item");
    highestScoreValue.textProperty().bind(Statistics.highestScore.asString());
    highestScoreBox.getChildren().addAll(highestScoreHeading, highestScoreValue);
    HBox highestMultiplierBox = new HBox(10);
    highestMultiplierBox.setAlignment(Pos.CENTER);
    Label highestMultiplierHeading = new Label("Highest Multiplier:");
    highestMultiplierHeading.getStyleClass().add("stats-heading");
    Label highestMultiplierValue = new Label();
    highestMultiplierValue.getStyleClass().add("stats-item");
    highestMultiplierValue.textProperty().bind(Bindings.format("%dx", Statistics.highestMultiplier));
    highestMultiplierBox.getChildren().addAll(highestMultiplierHeading, highestMultiplierValue);
    statsVBox.getChildren().addAll(statsTitle, gamesPlayedBox, linesClearedBox, highestScoreBox, highestMultiplierBox);
    return statsVBox;
  }

  private void promptForName(boolean isOnlineScore) {
    VBox bottomPane = (VBox) mainPane.getBottom();
    VBox inputBox = new VBox(10);
    inputBox.setAlignment(Pos.CENTER);
    inputBox.setPadding(new Insets(20));
    Text promptText = new Text("Save your score by entering your name.");
    promptText.getStyleClass().add("heading");
    TextField nameField = new TextField();
    nameField.setPromptText("Your Name");
    nameField.setMaxWidth(300);
    inputBox.getChildren().addAll(promptText, nameField);
    bottomPane.getChildren().add(inputBox);
    nameField.setOnKeyPressed(event -> {
      if (event.getCode() == KeyCode.ENTER) {
        Multimedia.playSound("save.wav");
        String name = nameField.getText().isBlank() ? "Player" : nameField.getText();
        addNewScore(name, game.scoreProperty().get(), isOnlineScore);
        bottomPane.getChildren().remove(inputBox);
      }
    });
    Platform.runLater(nameField::requestFocus);
  }

  @Override
  public void receiveCommunication(String message) {
    if (message.startsWith("HISCORES")) {
      parseScores(message);
    }
  }

  private void parseScores(String message) {
    ArrayList<Pair<String, Integer>> newScores = new ArrayList<>();
    String scoresData = message.substring(9);
    String[] lines = scoresData.split("\n");
    for (String line : lines) {
      String[] parts = line.split(":");
      if (parts.length == 2) {
        String name = parts[0];
        int score = Integer.parseInt(parts[1].trim());
        newScores.add(new Pair<>(name, score));
      }
    }
    Platform.runLater(() -> remoteScores.setAll(newScores));
  }

  private void checkAndPromptForScore() {
    int lowestLocalScore = 0;
    if (observableScores != null && observableScores.size() >= 10) {
      lowestLocalScore = observableScores.get(9).getValue();
    }
    boolean isLocalHighScore = (game.scoreProperty().get() > lowestLocalScore || (observableScores != null && observableScores.size() < 10));
    scoreListener = c -> {
      onlineScoresReceived = true;
      c.getList().removeListener(scoreListener);
      int lowestOnlineScore = 0;
      if (!remoteScores.isEmpty()) {
        lowestOnlineScore = remoteScores.get(remoteScores.size() - 1).getValue();
      }
      boolean isOnlineHighScore = (game.scoreProperty().get() > lowestOnlineScore || remoteScores.size() < 10);
      if (isOnlineHighScore) {
        promptForName(true);
      } else if (isLocalHighScore) {
        promptForName(false);
      }
    };
    remoteScores.addListener(scoreListener);
    PauseTransition failsafe = new PauseTransition(Duration.seconds(2));
    failsafe.setOnFinished(e -> {
      if (!onlineScoresReceived) {
        remoteScores.removeListener(scoreListener);
        if (isLocalHighScore) {
          promptForName(false);
        }
      }
    });
    failsafe.play();
  }

  private void addNewScore(String name, int score, boolean isOnlineScore) {
    observableScores.add(new Pair<>(name, score));
    observableScores.sort((a, b) -> b.getValue().compareTo(a.getValue()));
    if (observableScores.size() > 10) observableScores.remove(10);
    saveScores(new ArrayList<>(observableScores));
    if (isOnlineScore) {
      remoteScores.add(new Pair<>(name, score));
      remoteScores.sort((a, b) -> b.getValue().compareTo(a.getValue()));
      if (remoteScores.size() > 10) remoteScores.remove(10);
      communicator.send("HISCORE " + name + ":" + score);
    }
  }

  /**
   * Loads scores from a file named scores.txt in a dedicated game folder
   * within the user's home directory.
   * @return An ArrayList of Pairs, where each Pair is a name and a score.
   */
  private ArrayList<Pair<String, Integer>> loadScores() {
    ArrayList<Pair<String, Integer>> loadedScores = new ArrayList<>();
    logger.info("Attempting to load scores from: {}", scoresFilePath);
    File scoresFile = new File(scoresFilePath);
    if (!scoresFile.exists()) {
      logger.warn("scores.txt does not exist. Returning empty list.");
      return loadedScores;
    }

    try (BufferedReader reader = new BufferedReader(new FileReader(scoresFile))) {
      String line;
      while ((line = reader.readLine()) != null) {
        String[] parts = line.split(":", 2);
        if (parts.length == 2) {
          loadedScores.add(new Pair<>(parts[0], Integer.parseInt(parts[1].trim())));
        }
      }
    } catch (IOException | NumberFormatException e) {
      logger.error("Failed to load or parse scores file.", e);
    }
    loadedScores.sort((a, b) -> b.getValue().compareTo(a.getValue()));
    return loadedScores;
  }

  private void saveScores(ArrayList<Pair<String, Integer>> scores) {
    logger.info("Saving scores to: {}", scoresFilePath);
    try (FileWriter writer = new FileWriter(scoresFilePath)) {
      for (Pair<String, Integer> scoreEntry : scores) {
        writer.write(scoreEntry.getKey() + ":" + scoreEntry.getValue() + "\n");
      }
    } catch (IOException e) {
      logger.error("Failed to save scores: {}", e.getMessage());
    }
  }
}