package org.hildan.fxlog.view;

import java.util.function.Predicate;

import javafx.beans.binding.Binding;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableDoubleValue;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.text.Text;

import org.fxmisc.easybind.EasyBind;
import org.hildan.fxlog.coloring.Colorizer;
import org.hildan.fxlog.coloring.Style;
import org.hildan.fxlog.data.LogEntry;
import org.hildan.fx.bindings.rulesets.RuleSet;

/**
 * A table cell that can be styled using a {@link Colorizer}.
 */
public class StyledTableCell extends TableCell<LogEntry, String> {

    private static final String STYLE_BINDING_PROPERTY = "styleBinding";

    private final Text text = new Text();

    private final Property<Colorizer> colorizer = new SimpleObjectProperty<>();

    private final Property<Style> searchHighlightStyle = new SimpleObjectProperty<>(Style.HIGHLIGHT_SEARCH);

    private final Property<Predicate<String>> searchMatcher = new SimpleObjectProperty<>();

    private final BooleanBinding matchesSearch;

    private Binding<Style> rowStyleBinding;

    // this is to prevent the binding from being garbage collected
    @SuppressWarnings("FieldCanBeLocal")
    private Binding<Style> cellStyleBinding;

    public StyledTableCell(TableColumn<LogEntry, String> column) {
        text.wrappingWidthProperty().bind(wrappingWidthBinding(column.widthProperty()));
        text.fontProperty().bind(fontProperty());
        setGraphic(text);
        setText(null);

        matchesSearch = Bindings.createBooleanBinding(this::computeSearchMatch, itemProperty(), searchMatcher);

        // this is usually called only once (when this cell is attached to a row)
        EasyBind.subscribe(tableRowProperty(), row -> {
            if (row == null) {
                return;
            }
            //noinspection unchecked
            rowStyleBinding = getOrCreateRowStyleBinding((TableRow<LogEntry>) row, colorizer);

            // bind the text for the foreground, and this cell for the background
            //noinspection unchecked
            bindStyle(text, this);
        });
    }

    private boolean computeSearchMatch() {
        String cellText = getItem();
        return cellText != null && searchMatcher.getValue().test(cellText);
    }

    private static Binding<Style> getOrCreateRowStyleBinding(TableRow<LogEntry> row,
                                                             ObservableValue<Colorizer> colorizer) {
        //noinspection unchecked
        Binding<Style> rowStyleBinding = (Binding<Style>) row.getProperties().get(STYLE_BINDING_PROPERTY);
        if (rowStyleBinding == null) {
            rowStyleBinding = RuleSet.outputFor(colorizer, row.itemProperty(), Style.DEFAULT);
            row.getProperties().put(STYLE_BINDING_PROPERTY, rowStyleBinding);
        }
        return rowStyleBinding;
    }

    /**
     * Binds the style of the given Nodes to match either the search highlight or the default row style for the current
     * colorizer.
     *
     * @param nodes
     *         the nodes to style
     */
    private void bindStyle(Node... nodes) {
        // cannot be a local variable or it will be garbage collected
        cellStyleBinding = Bindings.createObjectBinding(this::computeCellStyle, matchesSearch, rowStyleBinding,
                searchHighlightStyle);
        EasyBind.subscribe(cellStyleBinding, style -> style.bindNodes(nodes));
    }

    private Style computeCellStyle() {
        if (matchesSearch.get()) {
            return searchHighlightStyle.getValue();
        } else {
            return rowStyleBinding.getValue();
        }
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

    @SuppressWarnings("unused")
    public Colorizer getColorizer() {
        return colorizer.getValue();
    }

    public Property<Colorizer> colorizerProperty() {
        return colorizer;
    }

    @SuppressWarnings("unused")
    public void setColorizer(Colorizer colorizer) {
        this.colorizer.setValue(colorizer);
    }

    public Predicate<String> getSearchMatcher() {
        return searchMatcher.getValue();
    }

    public Property<Predicate<String>> searchMatcherProperty() {
        return searchMatcher;
    }

    public void setSearchMatcher(Predicate<String> searchMatcher) {
        this.searchMatcher.setValue(searchMatcher);
    }

    @SuppressWarnings("unused")
    public Style getSearchHighlightStyle() {
        return searchHighlightStyle.getValue();
    }

    public Property<Style> searchHighlightStyleProperty() {
        return searchHighlightStyle;
    }

    @SuppressWarnings("unused")
    public void setSearchHighlightStyle(Style searchHighlightStyle) {
        this.searchHighlightStyle.setValue(searchHighlightStyle);
    }
}
