package org.hildan.fxlog.bindings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import com.sun.javafx.collections.ObservableListWrapper;

/**
 * An observable list containing all elements of multiple other lists (dependencies). Changes in the dependencies are
 * reflected in the merged list. This merged list is unordered, because an item added to the first dependency may appear
 * at the end of the merged list, after items of other dependencies.
 *
 * @param <E>
 *         the type of items in this list
 */
public class UnorderedMergedList<E> extends ObservableListWrapper<E> {

    private final ListChangeListener<E> updater = c -> {
        while (c.next()) {
            if (c.wasRemoved()) {
                removeAll(c.getRemoved());
            }
            if (c.wasAdded()) {
                addAll(c.getAddedSubList());
            }
        }
    };

    // to avoid garbage collection
    @SuppressWarnings("FieldCanBeLocal")
    private final ListChangeListener<ObservableList<? extends E>> subscriber = c -> {
        while (c.next()) {
            if (c.wasRemoved()) {
                c.getRemoved().forEach(this::unsubscribeFrom);
            }
            if (c.wasAdded()) {
                c.getAddedSubList().forEach(this::subscribeTo);
            }
        }
    };

    public UnorderedMergedList(ObservableList<? extends E>... lists) {
        super(new ArrayList<>(Arrays.stream(lists).mapToInt(List::size).sum()));

        for (ObservableList<? extends E> list : lists) {
            subscribeTo(list);
        }
    }

    public UnorderedMergedList(Collection<? extends ObservableList<? extends E>> lists) {
        super(new ArrayList<>(lists.stream().mapToInt(List::size).sum()));

        lists.forEach(this::subscribeTo);
    }

    public UnorderedMergedList(ObservableList<? extends ObservableList<? extends E>> lists) {
        this((Collection<? extends ObservableList<? extends E>>) lists);

        // the list of dependencies can change
        lists.addListener(subscriber);
    }

    private <T extends E> void subscribeTo(ObservableList<T> dependency) {
        addAll(dependency);
        dependency.addListener(updater);
    }

    private <T extends E> void unsubscribeFrom(ObservableList<T> dependency) {
        removeAll(dependency);
        dependency.removeListener(updater);
    }
}
