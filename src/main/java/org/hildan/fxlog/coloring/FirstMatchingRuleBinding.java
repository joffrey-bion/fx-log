package org.hildan.fxlog.coloring;

import java.util.HashMap;
import java.util.Map;

import javafx.beans.binding.Binding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import org.hildan.fxlog.core.LogEntry;

/**
 * A binding that computes the first {@link StyleRule} of a {@link Colorizer} that matches a {@link LogEntry}. It stays
 * up-do-date with respect to the log and the colorizer's rules change events.
 */
public class FirstMatchingRuleBinding extends ObjectBinding<StyleRule> {

    private final RuleContentBinder ruleContentBinder = new RuleContentBinder();

    private final ObservableValue<Colorizer> colorizerObservableValue;

    private final ObservableValue<LogEntry> logEntryObservableValue;

    private final Map<StyleRule, Binding<Boolean>> ruleMatchBindings;

    FirstMatchingRuleBinding(ObservableValue<Colorizer> colorizer, ObservableValue<LogEntry> logEntry) {
        colorizerObservableValue = colorizer;
        logEntryObservableValue = logEntry;
        ruleMatchBindings = new HashMap<>();

        Colorizer currentColorizer = colorizer.getValue();
        bindRulesList(currentColorizer);
        colorizer.addListener((obs, oldColorizer, newColorizer) -> {
            unbindRulesList(oldColorizer);
            bindRulesList(newColorizer);
            invalidate();
        });

        bind(logEntry);
    }

    private void bindRulesList(Colorizer colorizer) {
        if (colorizer != null) {
            ObservableList<StyleRule> rules = colorizer.getRules();
            bind(rules);
            rules.forEach(this::bindRuleContent);
            rules.addListener(ruleContentBinder);
        }
    }

    private void unbindRulesList(Colorizer colorizer) {
        if (colorizer != null) {
            ObservableList<StyleRule> rules = colorizer.getRules();
            unbind(rules);
            rules.removeListener(ruleContentBinder);
            rules.forEach(this::unbindRuleContent);
        }
    }

    private void bindRuleContent(StyleRule styleRule) {
        Binding<Boolean> currentBinding = styleRule.getFilter().matchesBinding(logEntryObservableValue);
        ruleMatchBindings.put(styleRule, currentBinding);
        bind(currentBinding);

        styleRule.filterProperty().addListener((observable, oldValue, newValue) -> {
            Binding<Boolean> oldBinding = ruleMatchBindings.remove(styleRule);
            unbind(oldBinding);

            Binding<Boolean> newBinding = newValue.matchesBinding(logEntryObservableValue);
            ruleMatchBindings.put(styleRule, newBinding);
            bind(newBinding);
        });
    }

    private void unbindRuleContent(StyleRule styleRule) {
        unbind(ruleMatchBindings.remove(styleRule));
    }

    @Override
    protected StyleRule computeValue() {
        Colorizer colorizer = colorizerObservableValue.getValue();
        if (colorizer == null || logEntryObservableValue.getValue() == null) {
            return StyleRule.DEFAULT;
        }
        return colorizer.getRules()
                        .stream()
                        .filter(r -> ruleMatchBindings.get(r).getValue())
                        .findFirst()
                        .orElse(StyleRule.DEFAULT);
    }

    private class RuleContentBinder implements ListChangeListener<StyleRule> {

        @Override
        public void onChanged(Change<? extends StyleRule> change) {
            while (change.next()) {
                if (change.wasAdded()) {
                    change.getAddedSubList().forEach(FirstMatchingRuleBinding.this::bindRuleContent);
                }
                if (change.wasRemoved()) {
                    change.getRemoved().forEach(FirstMatchingRuleBinding.this::unbindRuleContent);
                }
            }
        }
    }
}
