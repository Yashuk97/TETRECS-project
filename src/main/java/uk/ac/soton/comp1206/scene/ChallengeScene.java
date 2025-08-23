package uk.ac.soton.comp1206.scene;

import java.util.Set;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.shape.Rectangle;

/**
 * The Single Player challenge scene. Holds the UI for the single player challenge mode in the game.
 */
public class ChallengeScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(MenuScene.class);
    protected Game game;
    protected GameBoard board;
    private Rectangle timerBar;
    private PieceBoard nextPieceBoard;


    //coordinates for keyboard-controlled aim
    private int aimX = 2;
    private int aimY = 2;


    private Label scoreLabel;
    private Label levelLabel;
    private Label livesLabel;
    private Label multiplierLabel;

    /**
     * Create a new Single Player challenge scene
     * @param gameWindow the Game Window
     */
    public ChallengeScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Challenge Scene");
    }

    /**
     * Build the Challenge window
     */

    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        // Call setupGame() first to make sure our `game` object exists
        setupGame();

        // --- 1. Create the root and basic layout ---
        root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

        var challengePane = new StackPane();
        challengePane.setMaxWidth(gameWindow.getWidth());
        challengePane.setMaxHeight(gameWindow.getHeight());
        challengePane.getStyleClass().add("challenge-background"); // Use a specific style
        root.getChildren().add(challengePane);

        // Use a BorderPane for easy layout management
        var mainPane = new BorderPane();
        challengePane.getChildren().add(mainPane);

        // --- 2. Create and position the GameBoard ---
        board = new GameBoard(game.getGrid(), gameWindow.getWidth() / 2, gameWindow.getWidth() / 2);
        mainPane.setCenter(board); // Place the board in the center

        // --- 3. Create and position the UI Info Panel ---
        var infoPane = new VBox();
        infoPane.setSpacing(10);
        infoPane.setAlignment(Pos.CENTER); // Center the labels within the VBox
        mainPane.setRight(infoPane); // Place the info panel on the right

        // --- Create and position the Timer Bar ---
        timerBar = new Rectangle();
        timerBar.setHeight(20); // 20 pixels high
        timerBar.setWidth(gameWindow.getWidth()); // Start at full width
        timerBar.setFill(Color.GREEN); // Start with a green color
        mainPane.setBottom(timerBar);

        scoreLabel = new Label("Score: 0");
        levelLabel = new Label("Level: 0");
        livesLabel = new Label("Lives: 3");
        multiplierLabel = new Label("Multiplier: 1x");

        // Add labels to the infoPane VBox
        infoPane.getChildren().addAll(scoreLabel, levelLabel, livesLabel, multiplierLabel);

        Label nextPieceLabel = new Label("Next Piece:");
        nextPieceBoard = new PieceBoard();
        infoPane.getChildren().addAll(nextPieceLabel, nextPieceBoard);

        // Add styling to the labels
        scoreLabel.getStyleClass().add("score");
        levelLabel.getStyleClass().add("level");
        livesLabel.getStyleClass().add("lives"); // Let's add styles for these too
        multiplierLabel.getStyleClass().add("multiplier");

        // --- 4. Set up event handling ---
        // Handle block on gameboard grid being clicked
        board.setOnBlockClick(this::blockClicked);
    }

    /**
     * Handle when a block is clicked
     * @param gameBlock the Game Block that was clocked
     */
    private void blockClicked(GameBlock gameBlock) {
        game.blockClicked(gameBlock);
    }

    /**
     * Setup the game object and model
     */
    public void setupGame() {
        logger.info("Starting a new challenge");

        //Start new game
        game = new Game(5, 5);
    }

    /**
     * Initialise the scene and start the game
     */
    @Override
    public void initialise() {
        logger.info("Initialising Challenge");

        game.setNextPieceListener(this::displayNextPiece);

        //also display very first piece when game starts
        displayNextPiece(game.getNextPiece());
        // Bind the score label
        scoreLabel.textProperty().bind(Bindings.format("Score: %d", game.scoreProperty()));

// Bind the level label
        levelLabel.textProperty().bind(Bindings.format("Level: %d", game.levelProperty()));

// Bind the lives label
        livesLabel.textProperty().bind(Bindings.format("Lives: %d", game.livesProperty()));

// Bind the multiplier label
        multiplierLabel.textProperty().bind(Bindings.format("Multiplier: %dx", game.multiplierProperty()));
        scoreLabel.textProperty().bind(Bindings.format("Score: %d", game.scoreProperty()));
        levelLabel.textProperty().bind(Bindings.format("Level: %d", game.levelProperty()));
        livesLabel.textProperty().bind(Bindings.format("Lives: %d", game.livesProperty()));
        multiplierLabel.textProperty().bind(Bindings.format("Multiplier: %dx", game.multiplierProperty()));

        game.setNextPieceListener(this::displayNextPiece);
        displayNextPiece(game.getNextPiece());
        // Inside the initialise() method's key listener


        game.setLineClearedListener(this::onLineCleared);
        game.setGameLoopListener(delay -> animateTimerBar(delay));
        // --- ADD KEYBOARD SUPPORT ---
        scene.setOnKeyPressed(this::handleKeyPress);
        game.start();
        game.setGameOverListener(() -> endGame());
    }
    /**
     * Animates the timer bar based on the provided delay.
     * @param delay the duration for the animation
     */
    private void animateTimerBar(int delay) {
        logger.info("Animating timer bar with delay: {}", delay);

        // Create a timeline animation
        Timeline timeline = new Timeline();

        // Use .addAll() to add multiple KeyFrames at once
        timeline.getKeyFrames().addAll(
            new KeyFrame(Duration.ZERO, new KeyValue(timerBar.widthProperty(), gameWindow.getWidth())),
            new KeyFrame(new Duration(delay), new KeyValue(timerBar.widthProperty(), 0))
        );

        // Change color based on urgency
        if (delay <= 10000) {
            timerBar.setFill(Color.RED);
        } else if (delay <= 11000) {
            timerBar.setFill(Color.ORANGE);
        } else {
            timerBar.setFill(Color.GREEN);
        }

        timeline.play();
    }
    /**
     * Called when the game is over.
     */
    private void endGame() {
        logger.info("Ending game and returning to menu.");
        gameWindow.startMenu();
    }
    /**
     * Called when lines are cleared in the game. Triggers the fade-out animation.
     * @param clearedBlocks the set of blocks that were cleared
     */
    private void onLineCleared(Set<GameBlockCoordinate> clearedBlocks) {
        board.fadeOut(clearedBlocks);
    }
    /**
     * Handles keyboard input for aiming and placing pieces.
     * @param event the keyboard event
     */
    private void handleKeyPress(KeyEvent event) {
        // First, check for system-level keys like ESCAPE
        if (event.getCode() == KeyCode.ESCAPE) {
            game.shutdown();
            gameWindow.startMenu();
            return; // Stop processing further
        }

        // Then, handle the game controls
        switch (event.getCode()) {
            case UP:
            case W:
                aimY = Math.max(0, aimY - 1);
                break;
            case DOWN:
            case S:
                aimY = Math.min(game.getGrid().getRows() - 1, aimY + 1);
                break;
            case LEFT:
            case A:
                aimX = Math.max(0, aimX - 1);
                break;
            case RIGHT:
            case D:
                aimX = Math.min(game.getGrid().getCols() - 1, aimX + 1);
                break;
            case ENTER:
            case X:
                game.blockClicked(board.getBlock(aimX, aimY));
                break;
        }

        // After moving, update the visual hover effect
        board.hoverBlock(board.getBlock(aimX, aimY));
    }
    /**
     * Displays the next piece on the PieceBoard.
     * @param piece the piece to display
     */
    private void displayNextPiece(GamePiece piece) {
        nextPieceBoard.displayPiece(piece);
    }

}
