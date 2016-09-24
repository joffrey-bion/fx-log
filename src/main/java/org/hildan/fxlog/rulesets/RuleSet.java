package org.hildan.fxlog.rulesets;

import javafx.beans.binding.Binding;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.jetbrains.annotations.NotNull;

public class RuleSet<T, U, M extends Matcher<T>, R extends Rule<T, U, M>> {

    private final ObservableList<R> rules;

    /**
     * Creates a new RuleSet with no rules.
     */
    public RuleSet() {
        this(FXCollections.observableArrayList());
    }

    /**
     * Creates a new RuleSet with the given rules.
     *
     * @param rules
     *         the list of initial rules
     */
    public RuleSet(@NotNull ObservableList<R> rules) {
        this.rules = rules;
    }

    public ObservableList<R> getRules() {
        return rules;
    }

    public Binding<U> outputFor(ObservableValue<T> observableValue, U defaultValue) {
        return new FirstMatchBinding<>(this, observableValue, defaultValue);
    }

    public static <T, U, M extends Matcher<T>, R extends Rule<T, U, M>> Binding<U> outputFor(
            ObservableValue<? extends RuleSet<T, U, M, R>> ruleSet, ObservableValue<T> observableValue,
            U defaultValue) {
        return new FirstMatchBinding<>(ruleSet, observableValue, defaultValue);
    }
}
