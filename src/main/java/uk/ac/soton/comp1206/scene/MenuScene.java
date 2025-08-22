package uk.ac.soton.comp1206.scene;

import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.App;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * The main menu of the game. Provides a gateway to the rest of the game.
 */
public class MenuScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(MenuScene.class);

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

        root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

        var menuPane = new StackPane();
        menuPane.setMaxWidth(gameWindow.getWidth());
        menuPane.setMaxHeight(gameWindow.getHeight());
        menuPane.getStyleClass().add("menu-background");
        root.getChildren().add(menuPane);

        var mainPane = new BorderPane();
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

        var instructionsButton = new Button("How to Play");
        instructionsButton.getStyleClass().add("menu-button");

        var exitButton = new Button("Exit");
        exitButton.getStyleClass().add("menu-button");

        menuVBox.getChildren().addAll(singlePlayerButton, instructionsButton, exitButton);

        // --- Button Actions ---
        singlePlayerButton.setOnAction(this::startGame);
        instructionsButton.setOnAction(this::openInstructions);
        exitButton.setOnAction(event -> App.getInstance().shutdown());
    }

    /**
     * Initialise the menu
     */
    @Override
    public void initialise() {

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
