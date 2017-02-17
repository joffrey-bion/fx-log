package org.hildan.fx.components;

import java.util.function.Predicate;

import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.util.StringConverter;

import org.hildan.fxlog.themes.Css;

public class ValidatingTextFieldListCell<T> extends TextFieldListCell<T> {

    private final Predicate<String> validator;

    public ValidatingTextFieldListCell(StringConverter<T> converter, Predicate<String> validator) {
        super(converter);
        this.validator = validator;
    }

    @Override
    public void commitEdit(T item) {
        if (!isEditing()) {
            return;
        }

        // the edited text is not in getText() but in the TextField used as Graphic for this cell
        TextField textField = (TextField) getGraphic();
        String editedText = textField.getText();

        boolean itemIsValid = validator.test(editedText);
        pseudoClassStateChanged(Css.INVALID, !itemIsValid);
        if (itemIsValid) {
            // only commit if the item is valid, otherwise we stay in edit state
            super.commitEdit(item);
        }
    }
}
