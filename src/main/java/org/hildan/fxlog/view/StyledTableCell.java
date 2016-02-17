package org.hildan.fxlog.view;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.*;
import javafx.scene.text.Text;

import org.hildan.fxlog.coloring.Colorizer;
import org.hildan.fxlog.config.Config;
import org.hildan.fxlog.core.LogEntry;

/**
 * A table cell that can be styled.
 */
public class StyledTableCell extends TableCell<LogEntry, String> {

    private final Text text = new Text();

    private final Property<Colorizer> colorizer = new SimpleObjectProperty<>();

    public StyledTableCell(TableColumn<LogEntry, String> column) {
        text.wrappingWidthProperty().bind(column.widthProperty());
        text.fontProperty().bind(Config.getInstance().logsFontProperty());
        setGraphic(text);
        setText(null);
    }

    @Override
    public void updateItem(String item, boolean empty) {
        if (item == getItem()) {
            return;
        }
        super.updateItem(item, empty);
        if (item == null) {
            setGraphic(null);
            text.setText(null);
            return;
        }
        setGraphic(text);
        text.setText(item);
        if (colorizer.getValue() != null) {
            TableRow row = getTableRow();
            if (row != null && row.getItem() != null) {
                LogEntry log = (LogEntry) row.getItem();
                colorizer.getValue().applyTo(text, log);
            }
        }
    }

    public Colorizer getColorizer() {
        return colorizer.getValue();
    }

    public Property<Colorizer> colorizerProperty() {
        return colorizer;
    }

    public void setColorizer(Colorizer colorizer) {
        this.colorizer.setValue(colorizer);
    }
}
