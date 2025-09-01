package uk.ac.soton.comp1206.game;

import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.event.CommunicationsListener;
import uk.ac.soton.comp1206.network.Communicator;
import java.util.ArrayList;

public class MultiplayerGame extends Game {

  private static final Logger logger = LogManager.getLogger(MultiplayerGame.class);
  private final Communicator communicator;
  private final ArrayList<GamePiece> pieceQueue = new ArrayList<>();
  private final ListProperty<String> leaderboardProperty = new SimpleListProperty<>(FXCollections.observableArrayList());
  private CommunicationsListener listener; // Field to hold our listener

  public MultiplayerGame(int cols, int rows, Communicator communicator) {
    super(cols, rows);
    this.communicator = communicator;
    setupNetworking();
  }

  private void setupNetworking() {
    // Create and add the listener
    listener = this::handleMessage;
    communicator.addListener(listener);

    requestPiece();
    requestPiece();
  }

  private void handleMessage(String message) {
    Platform.runLater(() -> {
      String[] parts = message.split(" ", 2);
      String command = parts[0];
      String data = (parts.length > 1) ? parts[1] : "";

      if (command.equals("PIECE")) {
        int pieceIndex = Integer.parseInt(data);
        pieceQueue.add(GamePiece.createPiece(pieceIndex));
        if (currentPiece == null) {
          nextPiece();
        }
      } else if (command.equals("SCORES")) {
        updateLeaderboard(data);
      }
    });
  }

  @Override
  public void shutdown() {
    super.shutdown();
    // When the game shuts down, remove its listener
    if (listener != null) {
      communicator.removeListener(listener);
    }
  }

  @Override
  public GamePiece spawnPiece() {
    if (pieceQueue.isEmpty()) {
      logger.warn("Piece queue is empty! Requesting new piece.");
      requestPiece();
      return GamePiece.createPiece(0);
    }
    return pieceQueue.remove(0);
  }

  @Override
  public void nextPiece() {
    super.nextPiece();
    requestPiece();
  }

  @Override
  protected void score(int linesCleared, int blocksCleared) {
    super.score(linesCleared, blocksCleared);
    communicator.send("SCORE " + score.get());
  }

  @Override
  protected void gameLoop() {
    super.gameLoop();
    communicator.send("LIVES " + lives.get());
  }

  private void requestPiece() {
    communicator.send("PIECE");
  }

  public ListProperty<String> getLeaderboardProperty() {
    return leaderboardProperty;
  }

  private void updateLeaderboard(String data) {
    ArrayList<String> scores = new ArrayList<>();
    String[] lines = data.split("\n");
    for (String line : lines) {
      scores.add(line.replace(":", ": "));
    }
    leaderboardProperty.set(FXCollections.observableArrayList(scores));
  }
}