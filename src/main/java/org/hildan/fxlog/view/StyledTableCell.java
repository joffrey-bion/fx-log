package org.hildan.fxlog.view;

import org.fxmisc.easybind.EasyBind;
import org.hildan.fxlog.coloring.Colorizer;
import org.hildan.fxlog.coloring.Style;
import org.hildan.fxlog.core.LogEntry;
import org.hildan.fxlog.rulesets.RuleSet;

import javafx.beans.binding.Binding;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableDoubleValue;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.text.Text;

/**
 * A table cell that can be styled using a {@link Colorizer}.
 */
public class StyledTableCell extends TableCell<LogEntry, String> {

    private final Text text = new Text();

    private final Property<Colorizer> colorizer = new SimpleObjectProperty<>();

    private final Property<Style> searchHighlightStyle = new SimpleObjectProperty<>(Style.HIGHLIGHT_SEARCH);

    private final StringProperty searchText = new SimpleStringProperty("");

    private final BooleanBinding matchesSearch;

    private Binding<Style> colorizerStyle;

    // this is to prevent the binding from being garbage collected
    @SuppressWarnings("FieldCanBeLocal")
    private Binding<Style> cellStyleBinding;

    public StyledTableCell(TableColumn<LogEntry, String> column) {
        text.wrappingWidthProperty().bind(wrappingWidthBinding(column.widthProperty()));
        text.fontProperty().bind(fontProperty());
        setGraphic(text);
        setText(null);

        matchesSearch = Bindings.createBooleanBinding(this::computeSearchMatch, itemProperty(), searchText);

        // this is usually called only once (when this cell is attached to a row)
        EasyBind.subscribe(tableRowProperty(), row -> {
            if (row == null) {
                return;
            }

            // bind the text for the foreground, and this cell for the background
            //noinspection unchecked
            bindStyle(row.itemProperty(), text, this);
        });
    }

    private boolean computeSearchMatch() {
        String search = searchText.getValue();
        String cellText = getItem();
        return cellText != null && !search.isEmpty() && cellText.contains(search);
    }

    /**
     * Binds the style of the given Node to the given log observable. If it changes, the rule matching is re-computed to
     * update the style of the node accordingly.
     *
     * @param log
     *         the observable log on which to test the rules
     * @param nodes
     *         the nodes to style
     */
    private void bindStyle(ObservableValue<LogEntry> log, Node... nodes) {
        colorizerStyle = RuleSet.outputFor(colorizer, log, Style.DEFAULT);
        // cannot be a local variable or it will be garbage collected
        cellStyleBinding = Bindings.createObjectBinding(this::computeCellStyle, matchesSearch, colorizerStyle,
                searchHighlightStyle);
        EasyBind.subscribe(cellStyleBinding, style -> style.bindNodes(nodes));
    }

    private Style computeCellStyle() {
        if (matchesSearch.get()) {
            return searchHighlightStyle.getValue();
        } else {
            return colorizerStyle.getValue();
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
        return searchText.get();
    }

    public StringProperty searchTextProperty() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText.set(searchText);
    }

    public Style getSearchHighlightStyle() {
        return searchHighlightStyle.getValue();
    }

    public Property<Style> searchHighlightStyleProperty() {
        return searchHighlightStyle;
    }

    public void setSearchHighlightStyle(Style searchHighlightStyle) {
        this.searchHighlightStyle.setValue(searchHighlightStyle);
    }
}
