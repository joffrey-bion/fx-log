package org.hildan.fxlog.controllers;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.hildan.fxlog.columns.ColumnDefinition;
import org.hildan.fxlog.columns.Columnizer;
import org.hildan.fxlog.config.Config;
import org.hildan.fxlog.core.LogEntry;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.ListBinding;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * Controller associated to the columnizer customization view.
 */
public class ColumnizersController implements Initializable {

    private Config config;

    @FXML
    private ListView<Columnizer> columnizersList;

    @FXML
    private TextField newColumnizerNameField;

    @FXML
    private SplitPane selectedColumnizerPane;

    @FXML
    private TableView<ColumnDefinition> columnsTable;

    @FXML
    private TableColumn<ColumnDefinition, String> columnLabelColumn;

    @FXML
    private TableColumn<ColumnDefinition, String> capturingGroupColumn;

    @FXML
    private TextField newColumnNameField;

    @FXML
    private TextField newColumnGroupField;

    @FXML
    private ListView<Pattern> patternList;

    @FXML
    private TextField newPatternRegexField;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        config = Config.getInstance();
        initializeColumnizersList();
        initializeSelectedColumnizerPane();
    }

    private void initializeColumnizersList() {
        columnizersList.setItems(config.getColumnizers());
        columnizersList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    }

    private void initializeSelectedColumnizerPane() {
        BooleanBinding isEmpty = Bindings.isEmpty(columnizersList.getSelectionModel().getSelectedItems());
        selectedColumnizerPane.disableProperty().bind(isEmpty);
        initializePatternsList();
        initializeColumnsTable();
    }

    private void initializePatternsList() {
        patternList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        ListBinding<Pattern> patterns = new ListBinding<Pattern>() {
            {
                bind(columnizersList.getSelectionModel().selectedItemProperty());
            }

            @Override
            protected ObservableList<Pattern> computeValue() {
                Columnizer selectedColumnizer = columnizersList.getSelectionModel().getSelectedItem();
                if (selectedColumnizer != null) {
                    return selectedColumnizer.getPatterns();
                } else {
                    return FXCollections.emptyObservableList();
                }
            }
        };
        patternList.itemsProperty().bind(patterns);
    }

    private void initializeColumnsTable() {
        columnsTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        columnLabelColumn.setCellValueFactory(data -> data.getValue().headerLabelProperty());
        capturingGroupColumn.setCellValueFactory(data -> data.getValue().capturingGroupNameProperty());
        ListBinding<ColumnDefinition> columnDefinitions = new ListBinding<ColumnDefinition>() {
            {
                bind(columnizersList.getSelectionModel().selectedItemProperty());
            }

            @Override
            protected ObservableList<ColumnDefinition> computeValue() {
                Columnizer selectedColumnizer = columnizersList.getSelectionModel().getSelectedItem();
                if (selectedColumnizer != null) {
                    return selectedColumnizer.getColumnDefinitions();
                } else {
                    return FXCollections.emptyObservableList();
                }
            }
        };
        columnsTable.itemsProperty().bind(columnDefinitions);
    }

    @FXML
    public void addNewColumnizer() {
        Columnizer newColumnizer = new Columnizer(newColumnizerNameField.getText());
        config.getColumnizers().add(newColumnizer);
        newColumnizerNameField.setText("");
        columnizersList.getSelectionModel().select(newColumnizer);
    }

    @FXML
    public void addNewPattern() {
        PseudoClass errorClass = PseudoClass.getPseudoClass("error");
        try {
            Pattern newRule = Pattern.compile(newPatternRegexField.getText());
            Columnizer selectedColumnizer = columnizersList.getSelectionModel().getSelectedItem();
            selectedColumnizer.getPatterns().add(newRule);
            newPatternRegexField.setText("");
            patternList.getSelectionModel().select(newRule);
            newPatternRegexField.pseudoClassStateChanged(errorClass, false);
        } catch (PatternSyntaxException e) {
            newPatternRegexField.pseudoClassStateChanged(errorClass, true);
        }
    }

    @FXML
    public void addNewColumn() {
        String columnName = newColumnNameField.getText();
        String columnGroup = newColumnGroupField.getText();
        ColumnDefinition newDef = new ColumnDefinition(columnName, columnGroup);
        Columnizer selectedColumnizer = columnizersList.getSelectionModel().getSelectedItem();
        selectedColumnizer.getColumnDefinitions().add(newDef);
        newColumnNameField.setText("");
        newColumnGroupField.setText("");
        columnsTable.getSelectionModel().select(newDef);
    }
}
