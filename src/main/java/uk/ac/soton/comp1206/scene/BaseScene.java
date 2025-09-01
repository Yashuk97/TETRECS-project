package uk.ac.soton.comp1206.scene;

import javafx.scene.Scene;
import javafx.scene.paint.Color;
import uk.ac.soton.comp1206.multimedia.Multimedia;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * A Base Scene used in the game. Handles common functionality between all scenes.
 */
public abstract class BaseScene {

    protected final GameWindow gameWindow;
    protected final Communicator communicator;

    protected GamePane root;
    protected Scene scene;

    /**
     * Create a new scene, passing in the GameWindow the scene will be displayed in
     * @param gameWindow the game window
     */
    public BaseScene(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
        this.communicator = gameWindow.getCommunicator();
    }

    /**
     * Initialise this scene. Called after creation
     */
    public abstract void initialise();

    /**
     * Build the layout of the scene
     */
    public abstract void build();

    /**
     * Create a new JavaFX scene using the root contained within this scene
     * @return JavaFX scene
     */
    public Scene setScene() {
        this.scene = new Scene(root,gameWindow.getWidth(),gameWindow.getHeight());
    this.scene.setFill(Color.BLACK);
        scene.getStylesheets().add(getClass().getResource("/style/game.css").toExternalForm());
        return this.scene;
    }
    /**
     * Called when the scene is being shut down.
     * Subclasses can override this to clean up resources like timers and listeners.
     */
    public void shutdown() {
        Multimedia.stopMusic();
    }

    /**
     * Get the JavaFX scene contained inside
     * @return JavaFX scene
     */
    public Scene getScene() {
        return this.scene;
    }

}
