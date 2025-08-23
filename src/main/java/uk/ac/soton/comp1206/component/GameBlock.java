package uk.ac.soton.comp1206.component;

import javafx.animation.FadeTransition;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.*;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javafx.scene.canvas.GraphicsContext;

/**
 * The Visual User Interface component representing a single block in the grid.
 *
 * Extends Canvas and is responsible for drawing itself.
 *
 * Displays an empty square (when the value is 0) or a coloured square depending on value.
 *
 * The GameBlock value should be bound to a corresponding block in the Grid model.
 */
public class GameBlock extends Canvas {

    private final GraphicsContext gc;
    private boolean isCenter = false;

    private static final Logger logger = LogManager.getLogger(GameBlock.class);

    /**
     * The set of colours for different pieces
     */
    public static final Color[] COLOURS = {
            Color.TRANSPARENT,
            Color.DEEPPINK,
            Color.RED,
            Color.ORANGE,
            Color.YELLOW,
            Color.YELLOWGREEN,
            Color.LIME,
            Color.GREEN,
            Color.DARKGREEN,
            Color.DARKTURQUOISE,
            Color.DEEPSKYBLUE,
            Color.AQUA,
            Color.AQUAMARINE,
            Color.BLUE,
            Color.MEDIUMPURPLE,
            Color.PURPLE
    };


    private final GameBoard gameBoard;

    private final double width;
    private final double height;

    /**
     * The column this block exists as in the grid
     */
    private final int x;

    /**
     * The row this block exists as in the grid
     */
    private final int y;

    /**
     * The value of this block (0 = empty, otherwise specifies the colour to render as)
     */
    private final IntegerProperty value = new SimpleIntegerProperty(0);

    /**
     * Create a new single Game Block
     * @param gameBoard the board this block belongs to
     * @param x the column the block exists in
     * @param y the row the block exists in
     * @param width the width of the canvas to render
     * @param height the height of the canvas to render
     */
    public GameBlock(GameBoard gameBoard, int x, int y, double width, double height) {
        this.gameBoard = gameBoard;
        this.width = width;
        this.height = height;
        this.gc = getGraphicsContext2D();
        getStyleClass().add("gameblock");

        this.x = x;
        this.y = y;

        //A canvas needs a fixed width and height
        setWidth(width);
        setHeight(height);


        //Do an initial paint
        paint();

        //When the value property is updated, call the internal updateValue method
        value.addListener(this::updateValue);
    }

    /**
     * When the value of this block is updated,
     * @param observable what was updated
     * @param oldValue the old value
     * @param newValue the new value
     */
    private void updateValue(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        paint();
    }
    /**
     * Public method to set the hover state of this block.
     * This safely controls the protected setHover method and triggers a repaint.
     * @param hover true to enable hover, false to disable
     */
    public void setExternalHover(boolean hover) {
        // Internally, call the protected method that JavaFX provides
        setHover(hover);
        // Manually trigger a repaint to show the change
        paint();
    }

    /**
     * Handle painting of the block canvas
     */
    private void paint() {
        //If the block is empty, paint it transparent
        if(value.get() == 0) {
            gc.clearRect(0,0,width,height);
            return;
        }

        Color color = COLOURS[value.get()];

        // 1. Fill the main block color first
        gc.setFill(color);
        gc.fillRect(0, 0, width, height);

        // 2. Draw darker shades for a 3D shadow effect
        gc.setFill(color.darker());
        gc.fillRect(0, height - 3, width, 3); // Bottom edge
        gc.fillRect(width - 3, 0, 3, height); // Right edge

        // 3. Draw lighter shades for a 3D highlight effect
        gc.setFill(color.brighter());
        gc.fillRect(0, 0, width, 3); // Top edge
        gc.fillRect(0, 0, 3, height); // Left edge

        // 4. Draw the hover effect last so it's on top
        if (isHover()) {
            gc.setFill(Color.color(1, 1, 1, 0.5)); // White with 50% transparency
            gc.fillRect(0, 0, width, height);
        }

        // 5. Draw a circle in the center if this is a center block
        if (isCenter) {
            gc.setFill(Color.color(1, 1, 1, 0.7)); // Semi-transparent white
            gc.fillOval(width / 4, height / 4, width / 2, height / 2);
        }
    }
    public void setCenter(boolean isCenter) {
        this.isCenter = isCenter;
        paint(); // Repaint to show/hide the circle
    }
    /**
     * Triggers a fade-out animation on this block.
     */
    public void fadeOut() {
        FadeTransition fade = new FadeTransition(Duration.millis(500), this);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        fade.play();
    }


    /**
     * Paint this canvas empty
     */
    private void paintEmpty() {
        var gc = getGraphicsContext2D();

        //Clear
        gc.clearRect(0,0,width,height);

        //Fill
        gc.setFill(Color.WHITE);
        gc.fillRect(0,0, width, height);

        //Border
        gc.setStroke(Color.BLACK);
        gc.strokeRect(0,0,width,height);
    }

    /**
     * Paint this canvas with the given colour
     * @param colour the colour to paint
     */
    private void paintColor(Paint colour) {
        var gc = getGraphicsContext2D();

        //Clear
        gc.clearRect(0,0,width,height);

        //Colour fill
        gc.setFill(colour);
        gc.fillRect(0,0, width, height);

        //Border
        gc.setStroke(Color.BLACK);
        gc.strokeRect(0,0,width,height);
    }

    /**
     * Get the column of this block
     * @return column number
     */
    public int getX() {
        return x;
    }

    /**
     * Get the row of this block
     * @return row number
     */
    public int getY() {
        return y;
    }

    /**
     * Get the current value held by this block, representing it's colour
     * @return value
     */
    public int getValue() {
        return this.value.get();
    }

    /**
     * Bind the value of this block to another property. Used to link the visual block to a corresponding block in the Grid.
     * @param input property to bind the value to
     */
    public void bind(ObservableValue<? extends Number> input) {
        value.bind(input);
    }

}
