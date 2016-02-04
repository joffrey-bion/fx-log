package org.hildan.fxlog.coloring;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.util.Callback;

import org.hildan.fxlog.core.LogEntry;

public class ColorizedRowFactory implements Callback<TableView<LogEntry>, TableRow<LogEntry>> {

    private final Property<Colorizer> colorizer;

    public ColorizedRowFactory() {
        this.colorizer = new SimpleObjectProperty<>();
    }

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

    private void colorize(TableRow<LogEntry> row) {
        LogEntry log = row.getItem();
        if (log == null || row.isEmpty() || row.isSelected()) {
            row.setStyle(null);
        } else {
            colorizer.getValue().setStyle(row, row.getItem());
        }
    }
}
