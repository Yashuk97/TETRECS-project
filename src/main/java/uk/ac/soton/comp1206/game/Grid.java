package uk.ac.soton.comp1206.game;

import java.util.HashSet;
import java.util.Set;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;

/**
 * The Grid is a model which holds the state of a game board. It is made up of a set of Integer values arranged in a 2D
 * arrow, with rows and columns.
 *
 * Each value inside the Grid is an IntegerProperty can be bound to enable modification and display of the contents of
 * the grid.
 *
 * The Grid contains functions related to modifying the model, for example, placing a piece inside the grid.
 *
 * The Grid should be linked to a GameBoard for it's display.
 */
public class Grid {

    /**
     * The number of columns in this grid
     */
    private final int cols;

    /**
     * The number of rows in this grid
     */
    private final int rows;

    /**
     * The grid is a 2D arrow with rows and columns of SimpleIntegerProperties.
     */
    private final SimpleIntegerProperty[][] grid;

    /**
     * Create a new Grid with the specified number of columns and rows and initialise them
     * @param cols number of columns
     * @param rows number of rows
     */
    public Grid(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;

        //Create the grid itself
        grid = new SimpleIntegerProperty[cols][rows];

        //Add a SimpleIntegerProperty to every block in the grid
        for(var y = 0; y < rows; y++) {
            for(var x = 0; x < cols; x++) {
                grid[x][y] = new SimpleIntegerProperty(0);
            }
        }
    }

    /**
     * Get the Integer property contained inside the grid at a given row and column index. Can be used for binding.
     * @param x column
     * @param y row
     * @return the IntegerProperty at the given x and y in this grid
     */
    public IntegerProperty getGridProperty(int x, int y) {
        return grid[x][y];
    }

    /**
     * Update the value at the given x and y index within the grid
     * @param x column
     * @param y row
     * @param value the new value
     */
    public void set(int x, int y, int value) {
        grid[x][y].set(value);
    }

    /**
     * Get the value represented at the given x and y index within the grid
     * @param x column
     * @param y row
     * @return the value
     */
    public int get(int x, int y) {
        try {
            //Get the value held in the property at the x and y index provided
            return grid[x][y].get();
        } catch (ArrayIndexOutOfBoundsException e) {
            //No such index
            return -1;
        }
    }

    /**
     * check if a gamepiece can be placed at the given grid coordinates
     * coordinates are for the center of the piece
     *
     * @param piece piece to check
     * @param gridX x-coordinate for the center of the piece
     * @param gridY y-coordinate for the center of the piece
     * @return true if piece can be placed, false if not
     */
    public boolean canPlayPiece(GamePiece piece, int gridX, int gridY){
        int[][] blocks = piece.getBlocks();

        //loop through 3x3 blocks of the gamepiece
        for (int x = 0; x < 3; x++){
            for (int y = 0; y < 3; y++){

                //if block in the piece is empty, we dont care, so we skip to next one
                if (blocks[x][y] == 0){
                    continue;
                }

                //calculate the actual position on the main grid
                // offset is -1 because top-left of the piece is at (0,0) relative to its center at (1,1)
                int actualX = gridX + (x-1);
                int actualY = gridY + (y-1);

                // ---- Rule 1: Check if block is out of bounds ----
                if (actualX < 0 || actualX >= cols || actualY < 0 || actualY >= rows) {
                    //this block of piece would be off the grid, so the placement is invalid
                    return false;
                }

                // ----Rule 2: Check if grid space is already occupied ----
                if (get(actualX, actualY) != 0){

                    //this grid cell is already filled so we can't place block here
                    return false;
                }
            }
        }
        //if we looped through all the pieces blocks and none of them failed the checks, then placement is valid

        return true;

    }

    /**
     * Place a given GamePiece on the grid at the given coordinates
     * assumes canPlayPiece has already been checked
     * coordinates are for the center of the piece
     *
     * @param piece the piece to place
     * @param gridX the x-coordinate for the center of piece
     * @param gridY the y-coordinate for the center of piece
     */
    public void playPiece(GamePiece piece, int gridX, int gridY){
        int[][] blocks = piece.getBlocks();

        //loop through 3x3 blocks of gamePiece
        for(int x = 0; x < 3; x++){
            for(int y = 0; y < 3; y++){

                //if block is in the piece is empty, there's nothing to place, so we skip it
                if (blocks[x][y] == 0){
                    continue;
                }

                //calculate the actual position on the main game grid
                int actualX = gridX + (x-1);
                int actualY = gridY + (y-1);

                //set the value of the grid cell to the value of the piece
                //set method updates simpleIntegerProperty, which will automatically update UI later because of binding
                set(actualX, actualY, piece.getValue());
            }
        }
    }
    /**
     * Clears any full lines (rows or columns) from the grid.
     * This method both DETECTS and CLEARS the lines.
     *
     * @return A HashSet containing the GameBlockCoordinates of all blocks that were cleared.
     */
    public HashSet<GameBlockCoordinate> clearLines() {
        HashSet<GameBlockCoordinate> clearedBlocks = new HashSet<>();

        // --- Check for full rows ---
        for (int y = 0; y < rows; y++) {
            boolean rowIsFull = true;
            for (int x = 0; x < cols; x++) {
                if (get(x, y) == 0) {
                    rowIsFull = false;
                    break;
                }
            }

            if (rowIsFull) {
                for (int x = 0; x < cols; x++) {
                    clearedBlocks.add(new GameBlockCoordinate(x, y));
                }
            }
        }

        // --- Check for full columns ---
        for (int x = 0; x < cols; x++) {
            boolean columnIsFull = true;
            for (int y = 0; y < rows; y++) {
                if (get(x, y) == 0) {
                    columnIsFull = false;
                    break;
                }
            }

            if (columnIsFull) {
                for (int y = 0; y < rows; y++) {
                    clearedBlocks.add(new GameBlockCoordinate(x, y));
                }
            }
        }

        // --- Now, clear the blocks identified ---
        // Note: The animation will happen first, then the data will be cleared
        // when the animation finishes via the binding.
        for (GameBlockCoordinate coord : clearedBlocks) {
            // We are NOT calling set(x,y,0) here directly anymore.
            // The animation's onFinished event will do this for us.
        }

        return clearedBlocks;
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
    /**
     * Clears the grid by setting all values to 0.
     */
    public void clear() {
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                set(x, y, 0);
            }
        }
    }

}
