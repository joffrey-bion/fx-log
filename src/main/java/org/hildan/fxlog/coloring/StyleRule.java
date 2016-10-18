package org.hildan.fxlog.coloring;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;

import org.hildan.fx.bindings.rulesets.Rule;
import org.hildan.fx.components.list.Named;
import org.hildan.fxlog.data.LogEntry;
import org.hildan.fxlog.filtering.Filter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A rule that can apply a style to a {@link Node} based on a log {@link Filter}.
 */
public class StyleRule extends Rule<LogEntry, Style, Filter> implements Named {

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

    /**
     * Creates a copy of the given StyleRule. The Style is also copied from the given rule's style, it is not reused.
     *
     * @param source
     *         the rule to copy
     */
    public StyleRule(@NotNull StyleRule source) {
        this(source.getName(), new Filter(source.getMatcher()), new Style(source.getResult()));
    }

    @Override
    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name.set(name);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " '" + getName() + "'";
    }
}
