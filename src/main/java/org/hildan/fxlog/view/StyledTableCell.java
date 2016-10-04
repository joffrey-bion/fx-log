package org.hildan.fxlog.view;

import javafx.beans.binding.Binding;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableDoubleValue;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.*;

import org.fxmisc.easybind.EasyBind;
import org.hildan.fxlog.coloring.Colorizer;
import org.hildan.fxlog.coloring.Style;
import org.hildan.fxlog.data.LogEntry;
import org.hildan.fx.bindings.rulesets.RuleSet;

/**
 * A table cell that can be styled using a {@link Colorizer}.
 */
public class StyledTableCell extends TableCell<LogEntry, String> {

    private static final String STYLE_BINDING_KEY = "styleBinding";

    private final SearchableText text = new SearchableText();

    private final Property<Colorizer> colorizer = new SimpleObjectProperty<>();

    private final Property<Style> searchHighlightStyle = new SimpleObjectProperty<>(Style.HIGHLIGHT_SEARCH);

    public StyledTableCell(TableColumn<LogEntry, String> column) {
        setPrefHeight(USE_COMPUTED_SIZE);
        setMaxHeight(USE_COMPUTED_SIZE);

        text.setPrefHeight(80);
        text.setMaxHeight(USE_COMPUTED_SIZE);
        text.setPrefWidth(80);
        text.setMaxWidth(Double.MAX_VALUE);
//        text.prefWidthProperty().bind(wrappingWidthBinding(column.widthProperty()));
//        text.maxWidthProperty().bind(wrappingWidthBinding(column.widthProperty()));
        text.fontProperty().bind(fontProperty());

        setGraphic(text);
        setText(null);

        // this is usually called only once (when this cell is attached to a row)
        EasyBind.subscribe(tableRowProperty(), row -> {
            if (row == null) {
                return;
            }
            row.setPrefHeight(USE_COMPUTED_SIZE);
            row.setMaxHeight(USE_COMPUTED_SIZE);

            // bind the text for the foreground, and this cell for the background
            //noinspection unchecked
            Binding<Style> colorizerStyle = getOrCreateStyleBinding(row, colorizer);
            text.normalStyleProperty().bind(colorizerStyle);
            text.highlightedStyleProperty().bind(searchHighlightStyle);
        });
    }

    private static Binding<Style> getOrCreateStyleBinding(TableRow<LogEntry> row, ObservableValue<Colorizer> colorizer) {
        @SuppressWarnings("unchecked")
        Binding<Style> colorizerStyleBinding = (Binding<Style>) row.getProperties().get(STYLE_BINDING_KEY);
        if (colorizerStyleBinding == null) {
            ObservableValue<LogEntry> observableLogValue = row.itemProperty();
            colorizerStyleBinding = RuleSet.outputFor(colorizer, observableLogValue, Style.DEFAULT);
            row.getProperties().put(STYLE_BINDING_KEY, colorizerStyleBinding);
            // set the whole row background
            EasyBind.subscribe(colorizerStyleBinding, style -> style.bindNode(row));
        }
        return colorizerStyleBinding;
    }

    private DoubleBinding wrappingWidthBinding(ObservableDoubleValue columnWidth) {
        return Bindings.createDoubleBinding(() -> isWrapText() ? columnWidth.get() : USE_COMPUTED_SIZE,
                wrapTextProperty(), columnWidth);
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

    public String getSearchText() {
        return text.getSearchText();
    }

    public StringProperty searchTextProperty() {
        return text.searchTextProperty();
    }

    public void setSearchText(String searchText) {
        this.text.setSearchText(searchText);
    }

    public Style getSearchHighlightStyle() {
        return text.getHighlightedStyle();
    }

    public Property<Style> searchHighlightStyleProperty() {
        return text.highlightedStyleProperty();
    }

    public void setSearchHighlightStyle(Style searchHighlightStyle) {
        this.text.setHighlightedStyle(searchHighlightStyle);
    }
}
