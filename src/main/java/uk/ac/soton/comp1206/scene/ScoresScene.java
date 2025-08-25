package uk.ac.soton.comp1206.scene;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import javafx.beans.property.SimpleListProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
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
    title.getStyleClass().add("title");
    mainPane.setTop(title);

    // --- Use the ScoresList Component ---
    ScoresList scoresList = new ScoresList();
    mainPane.setCenter(scoresList);

    // Load the scores from the file and bind them to the list
    ArrayList<Pair<String, Integer>> loadedScores = loadScores();
    observableScores = FXCollections.observableArrayList(loadedScores);
    scoresList.scoresProperty().bind(new SimpleListProperty<>(observableScores));

    // --- Check for new high score ---
    checkScores();
  }
  private void checkScores() {
    // Find the lowest score on the board (or 0 if the board is not full)
    int lowestScore = 0;
    if (observableScores.size() >= 10) {
      lowestScore = observableScores.get(9).getValue();
    }

    if (game.scoreProperty().get() > lowestScore) {
      // New high score! Show the name input field.
      var mainPane = (BorderPane) ((StackPane) root.getChildren().get(0)).getChildren().get(0);

      HBox nameInputBox = new HBox(10);
      nameInputBox.setAlignment(Pos.CENTER);
      nameInputBox.setPadding(new Insets(10));

      TextField nameField = new TextField();
      nameField.setPromptText("Enter your name");

      Button submitButton = new Button("Submit");

      nameInputBox.getChildren().addAll(nameField, submitButton);
      mainPane.setBottom(nameInputBox);

      submitButton.setOnAction(e -> {
        String name = nameField.getText().isBlank() ? "Player" : nameField.getText();
        addNewScore(name, game.scoreProperty().get());
        mainPane.setBottom(null); // Hide the input box after submitting
      });
    }
  }

  private void addNewScore(String name, int score) {
    observableScores.add(new Pair<>(name, score));
    observableScores.sort((a, b) -> b.getValue().compareTo(a.getValue()));

    // Keep only the top 10 scores
    if (observableScores.size() > 10) {
      observableScores.remove(10, observableScores.size());
    }

    saveScores(new ArrayList<>(observableScores));
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