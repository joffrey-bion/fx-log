package org.hildan.fxlog.rulesets;

import javafx.beans.binding.Binding;
import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;

import org.junit.Assert;
import org.junit.Test;

public class MacherTest {

    public static class GreaterThanMatcher implements Matcher<Integer> {

        private final Property<Integer> threshold = new SimpleObjectProperty<>(0);

        public Property<Integer> thresholdProperty() {
            return threshold;
        }

        @Override
        public boolean test(Integer integer) {
            return integer > threshold.getValue();
        }

        @Override
        public Binding<Boolean> matches(ObservableValue<Integer> observableValue) {
            return Bindings.createBooleanBinding(() -> test(observableValue.getValue()), observableValue, threshold);
        }
    }

    @Test
    public void testSimpleMatchBinding() {
        Matcher<Integer> matcher = i -> i > 5;
        Property<Integer> intProp = new SimpleObjectProperty<>(2);
        Binding<Boolean> matches = matcher.matches(intProp);

        Assert.assertFalse(matches.getValue());

        intProp.setValue(6);

        Assert.assertFalse(matches.isValid());
        Assert.assertTrue(matches.getValue());

        intProp.setValue(3);

        Assert.assertFalse(matches.isValid());
        Assert.assertFalse(matches.getValue());
    }

    @Test
    public void testComplexMatchBinding() {
        Property<Integer> intProp = new SimpleObjectProperty<>(2);
        Property<Integer> thresholdProp = new SimpleObjectProperty<>(5);

        GreaterThanMatcher matcher = new GreaterThanMatcher();
        matcher.thresholdProperty().bind(thresholdProp);

        Binding<Boolean> matches = matcher.matches(intProp);

        Assert.assertFalse(matches.getValue());

        intProp.setValue(6);

        Assert.assertFalse(matches.isValid());
        Assert.assertTrue(matches.getValue());

        intProp.setValue(3);

        Assert.assertFalse(matches.isValid());
        Assert.assertFalse(matches.getValue());

        thresholdProp.setValue(2);

        Assert.assertFalse(matches.isValid());
        Assert.assertTrue(matches.getValue());

        thresholdProp.setValue(10);

        Assert.assertFalse(matches.isValid());
        Assert.assertFalse(matches.getValue());

        intProp.setValue(12);

        Assert.assertFalse(matches.isValid());
        Assert.assertTrue(matches.getValue());

        intProp.setValue(15);

        Assert.assertFalse(matches.isValid());
        Assert.assertTrue(matches.getValue());
    }
}
