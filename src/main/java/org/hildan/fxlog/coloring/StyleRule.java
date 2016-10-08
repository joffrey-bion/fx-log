package org.hildan.fxlog.coloring;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;

import org.hildan.fxlog.core.LogEntry;
import org.hildan.fxlog.filtering.Filter;
import org.hildan.fxlog.rulesets.Rule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A rule that can apply a style to a {@link Node} based on a log {@link Filter}.
 */
public class StyleRule extends Rule<LogEntry, Style, Filter> {

    private final StringProperty name;

    /**
     * Creates a StyleRule with no style override.
     *
     * @param name
     *         a name for this rule
     */
    public StyleRule(@NotNull String name) {
        this(name, Filter.findInRawLog(""), new Style(Style.DEFAULT));
    }

    /**
     * Creates a StyleRule with the given filter and style.
     *
     * @param name
     *         a name for this rule
     * @param filter
     *         the filter to use to match logs
     * @param style
     *         the style associated with this rule
     */
    public StyleRule(@NotNull String name, @NotNull Filter filter, @Nullable Style style) {
        super(filter, style);
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
        return getClass().getSimpleName() + " '" + getName() + "'";
    }
}
