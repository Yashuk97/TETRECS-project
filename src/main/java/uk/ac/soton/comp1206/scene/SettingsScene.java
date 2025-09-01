package uk.ac.soton.comp1206.scene;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.utility.Settings;

public class SettingsScene extends BaseScene {

  private static final Logger logger = LogManager.getLogger(SettingsScene.class);

  public SettingsScene(GameWindow gameWindow) {
    super(gameWindow);
  }

  @Override
  public void initialise() {
    // Go back to the menu when ESC is pressed
    scene.setOnKeyPressed(event -> {
      if (event.getCode() == KeyCode.ESCAPE) {
        gameWindow.startMenu();
      }
    });
  }

  @Override
  public void build() {
    logger.info("Building " + this.getClass().getName());
    root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

    var settingsPane = new StackPane();
    settingsPane.setMaxWidth(gameWindow.getWidth());
    settingsPane.setMaxHeight(gameWindow.getHeight());
    settingsPane.getStyleClass().add("menu-background");
    root.getChildren().add(settingsPane);

    var mainPane = new BorderPane();
    settingsPane.getChildren().add(mainPane);

    var title = new Text("Settings");
    title.getStyleClass().add("title");
    BorderPane.setAlignment(title, Pos.CENTER);
    mainPane.setTop(title);

    // --- UI for settings controls ---
    var settingsVBox = new VBox(20);
    settingsVBox.setAlignment(Pos.CENTER);
    settingsVBox.setPadding(new Insets(20));
    mainPane.setCenter(settingsVBox);

    // --- Music Volume Slider ---
    var musicLabel = new Label("Music Volume");
    musicLabel.getStyleClass().add("heading");
    Slider musicSlider = new Slider(0, 1, Settings.musicVolume.get());
    musicSlider.setMaxWidth(300);

    // Bind the slider's value directly to our settings property (bidirectional)
    Settings.musicVolume.bindBidirectional(musicSlider.valueProperty());

    // --- SFX Volume Slider ---
    var sfxLabel = new Label("Sound Effects Volume");
    sfxLabel.getStyleClass().add("heading");
    Slider sfxSlider = new Slider(0, 1, Settings.sfxVolume.get());
    sfxSlider.setMaxWidth(300);

    // Bind this slider too
    Settings.sfxVolume.bindBidirectional(sfxSlider.valueProperty());

    settingsVBox.getChildren().addAll(musicLabel, musicSlider, sfxLabel, sfxSlider);

    // --- Save/Back Button ---
    Button backButton = new Button("Back to Menu");
    backButton.getStyleClass().add("menu-button");
    BorderPane.setAlignment(backButton, Pos.CENTER);
    mainPane.setBottom(backButton);
    BorderPane.setMargin(backButton, new Insets(50));

    backButton.setOnAction(e -> gameWindow.startMenu());
  }

  @Override
  public void shutdown() {
    // When leaving the scene, save the settings
    Settings.saveSettings();
  }
}