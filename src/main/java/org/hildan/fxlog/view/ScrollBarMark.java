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
        if (scrollBar.getOrientation() == Orientation.VERTICAL) {
            widthProperty().bind(track.widthProperty());
            heightProperty().bind(thickness);
        } else {
            widthProperty().bind(thickness);
            heightProperty().bind(track.widthProperty());
        }
    }

    private void bindPosition(StackPane track, ScrollBar scrollBar) {
        if (scrollBar.getOrientation() == Orientation.VERTICAL) {
            layoutYProperty().bind(createYPositionBinding(track, scrollBar));
        } else {
            layoutXProperty().bind(createXPositionBinding(track, scrollBar));
        }
    }

    private DoubleBinding createXPositionBinding(StackPane track, ScrollBar scrollBar) {
        Callable<Double> getX = () -> {
            double width = track.getLayoutBounds().getWidth();
            return scalePosition(position.get(), width, scrollBar);
        };

        return Bindings.createDoubleBinding(getX, position, track.layoutBoundsProperty(), scrollBar.minProperty(),
                scrollBar.maxProperty());
    }

    private DoubleBinding createYPositionBinding(StackPane track, ScrollBar scrollBar) {
        Callable<Double> getY = () -> {
            double height = track.getLayoutBounds().getHeight();
            return scalePosition(position.get(), height, scrollBar);
        };

        return Bindings.createDoubleBinding(getY, position, track.layoutBoundsProperty(), scrollBar.minProperty(),
                scrollBar.maxProperty());
    }

    private static double scalePosition(double position, double total, ScrollBar scrollBar) {
        double max = scrollBar.getMax();
        double min = scrollBar.getMin();
        double delta = max - min;
        return total * (position * delta - min) / delta;
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
