package org.hildan.fxlog.view;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableDoubleValue;
import javafx.scene.control.*;
import javafx.scene.text.Text;

import org.fxmisc.easybind.EasyBind;
import org.hildan.fxlog.coloring.Colorizer;
import org.hildan.fxlog.core.LogEntry;

/**
 * A table cell that can be styled using a {@link Colorizer}.
 */
public class StyledTableCell extends TableCell<LogEntry, String> {

    private final Text text = new Text();

    private final Property<Colorizer> colorizer = new SimpleObjectProperty<>();

    public StyledTableCell(TableColumn<LogEntry, String> column) {
        text.wrappingWidthProperty().bind(wrappingWidthBinding(column.widthProperty()));
        text.fontProperty().bind(fontProperty());

        setGraphic(text);
        setText(null);

        EasyBind.subscribe(tableRowProperty(), row -> {
            if (row == null) {
                return;
            }
            //noinspection unchecked
            Colorizer.bindStyle(text, colorizerProperty(), row.itemProperty()); // for foreground style
            //noinspection unchecked
            Colorizer.bindStyle(this, colorizerProperty(), row.itemProperty()); // for background style
        });
    }

    private DoubleBinding wrappingWidthBinding(ObservableDoubleValue columnWidth) {
        return Bindings.createDoubleBinding(() -> isWrapText() ? columnWidth.get() : 0d, wrapTextProperty(),
                columnWidth);
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
