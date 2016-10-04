package org.hildan.fxlog.view;

import java.util.regex.Pattern;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import org.fxmisc.easybind.EasyBind;
import org.hildan.fxlog.coloring.Style;

public class SearchableText extends TextFlow {

    private static final String WITH_DELIMITER = "((?<=%1$s)|(?=%1$s))";

    private final StringProperty text = new SimpleStringProperty();

    private final StringProperty searchText = new SimpleStringProperty();

    private final Property<Style> normalStyle = new SimpleObjectProperty<>(Style.DEFAULT);

    private final Property<Style> highlightedStyle = new SimpleObjectProperty<>(Style.HIGHLIGHT_SEARCH);

    private final Property<Font> font = new SimpleObjectProperty<>(Font.getDefault());

    private final Text initialText;
    private final StackPane initialTextPane;

    public SearchableText() {
        initialText = new Text();
        initialText.textProperty().bind(text);
        initialTextPane = wrapAndConfigure(initialText, false);
        getChildren().add(initialText);

        EasyBind.subscribe(text, s -> refreshSearch());
        EasyBind.subscribe(searchText, s -> refreshSearch());
    }

    private StackPane createTextPane(String str, boolean matchesSearch) {
        Text text = new Text(str);
        return wrapAndConfigure(text, matchesSearch);
    }

    private StackPane wrapAndConfigure(Text text, boolean matchesSearch) {
        StackPane pane = new StackPane();
        pane.setMinSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        pane.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        pane.setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);

        text.fontProperty().bind(font);

        if (matchesSearch) {
            EasyBind.subscribe(highlightedStyle, style -> style.bindNodes(text, pane));
        } else {
            EasyBind.subscribe(normalStyle, style -> style.bindNodes(text, pane));
        }

        pane.getChildren().add(text);
        return pane;
    }

    private void refreshSearch() {
        String currentText = text.get();
        String search = searchText.get();
        getChildren().clear();
        if (currentText == null || currentText.isEmpty() || search == null || search.isEmpty()) {
            getChildren().add(initialText);
            return;
        }
        // separate the parts of the text that match the search and the others
        String[] parts = splitButKeepDelimiter(currentText, Pattern.quote(search));
        for (String part : parts) {
            boolean matchesSearch = part.equals(search);
            // each part
            for (String word : splitButKeepDelimiter(part, "\\s")) {
                StackPane wordPane = createTextPane(word, matchesSearch);
                getChildren().add(wordPane);
            }
        }
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

    public String getSearchText() {
        return searchText.get();
    }

    public StringProperty searchTextProperty() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText.set(searchText);
    }

    public Style getNormalStyle() {
        return normalStyle.getValue();
    }

    public Property<Style> normalStyleProperty() {
        return normalStyle;
    }

    public void setNormalStyle(Style normalStyle) {
        this.normalStyle.setValue(normalStyle);
    }

    public Style getHighlightedStyle() {
        return highlightedStyle.getValue();
    }

    public Property<Style> highlightedStyleProperty() {
        return highlightedStyle;
    }

    public void setHighlightedStyle(Style highlightedStyle) {
        this.highlightedStyle.setValue(highlightedStyle);
    }

    public Font getFont() {
        return font.getValue();
    }

    public Property<Font> fontProperty() {
        return font;
    }

    public void setFont(Font font) {
        this.font.setValue(font);
    }
}
