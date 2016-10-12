package org.hildan.fxlog.bindings;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;

public class MergedList {

    public static <E> ObservableList<E> create(ObservableList<? extends ObservableList<? extends E>> lists) {
        final ObservableList<E> combined = FXCollections.observableArrayList();
        final ListChangeListener<E> listener = (Change<? extends E> c) -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    combined.addAll(c.getAddedSubList());
                }
                if (c.wasRemoved()) {
                    combined.removeAll(c.getRemoved());
                }
            }
        };

        for (ObservableList<? extends E> list : lists) {
            subscribe(combined, list, listener);
        }

        lists.addListener((Change<? extends ObservableList<? extends E>> c) -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    c.getAddedSubList().forEach(l -> subscribe(combined, l, listener));
                }
                if (c.wasRemoved()) {
                    c.getRemoved().forEach(l -> unsubscribe(combined, l, listener));
                }
            }
        });

        return combined;
    }

    private static <E, T extends E> void subscribe(ObservableList<E> combinedList, ObservableList<T> dependency,
                                                   ListChangeListener<? super E> listener) {
        combinedList.addAll(dependency);
        dependency.addListener(listener);
    }

    private static <E, T extends E> void unsubscribe(ObservableList<E> combinedList, ObservableList<T> dependency,
                                                     ListChangeListener<? super E> listener) {
        combinedList.removeAll(dependency);
        dependency.removeListener(listener);
    }
}
