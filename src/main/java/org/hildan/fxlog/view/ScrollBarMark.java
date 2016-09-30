package org.hildan.fxlog.view;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Orientation;
import javafx.scene.control.ScrollBar;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class ScrollBarMark extends Rectangle {

    private final DoubleProperty position = new SimpleDoubleProperty(0.5);

    private final DoubleProperty thickness = new SimpleDoubleProperty(2);

    // needs to be stored in a field to avoid garbage collection
    @SuppressWarnings("FieldCanBeLocal")
    private DoubleBinding width;

    // needs to be stored in a field to avoid garbage collection
    @SuppressWarnings("FieldCanBeLocal")
    private DoubleBinding height;

    public ScrollBarMark() {
        setManaged(false);
        setFill(Color.web("#cc8800", 0.5));
    }

    public void attach(ScrollBar scrollBar) {
        StackPane track = (StackPane)scrollBar.lookup(".track");
        bindSizeTo(track, scrollBar);
        track.getChildren().add(this);
        layoutYProperty().bind(bindPositionWithin(track, scrollBar));
    }

    private void bindSizeTo(StackPane track, ScrollBar scrollBar) {
        width = Bindings.createDoubleBinding(() -> {
            if (scrollBar.getOrientation() == Orientation.VERTICAL) {
                return track.getWidth();
            } else {
                return thickness.getValue();
            }
        }, track.widthProperty(), thickness, scrollBar.orientationProperty());

        widthProperty().bind(width);

        height = Bindings.createDoubleBinding(() -> {
            if (scrollBar.getOrientation() == Orientation.HORIZONTAL) {
                return track.getHeight();
            } else {
                return thickness.getValue();
            }
        }, track.heightProperty(), thickness, scrollBar.orientationProperty());

        heightProperty().bind(height);
    }

    private DoubleBinding bindPositionWithin(StackPane track, ScrollBar scrollBar) {
        return Bindings.createDoubleBinding(() -> {
                double height = track.getLayoutBounds().getHeight();
                double visibleAmout = scrollBar.getVisibleAmount();
                double max = scrollBar.getMax();
                double min = scrollBar.getMin();
                double pos = position.get();
                double delta = max - min;

                height *= 1 - visibleAmout / delta;

                return height * (pos - min) / delta;
            }, position, track.layoutBoundsProperty(), scrollBar.visibleAmountProperty(), scrollBar.minProperty(),
            scrollBar.maxProperty());
    }

    public void detach() {
        StackPane parent = (StackPane)getParent();
        if (parent != null) {
            parent.getChildren().remove(this);
            layoutXProperty().unbind();
            layoutYProperty().unbind();
            widthProperty().unbind();
            heightProperty().unbind();
        }
    }

    public double getPosition() {
        return this.position.get();
    }

    public void setPosition(double value) {
        this.position.set(value);
    }

    public DoubleProperty positionProperty() {
        return this.position;
    }

    public double getThickness() {
        return thickness.get();
    }

    public DoubleProperty thicknessProperty() {
        return thickness;
    }

    public void setThickness(double thickness) {
        this.thickness.set(thickness);
    }
}
