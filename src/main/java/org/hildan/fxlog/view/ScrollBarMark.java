package org.hildan.fxlog.view;

import java.util.concurrent.Callable;

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
        bindPosition(track, scrollBar);
    }

    public void detach() {
        StackPane parent = (StackPane)getParent();
        if (parent != null) {
            parent.getChildren().remove(this);
            widthProperty().unbind();
            heightProperty().unbind();
            layoutXProperty().unbind();
            layoutYProperty().unbind();
        }
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

    private void bindPosition(StackPane track, ScrollBar scrollBar) {
        Callable<Double> getX = () -> {
            if (scrollBar.getOrientation() == Orientation.VERTICAL) {
                return 0.0;
            }
            double width = track.getLayoutBounds().getWidth();
            return scalePosition(position.get(), width, scrollBar);
        };

        Callable<Double> getY = () -> {
            if (scrollBar.getOrientation() == Orientation.HORIZONTAL) {
                return 0.0;
            }
            double height = track.getLayoutBounds().getHeight();
            return scalePosition(position.get(), height, scrollBar);
        };

        DoubleBinding xPosition = Bindings.createDoubleBinding(getX, position, track.layoutBoundsProperty(),
                scrollBar.visibleAmountProperty(), scrollBar.minProperty(), scrollBar.maxProperty(),
                scrollBar.orientationProperty());
        DoubleBinding yPosition = Bindings.createDoubleBinding(getY, position, track.layoutBoundsProperty(),
                scrollBar.visibleAmountProperty(), scrollBar.minProperty(), scrollBar.maxProperty(),
                scrollBar.orientationProperty());

        layoutYProperty().bind(yPosition);
        layoutXProperty().bind(xPosition);
    }

    private static double scalePosition(double position, double total, ScrollBar scrollBar) {
        double visibleAmout = scrollBar.getVisibleAmount();
        double max = scrollBar.getMax();
        double min = scrollBar.getMin();
        double delta = max - min;

        total *= 1 - visibleAmout / delta;

        return total * (position - min) / delta;
    }

    public double getPosition() {
        return this.position.get();
    }

    public DoubleProperty positionProperty() {
        return this.position;
    }

    public void setPosition(double value) {
        this.position.set(value);
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
