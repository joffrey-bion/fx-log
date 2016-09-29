package org.hildan.fxlog.coloring;

import javafx.beans.binding.Binding;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;

import org.fxmisc.easybind.EasyBind;
import org.hildan.fxlog.core.LogEntry;
import org.hildan.fxlog.filtering.Filter;
import org.hildan.fxlog.rulesets.RuleSet;
import org.jetbrains.annotations.NotNull;

/**
 * A Colorizer can apply a style to any {@link Node} based on a list of {@link StyleRule}s.
 */
public class Colorizer extends RuleSet<LogEntry, Style, Filter, StyleRule> {

    private final StringProperty name;

    /**
     * Creates a new Colorizer with no rules.
     *
     * @param name
     *         a name for this Colorizer
     */
    public Colorizer(@NotNull String name) {
        this(name, FXCollections.observableArrayList());
    }

    /**
     * Creates a new Colorizer with the given rules.
     *
     * @param name
     *         a name for this Colorizer
     * @param styleRules
     *         the list of StyleRules to use when styling a Node
     */
    public Colorizer(@NotNull String name, @NotNull ObservableList<StyleRule> styleRules) {
        super(styleRules);
        this.name = new SimpleStringProperty(name);
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    @Override
    public String toString() {
        return getName();
    }
}
