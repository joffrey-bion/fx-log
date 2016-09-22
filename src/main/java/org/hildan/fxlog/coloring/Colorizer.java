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
     * Binds the style of the given Node to the given log and colorizer observables. If one of them changes, the rule
     * matching is re-computed to update the style of the node accordingly.
     *
     * @param node
     *         the node to style
     * @param colorizer
     *         the observable colorizer to apply
     * @param logEntry
     *         the observable log on which to test the rules
     */
    public static void bindStyle(Node node, ObservableValue<Colorizer> colorizer, ObservableValue<LogEntry> logEntry) {
        Binding<StyleRule> matchingRuleBinding = new FirstMatchingRuleBinding(colorizer, logEntry);
        EasyBind.subscribe(matchingRuleBinding, r -> r.bindNodeStyle(node));
    }

    @Override
    public String toString() {
        return name.get();
    }
}
