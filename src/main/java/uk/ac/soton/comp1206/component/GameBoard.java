package uk.ac.soton.comp1206.component;

import java.util.Set;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.event.BlockClickedListener;
import uk.ac.soton.comp1206.event.RightclickedListener;
import uk.ac.soton.comp1206.game.Grid;
import uk.ac.soton.comp1206.multimedia.Multimedia;

/**
 * A GameBoard is a visual component to represent the visual GameBoard.
 * It extends a GridPane to hold a grid of GameBlocks.
 *
 * The GameBoard can hold an internal grid of it's own, for example, for displaying an upcoming block. It also be
 * linked to an external grid, for the main game board.
 *
 * The GameBoard is only a visual representation and should not contain game logic or model logic in it, which should
 * take place in the Grid.
 */
public class GameBoard extends GridPane {

    private static final Logger logger = LogManager.getLogger(GameBoard.class);

    /**
     * Number of columns in the board
     */
    private final int cols;

    /**
     * Number of rows in the board
     */
    private final int rows;

    private GameBlock currentlyHovered;

    /**
     * The visual width of the board - has to be specified due to being a Canvas
     */
    private final double width;

    /**
     * The visual height of the board - has to be specified due to being a Canvas
     */
    private final double height;

    /**
     * The grid this GameBoard represents
     */
    final Grid grid;

    /**
     * The blocks inside the grid
     */
    private final GameBlock[][] blocks;
    private RightclickedListener rightclickedListener;

    /**
     * The listener to call when a specific block is clicked
     */
    private BlockClickedListener blockClickedListener;


    /**
     * Create a new GameBoard, based off a given grid, with a visual width and height.
     * @param grid linked grid
     * @param width the visual width
     * @param height the visual height
     */
    public GameBoard(Grid grid, double width, double height) {
        this.cols = grid.getCols();
        this.rows = grid.getRows();
        this.width = width;
        this.height = height;
        this.grid = grid;

        setGridLinesVisible(true);

        getStyleClass().add("gameboard");
        this.blocks = new GameBlock[cols][rows];

        //Build the GameBoard
        buildGrid();
    }
    /**
     * Sets the hover effect on a specific block and removes it from the previous one.
     * @param block the block to hover over
     */
    public void hoverBlock(GameBlock block) {
        // If a block was previously hovered, turn off its hover effect
        if (currentlyHovered != null) {
            currentlyHovered.setExternalHover(false); // <--- CHANGE HERE
        }

        // Set the new block as the currently hovered one and turn on its hover effect
        currentlyHovered = block;
        if (currentlyHovered != null) {
            currentlyHovered.setExternalHover(true); // <--- AND HERE
        }
    }
    /**
     * Triggers the fade-out animation on a set of blocks.
     * @param coordinates The set of coordinates for the blocks to be faded.
     */
    public void fadeOut(Set<GameBlockCoordinate> coordinates) {
        for (GameBlockCoordinate coord : coordinates) {
            getBlock(coord.getX(), coord.getY()).fadeOut();
        }
    }

    /**
     * Create a new GameBoard with it's own internal grid, specifying the number of columns and rows, along with the
     * visual width and height.
     *
     * @param cols number of columns for internal grid
     * @param rows number of rows for internal grid
     * @param width the visual width
     * @param height the visual height
     */
    public GameBoard(int cols, int rows, double width, double height) {
        this.cols = cols;
        this.rows = rows;
        this.width = width;
        this.height = height;
        this.grid = new Grid(cols,rows);
        this.blocks = new GameBlock[cols][rows];

        //Build the GameBoard
        buildGrid();
    }

    /**
     * Get a specific block from the GameBoard, specified by it's row and column
     * @param x column
     * @param y row
     * @return game block at the given column and row
     */
    public GameBlock getBlock(int x, int y) {
        return blocks[x][y];
    }

    /**
     * Build the GameBoard by creating a block at every x and y column and row
     */
    private void buildGrid() {
        logger.info("Building grid: {} x {}",cols,rows);

        setMaxWidth(width);
        setMaxHeight(height);

        setGridLinesVisible(true);

        getChildren().clear(); // Clear the visual grid first

        for(var y = 0; y < rows; y++) {
            for (var x = 0; x < cols; x++) {
                // Create the block
                GameBlock block = buildBlock(x,y);
                // Add it to our 2D array for later access
                this.blocks[x][y] = block;
                // Add it to the visual grid
                add(block,x,y);
            }
        }
    }

    /**
     * Create a block at the given x and y position in the GameBoard
     * @param x column
     * @param y row
     */
    protected GameBlock createBlock(int x, int y) {
        var blockWidth = width / cols;
        var blockHeight = height / rows;

        //Create a new GameBlock UI component
        GameBlock block = new GameBlock(this, x, y, blockWidth, blockHeight);

        //Add to the GridPane
        add(block,x,y);

        //Add to our block directory
        blocks[x][y] = block;

        //Link the GameBlock component to the corresponding value in the Grid
        block.bind(grid.getGridProperty(x,y));

        //Add a mouse click handler to the block to trigger GameBoard blockClicked method
        block.setOnMouseClicked((e) -> blockClicked(e, block));

        return block;
    }

    /**
     * Set the listener to handle an event when a block is clicked
     * @param listener listener to add
     */
    public void setOnBlockClick(BlockClickedListener listener) {
        this.blockClickedListener = listener;
    }

    /**
     * Triggered when a block is clicked. Call the attached listener.
     * @param event mouse event
     * @param block block clicked on
     */
    private void blockClicked(MouseEvent event, GameBlock block) {
        logger.info("Block clicked: {}", block);
        Multimedia.playSound("place.wav");
        if(blockClickedListener != null) {
            blockClickedListener.blockClicked(block);
        }
    }
    /**
     * Create a block at the given x and y position in the GameBoard
     * @param x column
     * @param y row
     * @return the new GameBlock
     */
    protected GameBlock buildBlock(int x, int y) {
        var block = new GameBlock(this, x, y, width / cols, height / rows);

        //Give the block the correct value from our grid
        block.bind(grid.getGridProperty(x,y));

        //Add a mouse click handler to the block to trigger GameBoard's click handler
        block.setOnMouseClicked((e) -> blockClicked(e, block));

        block.setOnContextMenuRequested(e -> {
            if (rightclickedListener != null) {
                rightclickedListener.onRightClick();
            }
        });

        return block;
    }
    public void setOnRightClicked(RightclickedListener listener) {
        this.rightclickedListener = listener;
    }

}
