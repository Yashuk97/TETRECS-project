package uk.ac.soton.comp1206.component;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Pair;

/**
 * A custom UI component to display a list of scores.
 * It automatically updates its display when the underlying list of scores changes.
 */
public class ScoresList extends VBox {

  // A property to hold the list of scores.
  private final ListProperty<Pair<String, Integer>> scores = new SimpleListProperty<>();

  public ScoresList() {
    setAlignment(Pos.CENTER);
    setSpacing(5);
    getStyleClass().add("scorelist");

    // Add a listener to rebuild the visual list whenever the scores property is updated.
    scores.addListener((ListChangeListener<Pair<String, Integer>>) c -> {
      while (c.next()) {
        // This block runs for any change: add, remove, update.
        // The simplest way to handle all changes is to just rebuild the whole list.
      }
      buildList(); // Rebuild the visual list
    });  }

  /**
   * Clears and rebuilds the visual list of scores based on the current data.
   */
  private void buildList() {
    getChildren().clear();
    int counter = 1;

    if (scores.get() == null) return;

    for (Pair<String, Integer> scoreEntry : scores.get()) {
      // Create an HBox for each score entry
      HBox scoreBox = new HBox();
      scoreBox.setAlignment(Pos.CENTER);
      scoreBox.setSpacing(20); // Add a fixed space between name and value
      scoreBox.setMaxWidth(300); // ** Constrain the width **

      // Text for the rank and name
      Text nameText = new Text(counter + ". " + scoreEntry.getKey() + ":");
      nameText.getStyleClass().add("score-name");

      // Text for the score value
      Text valueText = new Text(scoreEntry.getValue().toString());
      valueText.getStyleClass().add("score-value");

      // We no longer need the spacer
      // Pane spacer = new Pane();
      // HBox.setHgrow(spacer, Priority.ALWAYS);

      scoreBox.getChildren().addAll(nameText, valueText); // Just the name and value
      getChildren().add(scoreBox);
      counter++;
    }
  }


  // Standard getter for the property, needed for binding.
  public ListProperty<Pair<String, Integer>> scoresProperty() {
    return scores;
  }

  // A convenience method to set the scores list.
  public void setScores(ObservableList<Pair<String, Integer>> scores) {
    this.scores.set(scores);
  }
}