package uk.ac.soton.comp1206.scene;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.multimedia.Multimedia;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

public class IntroScene extends BaseScene {

  private static final Logger logger = LogManager.getLogger(IntroScene.class);

  public IntroScene(GameWindow gameWindow) {
    super(gameWindow);
  }

  @Override
  public void initialise() {
    Multimedia.playMusic("intro.mp3");
  }

  @Override
  public void build() {
    logger.info("Building " + this.getClass().getName());

    root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

    var introPane = new StackPane();
    introPane.setMaxWidth(gameWindow.getWidth());
    introPane.setMaxHeight(gameWindow.getHeight());
    root.getChildren().add(introPane);

    // --- Add your custom background image ---
    // Make sure you have an "intro_background.jpg" in your resources/images folder
    var backgroundImage = new ImageView(new Image(getClass().getResource("/images/space-14958_256.gif").toExternalForm()));
    backgroundImage.setFitWidth(gameWindow.getWidth());
    backgroundImage.setFitHeight(gameWindow.getHeight());
    introPane.getChildren().add(backgroundImage);

    // --- Create the animated text ---
    var welcomeText = new Text("WELCOME TO TETRECS");
    welcomeText.getStyleClass().add("bigtitle"); // Use the existing style for big titles
    welcomeText.setFill(Color.BLACK); // Set the text color to black as requested
    introPane.getChildren().add(welcomeText);

    // --- Set up the animations ---
    // 1. Drop from top animation
    TranslateTransition drop = new TranslateTransition(Duration.seconds(4), welcomeText);
    drop.setFromY(-400); // Start 400 pixels above the center
    drop.setToY(0);      // End in the center

    // 2. Pause in the middle for 2 seconds
    PauseTransition pause = new PauseTransition(Duration.seconds(2));

    // 3. Fade out animation
    FadeTransition fadeOut = new FadeTransition(Duration.seconds(1), welcomeText);
    fadeOut.setFromValue(1.0);
    fadeOut.setToValue(0.0);

    // --- Chain the animations together ---
    SequentialTransition sequence = new SequentialTransition(drop, pause, fadeOut);

    // When the entire animation sequence is finished, go to the menu
    sequence.setOnFinished(event -> gameWindow.startMenu());

    // Start the show!
    sequence.play();
  }
}