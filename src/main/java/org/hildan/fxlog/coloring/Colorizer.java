package org.hildan.fxlog.coloring;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;

import org.hildan.fx.bindings.rulesets.RuleSet;
import org.hildan.fx.components.list.Named;
import org.hildan.fxlog.data.LogEntry;
import org.hildan.fxlog.filtering.Filter;
import org.jetbrains.annotations.NotNull;

/**
 * A Colorizer can apply a style to any {@link Node} based on a list of {@link StyleRule}s.
 */
public class Colorizer extends RuleSet<LogEntry, Style, Filter, StyleRule> implements Named {

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

    /**
     * Creates a copy of the given Colorizer.
     *
     * @param source
     *         the colorizer to create a copy of
     */
    public Colorizer(@NotNull Colorizer source) {
        this(source.getName());
        this.getRules().setAll(source.getRules());
    }

    @Override
    public String getName() {
        return name.get();
    }

    @SuppressWarnings("unused")
    public StringProperty nameProperty() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name.set(name);
    }

    @Override
    public String toString() {
        return getName();
    }
}
