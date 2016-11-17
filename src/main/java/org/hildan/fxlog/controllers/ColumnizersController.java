package org.hildan.fxlog.controllers;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.ListBinding;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;

import org.hildan.fx.components.list.BaseEditableListPane;
import org.hildan.fx.components.list.EditableListPane;
import org.hildan.fxlog.columns.ColumnDefinition;
import org.hildan.fxlog.columns.Columnizer;
import org.hildan.fxlog.config.Config;
import org.hildan.fxlog.view.UIUtils;

/**
 * Controller associated to the columnizer customization view.
 */
public class ColumnizersController implements Initializable {

    private Config config;

    @FXML
    private BaseEditableListPane<Columnizer> columnizersPane;

    @FXML
    private SplitPane selectedColumnizerPane;

    @FXML
    private TableView<ColumnDefinition> columnsTable;

    @FXML
    private TableColumn<ColumnDefinition, String> visibleColumn;

    @FXML
    private TableColumn<ColumnDefinition, String> headerColumn;

    @FXML
    private TableColumn<ColumnDefinition, String> capturingGroupColumn;

    @FXML
    private TableColumn<ColumnDefinition, String> descriptionColumn;

    @FXML
    private TextField newColumnHeaderField;

    @FXML
    private TextField newColumnGroupField;

    @FXML
    private EditableListPane<Pattern> patternsPane;

    @FXML
    public Button addColumnButton;

    @FXML
    private Button removeColumnButton;

    @FXML
    private Button moveColumnUpButton;

    @FXML
    private Button moveColumnDownButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        config = Config.getInstance();
        initializeColumnizersPane();
        initializePatternsPane();
    }

    private void initializeColumnizersPane() {
        columnizersPane.setItemFactory(Columnizer::new);
        columnizersPane.setItemDuplicator(Columnizer::new);
        columnizersPane.getList().setItems(config.getColumnizers());
        columnizersPane.itemInUseIndexProperty().bindBidirectional(config.getState().selectedColumnizerIndexProperty());
    }

    private void initializePatternsPane() {
        ReadOnlyObjectProperty<Columnizer> selectedColumnizer = columnizersPane.selectedItemProperty();
        selectedColumnizerPane.disableProperty().bind(selectedColumnizer.isNull());

        ListBinding<Pattern> patterns = UIUtils.selectList(selectedColumnizer, Columnizer::getPatterns);
        Predicate<String> isValidRegex = regex -> createPattern(regex) != null;
        patternsPane.setItemFactory(ColumnizersController::createPattern);
        patternsPane.setItemDuplicator(p -> p); // not updated anyway
        patternsPane.setNewItemValidator(isValidRegex);
        patternsPane.getList().setConverter(ColumnizersController::createPattern, Pattern::pattern, isValidRegex);
        patternsPane.getList().itemsProperty().bind(patterns);

        initializeColumnsTable();
    }

    private static Pattern createPattern(String regex) {
        try {
            return Pattern.compile(regex);
        } catch (PatternSyntaxException e) {
            return null;
        }
    }

    private void initializeColumnsTable() {
        columnsTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        newColumnHeaderField.setMaxWidth(headerColumn.getPrefWidth());
        newColumnGroupField.setMaxWidth(capturingGroupColumn.getPrefWidth());

        ListBinding<ColumnDefinition> columnDefinitions =
                UIUtils.selectList(columnizersPane.selectedItemProperty(), Columnizer::getColumnDefinitions);

        visibleColumn.setCellFactory(
                CheckBoxTableCell.forTableColumn(index -> columnDefinitions.get(index).visibleProperty()));
        visibleColumn.setCellValueFactory(data -> data.getValue().headerLabelProperty());

        headerColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        headerColumn.setCellValueFactory(data -> data.getValue().headerLabelProperty());

        capturingGroupColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        capturingGroupColumn.setCellValueFactory(data -> data.getValue().capturingGroupNamesProperty());

        descriptionColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        descriptionColumn.setCellValueFactory(data -> data.getValue().descriptionProperty());

        columnsTable.itemsProperty().bind(columnDefinitions);

        initializeDeleteButtons();
    }

    private void initializeDeleteButtons() {
        BooleanBinding noColumnDefSelected = UIUtils.noItemIsSelected(columnsTable);
        BooleanBinding firstColumnDefSelected = UIUtils.firstItemIsSelected(columnsTable);
        BooleanBinding lastColumnDefSelected = UIUtils.lastItemIsSelected(columnsTable);
        removeColumnButton.disableProperty().bind(noColumnDefSelected);
        moveColumnUpButton.disableProperty().bind(noColumnDefSelected.or(firstColumnDefSelected));
        moveColumnDownButton.disableProperty().bind(noColumnDefSelected.or(lastColumnDefSelected));
    }

    @FXML
    public void addNewColumnDefinition() {
        String columnName = newColumnHeaderField.getText();
        String columnGroup = newColumnGroupField.getText();
        ColumnDefinition newDef = new ColumnDefinition(columnName, columnGroup);
        Columnizer selectedColumnizer = columnizersPane.getList().getSelectionModel().getSelectedItem();
        selectedColumnizer.getColumnDefinitions().add(newDef);
        newColumnHeaderField.setText("");
        newColumnGroupField.setText("");
        columnsTable.getSelectionModel().select(newDef);
    }

    @FXML
    public void removeSelectedColumnDefinition() {
        ColumnDefinition selectedColumnDef = columnsTable.getSelectionModel().getSelectedItem();
        Columnizer selectedColumnizer = columnizersPane.getList().getSelectionModel().getSelectedItem();
        selectedColumnizer.getColumnDefinitions().remove(selectedColumnDef);
    }

    @FXML
    public void moveSelectedColumnUp() {
        moveColumn(-1);
    }

    @FXML
    public void moveSelectedColumnDown() {
        moveColumn(1);
    }

    private void moveColumn(int offset) {
        ColumnDefinition selectedColumn = columnsTable.getSelectionModel().getSelectedItem();
        Columnizer selectedColumnizer = columnizersPane.getList().getSelectionModel().getSelectedItem();
        List<ColumnDefinition> columnDefinitions = selectedColumnizer.getColumnDefinitions();
        int selectedColumnIndex = columnDefinitions.indexOf(selectedColumn);
        Collections.swap(columnDefinitions, selectedColumnIndex, selectedColumnIndex + offset);
    }
}
