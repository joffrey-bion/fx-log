package org.hildan.fxlog.view;

import javafx.beans.binding.Binding;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.*;

import org.fxmisc.easybind.EasyBind;
import org.hildan.fxlog.coloring.Colorizer;
import org.hildan.fxlog.coloring.Style;
import org.hildan.fxlog.data.LogEntry;
import org.hildan.fx.bindings.rulesets.RuleSet;
import org.hildan.fxlog.search.Search;
import org.hildan.fxlog.view.components.SearchableLabel;

/**
 * A table cell that can be styled using a {@link Colorizer}.
 */
public class StyledTableCell extends TableCell<LogEntry, String> {

    private static final String STYLE_BINDING_KEY = "styleBinding";

    private final SearchableLabel text;

    private final Property<Colorizer> colorizer = new SimpleObjectProperty<>();

    public StyledTableCell(TableColumn<LogEntry, String> column, Search search) {
        text = new SearchableLabel(search);
        text.fontProperty().bind(fontProperty());

        setGraphic(text);
        setText(null);

        // this is usually called only once (when this cell is attached to a row)
        EasyBind.subscribe(tableRowProperty(), row -> {
            if (row == null) {
                return;
            }

            // bind the text for the foreground
            //noinspection unchecked
            Binding<Style> colorizedLogStyle = getOrCreateStyleBinding(row, colorizer);
            text.normalStyleProperty().bind(colorizedLogStyle);

            // apply the style to the cell for the background
            EasyBind.subscribe(colorizedLogStyle, s -> s.bindNode(this));
        });
    }

    private static Binding<Style> getOrCreateStyleBinding(TableRow<LogEntry> row,
                                                          ObservableValue<Colorizer> colorizer) {
        @SuppressWarnings("unchecked")
        Binding<Style> colorizedLogStyle = (Binding<Style>) row.getProperties().get(STYLE_BINDING_KEY);
        if (colorizedLogStyle == null) {
            ObservableValue<LogEntry> observableLogValue = row.itemProperty();
            colorizedLogStyle = RuleSet.outputFor(colorizer, observableLogValue, Style.DEFAULT);
            row.getProperties().put(STYLE_BINDING_KEY, colorizedLogStyle);
        }
        return colorizedLogStyle;
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

    public Style getSearchHighlightStyle() {
        return text.getSearchMatchStyle();
    }

    public Property<Style> searchHighlightStyleProperty() {
        return text.searchMatchStyleProperty();
    }

    public void setSearchHighlightStyle(Style searchHighlightStyle) {
        this.text.setSearchMatchStyle(searchHighlightStyle);
    }
}
