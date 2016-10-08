package org.hildan.fxlog.view.components;

import java.util.function.Predicate;

import javafx.scene.control.cell.TextFieldListCell;
import javafx.util.StringConverter;

import org.hildan.fxlog.themes.Css;

public class ValidatingTextFieldListCell<T> extends TextFieldListCell<T> {

    private final Predicate<String> validator;

    public ValidatingTextFieldListCell(StringConverter<T> converter, Predicate<String> validator) {
        super(converter);
        this.validator = validator;
    }

    public void commitEdit(T item) {
        if (!isEditing()) {
            return;
        }
        boolean itemIsValid = validator.test(getText());
        pseudoClassStateChanged(Css.INVALID, !itemIsValid);
        if (itemIsValid) {
            // only commit if the item is valid, otherwise we stay in edit state
            super.commitEdit(item);
        }
    }
}
