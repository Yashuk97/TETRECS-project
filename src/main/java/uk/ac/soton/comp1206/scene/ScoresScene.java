package uk.ac.soton.comp1206.scene;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.ScoresList;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import javafx.util.Pair;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ScoresScene extends BaseScene {

  private static final Logger logger = LogManager.getLogger(ScoresScene.class);
  private final Game game; // To get the final score from

  private ObservableList<Pair<String, Integer>> observableScores;
  private ObservableList<Pair<String, Integer>> remoteScores;
  private ListChangeListener<Pair<String, Integer>> scoreListener;

  public ScoresScene(GameWindow gameWindow, Game game) {
    super(gameWindow);
    this.game = game;
  }

  @Override
  public void initialise() {
    // Add ESC key listener to go back to the menu
    scene.setOnKeyPressed(event -> {
      if (event.getCode().toString().equals("ESCAPE")) {
        gameWindow.startMenu();
      }
    });
    communicator.addListener(message -> {
      Platform.runLater(() -> handleServerMessage(message));
    });
  }

  @Override
  public void build() {
    logger.info("Building " + this.getClass().getName());

    root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

    var scoresPane = new StackPane();
    scoresPane.setMaxWidth(gameWindow.getWidth());
    scoresPane.setMaxHeight(gameWindow.getHeight());
    scoresPane.getStyleClass().add("menu-background");
    root.getChildren().add(scoresPane);

    var mainPane = new BorderPane();
    scoresPane.getChildren().add(mainPane);

    var title = new Text("High Scores");
    title.getStyleClass().add("scores-title");
    BorderPane.setAlignment(title, Pos.CENTER);
    mainPane.setTop(title);

    RotateTransition rotate = new RotateTransition(Duration.seconds(2), title);
    rotate.setFromAngle(0);
    rotate.setToAngle(360);
    rotate.setCycleCount(1); // Play once
    rotate.play();

    // --- Create a container for both local and remote score lists ---
    var scoresContainer = new HBox(20);
    scoresContainer.setAlignment(Pos.CENTER);
    mainPane.setCenter(scoresContainer);

    // --- Local Scores List ---
    var localScoresList = new ScoresList();
    localScoresList.setId("localScores");
    var localTitle = new Text("Local Scores");
    localTitle.getStyleClass().add("heading");
    var localVBox = new VBox(10, localTitle, localScoresList);
    localVBox.setAlignment(Pos.CENTER);

    // --- Remote Scores List ---
    var remoteScoresList = new ScoresList();
    remoteScoresList.setId("remoteScores");
    var remoteTitle = new Text("Online Scores");
    remoteTitle.getStyleClass().add("heading");
    var remoteVBox = new VBox(10, remoteTitle, remoteScoresList);
    remoteVBox.setAlignment(Pos.CENTER);

    scoresContainer.getChildren().addAll(localVBox, remoteVBox);

    // --- Load local scores and bind them ---
    ArrayList<Pair<String, Integer>> loadedScores = loadScores();
    observableScores = FXCollections.observableArrayList(loadedScores);
    localScoresList.scoresProperty().bind(new SimpleListProperty<>(observableScores));

    // --- Prepare remote scores list and bind it ---
    remoteScores = FXCollections.observableArrayList();
    remoteScoresList.scoresProperty().bind(new SimpleListProperty<>(remoteScores));

    // Ask the server for the high scores
    communicator.send("HISCORES");

    // Check if the player's score is a new high score
    checkAndPromptForScore();
  }
  /**
   * Handles messages received from the server.
   * @param message the message from the server
   */
  private void handleServerMessage(String message) {
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
    // Define the listener that will check scores once they arrive.
    scoreListener = c -> {
      // This will run ONCE when the scores first arrive from the server.
      // We remove the listener immediately to prevent it from running again.
      c.getList().removeListener(scoreListener);

      // Now that we have the online scores, check if our score is a high score.
      int lowestOnlineScore = 0;
      if (!remoteScores.isEmpty()) {
        lowestOnlineScore = remoteScores.get(remoteScores.size() - 1).getValue();
      }

      if (game.scoreProperty().get() > lowestOnlineScore || remoteScores.size() < 10) {
        logger.info("New ONLINE high score detected!");
        promptForName(true);
      } else {
        int lowestLocalScore = 0;
        if (observableScores.size() >= 10) {
          lowestLocalScore = observableScores.get(9).getValue();
        }
        if (game.scoreProperty().get() > lowestLocalScore || observableScores.size() < 10) {
          logger.info("New LOCAL high score detected!");
          promptForName(false);
        }
      }
    };

    // Add the listener to the remoteScores list.
    remoteScores.addListener(scoreListener);
  }
  /**
   * Displays the name input field and submit button.
   * @param isOnlineScore true if this score will be submitted online
   */
  private void promptForName(boolean isOnlineScore) {
    var mainPane = (BorderPane) ((StackPane) root.getChildren().get(0)).getChildren().get(0);

    VBox inputBox = new VBox(10);
    inputBox.setAlignment(Pos.CENTER);
    inputBox.setPadding(new Insets(20));

    // 7. Update the prompt text
    Text promptText = new Text("Save your score by entering your name.");
    promptText.getStyleClass().add("heading");

    TextField nameField = new TextField();
    nameField.setPromptText("Your Name");
    nameField.setMaxWidth(300); // Give the text field a reasonable size

    inputBox.getChildren().addAll(promptText, nameField);
    mainPane.setBottom(inputBox);

    // 8. Add handler for the ENTER key on the text field
    nameField.setOnKeyPressed(event -> {
      if (event.getCode() == KeyCode.ENTER) {
        String name = nameField.getText().isBlank() ? "Player" : nameField.getText();
        addNewScore(name, game.scoreProperty().get(), isOnlineScore);
        mainPane.setBottom(null); // Hide the input box after submitting
      }
    });

    // Request focus so the user can start typing immediately
    Platform.runLater(nameField::requestFocus);
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