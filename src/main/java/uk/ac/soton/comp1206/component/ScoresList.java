package uk.ac.soton.comp1206.component;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Pair;

public class ScoresList extends VBox {

  private final ListProperty<Pair<String, Integer>> scores = new SimpleListProperty<>();

  public ScoresList() {
    setAlignment(Pos.CENTER);
    setSpacing(5);
    getStyleClass().add("scorelist");

    // Add a listener to rebuild the list when the scores property changes
    scores.addListener((ListChangeListener<Pair<String, Integer>>) c -> build());
  }

  public void build() {
    getChildren().clear();
    int counter = 1;
    for (Pair<String, Integer> scoreEntry : scores.get()) {
      Text scoreText = new Text(counter + ". " + scoreEntry.getKey() + ": " + scoreEntry.getValue());
      scoreText.getStyleClass().add("scoreitem");
      getChildren().add(scoreText);
      counter++;
    }
  }

  public ListProperty<Pair<String, Integer>> scoresProperty() {
    return scores;
  }

  public void setScores(ObservableList<Pair<String, Integer>> scores) {
    this.scores.set(scores);
  }
}