package org.example.sportconnect.components;

import javafx.animation.TranslateTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class ToggleSwitch extends StackPane {

    private final BooleanProperty selectedProperty = new SimpleBooleanProperty(false);

    private final Rectangle track;
    private final Circle thumb;
    private final TranslateTransition transition;

    private static final double WIDTH  = 46;
    private static final double HEIGHT = 24;
    private static final double RADIUS = 10;
    private static final double TRAVEL = WIDTH - HEIGHT;

    public ToggleSwitch() {
        setPrefSize(WIDTH, HEIGHT);
        setMaxSize(WIDTH, HEIGHT);
        setAlignment(Pos.CENTER_LEFT);

        track = new Rectangle(WIDTH, HEIGHT);
        track.setArcWidth(HEIGHT);
        track.setArcHeight(HEIGHT);
        track.setFill(Color.web("#334155"));

        thumb = new Circle(RADIUS);
        thumb.setFill(Color.WHITE);
        thumb.setTranslateX(-(TRAVEL / 2));
        thumb.setEffect(new DropShadow(4, 0, 1, Color.rgb(0, 0, 0, 0.3)));

        transition = new TranslateTransition(Duration.millis(180), thumb);

        getChildren().addAll(track, thumb);
        setCursor(Cursor.HAND);

        setOnMouseClicked(e -> setSelected(!isSelected()));

        selectedProperty.addListener((obs, oldVal, newVal) -> animateTo(newVal));
    }

    private void animateTo(boolean on) {
        transition.stop();
        transition.setToX(on ? (TRAVEL / 2) : -(TRAVEL / 2));
        transition.play();
        track.setFill(on ? Color.web("#2563eb") : Color.web("#334155"));
    }

    public boolean isSelected() { return selectedProperty.get(); }
    public void setSelected(boolean value) { selectedProperty.set(value); }
    public BooleanProperty selectedProperty() { return selectedProperty; }
}