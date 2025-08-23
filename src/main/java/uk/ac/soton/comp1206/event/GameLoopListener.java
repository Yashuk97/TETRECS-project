package uk.ac.soton.comp1206.event;

public interface GameLoopListener { // <-- FIX
  /**
   * Called when the game loop is reset.
   * @param delay the duration of the new timer cycle in milliseconds
   */
  void onGameLoop(int delay);
}