package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import java.util.Set;

public interface LineClearedListener {
  /**
   * Called when one or more lines are cleared.
   * @param clearedBlocks A set of the coordinates of the blocks that were cleared.
   */
  void onLineCleared(Set<GameBlockCoordinate> clearedBlocks);
}