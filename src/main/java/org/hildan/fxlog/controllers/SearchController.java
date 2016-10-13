package org.hildan.fxlog.controllers;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;
import java.util.function.Predicate;

import javafx.beans.binding.Binding;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;

import org.controlsfx.control.textfield.CustomTextField;
import org.hildan.fxlog.columns.ColumnDefinition;
import org.hildan.fxlog.columns.Columnizer;
import org.hildan.fxlog.config.Config;
import org.hildan.fxlog.data.LogEntry;
import org.hildan.fxlog.view.UIUtils;
import org.hildan.fxlog.view.scrollbarmarks.ScrollBarMarker;

public class SearchController implements Initializable, ListChangeListener<LogEntry> {

    private static final int MIN_QUERY_LENGTH_TO_TRIGGER = 3;

    @FXML
    private Pane searchPanel;

    @FXML
    private CustomTextField searchTextField;

    @FXML
    private Button previousButton;

    @FXML
    private Button nextButton;

    @FXML
    private CheckBox matchCaseCheckBox;

    @FXML
    private CheckBox regexCheckBox;

    private ObservableList<? extends LogEntry> logs;

    private TableView<LogEntry> logsTable;

    private Binding<ObservableList<ColumnDefinition>> columnDefinitions;

    private final ObservableList<Integer> matchRows = FXCollections.observableArrayList();

    private Binding<Predicate<String>> textMatcherBinding;

    private Binding<Predicate<LogEntry>> logMatcherBinding;

    private ScrollBarMarker scrollBarMarker;

    private final Property<Integer> currentMatchIndex = new SimpleObjectProperty<>(null);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        UIUtils.makeClearable(searchTextField);

        BooleanBinding disableMatchBrowsing = Bindings.createBooleanBinding(matchRows::isEmpty, matchRows);
        nextButton.disableProperty().bind(disableMatchBrowsing);
        previousButton.disableProperty().bind(disableMatchBrowsing);

        // TODO regex mode feature
        regexCheckBox.setDisable(true);
    }

    void configure(Config config, ObservableList<? extends LogEntry> logs, TableView<LogEntry> logsTable,
            ObservableValue<Columnizer> columnizer) {
        this.logs = logs;
        this.logsTable = logsTable;
        this.columnDefinitions = Bindings.createObjectBinding(() -> columnizer.getValue().getColumnDefinitions(),
                columnizer);
        this.textMatcherBinding = createTextMatcherBinding();
        this.logMatcherBinding = createLogMatcherBinding(textMatcherBinding);

        scrollBarMarker = new ScrollBarMarker(logsTable, Orientation.VERTICAL);
        scrollBarMarker.colorProperty().bind(config.getPreferences().searchMatchMarkColorProperty());
        scrollBarMarker.thicknessProperty().bind(config.getPreferences().searchMatchMarkThicknessProperty());
        scrollBarMarker.alignmentProperty().bind(config.getPreferences().searchMatchMarkAlignmentProperty());

        configureSearchFieldUpdates();
    }

    private Binding<Predicate<String>> createTextMatcherBinding() {
        return Bindings.createObjectBinding(() -> {
            return createTextMatcher(matchCaseCheckBox.isSelected(), searchTextField.getText());
        }, matchCaseCheckBox.selectedProperty(), searchTextField.textProperty());
    }

    private static Predicate<String> createTextMatcher(boolean matchCase, String searchText) {
        if (searchText.isEmpty()) {
            return s -> false;
        }
        if (matchCase) {
            return s -> s.contains(searchText);
        } else {
            String searchTextLowercase = searchText.toLowerCase();
            return s -> s.toLowerCase().contains(searchTextLowercase);
        }
    }

    private Binding<Predicate<LogEntry>> createLogMatcherBinding(Binding<Predicate<String>> textMatcherBinding) {
        Callable<Predicate<LogEntry>> createLogMatcher = () -> createLogEntryMatcher(textMatcherBinding.getValue(),
                columnDefinitions.getValue());

        return Bindings.createObjectBinding(createLogMatcher, textMatcherBinding, columnDefinitions);
    }

    private static Predicate<LogEntry> createLogEntryMatcher(Predicate<String> textMatcher,
            List<ColumnDefinition> columnDefinitions) {
        return log -> log.getVisibleColumnValues(columnDefinitions).stream().anyMatch(textMatcher);
    }

    private void configureSearchFieldUpdates() {
        searchTextField.textProperty().addListener((observable, oldSearch, newSearch) -> {
            if (newSearch.length() >= MIN_QUERY_LENGTH_TO_TRIGGER) {
                recomputeMatchesAndGoToFirst();
            } else {
                matchRows.clear();
            }
        });

        // to search anyway even below 3 characters
        searchTextField.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                if (matchRows.size() > 0) {
                    goToNextMatch();
                } else {
                    recomputeMatchesAndGoToFirst();
                }
            }
        });
    }

    @Override
    public void onChanged(Change<? extends LogEntry> c) {
        while (c.next() && !searchTextField.getText().isEmpty()) {
            if (c.wasAdded()) {
                addPotentialMatches(c.getAddedSubList(), c.getFrom());
            } else if (c.wasRemoved()) {
                removeMatches(c.getFrom(), c.getFrom() + c.getRemovedSize());
            }
        }
    }

    ObservableValue<Predicate<String>> textMatcher() {
        return textMatcherBinding;
    }

    void startSearch() {
        // first, pre-mark the former search, then re-add the listener (to avoid concurrent modification)
        scrollBarMarker.markAll(matchRows);
        logs.addListener(this);
        matchRows.addListener(scrollBarMarker);

        // for the ease of use
        searchTextField.requestFocus();
        searchTextField.selectAll();
    }

    private void hideSearch() {
        // first, remove the listener, then clear (to avoid concurrent modification)
        matchRows.removeListener(scrollBarMarker);
        logs.removeListener(this);
        scrollBarMarker.clear();

        searchPanel.setVisible(false);
    }

    private void recomputeMatchesAndGoToFirst() {
        matchRows.clear();
        addPotentialMatches(logs, 0);
        if (matchRows.size() > 0) {
            currentMatchIndex.setValue(0);
            scrollToMatch(0);
        } else {
            currentMatchIndex.setValue(null);
        }
    }

    private void addPotentialMatches(List<? extends LogEntry> newLogs, int indexOffset) {
        String textSearch = searchTextField.getText();
        if (textSearch.isEmpty()) {
            return;
        }
        Predicate<LogEntry> matchTest = logMatcherBinding.getValue();
        for (int i = 0; i < newLogs.size(); i++) {
            LogEntry log = newLogs.get(i);
            if (matchTest.test(log)) {
                matchRows.add(indexOffset + i);
            }
        }
    }

    private void removeMatches(int fromIndex, int toIndex) {
        for (int rowIndex = fromIndex; rowIndex < toIndex; rowIndex++) {
            matchRows.remove((Integer)rowIndex);
        }
    }

    @FXML
    void goToNextMatch() {
        moveCurrentMatch(+1);
        scrollToMatch(currentMatchIndex.getValue());
    }

    @FXML
    void goToPreviousMatch() {
        moveCurrentMatch(-1);
        scrollToMatch(currentMatchIndex.getValue());
    }

    private void moveCurrentMatch(int offset) {
        int newIndex = Math.floorMod(currentMatchIndex.getValue() + offset, matchRows.size());
        currentMatchIndex.setValue(newIndex);
    }

    private void scrollToMatch(int matchIndex) {
        int rowIndexOfMatch = matchRows.get(matchIndex);
        UIUtils.scrollTo(logsTable, rowIndexOfMatch);
        logsTable.getSelectionModel().clearAndSelect(rowIndexOfMatch);
        logsTable.getFocusModel().focus(rowIndexOfMatch);
    }

    @FXML
    void exitSearch() {
        hideSearch();
    }
}
