package org.hildan.fx.bindings.lists;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.collections.transformation.TransformationList;

/**
 * Returns a wrapper list whose items are mapped from the source list's items.
 *
 * @param <E>
 *         the type of the elements in this list
 * @param <F>
 *         the type of the elements in the source list
 */
public class MappedList<E, F> extends TransformationList<E, F> {

    private final Function<F, E> mapper;

    /**
     * Creates a new mapped list containing the items of the given source transformed with the given mapper.
     *
     * @param source
     *         the source list to wrap
     * @param mapper
     *         the mapper to use to transform the items of the source into items of this list
     */
    public MappedList(ObservableList<F> source, Function<F, E> mapper) {
        super(source);
        this.mapper = mapper;
    }

    @Override
    public int getSourceIndex(int index) {
        return index;
    }

    @Override
    public E get(int index) {
        return mapper.apply(getSource().get(index));
    }

    @Override
    public int size() {
        return getSource().size();
    }

    @Override
    protected void sourceChanged(Change<? extends F> c) {
        fireChange(new Change<E>(this) {

            @Override
            public boolean wasAdded() {
                return c.wasAdded();
            }

            @Override
            public boolean wasRemoved() {
                return c.wasRemoved();
            }

            @Override
            public boolean wasReplaced() {
                return c.wasReplaced();
            }

            @Override
            public boolean wasUpdated() {
                return c.wasUpdated();
            }

            @Override
            public boolean wasPermutated() {
                return c.wasPermutated();
            }

            @Override
            public int getPermutation(int i) {
                return c.getPermutation(i);
            }

            @Override
            protected int[] getPermutation() {
                // This method is only called by the superclass methods wasPermutated() and getPermutation(int), which
                // are both overriden by this class. There is no other way this method can be called.
                throw new AssertionError("Unreachable code");
            }

            @Override
            public List<E> getRemoved() {
                return c.getRemoved().stream().map(mapper).collect(Collectors.toList());
            }

            @Override
            public int getFrom() {
                return c.getFrom();
            }

            @Override
            public int getTo() {
                return c.getTo();
            }

            @Override
            public boolean next() {
                return c.next();
            }

            @Override
            public void reset() {
                c.reset();
            }
        });
    }
}
