package org.hildan.fx.components;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.Predicate;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.IntegerExpression;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;

import org.hildan.fxlog.themes.Css;

public class EditableListPane<T> extends BorderPane {

    @FXML
    private Label titleLabel;

    @FXML
    private EditableListView<T> list;

    @FXML
    private TextField newItemTextField;

    @FXML
    private Button addButton;

    @FXML
    private Button removeButton;

    @FXML
    private Button moveUpButton;

    @FXML
    private Button moveDownButton;

    private Function<String, T> createItem;

    private final ObjectProperty<Predicate<String>> newItemValidator = new SimpleObjectProperty<>();

    // -1 because no item is in use by default
    private final IntegerProperty itemInUseIndex = new SimpleIntegerProperty(-1);

    public EditableListPane() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("editable_list_pane.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
            configure();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    private void configure() {
        IntegerExpression selectedItemIndex = list.getSelectionModel().selectedIndexProperty();
        BooleanBinding selectedColorizerIsUsed = selectedItemIndex.isEqualTo(itemInUseIndex);
        BooleanBinding noColumnizerSelected = list.getSelectionModel().selectedItemProperty().isNull();
        BooleanBinding firstColumnizerSelected = list.getSelectionModel().selectedIndexProperty().isEqualTo(0);
        IntegerExpression lastIndex =
                Bindings.createIntegerBinding(() -> list.getItems().size() - 1, list.itemsProperty());
        BooleanBinding lastColumnizerSelected = list.getSelectionModel().selectedIndexProperty().isEqualTo(lastIndex);
        addButton.disableProperty().bind(this.isNewItemTextValid().not());
        removeButton.disableProperty().bind(noColumnizerSelected.or(selectedColorizerIsUsed));
        moveUpButton.disableProperty().bind(noColumnizerSelected.or(firstColumnizerSelected));
        moveDownButton.disableProperty().bind(noColumnizerSelected.or(lastColumnizerSelected));
        newItemTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            // we don't want the text field to be marked invalid when the user doesn't intend to add an item
            boolean isAcceptable = newValue.isEmpty() || isValidItemText(newValue);
            newItemTextField.pseudoClassStateChanged(Css.INVALID, !isAcceptable);
        });
    }

    private BooleanBinding isNewItemTextValid() {
        Callable<Boolean> isValid = () -> isValidItemText(newItemTextField.getText());
        return Bindings.createBooleanBinding(isValid, newItemValidator, newItemTextField.textProperty());
    }

    private boolean isValidItemText(String text) {
        return newItemValidator.get() == null || newItemValidator.get().test(text);
    }

    /**
     * Convenience method for {@code getList().getSelectionModel().selectedItemProperty()}.
     *
     * @return a property representing the currently selected item
     */
    public ReadOnlyObjectProperty<T> selectedItemProperty() {
        return list.getSelectionModel().selectedItemProperty();
    }

    @FXML
    void addItem() {
        T newColorizer = createItem.apply(newItemTextField.getText());
        list.getItems().add(newColorizer);
        // reset the field after add
        newItemTextField.setText("");
        // pre-select the newly created item
        list.getSelectionModel().select(newColorizer);
    }

    @FXML
    void moveSelectedItemDown() {
        moveSelectedItem(1);
    }

    @FXML
    void moveSelectedItemUp() {
        moveSelectedItem(-1);
    }

    private void moveSelectedItem(int offset) {
        int selectedItemIndex = list.getSelectionModel().getSelectedIndex();
        Collections.swap(list.getItems(), selectedItemIndex, selectedItemIndex + offset);
    }

    @FXML
    void removeSelectedItem() {
        T selectedColorizer = list.getSelectionModel().getSelectedItem();
        list.getItems().remove(selectedColorizer);
    }

    public EditableListView<T> getList() {
        return list;
    }

    public String getTitle() {
        return titleLabel.getText();
    }

    public StringProperty titleProperty() {
        return titleLabel.textProperty();
    }

    public void setTitle(String title) {
        this.titleLabel.setText(title);
    }

    public void setItemCreator(Function<String, T> createItem) {
        this.createItem = createItem;
    }

    public Predicate<String> getNewItemValidator() {
        return newItemValidator.getValue();
    }

    public Property<Predicate<String>> newItemValidatorProperty() {
        return newItemValidator;
    }

    public void setNewItemValidator(Predicate<String> newItemValidator) {
        this.newItemValidator.setValue(newItemValidator);
    }

    public int getItemInUseIndex() {
        return itemInUseIndex.get();
    }

    public IntegerProperty itemInUseIndexProperty() {
        return itemInUseIndex;
    }

    public void setItemInUseIndex(int itemInUseIndex) {
        this.itemInUseIndex.set(itemInUseIndex);
    }
}
