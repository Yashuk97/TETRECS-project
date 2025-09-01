package uk.ac.soton.comp1206.utility;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class Settings {
  private static final Logger logger = LogManager.getLogger(Settings.class);
  private static final String CONFIG_FILE = "config.properties";

  // Use JavaFX properties so the UI can bind to them
  public static final DoubleProperty musicVolume = new SimpleDoubleProperty(0.5); // Default 50%
  public static final DoubleProperty sfxVolume = new SimpleDoubleProperty(0.5);   // Default 50%

  static {
    // This block runs once when the class is first loaded
    loadSettings();
  }

  public static void loadSettings() {
    Properties props = new Properties();
    try (FileInputStream in = new FileInputStream(CONFIG_FILE)) {
      props.load(in);
      musicVolume.set(Double.parseDouble(props.getProperty("musicVolume", "0.5")));
      sfxVolume.set(Double.parseDouble(props.getProperty("sfxVolume", "0.5")));
      logger.info("Settings loaded from {}", CONFIG_FILE);
    } catch (IOException e) {
      logger.error("Failed to load settings, using defaults. {}", e.getMessage());
      // If file doesn't exist, defaults are already set.
    }
  }

  public static void saveSettings() {
    Properties props = new Properties();
    props.setProperty("musicVolume", String.valueOf(musicVolume.get()));
    props.setProperty("sfxVolume", String.valueOf(sfxVolume.get()));
    try (FileOutputStream out = new FileOutputStream(CONFIG_FILE)) {
      props.store(out, "TetrECS Game Settings");
      logger.info("Settings saved to {}", CONFIG_FILE);
    } catch (IOException e) {
      logger.error("Failed to save settings: {}", e.getMessage());
    }
  }
}