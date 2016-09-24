package org.hildan.fxlog.rulesets;

import java.util.function.Predicate;

import javafx.beans.binding.Binding;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;

public interface Matcher<T> extends Predicate<T> {

    default Binding<Boolean> matches(ObservableValue<T> observableValue) {
        return Bindings.createBooleanBinding(() -> test(observableValue.getValue()), observableValue);
    }

}
