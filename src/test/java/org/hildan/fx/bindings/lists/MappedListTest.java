package org.hildan.fx.bindings.lists;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MappedListTest {

    private ObservableList<String> source;

    @Before
    public void initSource() {
        source = FXCollections.observableArrayList("zero", "one", "two");
    }

    @Test
    public void testMapping() {
        ObservableList<String> mapped = new MappedList<>(source, s -> s + 'X');
        Assert.assertEquals("zeroX", mapped.get(0));
        Assert.assertEquals("oneX", mapped.get(1));
        Assert.assertEquals("twoX", mapped.get(2));
    }

    @Test
    public void testAdd() {
        ObservableList<String> mapped = new MappedList<>(source, s -> s + 'X');
        source.add("three");
        Assert.assertEquals(4, mapped.size());
        Assert.assertEquals("threeX",  mapped.get(3));
    }

    @Test
    public void testRemove() {
        ObservableList<String> mapped = new MappedList<>(source, s -> s + 'X');
        source.remove("one");
        Assert.assertEquals(2, mapped.size());
        Assert.assertEquals("twoX",  mapped.get(1));
    }

}
