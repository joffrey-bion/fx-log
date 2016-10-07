package org.hildan.fxlog.controllers;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.IntegerExpression;
import javafx.beans.binding.ListBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.StringConverter;

import org.hildan.fxlog.columns.ColumnDefinition;
import org.hildan.fxlog.columns.Columnizer;
import org.hildan.fxlog.config.Config;
import org.hildan.fxlog.themes.Css;
import org.hildan.fxlog.view.UIUtils;

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
    private TableColumn<ColumnDefinition, String> headerColumn;

    @FXML
    private TableColumn<ColumnDefinition, String> capturingGroupColumn;

    @FXML
    private TextField newColumnHeaderField;

    @FXML
    private TextField newColumnGroupField;

    @FXML
    private ListView<Pattern> patternList;

    @FXML
    private TextField newPatternRegexField;

    @FXML
    public Button addColumnizerButton;

    @FXML
    private Button removeColumnizerButton;

    @FXML
    private Button moveColumnizerUpButton;

    @FXML
    private Button moveColumnizerDownButton;

    @FXML
    public Button addColumnButton;

    @FXML
    private Button removeColumnButton;

    @FXML
    private Button moveColumnUpButton;

    @FXML
    private Button moveColumnDownButton;

    @FXML
    public Button addPatternButton;

    @FXML
    private Button removePatternButton;

    @FXML
    private Button movePatternUpButton;

    @FXML
    private Button movePatternDownButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        config = Config.getInstance();
        initializeColumnizersList();
        initializeSelectedColumnizerPane();
        initializeDeleteButtons();
    }

    private void initializeColumnizersList() {
        columnizersList.setItems(config.getColumnizers());
        columnizersList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        columnizersList.setCellFactory(TextFieldListCell.forListView(new StringConverter<Columnizer>() {
            @Override
            public String toString(Columnizer columnizer) {
                return columnizer.getName();
            }

            @Override
            public Columnizer fromString(String string) {
                // temporary object
                return new Columnizer(string);
            }
        }));
        columnizersList.setOnEditCommit(e -> {
            Columnizer editedColumnizer = columnizersList.getItems().get(e.getIndex());
            editedColumnizer.setName(e.getNewValue().getName());
        });
    }

    private void initializeSelectedColumnizerPane() {
        BooleanBinding noColumnizerSelected = columnizersList.getSelectionModel().selectedItemProperty().isNull();
        selectedColumnizerPane.disableProperty().bind(noColumnizerSelected);
        initializePatternsList();
        initializeColumnsTable();
    }

    private void initializePatternsList() {
        patternList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        patternList.setCellFactory(l -> new TextFieldListCell<Pattern>(new StringConverter<Pattern>() {
            @Override
            public String toString(Pattern pattern) {
                return pattern.toString();
            }

            @Override
            public Pattern fromString(String string) {
                try {
                    return Pattern.compile(string);
                } catch (PatternSyntaxException e) {
                    return null;
                }
            }
        }) {
            @Override
            public void commitEdit(Pattern pattern) {
                if (!isEditing()) {
                    return;
                }
                pseudoClassStateChanged(Css.INVALID, pattern == null);
                if (pattern != null) {
                    // only if the pattern is valid, otherwise we stay in edit state
                    super.commitEdit(pattern);
                }
            }
        });
        patternList.setOnEditCommit(e -> patternList.getItems().set(e.getIndex(), e.getNewValue()));
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
        newColumnHeaderField.setMaxWidth(headerColumn.getPrefWidth());
        newColumnGroupField.setMaxWidth(capturingGroupColumn.getPrefWidth());

        headerColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        headerColumn.setCellValueFactory(data -> data.getValue().headerLabelProperty());

        capturingGroupColumn.setCellFactory(TextFieldTableCell.forTableColumn());
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

    private void initializeDeleteButtons() {
        IntegerExpression currentlyUsedColumnizer = config.getState().selectedColumnizerIndexProperty();
        IntegerExpression selectedColumnizer = columnizersList.getSelectionModel().selectedIndexProperty();
        BooleanBinding selectedColorizerIsUsed = selectedColumnizer.isEqualTo(currentlyUsedColumnizer);
        BooleanBinding noColumnizerSelected = UIUtils.noItemIsSelected(columnizersList);
        BooleanBinding firstColumnizerSelected = UIUtils.firstItemIsSelected(columnizersList);
        BooleanBinding lastColumnizerSelected = UIUtils.lastItemIsSelected(columnizersList);
        removeColumnizerButton.disableProperty().bind(noColumnizerSelected.or(selectedColorizerIsUsed));
        moveColumnizerUpButton.disableProperty().bind(noColumnizerSelected.or(firstColumnizerSelected));
        moveColumnizerDownButton.disableProperty().bind(noColumnizerSelected.or(lastColumnizerSelected));

        BooleanBinding noColumnDefSelected = UIUtils.noItemIsSelected(columnsTable);
        BooleanBinding firstColumnDefSelected = UIUtils.firstItemIsSelected(columnsTable);
        BooleanBinding lastColumnDefSelected = UIUtils.lastItemIsSelected(columnsTable);
        removeColumnButton.disableProperty().bind(noColumnDefSelected);
        moveColumnUpButton.disableProperty().bind(noColumnDefSelected.or(firstColumnDefSelected));
        moveColumnDownButton.disableProperty().bind(noColumnDefSelected.or(lastColumnDefSelected));

        BooleanBinding noPatternSelected = UIUtils.noItemIsSelected(patternList);
        BooleanBinding firstPatternSelected = UIUtils.firstItemIsSelected(patternList);
        BooleanBinding lastPatternSelected = UIUtils.lastItemIsSelected(patternList);
        removePatternButton.disableProperty().bind(noPatternSelected);
        movePatternUpButton.disableProperty().bind(noPatternSelected.or(firstPatternSelected));
        movePatternDownButton.disableProperty().bind(noPatternSelected.or(lastPatternSelected));
    }

    @FXML
    public void addNewColumnizer() {
        Columnizer newColumnizer = new Columnizer(newColumnizerNameField.getText());
        config.getColumnizers().add(newColumnizer);
        newColumnizerNameField.setText("");
        columnizersList.getSelectionModel().select(newColumnizer);
    }

    @FXML
    public void removeSelectedColumnizer() {
        Columnizer selectedColumnizer = columnizersList.getSelectionModel().getSelectedItem();
        config.getColumnizers().remove(selectedColumnizer);
    }

    @FXML
    public void moveSelectedColumnizerUp() {
        moveColumnizer(-1);
    }

    @FXML
    public void moveSelectedColumnizerDown() {
        moveColumnizer(1);
    }

    private void moveColumnizer(int offset) {
        Columnizer selectedColumnizer = columnizersList.getSelectionModel().getSelectedItem();
        List<Columnizer> columnizers = config.getColumnizers();
        int selectedColumnizerIndex = columnizers.indexOf(selectedColumnizer);
        Collections.swap(columnizers, selectedColumnizerIndex, selectedColumnizerIndex + offset);
    }

    @FXML
    public void addNewColumnDefinition() {
        String columnName = newColumnHeaderField.getText();
        String columnGroup = newColumnGroupField.getText();
        ColumnDefinition newDef = new ColumnDefinition(columnName, columnGroup);
        Columnizer selectedColumnizer = columnizersList.getSelectionModel().getSelectedItem();
        selectedColumnizer.getColumnDefinitions().add(newDef);
        newColumnHeaderField.setText("");
        newColumnGroupField.setText("");
        columnsTable.getSelectionModel().select(newDef);
    }

    @FXML
    public void removeSelectedColumnDefinition() {
        ColumnDefinition selectedColumnDef = columnsTable.getSelectionModel().getSelectedItem();
        Columnizer selectedColumnizer = columnizersList.getSelectionModel().getSelectedItem();
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
        Columnizer selectedColumnizer = columnizersList.getSelectionModel().getSelectedItem();
        List<ColumnDefinition> columnDefinitions = selectedColumnizer.getColumnDefinitions();
        int selectedColumnIndex = columnDefinitions.indexOf(selectedColumn);
        Collections.swap(columnDefinitions, selectedColumnIndex, selectedColumnIndex + offset);
    }

    @FXML
    public void addNewPattern() {
        try {
            Pattern newPattern = Pattern.compile(newPatternRegexField.getText());
            Columnizer selectedColumnizer = columnizersList.getSelectionModel().getSelectedItem();
            selectedColumnizer.getPatterns().add(newPattern);
            newPatternRegexField.setText("");
            patternList.getSelectionModel().select(newPattern);
            newPatternRegexField.pseudoClassStateChanged(Css.INVALID, false);
        } catch (PatternSyntaxException e) {
            newPatternRegexField.pseudoClassStateChanged(Css.INVALID, true);
        }
    }

    @FXML
    public void removeSelectedPattern() {
        Pattern selectedPattern = patternList.getSelectionModel().getSelectedItem();
        Columnizer selectedColumnizer = columnizersList.getSelectionModel().getSelectedItem();
        selectedColumnizer.getPatterns().remove(selectedPattern);
    }

    @FXML
    public void moveSelectedPatternUp() {
        movePattern(-1);
    }

    @FXML
    public void moveSelectedPatternDown() {
        movePattern(1);
    }

    private void movePattern(int offset) {
        Pattern selectedPattern = patternList.getSelectionModel().getSelectedItem();
        Columnizer selectedColumnizer = columnizersList.getSelectionModel().getSelectedItem();
        List<Pattern> patterns = selectedColumnizer.getPatterns();
        int selectedPatternIndex = patterns.indexOf(selectedPattern);
        Collections.swap(patterns, selectedPatternIndex, selectedPatternIndex + offset);
    }
}
