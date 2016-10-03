package org.hildan.fxlog.rulesets;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A rule that can match any input and associate a result to it.
 *
 * @param <T>
 *         the input type
 * @param <U>
 *         the result type
 */
public class Rule<T, U, M extends Matcher<T>> {

    private final Property<M> matcher;

    private final Property<U> result;

    /**
     * Creates a rule with the given matcher and result.
     *
     * @param filter
     *         the matcher for this rule
     * @param result
     *         the result value of this rule
     */
    public Rule(@NotNull M filter, @Nullable U result) {
        this.matcher = new SimpleObjectProperty<>(filter);
        this.result = new SimpleObjectProperty<>(result);
    }

    public M getMatcher() {
        return matcher.getValue();
    }

    public Property<M> matcherProperty() {
        return matcher;
    }

    public void setMatcher(M matcher) {
        this.matcher.setValue(matcher);
    }

    public U getResult() {
        return result.getValue();
    }

    public Property<U> resultProperty() {
        return result;
    }

    public void setResult(U result) {
        this.result.setValue(result);
    }
}
