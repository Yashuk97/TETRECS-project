package uk.ac.soton.comp1206.utility;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class Statistics {
  private static final Logger logger = LogManager.getLogger(Statistics.class);
  private static final String STATS_FILE_NAME = "stats.properties";
  private static final String statsFilePath;

  public static final IntegerProperty gamesPlayed = new SimpleIntegerProperty(0);
  public static final IntegerProperty linesCleared = new SimpleIntegerProperty(0);
  public static final IntegerProperty highestScore = new SimpleIntegerProperty(0);
  public static final IntegerProperty highestMultiplier = new SimpleIntegerProperty(0);

  static {
    // Define the save file path once when the class is loaded
    String gameDataFolder = System.getProperty("user.home") + "/.TetrECS";
    statsFilePath = gameDataFolder + "/" + STATS_FILE_NAME;
    new File(gameDataFolder).mkdirs(); // Ensure the directory exists
    loadStats();
  }

  public static void loadStats() {
    Properties props = new Properties();
    File statsFile = new File(statsFilePath);
    if (!statsFile.exists()) {
      logger.warn("stats.properties does not exist. Using defaults.");
      return;
    }

    logger.info("Loading stats from: {}", statsFilePath);
    try (FileInputStream in = new FileInputStream(statsFile)) {
      props.load(in);
      gamesPlayed.set(Integer.parseInt(props.getProperty("gamesPlayed", "0")));
      linesCleared.set(Integer.parseInt(props.getProperty("linesCleared", "0")));
      highestScore.set(Integer.parseInt(props.getProperty("highestScore", "0")));
      highestMultiplier.set(Integer.parseInt(props.getProperty("highestMultiplier", "0")));
    } catch (IOException | NumberFormatException e) {
      logger.error("Failed to load or parse stats, using defaults.", e);
    }
  }

  public static void saveStats() {
    Properties props = new Properties();
    props.setProperty("gamesPlayed", String.valueOf(gamesPlayed.get()));
    props.setProperty("linesCleared", String.valueOf(linesCleared.get()));
    props.setProperty("highestScore", String.valueOf(highestScore.get()));
    props.setProperty("highestMultiplier", String.valueOf(highestMultiplier.get()));

    logger.info("Saving stats to: {}", statsFilePath);
    try (FileOutputStream out = new FileOutputStream(statsFilePath)) {
      props.store(out, "TetrECS Game Statistics");
    } catch (IOException e) {
      logger.error("Failed to save stats: {}", e.getMessage());
    }
  }
}