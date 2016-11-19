package org.hildan.fx.components.list;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.Predicate;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.IntegerExpression;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
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
    private Button duplicateButton;

    @FXML
    private Button removeButton;

    @FXML
    private Button moveUpButton;

    @FXML
    private Button moveDownButton;

    private ObjectProperty<Function<String, T>> itemFactory = new SimpleObjectProperty<>();

    private ObjectProperty<Function<T, T>> itemDuplicator = new SimpleObjectProperty<>();

    private BooleanProperty itemsReorderable = new SimpleBooleanProperty(true);

    private BooleanProperty itemsDeletable = new SimpleBooleanProperty(true);

    private final ObjectProperty<Predicate<String>> newItemValidator = new SimpleObjectProperty<>();

    // -1 because no item is in use by default
    private final IntegerProperty itemInUseIndex = new SimpleIntegerProperty(-1);

    public EditableListPane() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("editable_list_pane.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        // for proper loading in SceneBuilder (https://bugs.openjdk.java.net/browse/JDK-8094907)
        fxmlLoader.setClassLoader(getClass().getClassLoader());

        try {
            fxmlLoader.load();
            configureNewItemTextFieldErrorCheck();
            configureButtonsActivation();
            configureButtonsVisibility();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    private void configureNewItemTextFieldErrorCheck() {
        newItemTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            // we don't want the text field to be marked invalid when the user doesn't intend to add an item
            boolean isAcceptable = newValue.isEmpty() || isValidItemText(newValue);
            newItemTextField.pseudoClassStateChanged(Css.INVALID, !isAcceptable);
        });
    }

    private void configureButtonsActivation() {
        BooleanBinding noItemSelected = list.getSelectionModel().selectedItemProperty().isNull();
        IntegerExpression selectedItemIndex = list.getSelectionModel().selectedIndexProperty();
        BooleanBinding selectedItemIsUsed = selectedItemIndex.isEqualTo(itemInUseIndex);
        BooleanBinding selectedItemIsFirst = list.getSelectionModel().selectedIndexProperty().isEqualTo(0);
        IntegerExpression lastIndex =
                Bindings.createIntegerBinding(() -> list.getItems().size() - 1, list.itemsProperty());
        BooleanBinding selectedItemIsLast = list.getSelectionModel().selectedIndexProperty().isEqualTo(lastIndex);

        addButton.disableProperty().bind(isNewItemTextValid().not());
        duplicateButton.disableProperty().bind(noItemSelected);
        removeButton.disableProperty().bind(noItemSelected.or(selectedItemIsUsed));
        moveUpButton.disableProperty().bind(noItemSelected.or(selectedItemIsFirst));
        moveDownButton.disableProperty().bind(noItemSelected.or(selectedItemIsLast));
    }

    private void configureButtonsVisibility() {
        addButton.visibleProperty().bind(itemFactory.isNotNull());
        duplicateButton.visibleProperty().bind(itemDuplicator.isNotNull());
        removeButton.visibleProperty().bind(itemsDeletable);
        moveUpButton.visibleProperty().bind(itemsReorderable);
        moveDownButton.visibleProperty().bind(itemsReorderable);

        // prevents taking space when invisible
        addButton.managedProperty().bind(addButton.visibleProperty());
        duplicateButton.managedProperty().bind(duplicateButton.visibleProperty());
        removeButton.managedProperty().bind(removeButton.visibleProperty());
        moveUpButton.managedProperty().bind(moveUpButton.visibleProperty());
        moveDownButton.managedProperty().bind(moveDownButton.visibleProperty());
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
        T newItem = itemFactory.get().apply(newItemTextField.getText());
        list.getItems().add(newItem);
        // reset the field after add
        newItemTextField.setText("");
        // pre-select the newly created item (use the index to avoid selecting another item equal to the created one)
        list.getSelectionModel().select(list.getItems().size() - 1);
    }

    @FXML
    void duplicateItem() {
        int selectedItemIndex = list.getSelectionModel().getSelectedIndex();
        T selectedItem = list.getSelectionModel().getSelectedItem();
        T newItem = itemDuplicator.get().apply(selectedItem);
        list.getItems().add(selectedItemIndex + 1, newItem);
        // pre-select the newly created item (use the index to avoid selecting another item equal to the created one)
        list.getSelectionModel().select(selectedItemIndex + 1);
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
        // use the index, not the item, to avoid deleting another item equal to the selected one
        int selectedItemIndex = list.getSelectionModel().getSelectedIndex();
        list.getItems().remove(selectedItemIndex);
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

    public Function<String, T> getItemFactory() {
        return itemFactory.get();
    }

    public ObjectProperty<Function<String, T>> itemFactoryProperty() {
        return itemFactory;
    }

    public void setItemFactory(Function<String, T> itemFactory) {
        this.itemFactory.set(itemFactory);
    }

    public Function<T, T> getItemDuplicator() {
        return itemDuplicator.get();
    }

    public ObjectProperty<Function<T, T>> itemDuplicatorProperty() {
        return itemDuplicator;
    }

    public void setItemDuplicator(Function<T, T> itemDuplicator) {
        this.itemDuplicator.set(itemDuplicator);
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

    public boolean isItemsReorderable() {
        return itemsReorderable.get();
    }

    public BooleanProperty itemsReorderableProperty() {
        return itemsReorderable;
    }

    public void setItemsReorderable(boolean itemsReorderable) {
        this.itemsReorderable.set(itemsReorderable);
    }

    public boolean isItemsDeletable() {
        return itemsDeletable.get();
    }

    public BooleanProperty itemsDeletableProperty() {
        return itemsDeletable;
    }

    public void setItemsDeletable(boolean itemsDeletable) {
        this.itemsDeletable.set(itemsDeletable);
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
