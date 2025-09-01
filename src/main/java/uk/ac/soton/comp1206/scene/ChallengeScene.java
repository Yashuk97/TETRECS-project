package uk.ac.soton.comp1206.scene;

import java.util.Set;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.multimedia.Multimedia;
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
  private PieceBoard currentPieceDisplay;
  private PieceBoard nextPieceDisplay;



  //coordinates for keyboard-controlled aim
    private int aimX = 2;
    private int aimY = 2;


    private Label scoreLabel;
    private Label levelLabel;
    private Label livesLabel;
    private Label multiplierLabel;
    private Timeline timeline;

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

      Multimedia.playMusic("game_start.wav");

        // Call setupGame() first to make sure our `game` object exists
        setupGame();

        // --- 1. Create the root and basic layout ---
        root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

        var menuPane = new StackPane();
        menuPane.setMaxWidth(gameWindow.getWidth());
        menuPane.setMaxHeight(gameWindow.getHeight());
// menuPane.getStyleClass().add("menu-background"); // We no longer need this CSS class for the background

// --- Add the animated background ---
        var backgroundImage = new ImageView(new Image(getClass().getResource("/images/ezgif.com-animated-gif-maker.gif").toExternalForm()));
        backgroundImage.setFitWidth(gameWindow.getWidth());
        backgroundImage.setFitHeight(gameWindow.getHeight());
        backgroundImage.setPreserveRatio(false); // Stretch to fill
        menuPane.getChildren().add(backgroundImage);
// --- End of background section ---

        root.getChildren().add(menuPane);

        var mainPane = new BorderPane();
        mainPane.setStyle("-fx-background-color: transparent;"); // Make mainPane see-through
        menuPane.getChildren().add(mainPane);

        // --- 2. Create and position the GameBoard ---
        board = new GameBoard(game.getGrid(), gameWindow.getWidth() / 2, gameWindow.getWidth() / 2);
        mainPane.setCenter(board); // Place the board in the center

        // --- 3. Create and position the UI Info Panel ---
        var infoPane = new VBox();
        infoPane.setSpacing(10);
        infoPane.setAlignment(Pos.CENTER); // Center the labels within the VBox
        infoPane.getStyleClass().add("game-info-box");
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
        multiplierLabel.getStyleClass().add("multiplier");


      Label currentPieceLabel = new Label("Current Piece:");
      currentPieceLabel.getStyleClass().add("sidebar-heading");
      currentPieceDisplay = new PieceBoard();

      Label nextPieceLabel = new Label("Next Piece:");
      nextPieceLabel.getStyleClass().add("sidebar-heading");
      nextPieceDisplay = new PieceBoard();

      // Add labels to the infoPane VBox
        infoPane.getChildren().addAll(scoreLabel, levelLabel, livesLabel, multiplierLabel, currentPieceLabel, currentPieceDisplay, nextPieceLabel, nextPieceDisplay);

        // Add styling to the labels
        scoreLabel.getStyleClass().add("score");
        levelLabel.getStyleClass().add("level");
        livesLabel.getStyleClass().add("lives"); // Let's add styles for these too
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

        // --- Set up Listeners from Game to Scene ---

        // Listen for the next piece from the game model
        game.setNextPieceListener((current, next) -> displayNextPiece(current, next));

        // Listen for when lines are cleared to trigger animations
        game.setLineClearedListener(this::onLineCleared);

        // Listen for the game loop starting/resetting to animate the timer bar
        game.setGameLoopListener(delay -> animateTimerBar(delay));

        // Listen for the game over event
        game.setGameOverListener(this::endGame);


        // --- Set up Bindings from Game Properties to UI Labels ---

        // Bind the score label
        scoreLabel.textProperty().bind(Bindings.format("Score: %d", game.scoreProperty()));

        // Bind the level label
        levelLabel.textProperty().bind(Bindings.format("Level: %d", game.levelProperty()));

        // Bind the lives label
        livesLabel.textProperty().bind(Bindings.format("Lives: %d", game.livesProperty()));

        // Bind the multiplier label
        multiplierLabel.textProperty().bind(Bindings.format("Multiplier: %dx", game.multiplierProperty()));


        // --- Set up Keyboard Input ---
        scene.setOnKeyPressed(this::handleKeyPress);


        // --- Final Setup ---

    // Display the very first piece if it already exists
      if (game.getNextPiece() != null) {
        displayNextPiece(game.getCurrentPiece(), game.getNextPiece());
      }
        // Start the game logic (including the first timer)
        game.start();
    }
    /**
     * Animates the timer bar based on the provided delay.
     * @param delay the duration for the animation
     */
    private void animateTimerBar(int delay) {
        logger.info("Animating timer bar with delay: {}", delay);

        // Stop any existing animation before starting a new one
        if (timeline != null) {
            timeline.stop();
        }

        // Always start the bar as green and at full width
        timerBar.setFill(Color.GREEN);
        timerBar.setWidth(gameWindow.getWidth());

        // Create a new timeline and assign it to the class field
        timeline = new Timeline();
        timeline.getKeyFrames().addAll(
            // KeyFrame 1: At the beginning (0 seconds), the width is the full window width.
            new KeyFrame(Duration.ZERO, new KeyValue(timerBar.widthProperty(), gameWindow.getWidth())),

            // KeyFrame 2: At the end of the delay, the width is 0.
            new KeyFrame(new Duration(delay), new KeyValue(timerBar.widthProperty(), 0))
        );

        // --- DYNAMIC COLOR CHANGE LOGIC ---
        // Calculate the width of the bar that corresponds to 3 seconds left
        double redThreshold = (3000.0 / delay) * gameWindow.getWidth();
        // Calculate the width for the orange threshold (e.g., 50% of the time)
        double orangeThreshold = (0.5) * gameWindow.getWidth();

        // Create a listener that watches the width property of the timer bar
        ChangeListener<Number> widthListener = (obs, oldVal, newVal) -> {
            if (newVal.doubleValue() <= redThreshold) {
                timerBar.setFill(Color.RED);
            } else if (newVal.doubleValue() <= orangeThreshold) {
                timerBar.setFill(Color.ORANGE);
            }
        };

        // Add the listener to the width property
        timerBar.widthProperty().addListener(widthListener);

        // --- CLEANUP ---
        // When the animation is finished, we MUST remove the listener
        // to prevent it from affecting the next timer bar.
        timeline.setOnFinished(event -> {
            timerBar.widthProperty().removeListener(widthListener);
        });

        timeline.play();
    }
    /**
     * Called when the game is over.
     */
    private void endGame() {
      logger.info("Game over. Starting game over sequence.");
      shutdown(); // Call our new shutdown method to stop music and timers
      gameWindow.startGameOver(game);
    }
  /**
   * Called when this scene is shut down. This method should stop any timers,
   * music, or listeners that are specific to this scene.
   */
  @Override
  public void shutdown() {
    logger.info("Shutting down challenge scene");

    // Stop the game logic timer
    game.shutdown();

    // Stop any sounds that might be looping
    Multimedia.stopTicking();
    Multimedia.stopMusic();

    // Stop the visual timer bar animation
    if (timeline != null) {
      timeline.stop();
    }
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
            // Stop the visual timer bar animation
            if (timeline != null) {
                timeline.stop();
            }
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
          case Q:
          case E:
            game.rotateCurrentPiece();
            break;
          case SPACE:
          case R:
            game.swapCurrentPiece();
            break;
        }

        // After moving, update the visual hover effect
        board.hoverBlock(board.getBlock(aimX, aimY));
    }
    /**
     * Displays the next piece on the PieceBoard.
     * @param next the piece to display
     */
    private void displayNextPiece(GamePiece current, GamePiece next) {
      if (current != null) {
        currentPieceDisplay.displayPiece(current);
      }
      if (next != null) {
        nextPieceDisplay.displayPiece(next);
      }
    }


}
