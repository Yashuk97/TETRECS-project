package uk.ac.soton.comp1206.component;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
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
    scores.addListener((ListChangeListener<Pair<String, Integer>>) c -> buildList());
  }

  /**
   * Clears and rebuilds the visual list of scores based on the current data.
   */
  private void buildList() {
    getChildren().clear();
    int counter = 1;

    if (scores.get() == null) return; // Safety check

    for (Pair<String, Integer> scoreEntry : scores.get()) {
      Text scoreText = new Text(counter + ". " + scoreEntry.getKey() + ": " + scoreEntry.getValue());
      scoreText.getStyleClass().add("scoreitem");
      getChildren().add(scoreText);
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