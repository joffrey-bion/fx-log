package org.hildan.fxlog.search;

import java.util.function.Predicate;

import javafx.beans.binding.Binding;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Search {

    private final BooleanProperty active = new SimpleBooleanProperty();

    private final StringProperty text = new SimpleStringProperty();

    private final BooleanProperty matchCase = new SimpleBooleanProperty();

    private final BooleanProperty regexMode = new SimpleBooleanProperty();

    private final Binding<Predicate<String>> textSearcher = createTextSearcherBinding();

    private Binding<Predicate<String>> createTextSearcherBinding() {
        return Bindings.createObjectBinding(() -> {
            return createTextMatcher(matchCase.get(), text.get());
        }, matchCase, text);
    }

    private static Predicate<String> createTextMatcher(boolean matchCase, String searchText) {
        if (searchText.isEmpty()) {
            return s -> false;
        }
        if (matchCase) {
            return s -> s.contains(searchText);
        } else {
            String searchTextLowercase = searchText.toLowerCase();
            return s -> s.toLowerCase().contains(searchTextLowercase);
        }
    }

    public boolean isActive() {
        return active.get();
    }

    public BooleanProperty activeProperty() {
        return active;
    }

    public void setActive(boolean active) {
        this.active.set(active);
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

    public boolean isMatchCase() {
        return matchCase.get();
    }

    public BooleanProperty matchCaseProperty() {
        return matchCase;
    }

    public void setMatchCase(boolean matchCase) {
        this.matchCase.set(matchCase);
    }

    public boolean isRegexMode() {
        return regexMode.get();
    }

    public BooleanProperty regexModeProperty() {
        return regexMode;
    }

    public void setRegexMode(boolean regexMode) {
        this.regexMode.set(regexMode);
    }

    public Predicate<String> getTextSearcher() {
        return textSearcher.getValue();
    }

    public Binding<Predicate<String>> textSearcherProperty() {
        return textSearcher;
    }
}
