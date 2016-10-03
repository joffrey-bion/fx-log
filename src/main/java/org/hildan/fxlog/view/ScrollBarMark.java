package org.hildan.fxlog.view;

import java.util.concurrent.Callable;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * Represents a colored mark on a {@link ScrollBar}'s track.
 */
public class ScrollBarMark extends Rectangle {

    public enum Alignment {
        START(0), CENTER(-0.5), END(-1);

        private final double thicknessFactor;

        Alignment(double thicknessFactor) {
            this.thicknessFactor = thicknessFactor;
        }

        public double computeOffset(double thickness) {
            return thickness * thicknessFactor;
        }
    }

    private final DoubleProperty position = new SimpleDoubleProperty(0.5);

    private final DoubleProperty thickness = new SimpleDoubleProperty(2);

    private final Property<Alignment> alignment = new SimpleObjectProperty<>(Alignment.CENTER);

    public ScrollBarMark() {
        setManaged(false);
        setFill(Color.web("#cc8800", 0.5));
    }

    public void attach(ScrollBar scrollBar) {
        StackPane track = (StackPane) scrollBar.lookup(".track");
        bindSizeTo(track, scrollBar);
        track.getChildren().add(this);
        bindPosition(track, scrollBar);
    }

    public void detach() {
        StackPane parent = (StackPane) getParent();
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

        return Bindings.createDoubleBinding(getX, position, alignment, thickness, track.layoutBoundsProperty(),
                scrollBar.minProperty(), scrollBar.maxProperty());
    }

    private DoubleBinding createYPositionBinding(StackPane track, ScrollBar scrollBar) {
        Callable<Double> getY = () -> {
            double height = track.getLayoutBounds().getHeight();
            return scalePosition(position.get(), height, scrollBar);
        };

        return Bindings.createDoubleBinding(getY, position, alignment, thickness, track.layoutBoundsProperty(),
                scrollBar.minProperty(), scrollBar.maxProperty());
    }

    private double scalePosition(double position, double total, ScrollBar scrollBar) {
        double max = scrollBar.getMax();
        double min = scrollBar.getMin();
        double delta = max - min;
        double scaledPosition = total * (position * delta - min) / delta;
        double offset = alignment.getValue().computeOffset(thickness.get());
        return scaledPosition + offset;
    }

    public double getPosition() {
        return this.position.get();
    }

    /**
     * The position of this mark on the {@link ScrollBar}. This is a double between 0 and 1.
     *
     * @return the position of this mark on the track of the scroll bar; 0 meaning at the start, 1 at the end.
     */
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

    public Alignment getAlignment() {
        return alignment.getValue();
    }

    public Property<Alignment> alignmentProperty() {
        return alignment;
    }

    public void setAlignment(Alignment alignment) {
        this.alignment.setValue(alignment);
    }
}
