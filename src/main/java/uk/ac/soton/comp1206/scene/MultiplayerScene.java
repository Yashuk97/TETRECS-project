package uk.ac.soton.comp1206.scene;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.game.MultiplayerGame;
import uk.ac.soton.comp1206.ui.GameWindow;

public class MultiplayerScene extends ChallengeScene {

  private static final Logger logger = LogManager.getLogger(MultiplayerScene.class);
  private ObservableList<Pair<String, Integer>> leaderboardScores;
  private ListView<String> leaderboardList;
  private ListView<String> userList; // For chat

  public MultiplayerScene(GameWindow gameWindow) {
    super(gameWindow);
    logger.info("Creating Multiplayer Scene");
  }

  @Override
  public void setupGame() {
    logger.info("Starting a new multiplayer game");
    // Use MultiplayerGame instead of the regular Game
    this.game = new MultiplayerGame(5, 5, communicator);
  }

  @Override
  public void build() {
    // Build the standard ChallengeScene UI first.
    super.build();

    // Correctly find the BorderPane inside the scene's structure
    StackPane menuPane = (StackPane) root.getChildren().get(0);
    BorderPane mainPane = (BorderPane) menuPane.getChildren().get(1); // It's the 2nd child

    // --- Replace the local info panel with a live leaderboard ---
    var leaderboardVBox = new VBox(10);
    leaderboardVBox.setAlignment(Pos.CENTER);
    leaderboardVBox.setPadding(new Insets(10));
    leaderboardVBox.getStyleClass().add("game-info-box"); // Reuse the nice curved box style

    var leaderboardTitle = new Text("Leaderboard");
    leaderboardTitle.getStyleClass().add("heading");

    leaderboardList = new ListView<>();
    VBox.setVgrow(leaderboardList, Priority.ALWAYS);

    leaderboardVBox.getChildren().addAll(leaderboardTitle, leaderboardList);

    // Replace the right-side content
    mainPane.setRight(leaderboardVBox);
  }

  @Override
  public void initialise() {
    super.initialise(); // This will call the ChallengeScene's initialise method

    // Bind the leaderboard to the game's leaderboard property
    // (We need to add this property to MultiplayerGame)
    var game = (MultiplayerGame) this.game;
    leaderboardList.itemsProperty().bind(game.getLeaderboardProperty());
  }
}