package org.hildan.fx.components;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.util.StringConverter;

public class EditableListView<T> extends ListView<T> {

    private StringConverter<T> converter;

    public EditableListView() {
        setEditable(true);
        // replace edited items by default
        setOnEditCommit(e -> getItems().set(e.getIndex(), e.getNewValue()));
    }

    public void setConverter(Function<String, T> fromString, Function<T, String> toString) {
        setConverter(createConverter(fromString, toString));
    }

    public void setConverter(Function<String, T> fromString, Function<T, String> toString,
                             Predicate<String> validityPredicate) {
        setConverter(createConverter(fromString, toString), validityPredicate);
    }

    public void setConverter(StringConverter<T> converter) {
        this.converter = converter;
        setCellFactory(TextFieldListCell.forListView(converter));
    }

    public void setConverter(StringConverter<T> converter, Predicate<String> validator) {
        this.converter = converter;
        setCellFactory(l -> new ValidatingTextFieldListCell<>(converter, validator));
    }

    public void setUpdater(BiConsumer<T, String> updateString) {
        if (converter == null) {
            throw new IllegalStateException("You must first set a converter before setting the updater");
        }
        setOnEditCommit(e -> {
            T editedItem = getItems().get(e.getIndex());
            updateString.accept(editedItem, converter.toString(e.getNewValue()));
        });
    }

    private static <T> StringConverter<T> createConverter(Function<String, T> fromString,
                                                          Function<T, String> toString) {
        return new StringConverter<T>() {
            @Override
            public String toString(T object) {
                return toString.apply(object);
            }

            @Override
            public T fromString(String string) {
                return fromString.apply(string);
            }
        };
    }
}
