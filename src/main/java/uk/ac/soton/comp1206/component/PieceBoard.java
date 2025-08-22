package uk.ac.soton.comp1206.component;

import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.game.Grid;

/**
 * A special GameBoard used to display a single GamePiece.
 */
public class PieceBoard extends GameBoard {

  /**
   * Create a new PieceBoard with a 3x3 grid.
   */
  public PieceBoard() {
    super(new Grid(3, 3), 100, 100); // Create a 3x3 grid, size 100x100 pixels
  }

  /**
   * Displays a given GamePiece on this board.
   * @param piece The piece to display
   */
  public void displayPiece(GamePiece piece) {
    // Clear the board first
    grid.clear();

    // Place the piece in the center of our 3x3 grid
    grid.playPiece(piece, 1, 1);
  }
}