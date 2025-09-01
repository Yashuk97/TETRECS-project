package uk.ac.soton.comp1206.multimedia; // <-- FIX: Lowercase 'm'

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A utility class for managing and playing sound effects and music.
 */
public class Multimedia {

  private static final Logger logger = LogManager.getLogger(Multimedia.class);
  private static MediaPlayer musicPlayer;
  private static MediaPlayer soundPlayer;
  private static MediaPlayer tickingPlayer; // Added for looping sounds

  /**
   * Play a music file on a loop. If music is already playing, it will be stopped.
   * @param musicFile the name of the music file in the resources/music/ folder
   */
  public static void playMusic(String musicFile) {
    if (musicPlayer != null) {
      musicPlayer.stop();
    }
    try {
      String musicPath = Multimedia.class.getResource("/music/" + musicFile).toExternalForm();
      Media media = new Media(musicPath);
      musicPlayer = new MediaPlayer(media);
      musicPlayer.setCycleCount(MediaPlayer.INDEFINITE); // Loop forever
      musicPlayer.play();
      logger.info("Playing music: {}", musicFile);
    } catch (Exception e) {
      logger.error("Failed to play music {}: {}", musicFile, e.getMessage());
    }
  }

  /**
   * Stop the currently playing music.
   */
  public static void stopMusic() {
    if (musicPlayer != null) {
      musicPlayer.stop();
      logger.info("Music stopped.");
    }
  }

  /**
   * Play a short sound effect once.
   * @param soundFile the name of the sound file in the resources/sounds/ folder
   */
  public static void playSound(String soundFile) {
    try {
      String soundPath = Multimedia.class.getResource("/sounds/" + soundFile).toExternalForm();
      Media media = new Media(soundPath);
      soundPlayer = new MediaPlayer(media);
      soundPlayer.play();
      logger.info("Playing sound: {}", soundFile);
    } catch (Exception e) {
      logger.error("Failed to play sound {}: {}", soundFile, e.getMessage());
    }
  }

  /**
   * Start playing the ticking sound on a loop.
   */
  public static void startTicking() {
    if (tickingPlayer != null) {
      tickingPlayer.stop();
    }
    try {
      String soundPath = Multimedia.class.getResource("/sounds/tick.wav").toExternalForm();
      Media media = new Media(soundPath);
      tickingPlayer = new MediaPlayer(media);
      tickingPlayer.setCycleCount(MediaPlayer.INDEFINITE); // Loop forever
      tickingPlayer.play();
      logger.info("Starting ticking sound");
    } catch (Exception e) {
      logger.error("Failed to play ticking sound: {}", e.getMessage());
    }
  }

  /**
   * Stop the ticking sound.
   */
  public static void stopTicking() {
    if (tickingPlayer != null) {
      tickingPlayer.stop();
      logger.info("Stopping ticking sound");
    }
  }
}