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
        setWrap(column, Config.getInstance().getWrapLogsText());
        Config.getInstance().wrapLogsTextProperty().addListener((obs, oldVal, newVal) -> setWrap(column, newVal));
        text.fontProperty().bind(Config.getInstance().logsFontProperty());
        setGraphic(text);
        setText(null);
    }

    private void setWrap(TableColumn<LogEntry, String> column, boolean wrap) {
        if (wrap) {
            text.wrappingWidthProperty().bind(column.widthProperty());
        } else {
            text.wrappingWidthProperty().unbind();
            text.setWrappingWidth(0); // disables wrapping
        }
    }

    @Override
    public void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setGraphic(null);
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
