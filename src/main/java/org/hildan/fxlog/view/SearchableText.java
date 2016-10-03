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

import org.fxmisc.easybind.EasyBind;
import org.hildan.fxlog.coloring.Style;

public class SearchableText extends FlowPane {

    private static final String WITH_DELIMITER = "((?<=%1$s)|(?=%1$s))";

    private final StringProperty text = new SimpleStringProperty();

    private final StringProperty searchText = new SimpleStringProperty();

    private final Property<Style> normalStyle = new SimpleObjectProperty<>(Style.DEFAULT);

    private final Property<Style> highlightedStyle = new SimpleObjectProperty<>(Style.HIGHLIGHT_SEARCH);

    private final Property<Font> font = new SimpleObjectProperty<>(Font.getDefault());

    private final StackPane initialTextPane;

    public SearchableText() {
        setMinSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        setMaxSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);

        Text initialText = new Text();
        initialText.textProperty().bind(text);
        initialTextPane = wrapAndConfigure(initialText);
        getChildren().add(initialTextPane);

        EasyBind.subscribe(text, s -> refreshSearch());
        EasyBind.subscribe(searchText, s -> refreshSearch());
    }

    private StackPane createTextPane(String str) {
        Text text = new Text(str);
        return wrapAndConfigure(text);
    }

    private StackPane wrapAndConfigure(Text text) {
        StackPane pane = new StackPane();

        text.fontProperty().bind(font);

        String search = searchText.get();
        if (search != null && !search.isEmpty() && text.getText().equals(search)) {
            EasyBind.subscribe(highlightedStyle, style -> style.bindNodes(text, pane));
        } else {
            EasyBind.subscribe(normalStyle, style -> style.bindNodes(text, pane));
        }

        pane.getChildren().add(text);
        return pane;
    }

    private void refreshSearch() {
        String currentText = text.get();
        String currentSearchText = searchText.get();
        getChildren().clear();
        if (currentText == null || currentText.isEmpty() || currentSearchText == null || currentSearchText.isEmpty()) {
            getChildren().add(initialTextPane);
            return;
        }
        String[] parts = computeParts(currentText, currentSearchText);
        for (String part : parts) {
            StackPane partText = createTextPane(part);
            getChildren().add(partText);
        }
    }

    private static String[] computeParts(String text, String search) {
        String splitRegex = String.format(WITH_DELIMITER, Pattern.quote(search));
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
