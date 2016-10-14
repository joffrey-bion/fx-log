package org.hildan.fxlog.view.components;

import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;

import org.fxmisc.easybind.EasyBind;
import org.fxmisc.easybind.Subscription;
import org.hildan.fxlog.coloring.Style;
import org.hildan.fxlog.search.Search;

public class SearchableLabel extends HBox {

    private static final String WITH_DELIMITER = "((?<=%1$s)|(?=%1$s))";

    private final StringProperty text = new SimpleStringProperty();

    private final ObjectProperty<Style> normalStyle = new SimpleObjectProperty<>(Style.DEFAULT);

    private final ObjectProperty<Style> searchMatchStyle = new SimpleObjectProperty<>(Style.HIGHLIGHT_SEARCH);

    private final ObjectProperty<Style> selectedSearchMatchStyle = new SimpleObjectProperty<>(Style.HIGHLIGHT_SEARCH);

    private final ObjectProperty<Font> font = new SimpleObjectProperty<>(Font.getDefault());

    private final Search search;

    private final Label initialText;

    public SearchableLabel(Search search) {
        this.search = search;

        // reuse the same initial text when not searching
        initialText = new Label();
        initialText.textProperty().bind(text);
        initialText.fontProperty().bind(font);
        initialText.setMinWidth(USE_COMPUTED_SIZE);

        EasyBind.subscribe(normalStyle, style -> style.bindNodes(initialText));

        text.addListener((obs, old, val) -> refreshSearch());
        search.textProperty().addListener((obs, old, val) -> refreshSearch());
        search.activeProperty().addListener((obs, old, val) -> refreshSearch());
        search.matchCaseProperty().addListener((obs, old, val) -> refreshSearch());
        search.regexModeProperty().addListener((obs, old, val) -> refreshSearch());

        // initialize the content
        refreshSearch();
    }

    private void refreshSearch() {
        ObservableList<Node> children = getChildren();
        String currentText = text.get();
        String searchText = search.getText();
        if (currentText == null || currentText.isEmpty() || searchText == null || searchText.isEmpty()
                || !search.isActive()) {
            children.setAll(initialText);
            return;
        }
        // we don't want to update the initial text label
        if (children.get(0) == initialText) {
            children.clear();
        }
        // separate the parts of the text that match the search and the others
        String[] parts = splitButKeepDelimiter(currentText, Pattern.quote(searchText));
        if (parts.length > children.size()) {
            updateAndCreateMore(parts);
        } else {
            updateAndRemoveExtra(parts);
        }
    }

    private boolean matchesSearch(String part) {
        return part.equals(search.getText());
    }

    private void updateAndCreateMore(String[] parts) {
        List<Node> children = getChildren();

        // update existing labels
        updateChildren(parts, children.size());

        // create extra labels
        for (int i = children.size(); i < parts.length; i++) {
            Node partNode = createLabel(parts[i]);
            children.add(partNode);
        }
    }

    private void updateAndRemoveExtra(String[] parts) {
        List<Node> children = getChildren();

        // update existing labels
        updateChildren(parts, parts.length);

        // remove extra labels
        for (int i = parts.length; i < children.size(); i++) {
            children.remove(i);
        }
    }

    private void updateChildren(String[] parts, int upperBoundExclusive) {
        for (int i = 0; i < upperBoundExclusive; i++) {
            Label label = (Label) getChildren().get(i);
            label.setText(parts[i]);
            bindStyle(label, matchesSearch(parts[i]));
        }
    }

    private Label createLabel(String str) {
        Label label = new Label(str);
        label.fontProperty().bind(font);

        // prevents the different parts from collapsing in favor of others
        label.setMinWidth(USE_PREF_SIZE);

        Consumer<Style> styleChangeListener = style -> style.bindNodes(label);
        label.getProperties().put("styleListener", styleChangeListener);

        bindStyle(label, matchesSearch(str));

        return label;
    }

    private void bindStyle(Label label, boolean matchesSearch) {
        @SuppressWarnings("unchecked")
        Consumer<Style> styleListener = (Consumer<Style>)label.getProperties().get("styleListener");

        // remove previous style subscription
        Subscription subscription = (Subscription)label.getProperties().get("subscription");
        if (subscription != null) {
            subscription.unsubscribe();
        }

        // subscribe to new style
        subscription = EasyBind.subscribe(matchesSearch ? searchMatchStyle : normalStyle, styleListener);
        label.getProperties().put("subscription", subscription);
    }

    private static String[] splitButKeepDelimiter(String text, String delimiterRegex) {
        String splitRegex = String.format(WITH_DELIMITER, delimiterRegex);
        return text.split(splitRegex);
    }

    public String getText() {
        return text.get();
    }

    public StringProperty textProperty() {
        return text;
    }

    public void setText(String text) {
        this.text.set(text);
    }

    public Style getNormalStyle() {
        return normalStyle.get();
    }

    public Property<Style> normalStyleProperty() {
        return normalStyle;
    }

    public void setNormalStyle(Style normalStyle) {
        this.normalStyle.set(normalStyle);
    }

    public Style getSearchMatchStyle() {
        return searchMatchStyle.get();
    }

    public Property<Style> searchMatchStyleProperty() {
        return searchMatchStyle;
    }

    public void setSearchMatchStyle(Style searchMatchStyle) {
        this.searchMatchStyle.set(searchMatchStyle);
    }

    public Style getSelectedSearchMatchStyle() {
        return selectedSearchMatchStyle.get();
    }

    public Property<Style> selectedSearchMatchStyleProperty() {
        return selectedSearchMatchStyle;
    }

    public void setSelectedSearchMatchStyle(Style selectedSearchMatchStyle) {
        this.selectedSearchMatchStyle.set(selectedSearchMatchStyle);
    }

    public Font getFont() {
        return font.get();
    }

    public Property<Font> fontProperty() {
        return font;
    }

    public void setFont(Font font) {
        this.font.set(font);
    }
}
