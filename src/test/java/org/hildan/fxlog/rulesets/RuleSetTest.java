package org.hildan.fxlog.rulesets;

import javafx.beans.binding.Binding;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;

import org.hildan.fxlog.rulesets.MacherTest.GreaterThanMatcher;
import org.junit.Assert;
import org.junit.Test;

public class RuleSetTest {

    @Test
    public void testSimpleRuleSetBinding() {
        RuleSet<Integer, String, Matcher<Integer>, Rule<Integer, String, Matcher<Integer>>> ruleSet = new RuleSet<>();
        ruleSet.getRules().add(new Rule<>(i -> i != null && i < 5, "Very small"));
        ruleSet.getRules().add(new Rule<>(i -> i != null && i < 10, "Small"));
        ruleSet.getRules().add(new Rule<>(i -> i != null && i < 15, "Large"));
        ruleSet.getRules().add(new Rule<>(i1 -> i1 != null && i1 < 20, "Very large"));

        Property<Integer> intProp = new SimpleObjectProperty<>(null);
        Binding<String> resultBinding = ruleSet.outputFor(intProp, "default");

        Assert.assertEquals("Should return the default", "default", resultBinding.getValue());

        intProp.setValue(3);

        Assert.assertFalse(resultBinding.isValid());
        Assert.assertEquals("Should match 1st rule", "Very small", resultBinding.getValue());

        intProp.setValue(8);

        Assert.assertFalse(resultBinding.isValid());
        Assert.assertEquals("Should match 2nd rule", "Small", resultBinding.getValue());

        intProp.setValue(17);

        Assert.assertFalse(resultBinding.isValid());
        Assert.assertEquals("Should match 4th rule", "Very large", resultBinding.getValue());

        intProp.setValue(50);

        Assert.assertFalse(resultBinding.isValid());
        Assert.assertEquals("Should return the default", "default", resultBinding.getValue());
    }

    @Test
    public void testChangingMatchers() {
        Property<Integer> threshold1 = new SimpleObjectProperty<>(100);
        Property<Integer> threshold2 = new SimpleObjectProperty<>(50);
        Property<Integer> threshold3 = new SimpleObjectProperty<>(20);
        GreaterThanMatcher matcher1 = new GreaterThanMatcher();
        GreaterThanMatcher matcher2 = new GreaterThanMatcher();
        GreaterThanMatcher matcher3 = new GreaterThanMatcher();
        matcher1.thresholdProperty().bind(threshold1);
        matcher2.thresholdProperty().bind(threshold2);
        matcher3.thresholdProperty().bind(threshold3);

        RuleSet<Integer, String, GreaterThanMatcher, Rule<Integer, String, GreaterThanMatcher>> ruleSet =
                new RuleSet<>();
        ruleSet.getRules().add(new Rule<>(matcher1, "Matches 1"));
        ruleSet.getRules().add(new Rule<>(matcher2, "Matches 2"));
        ruleSet.getRules().add(new Rule<>(matcher3, "Matches 3"));

        Property<Integer> intProp = new SimpleObjectProperty<>(5);
        Binding<String> resultBinding = ruleSet.outputFor(intProp, "default");

        intProp.setValue(3);

        Assert.assertFalse(resultBinding.isValid());
        Assert.assertEquals("Should return the default", "default", resultBinding.getValue());

        intProp.setValue(150);

        Assert.assertFalse(resultBinding.isValid());
        Assert.assertEquals("Should match 1st rule", "Matches 1", resultBinding.getValue());

        intProp.setValue(80);

        Assert.assertFalse(resultBinding.isValid());
        Assert.assertEquals("Should match 2nd rule", "Matches 2", resultBinding.getValue());

        intProp.setValue(32);

        Assert.assertFalse(resultBinding.isValid());
        Assert.assertEquals("Should match 3rd rule", "Matches 3", resultBinding.getValue());

        threshold3.setValue(50);

        Assert.assertFalse(resultBinding.isValid());
        Assert.assertEquals("Should return the default", "default", resultBinding.getValue());

        intProp.setValue(70);

        Assert.assertFalse(resultBinding.isValid());
        Assert.assertEquals("Should match 2nd rule", "Matches 2", resultBinding.getValue());

        threshold1.setValue(0);

        Assert.assertFalse(resultBinding.isValid());
        Assert.assertEquals("Should match 1st rule", "Matches 1", resultBinding.getValue());
    }

    @Test
    public void testChangingRules() {
        RuleSet<Integer, String, Matcher<Integer>, Rule<Integer, String, Matcher<Integer>>> ruleSet = new RuleSet<>();
        ruleSet.getRules().add(new Rule<>(i -> i != null && i < 5, "Very small"));
        ruleSet.getRules().add(new Rule<>(i -> i != null && i < 10, "Small"));
        ruleSet.getRules().add(new Rule<>(i -> i != null && i < 15, "Large"));
        Rule<Integer, String, Matcher<Integer>> veryLargeRule = new Rule<>(i -> i != null && i < 20, "Very large");
        ruleSet.getRules().add(veryLargeRule);

        Property<Integer> intProp = new SimpleObjectProperty<>(null);
        Binding<String> resultBinding = ruleSet.outputFor(intProp, "default");

        intProp.setValue(50);

        Assert.assertFalse(resultBinding.isValid());
        Assert.assertEquals("Should return the default", "default", resultBinding.getValue());

        ruleSet.getRules().add(new Rule<>(i -> i != null && i < 100, "Extra large"));

        Assert.assertFalse(resultBinding.isValid());
        Assert.assertEquals("Should match new rule", "Extra large", resultBinding.getValue());

        intProp.setValue(17);

        Assert.assertFalse(resultBinding.isValid());
        Assert.assertEquals("Should match 4th rule", "Very large", resultBinding.getValue());

        ruleSet.getRules().remove(veryLargeRule);

        Assert.assertFalse(resultBinding.isValid());
        Assert.assertEquals("Should match 5th rule", "Extra large", resultBinding.getValue());
    }

    @Test
    public void testChangingRuleSet() {
        RuleSet<Integer, String, Matcher<Integer>, Rule<Integer, String, Matcher<Integer>>> ruleSet1 = new RuleSet<>();
        ruleSet1.getRules().add(new Rule<>(i -> i != null && i < 10, "Small 1"));
        ruleSet1.getRules().add(new Rule<>(i -> i != null && i < 50, "Medium 1"));
        ruleSet1.getRules().add(new Rule<>(i -> i != null && i < 100, "Large 1"));

        RuleSet<Integer, String, Matcher<Integer>, Rule<Integer, String, Matcher<Integer>>> ruleSet2 = new RuleSet<>();
        ruleSet2.getRules().add(new Rule<>(i -> i != null && i < 50, "Small 2"));
        ruleSet2.getRules().add(new Rule<>(i -> i != null && i < 100, "Medium 2"));
        ruleSet2.getRules().add(new Rule<>(i -> i != null && i < 200, "Large 2"));

        Property<Integer> intProp = new SimpleObjectProperty<>(null);
        Property<RuleSet<Integer, String, Matcher<Integer>, Rule<Integer, String, Matcher<Integer>>>> ruleSetProp =
                new SimpleObjectProperty<>(ruleSet1);
        Binding<String> resultBinding = RuleSet.outputFor(ruleSetProp, intProp, "default");

        intProp.setValue(null);

        Assert.assertFalse(resultBinding.isValid());
        Assert.assertEquals("Should return the default", "default", resultBinding.getValue());

        intProp.setValue(80);

        Assert.assertFalse(resultBinding.isValid());
        Assert.assertEquals("Should match 3rd rule", "Large 1", resultBinding.getValue());

        ruleSetProp.setValue(ruleSet2);

        Assert.assertFalse(resultBinding.isValid());
        Assert.assertEquals("Should match new rule set", "Medium 2", resultBinding.getValue());

        intProp.setValue(120);

        Assert.assertFalse(resultBinding.isValid());
        Assert.assertEquals("Should match 3rd rule", "Large 2", resultBinding.getValue());

        ruleSetProp.setValue(ruleSet1);

        Assert.assertFalse(resultBinding.isValid());
        Assert.assertEquals("Should return the default", "default", resultBinding.getValue());

        intProp.setValue(80);

        Assert.assertFalse(resultBinding.isValid());
        Assert.assertEquals("Should match 3rd rule", "Large 1", resultBinding.getValue());

        ruleSetProp.setValue(null);

        Assert.assertFalse(resultBinding.isValid());
        Assert.assertEquals("Should return the default", "default", resultBinding.getValue());
    }
}
