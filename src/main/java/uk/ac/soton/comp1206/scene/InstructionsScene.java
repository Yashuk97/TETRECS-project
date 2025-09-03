package uk.ac.soton.comp1206.scene;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * The Instructions scene. Displays the game rules and pieces.
 */
public class InstructionsScene extends BaseScene {

  private static final Logger logger = LogManager.getLogger(InstructionsScene.class);

  public InstructionsScene(GameWindow gameWindow) {
    super(gameWindow);
  }

  @Override
  public void initialise() {
    // Setup keyboard listener for ESC key to go back to the menu
    getScene()
        .setOnKeyPressed(
            event -> {
              if (event.getCode().toString().equals("ESCAPE")) {
                gameWindow.startMenu();
              }
            });
  }

  @Override
  public void build() {
    logger.info("Building " + this.getClass().getName());
    root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

    var instructionsPane = new StackPane();
    instructionsPane.setMaxWidth(gameWindow.getWidth());
    instructionsPane.setMaxHeight(gameWindow.getHeight());
    instructionsPane.getStyleClass().add("menu-background");
    root.getChildren().add(instructionsPane);

    // --- WRAP IN A SCROLLPANE ---
    var scroller = new ScrollPane();
    scroller.setFitToWidth(true);
    scroller.setFitToHeight(true);
    scroller.getStyleClass().add("scroller"); // Makes it transparent
    instructionsPane.getChildren().add(scroller);

    var mainPane = new BorderPane();
    scroller.setContent(mainPane); // Put the BorderPane inside the scroller
    mainPane.setPadding(new Insets(20));

    // Title
    var title = new Text("Instructions");
    title.getStyleClass().add("title");
    mainPane.setTop(title);
    BorderPane.setAlignment(title, Pos.CENTER);

    // Instructions Text
    var instructionsText =
        new Text(
            "TetrECS is a fast-paced block placement game.\n\n"
                + "Place pieces on the 5x5 grid to clear horizontal and vertical lines.\n\n"
                + "The more lines you clear at once, the more points you get!\n\n"
                + "Clearing lines consecutively builds your score multiplier.\n\n"
                + "Don't let the timer run out, or you'll lose a life!\n\n"
                + "Lose 3 lives, and it's game over.");
    instructionsText.getStyleClass().add("instructions");
    mainPane.setCenter(instructionsText);
    BorderPane.setAlignment(instructionsText, Pos.TOP_CENTER);
    BorderPane.setMargin(instructionsText, new Insets(20));

    // Piece Display
    var piecesTitle = new Text("Game Pieces");
    piecesTitle.getStyleClass().add("heading");

    GridPane pieceGrid = new GridPane();
    pieceGrid.setHgap(12);
    pieceGrid.setVgap(12);
    pieceGrid.setAlignment(Pos.CENTER);

    int counter = 0;
    for (int i = 0; i < 3; i++) {
      for (int j = 0; j < 5; j++) {
        PieceBoard pieceBoard = new PieceBoard();
        pieceBoard.displayPiece(GamePiece.createPiece(counter));
        pieceGrid.add(pieceBoard, j, i);
        counter++;
      }
    }

    VBox piecesBox = new VBox(20);
    piecesBox.setAlignment(Pos.CENTER);
    piecesBox.getChildren().addAll(piecesTitle, pieceGrid);

    mainPane.setBottom(piecesBox);
  }
  }