package org.hildan.fx.bindings.lists;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class UnorderedMergedListTest {

    private ObservableList<String> source1;

    private ObservableList<String> source2;

    private ObservableList<String> source3;

    @Before
    public void initSource() {
        source1 = FXCollections.observableArrayList("A", "B", "C");
        source2 = FXCollections.observableArrayList("1", "2", "3");
        source3 = FXCollections.observableArrayList("X", "Y", "Z");
    }

    private void testInit(ObservableList<String> merged) {
        Assert.assertTrue("The merged list should contain all elements of source1", merged.containsAll(source1));
        Assert.assertTrue("The merged list should contain all elements of source2", merged.containsAll(source2));
    }

    private void testAdd(ObservableList<String> merged) {
        source1.add("added1");
        Assert.assertTrue("The merged list should contain elements added to source1", merged.contains("added1"));
        source2.add("added2");
        Assert.assertTrue("The merged list should contain elements added to source2", merged.contains("added2"));
    }

    private void testRemove(ObservableList<String> merged) {
        source1.remove("B");
        Assert.assertFalse("The merged list should not contain removed elements from source1", merged.contains("B"));
        source2.remove("3");
        Assert.assertFalse("The merged list should not contain removed elements from source2", merged.contains("3"));
    }

    @SuppressWarnings("unchecked")
    private ObservableList<String> createMergedListWithVarargs() {
        return new UnorderedMergedList<>(source1, source2);
    }

    @SuppressWarnings("unchecked")
    private ObservableList<String> createMergedListWithObservableDependencies() {
        ObservableList<ObservableList<String>> dependencies = FXCollections.observableArrayList();
        dependencies.add(source1);
        dependencies.add(source2);
        return new UnorderedMergedList<>(dependencies);
    }

    @Test
    public void testVarargInit() {
        ObservableList<String> merged = createMergedListWithVarargs();
        testInit(merged);
    }

    @Test
    public void testVarargAdd() {
        ObservableList<String> merged = createMergedListWithVarargs();
        testAdd(merged);
    }

    @Test
    public void testVarargRemove() {
        ObservableList<String> merged = createMergedListWithVarargs();
        testRemove(merged);
    }

    @Test
    public void testObservableDependenciesInit() {
        ObservableList<String> merged = createMergedListWithObservableDependencies();
        testInit(merged);
    }

    @Test
    public void testObservableDependenciesAdd() {
        ObservableList<String> merged = createMergedListWithObservableDependencies();
        testAdd(merged);
    }

    @Test
    public void testObservableDependenciesRemove() {
        ObservableList<String> merged = createMergedListWithObservableDependencies();
        testRemove(merged);
    }

    @Test
    public void testAddDependency() {
        ObservableList<ObservableList<String>> dependencies = FXCollections.observableArrayList();
        dependencies.add(source1);
        dependencies.add(source2);
        ObservableList<String> merged = new UnorderedMergedList<>(dependencies);
        dependencies.add(source3);
        Assert.assertTrue("The new dependency should be taken into account", merged.containsAll(source3));
    }

    @Test
    public void testAddToNewDependency() {
        ObservableList<ObservableList<String>> dependencies = FXCollections.observableArrayList();
        dependencies.add(source1);
        dependencies.add(source2);
        ObservableList<String> merged = new UnorderedMergedList<>(dependencies);
        dependencies.add(source3);
        source3.add("added");
        Assert.assertTrue("The new dependency's new items should be taken into account", merged.contains("added"));
    }
}
