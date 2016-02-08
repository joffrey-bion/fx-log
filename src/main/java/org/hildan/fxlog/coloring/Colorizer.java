package org.hildan.fxlog.coloring;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;

import org.hildan.fxlog.core.LogEntry;
import org.jetbrains.annotations.NotNull;

/**
 * A Colorizer can apply a style to any {@link Node} based on a list of {@link StyleRule}s.
 */
public class Colorizer {

    private final StringProperty name;

    private final ObservableList<StyleRule> styleRules;

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
        this.name = new SimpleStringProperty(name);
        this.styleRules = styleRules;
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

    public ObservableList<StyleRule> getRules() {
        return styleRules;
    }

    /**
     * Applies a style to the given Node according to the StyleRules of this Colorizer.
     *
     * @param node
     *         the node to style
     * @param log
     *         the log on which to test the rules
     */
    void applyTo(@NotNull Node node, @NotNull LogEntry log) {
        node.setStyle(null);
        for (StyleRule rule : styleRules) {
            if (rule.applyTo(node, log)) {
                return;
            }
        }
    }

    @Override
    public String toString() {
        return name.get();
    }
}
