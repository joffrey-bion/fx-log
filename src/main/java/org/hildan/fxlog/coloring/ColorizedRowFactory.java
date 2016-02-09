package org.hildan.fxlog.coloring;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.*;
import javafx.util.Callback;

import org.hildan.fxlog.core.LogEntry;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link TableRow} factory linked to a colorizer. The created rows are styled using the current colorizer. The
 * colorizer should be set by binding the {@link #colorizerProperty()}.
 */
public class ColorizedRowFactory implements Callback<TableView<LogEntry>, TableRow<LogEntry>> {

    private final Property<Colorizer> colorizer;

    /**
     * Creates a ColorizedRowFactory with a null colorizer. The colorizer should be set by binding the {@link
     * #colorizerProperty()}.
     */
    public ColorizedRowFactory() {
        this.colorizer = new SimpleObjectProperty<>();
    }

    /**
     * The current colorizer used by this factory.
     *
     * @return the current colorizer used by this factory.
     */
    @NotNull
    public Property<Colorizer> colorizerProperty() {
        return colorizer;
    }

    @Override
    public TableRow<LogEntry> call(TableView<LogEntry> param) {
        final TableRow<LogEntry> row = new TableRow<LogEntry>() {
            @Override
            protected void updateItem(LogEntry log, boolean empty) {
                super.updateItem(log, empty);
                colorize(this);
            }
        };
        // default style when selected
        row.selectedProperty().addListener((observable, oldValue, newValue) -> {
            colorize(row);
        });
        return row;
    }

    /**
     * Applies or removes the current colorizer's style to the given row. If the row is selected or empty, the style is
     * removed. Otherwise, the colorizer is used to style the row.
     *
     * @param row
     *         the TableRow to colorize
     */
    private void colorize(@NotNull TableRow<LogEntry> row) {
        LogEntry log = row.getItem();
        if (log == null || row.isEmpty() || row.isSelected()) {
            row.setStyle(null);
        } else if (colorizer.getValue() != null) {
            colorizer.getValue().applyTo(row, row.getItem());
        }
    }
}
