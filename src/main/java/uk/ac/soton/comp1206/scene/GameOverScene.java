package uk.ac.soton.comp1206.scene;

import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.util.Duration;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

public class GameOverScene extends BaseScene {

  private final Game game;

  public GameOverScene(GameWindow gameWindow, Game game) {
    super(gameWindow);
    this.game = game;
  }

  @Override
  public void initialise() {
    // No specific initialisation needed
  }

  @Override
  public void build() {
    root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

    var gameOverPane = new StackPane();
    gameOverPane.setMaxWidth(gameWindow.getWidth());
    gameOverPane.setMaxHeight(gameWindow.getHeight());
    gameOverPane.getStyleClass().add("menu-background");
    root.getChildren().add(gameOverPane);

    // Create the "Game Over" text
    var gameOverText = new Text("Game Over");
    gameOverText.getStyleClass().add("bigtitle");
    gameOverPane.getChildren().add(gameOverText);

    // --- Animations ---
    // Fade in
    FadeTransition fadeIn = new FadeTransition(Duration.seconds(3), gameOverText);
    fadeIn.setFromValue(0);
    fadeIn.setToValue(1);

    // Scale up
    ScaleTransition scaleUp = new ScaleTransition(Duration.seconds(3), gameOverText);
    scaleUp.setFromX(0.5);
    scaleUp.setFromY(0.5);
    scaleUp.setToX(1.0);
    scaleUp.setToY(1.0);

    ParallelTransition entranceAnimation = new ParallelTransition(fadeIn, scaleUp);

    // A pause before transitioning to the scores screen
    PauseTransition pause = new PauseTransition(Duration.seconds(3));

    // Chain them together: entrance animation, then pause, then switch scene
    SequentialTransition sequence = new SequentialTransition(entranceAnimation, pause);
    sequence.setOnFinished(e -> gameWindow.startScores(game));

    sequence.play();
  }
}