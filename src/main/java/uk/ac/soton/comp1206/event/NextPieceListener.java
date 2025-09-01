package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.game.GamePiece;

public interface NextPieceListener {
  /**
   * Called when a new piece is generated.
   * @param nextPiece the new piece
   */
  void onNextPiece(GamePiece currentPiece, GamePiece nextPiece);
}