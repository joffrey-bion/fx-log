package org.hildan.fxlog.coloring;

import javafx.beans.binding.Binding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import org.hildan.fxlog.core.LogEntry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A binding that computes the first {@link StyleRule} of a {@link Colorizer} that matches a {@link LogEntry}. It stays
 * up-do-date with respect to the log and the colorizer's rules change events.
 */
public class FirstMatchingRuleBinding extends ObjectBinding<StyleRule> {

    private final RuleContentBinder ruleContentBinder = new RuleContentBinder();

    private final ObservableValue<Colorizer> colorizerObservableValue;

    private final ObservableValue<LogEntry> logEntryObservableValue;

    FirstMatchingRuleBinding(ObservableValue<Colorizer> colorizer, ObservableValue<LogEntry> logEntry) {
        colorizerObservableValue = colorizer;
        logEntryObservableValue = logEntry;

        Colorizer currentColorizer = colorizer.getValue();
        bindRulesList(currentColorizer);
        colorizer.addListener((obs, oldColorizer, newColorizer) -> {
            System.out.println("Colorizer changed to " + newColorizer.getName());
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
        System.out.println("Binding to rule " + styleRule.getName());
        Binding[] observables = styleRule.getMatchingInternalsObservable();

        // this is crazy, but required to get instant updates
        // for some reason, this binding does not get invalidated if these dependencies are not re-computed
        // TODO find a way to get invalidated without forcing re-computation like this
        for (Binding observable : observables) {
            observable.addListener(o -> observable.getValue());
        }

        bind(observables);
    }

    private void unbindRuleContent(StyleRule styleRule) {
        unbind(styleRule.getMatchingInternalsObservable());
    }

    @Override
    protected StyleRule computeValue() {
        return getMatchingRule(colorizerObservableValue.getValue(), logEntryObservableValue.getValue());
    }

    @NotNull
    private static StyleRule getMatchingRule(@Nullable Colorizer colorizer, @Nullable LogEntry log) {
        System.out.println("Recomputing matching rule for log: " + (log == null ? null : log.toString()));
        if (colorizer == null || log == null) {
            return StyleRule.DEFAULT;
        }
        return colorizer.getRules().stream().filter(r -> r.matches(log)).findFirst().orElse(StyleRule.DEFAULT);
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
