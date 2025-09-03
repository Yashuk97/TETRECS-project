package uk.ac.soton.comp1206.scene;

import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.App;
import uk.ac.soton.comp1206.multimedia.Multimedia;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * The main menu of the game. Provides a gateway to the rest of the game.
 */
public class MenuScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(MenuScene.class);

    private static MediaPlayer musicPlayer;
    /**
     * Create a new menu scene
     * @param gameWindow the Game Window this will be displayed in
     */
    public MenuScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Menu Scene");
    }

    /**
     * Build the menu layout
     */
    @Override

    public void build() {
        logger.info("Building " + this.getClass().getName());
        Multimedia.playMusic("menu.mp3");


        root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

        var menuPane = new StackPane();
        menuPane.setMaxWidth(gameWindow.getWidth());
        menuPane.setMaxHeight(gameWindow.getHeight());
// menuPane.getStyleClass().add("menu-background"); // We no longer need this CSS class for the background

// --- Add the animated background ---
        var backgroundImage = new ImageView(new Image(getClass().getResource("/images/1641264230-origin.jpg").toExternalForm()));
        backgroundImage.setFitWidth(gameWindow.getWidth());
        backgroundImage.setFitHeight(gameWindow.getHeight());
        backgroundImage.setPreserveRatio(false); // Stretch to fill
        menuPane.getChildren().add(backgroundImage);
// --- End of background section ---

        root.getChildren().add(menuPane);

        var mainPane = new BorderPane();
        mainPane.setStyle("-fx-background-color: transparent;"); // Make mainPane see-through
        menuPane.getChildren().add(mainPane);

        // Awesome Title
        var title = new Text("TetrECS");
        title.getStyleClass().add("title");
        BorderPane.setAlignment(title, Pos.CENTER);
        mainPane.setTop(title);

        // Menu Buttons
        var menuVBox = new VBox(10); // 10 is the spacing between buttons
        menuVBox.setAlignment(Pos.CENTER);
        mainPane.setCenter(menuVBox);

        var singlePlayerButton = new Button("Single Player");
        singlePlayerButton.getStyleClass().add("menu-button");

        var multiplayerButton = new Button("Multiplayer");
        multiplayerButton.getStyleClass().add("menu-button");

        var instructionsButton = new Button("How to Play");
        instructionsButton.getStyleClass().add("menu-button");

        var exitButton = new Button("Exit");
        exitButton.getStyleClass().add("menu-button");

        var settingsButton  = new Button("Settings");
        settingsButton.getStyleClass().add("menu-button");
        settingsButton.setPrefWidth(300);

        int buttonWidth = 300;
        singlePlayerButton.setPrefWidth(buttonWidth);
        multiplayerButton.setPrefWidth(buttonWidth);
        instructionsButton.setPrefWidth(buttonWidth);
        exitButton.setPrefWidth(buttonWidth);

        TranslateTransition drop = new TranslateTransition(Duration.millis(800), menuVBox);
        drop.setFromY(-500); // Start 500 pixels above its final position
        drop.setToY(0);      // Animate to its final position
        drop.play();

        menuVBox.getChildren().addAll(singlePlayerButton, multiplayerButton, instructionsButton, settingsButton, exitButton);

        // --- Button Actions ---
        singlePlayerButton.setOnAction(e -> {
            Multimedia.playSound("click-button-166324.mp3");
            startGame(e);
        });
        multiplayerButton.setOnAction(e-> {
            Multimedia.playSound("click-button-166324.mp3");
            startLobby(e);
        });
        instructionsButton.setOnAction(e-> {
            Multimedia.playSound("click-button-166324.mp3");
            openInstructions(e);
        });
        settingsButton.setOnAction(e -> {
            Multimedia.playSound("click-button-166324.mp3");
            gameWindow.startSettings();
        });
        exitButton.setOnAction(e -> {
            Multimedia.playSound("click-button-166324.mp3");
            App.getInstance().shutdown();
        });

//        settingsButton.setOnAction(e -> gameWindow.startSettings());
//        exitButton.setOnAction(event -> App.getInstance().shutdown());

        singlePlayerButton.setOnMouseEntered(e -> Multimedia.playSound("mixkit-water-bubble-1317.wav"));
        multiplayerButton.setOnMouseEntered(e -> Multimedia.playSound("mixkit-water-bubble-1317.wav"));
        instructionsButton.setOnMouseEntered(e -> Multimedia.playSound("mixkit-water-bubble-1317.wav"));
        exitButton.setOnMouseEntered(e -> Multimedia.playSound("mixkit-water-bubble-1317.wav"));


    }

    /**
     * Initialise the menu
     */
    @Override
    public void initialise() {

    }

    private void startLobby(ActionEvent event) {
        gameWindow.startLobby();
    }

    /**
     * Handle when the Instructions button is pressed.
     */
    private void openInstructions(ActionEvent event) {
        // We will fill this in next
        logger.info("Open Instructions button pressed");
        gameWindow.startInstructions();
    }

    /**
     * Handle when the Start Game button is pressed
     * @param event event
     */
    private void startGame(ActionEvent event) {
        gameWindow.startChallenge();
    }

}
