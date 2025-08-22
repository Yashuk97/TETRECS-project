package uk.ac.soton.comp1206.game;

import java.util.HashSet;
import java.util.Random;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import uk.ac.soton.comp1206.event.LineClearedListener;
import uk.ac.soton.comp1206.event.NextPieceListener;

/**
 * The Game class handles the main logic, state and properties of the TetrECS game. Methods to manipulate the game state
 * and to handle actions made by the player should take place inside this class.
 */
public class Game {

    private static final Logger logger = LogManager.getLogger(Game.class);

    private GamePiece currentPiece;
    private GamePiece nextPiece;
    /**
     * Number of rows
     */
    protected final int rows;
    private NextPieceListener nextPieceListener;

    /**
     * Number of columns
     */
    protected final int cols;
    private LineClearedListener lineClearedListener;

    /**
     * The grid model linked to the game
     */
    protected final Grid grid;
    public final IntegerProperty score = new SimpleIntegerProperty(0);
    public final IntegerProperty level = new SimpleIntegerProperty(0);
    public final IntegerProperty lives = new SimpleIntegerProperty(3);
    public final IntegerProperty multiplier = new SimpleIntegerProperty(1);

    /**
     * Create a new game with the specified rows and columns. Creates a corresponding grid model.
     * @param cols number of columns
     * @param rows number of rows
     */
    public Game(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;

        //Create a new grid model to represent the game state
        this.grid = new Grid(cols,rows);
        logger.info("New game created with size {}x{}", cols, rows);

        //start game with a piece
        nextPiece = spawnPiece();
        nextPiece();
    }

    /**
     * Start the game
     */
    public void start() {
        logger.info("Starting game");
        initialiseGame();
    }

    /**
     * Initialise a new game and set up anything that needs to be done at the start
     */
    public void initialiseGame() {
        logger.info("Initialising game");
    }

    /**
     * Handle what should happen when a particular block is clicked
     * @param gameBlock the block that was clicked
     */
    public void blockClicked(GameBlock gameBlock) {
        //Get the position of this block
        int x = gameBlock.getX();
        int y = gameBlock.getY();
        logger.info("Block clicked at ({}, {})", x, y);

        //checking if the piece can be played at this location
        if (grid.canPlayPiece(currentPiece, x, y)){
            //if it can, then play the piece
            logger.info("Piece {} can be played at ({}, {})", currentPiece.toString(), x, y);
            grid.playPiece(currentPiece, x, y);

            afterPiece();

        } else {
            logger.info("Cannot play piece {} at ({}, {})", currentPiece.toString(), x, y);
        //add sound if a piece fails to move
        }

        //Get the new value for this block
        int previousValue = grid.get(x,y);
        int newValue = previousValue + 1;
        if (newValue  > GamePiece.PIECES) {
            newValue = 0;
        }

        //Update the grid with the new value
        grid.set(x,y,newValue);
    }
    /**
     * spawns a new random GamePiece
     * @return the new GamePiece
     */
    public GamePiece spawnPiece(){
        //GamePiece class has 15 pieces defined, numbered 0-14
        Random random = new Random();
        int pieceNumber = random.nextInt(15);
        logger.info("Spawning new piece: {}", pieceNumber);
        return GamePiece.createPiece(pieceNumber);
    }
    /**
     * handle what happens when next piece is needed
     */
    public void nextPiece() {
        logger.info("Generating next piece");
        currentPiece = nextPiece; // The old "next" is now the "current"
        nextPiece = spawnPiece(); // Generate a new "next" piece

        //notify listener that a new piece is ready
        if(nextPieceListener != null){
            nextPieceListener.onNextPiece(nextPiece);
        }
    }
    /**
     * handles the logic that should occur after a piece has been played
     * includes clearing lines and preparing the next piece
     */
    private void afterPiece() {
        // This single call now detects the lines and returns the blocks to be cleared
        HashSet<GameBlockCoordinate> clearedBlocks = grid.clearLines();

        if (!clearedBlocks.isEmpty()) {
            logger.info("Lines cleared! Animating {} blocks.", clearedBlocks.size());

            // Notify the listener so the animation can start
            if (lineClearedListener != null) {
                lineClearedListener.onLineCleared(clearedBlocks);
            }
        }

        // --- Calculate score ---
        int linesCleared = 0;
        HashSet<Integer> clearedRows = new HashSet<>();
        HashSet<Integer> clearedCols = new HashSet<>();
        for(GameBlockCoordinate coord : clearedBlocks) {
            clearedRows.add(coord.getY());
            clearedCols.add(coord.getX());
        }
        linesCleared = clearedRows.size() + clearedCols.size();

        score(linesCleared, clearedBlocks.size());

        // Get the next piece ready for the player
        nextPiece();
    }

    /**
     * Handles scoring based on lines and blocks cleared.
     * @param linesCleared the number of lines cleared
     * @param blocksCleared the number of blocks cleared
     */
    private void score(int linesCleared, int blocksCleared) {
        if (linesCleared > 0) {
            // Add points based on the formula
            int points = linesCleared * blocksCleared * 10 * multiplier.get();
            score.set(score.get() + points);

            // Increase the multiplier for the next successful clear
            multiplier.set(multiplier.get() + 1);
        } else {
            // If no lines were cleared, reset the multiplier
            multiplier.set(1);
        }

        // Update the level based on the score
        level.set(score.get() / 1000);
    }
    public void setNextPieceListener(NextPieceListener listener){
        this.nextPieceListener = listener;
    }
    public void setLineClearedListener(LineClearedListener listener) {
        this.lineClearedListener = listener;
    }

    /**
     * Get the grid model inside this game representing the game state of the board
     * @return game grid model
     */
    public Grid getGrid() {
        return grid;
    }

    public GamePiece getNextPiece() {
        return nextPiece;
    }
    /**
     * Get the number of columns in this game
     * @return number of columns
     */
    public int getCols() {
        return cols;
    }

    /**
     * Get the number of rows in this game
     * @return number of rows
     */
    public int getRows() {
        return rows;
    }
    public IntegerProperty scoreProperty(){
        return score;
    }
    public IntegerProperty levelProperty(){
        return level;
    }
    public IntegerProperty livesProperty(){
        return lives;
    }
    public IntegerProperty multiplierProperty(){
        return multiplier;
    }


}
