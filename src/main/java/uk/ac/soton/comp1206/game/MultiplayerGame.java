package uk.ac.soton.comp1206.game;

import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.util.Pair;
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
    super(cols, rows, true);
    this.communicator = communicator;
    setupNetworking();
  }

  private void setupNetworking() {
    // Create and add the listener
    listener = this::receiveCommunication;
    communicator.addListener(listener);

    requestPiece();
    requestPiece();
  }

  public void receiveCommunication(String message) {
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

    }finalScores.clear();
    if (leaderboardProperty.get() != null){
      for (String scoreLine : leaderboardProperty.get()) {
        try {
          String[] nameAndScoreParts = scoreLine.split(":");
          String name = nameAndScoreParts[0];
          // Safely parse the score, ignoring the (X Lives) part
          int score = Integer.parseInt(nameAndScoreParts[1].trim().split(" ")[0]);
          finalScores.add(new Pair<>(name, score));

        } catch (Exception e){
          logger.error("Failed to parse final score line: {}", scoreLine, e);
        }
      }
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
      String[] parts = line.split(":");
      if (parts.length == 3) { // Ensure we have all 3 parts
        String name = parts[0];
        String score = parts[1];
        String lives = parts[2];

        // Format the string for display
        String status = lives.equals("DEAD") ? "DEAD" : lives + " Lives";
        scores.add(name + ": " + score + " (" + status + ")");
      }
    }
    leaderboardProperty.set(FXCollections.observableArrayList(scores));
  }
}