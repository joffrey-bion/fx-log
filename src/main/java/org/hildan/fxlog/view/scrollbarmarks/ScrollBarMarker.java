package org.hildan.fxlog.view.scrollbarmarks;

import java.util.HashMap;
import java.util.Map;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener.Change;
import javafx.geometry.Orientation;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import org.hildan.fxlog.view.UIUtils;

public class ScrollBarMarker {

    private final Map<Integer, ScrollBarMark> activeMarks = new HashMap<>();

    private final Property<Paint> color = new SimpleObjectProperty<>(Color.ORANGE);

    private final DoubleProperty thickness = new SimpleDoubleProperty(2);

    private final Property<Alignment> alignment = new SimpleObjectProperty<>(Alignment.CENTER);

    private final TableView tableView;

    private ScrollBar scrollBar;

    /**
     * Creates a marking model for the given table view.
     *
     * @param tableView
     *         the {@link TableView} to mark the {@link ScrollBar} of
     * @param orientation
     *         the orientation of the {@link ScrollBar} to mark
     */
    public ScrollBarMarker(TableView tableView, Orientation orientation) {
        this.tableView = tableView;

        // get the scrollbar when ready
        tableView.getChildrenUnmodifiable().addListener((Change<? extends Node> c) -> {
            while (c.next() && scrollBar == null) {
                this.scrollBar = findScrollBar(tableView, orientation);
            }
        });
    }

    private static ScrollBar findScrollBar(TableView tableView, Orientation orientation) {
        return tableView.lookupAll(".scroll-bar")
                        .stream()
                        .filter(n -> n instanceof ScrollBar)
                        .map(n -> (ScrollBar) n)
                        .filter(sb -> sb.getOrientation() == orientation)
                        .findFirst()
                        .orElse(null);
    }

    private ScrollBarMark createMark() {
        ScrollBarMark mark = new ScrollBarMark();
        mark.fillProperty().bind(color);
        mark.thicknessProperty().bind(thickness);
        mark.alignmentProperty().bind(alignment);
        return mark;
    }

    private DoubleBinding positionBinding(int index) {
        return Bindings.createDoubleBinding(() -> {
            int max = tableView.getItems().size();
            return (double) index / (double) max;
        }, tableView.getItems());
    }

    public ScrollBarMark mark(int index) {
        if (scrollBar == null) {
            throw new IllegalStateException("Trying to mark a ScrollBar that does not exist yet");
        }
        ScrollBarMark mark = createMark();
        mark.positionProperty().bind(positionBinding(index));
        mark.setOnMouseClicked(e -> UIUtils.scrollTo(tableView, index));
        mark.setCursor(Cursor.HAND);
        mark.attach(scrollBar);
        activeMarks.put(index, mark);
        return mark;
    }

    /**
     * Unmarks the given position. If the given position was not marked, this method does nothing.
     *
     * @param index
     *         the index to unmark
     */
    public void unmark(int index) {
        ScrollBarMark mark = activeMarks.remove(index);
        if (mark != null) {
            mark.detach();
        }
    }

    public void clear() {
        activeMarks.forEach((i, m) -> m.detach());
        activeMarks.clear();
    }

    @SuppressWarnings("unused")
    public Paint getColor() {
        return color.getValue();
    }

    public Property<Paint> colorProperty() {
        return color;
    }

    @SuppressWarnings("unused")
    public void setColor(Paint color) {
        this.color.setValue(color);
    }

    @SuppressWarnings("unused")
    public double getThickness() {
        return thickness.get();
    }

    public DoubleProperty thicknessProperty() {
        return thickness;
    }

    @SuppressWarnings("unused")
    public void setThickness(double thickness) {
        this.thickness.set(thickness);
    }

    @SuppressWarnings("unused")
    public Alignment getAlignment() {
        return alignment.getValue();
    }

    public Property<Alignment> alignmentProperty() {
        return alignment;
    }

    @SuppressWarnings("unused")
    public void setAlignment(Alignment alignment) {
        this.alignment.setValue(alignment);
    }
}
