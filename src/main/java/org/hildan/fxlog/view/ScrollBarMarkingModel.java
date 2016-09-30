package org.hildan.fxlog.view;

import java.util.HashMap;
import java.util.Map;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Cursor;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TableView;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

public class ScrollBarMarkingModel {

    private final Map<Integer, ScrollBarMark> activeMarks = new HashMap<>();

    private final Property<Paint> color = new SimpleObjectProperty<>(Color.ORANGE);

    private final TableView tableView;

    public ScrollBarMarkingModel(TableView tableView) {
        this.tableView = tableView;
    }

    private ScrollBarMark createMark() {
        ScrollBarMark mark = new ScrollBarMark();
        mark.fillProperty().bind(color);
        mark.setThickness(3);
        return mark;
    }

    private DoubleBinding positionBinding(int index) {
        return Bindings.createDoubleBinding(() -> {
            int max = tableView.getItems().size();
            return (double)index / (double)max;
        }, tableView.getItems());
    }

    public ScrollBarMark mark(int index) {
        ScrollBarMark mark = createMark();
        mark.positionProperty().bind(positionBinding(index));
        mark.setOnMouseClicked(e -> UIUtils.scrollTo(tableView, index));
        mark.setCursor(Cursor.HAND);

        ScrollBar scrollBar = UIUtils.findVerticalScrollbar(tableView);
        mark.attach(scrollBar);
        activeMarks.put(index, mark);
        return mark;
    }

    public void unmark(int index) {
        ScrollBarMark mark = activeMarks.remove(index);
        mark.detach();
    }

    public boolean isMarked(int index) {
        return activeMarks.containsKey(index);
    }

    public void clear() {
        activeMarks.forEach((i, m) -> m.detach());
        activeMarks.clear();
    }

    public Paint getColor() {
        return color.getValue();
    }

    public Property<Paint> colorProperty() {
        return color;
    }

    public void setColor(Paint color) {
        this.color.setValue(color);
    }
}
