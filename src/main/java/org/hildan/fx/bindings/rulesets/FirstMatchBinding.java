package org.hildan.fx.bindings.rulesets;

import java.util.HashMap;
import java.util.Map;

import javafx.beans.binding.Binding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import org.fxmisc.easybind.EasyBind;

/**
 * A binding that computes the result of the first rule of a rule set that matches an observable input. It stays
 * up-do-date despite any change to the input and the rule set's internals.
 */
class FirstMatchBinding<T, U, M extends Matcher<T>, R extends Rule<T, U, M>> extends ObjectBinding<U> {

    private final RuleReBinder ruleReBinder = new RuleReBinder();

    private final ObservableValue<? extends RuleSet<T, U, M, R>> ruleSetObservableValue;

    private final ObservableValue<T> input;

    private final Map<Rule<T, U, M>, Binding<Boolean>> ruleMatchBindings = new HashMap<>();

    private final U defaultResult;

    FirstMatchBinding(RuleSet<T, U, M, R> ruleSet, ObservableValue<T> input, U defaultResult) {
        this.ruleSetObservableValue = new SimpleObjectProperty<>(ruleSet);
        this.input = input;
        this.defaultResult = defaultResult;

        bindRulesList(ruleSet);
        bind(input);
    }

    FirstMatchBinding(ObservableValue<? extends RuleSet<T, U, M, R>> ruleSet, ObservableValue<T> input,
                             U defaultResult) {
        this.ruleSetObservableValue = ruleSet;
        this.input = input;
        this.defaultResult = defaultResult;

        RuleSet<T, U, M, R> currentRuleSet = ruleSet.getValue();
        bindRulesList(currentRuleSet);
        ruleSet.addListener((obs, oldColorizer, newColorizer) -> {
            unbindRulesList(oldColorizer);
            bindRulesList(newColorizer);
            invalidate();
        });

        bind(input);
    }

    private void bindRulesList(RuleSet<T, U, M, R> ruleSet) {
        if (ruleSet != null) {
            ObservableList<R> rules = ruleSet.getRules();
            bind(rules);
            rules.forEach(this::bindRule);
            rules.addListener(ruleReBinder);
        }
    }

    private void unbindRulesList(RuleSet<T, U, M, R> ruleSet) {
        if (ruleSet != null) {
            ObservableList<? extends Rule<T, U, M>> rules = ruleSet.getRules();
            unbind(rules);
            rules.removeListener(ruleReBinder);
            rules.forEach(this::unbindRule);
        }
    }

    private void bindRule(Rule<T, U, M> rule) {
        bind(rule.resultProperty());

        Binding<Boolean> currentBinding = EasyBind.select(rule.matcherProperty()).selectObject(m -> m.matches(input));
        ruleMatchBindings.put(rule, currentBinding);
        bind(currentBinding);
    }

    private void unbindRule(Rule<T, U, M> rule) {
        unbind(rule.resultProperty());
        unbind(ruleMatchBindings.remove(rule));
    }

    @Override
    protected U computeValue() {
        RuleSet<T, U, M, R> ruleSet = ruleSetObservableValue.getValue();
        if (ruleSet == null) {
            return defaultResult;
        }
        return ruleSet.getRules()
                      .stream()
                      .filter(r -> ruleMatchBindings.get(r).getValue())
                      .findFirst()
                      .map(Rule::getResult)
                      .orElse(defaultResult);
    }

    private class RuleReBinder implements ListChangeListener<Rule<T, U, M>> {

        @Override
        public void onChanged(Change<? extends Rule<T, U, M>> change) {
            while (change.next()) {
                if (change.wasAdded()) {
                    change.getAddedSubList().forEach(FirstMatchBinding.this::bindRule);
                }
                if (change.wasRemoved()) {
                    change.getRemoved().forEach(FirstMatchBinding.this::unbindRule);
                }
            }
        }
    }
}
