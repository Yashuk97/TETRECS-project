package uk.ac.soton.comp1206.utility;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class Statistics {
  private static final Logger logger = LogManager.getLogger(Statistics.class);
  private static final String STATS_FILE = "stats.properties";

  // Use JavaFX properties for stats that might be displayed live
  public static final IntegerProperty gamesPlayed = new SimpleIntegerProperty(0);
  public static final IntegerProperty linesCleared = new SimpleIntegerProperty(0);
  public static final IntegerProperty highestScore = new SimpleIntegerProperty(0);
  public static final IntegerProperty highestMultiplier = new SimpleIntegerProperty(0);

  static {
    loadStats();
  }

  public static void loadStats() {
    Properties props = new Properties();
    try (FileInputStream in = new FileInputStream(STATS_FILE)) {
      props.load(in);
      gamesPlayed.set(Integer.parseInt(props.getProperty("gamesPlayed", "0")));
      linesCleared.set(Integer.parseInt(props.getProperty("linesCleared", "0")));
      highestScore.set(Integer.parseInt(props.getProperty("highestScore", "0")));
      highestMultiplier.set(Integer.parseInt(props.getProperty("highestMultiplier", "0")));
      logger.info("Stats loaded from {}", STATS_FILE);
    } catch (IOException e) {
      logger.error("Failed to load stats, using defaults. {}", e.getMessage());
    }
  }

  public static void saveStats() {
    Properties props = new Properties();
    props.setProperty("gamesPlayed", String.valueOf(gamesPlayed.get()));
    props.setProperty("linesCleared", String.valueOf(linesCleared.get()));
    props.setProperty("highestScore", String.valueOf(highestScore.get()));
    props.setProperty("highestMultiplier", String.valueOf(highestMultiplier.get()));
    try (FileOutputStream out = new FileOutputStream(STATS_FILE)) {
      props.store(out, "TetrECS Game Statistics");
      logger.info("Stats saved to {}", STATS_FILE);
    } catch (IOException e) {
      logger.error("Failed to save stats: {}", e.getMessage());
    }
  }
}