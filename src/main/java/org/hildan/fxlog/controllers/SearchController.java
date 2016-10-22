package org.hildan.fxlog.controllers;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;
import java.util.function.Predicate;

import javafx.beans.binding.Binding;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;

import org.controlsfx.control.textfield.CustomTextField;
import org.hildan.fxlog.columns.ColumnDefinition;
import org.hildan.fxlog.columns.Columnizer;
import org.hildan.fxlog.config.Config;
import org.hildan.fxlog.data.LogEntry;
import org.hildan.fxlog.search.Search;
import org.hildan.fxlog.view.UIUtils;
import org.hildan.fxlog.view.components.ProportionLabel;
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

    @FXML
    private ProportionLabel<Integer> matchNavigationLabel;

    private final Search search = new Search();

    private ObservableList<? extends LogEntry> logs;

    private TableView<LogEntry> logsTable;

    private Binding<ObservableList<ColumnDefinition>> columnDefinitions;

    private final ObservableList<Integer> matchRows = FXCollections.observableArrayList();

    private Binding<Predicate<LogEntry>> logSearcherBinding;

    private ScrollBarMarker scrollBarMarker;

    private final ObjectProperty<Integer> currentMatchRowIndex = new SimpleObjectProperty<>(null);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        UIUtils.makeClearable(searchTextField);

        BooleanBinding disableMatchBrowsing = Bindings.createBooleanBinding(matchRows::isEmpty, matchRows);
        nextButton.disableProperty().bind(disableMatchBrowsing);
        previousButton.disableProperty().bind(disableMatchBrowsing);

        // TODO regex mode feature
        regexCheckBox.setDisable(true);

        search.textProperty().bind(searchTextField.textProperty());
        search.matchCaseProperty().bind(matchCaseCheckBox.selectedProperty());
        search.regexModeProperty().bind(regexCheckBox.selectedProperty());

        Binding<Integer> matchRowsCount = Bindings.createObjectBinding(matchRows::size, matchRows);
        Binding<Integer> currentMatchRowIndexOneBased = Bindings.createObjectBinding(() -> {
            return currentMatchRowIndex.get() == null ? 0 : currentMatchRowIndex.get() + 1;
        }, currentMatchRowIndex);
        matchNavigationLabel.currentCountProperty().bind(currentMatchRowIndexOneBased);
        matchNavigationLabel.totalCountProperty().bind(matchRowsCount);
        matchNavigationLabel.visibleProperty().bind(currentMatchRowIndex.isNotNull());
    }

    void configure(Config config, ObservableList<? extends LogEntry> logs, TableView<LogEntry> logsTable,
                   ObservableValue<Columnizer> columnizer) {
        this.logs = logs;
        this.logsTable = logsTable;
        this.columnDefinitions =
                Bindings.createObjectBinding(() -> columnizer.getValue().getColumnDefinitions(), columnizer);
        this.logSearcherBinding = createLogSearcherBinding(search.textSearcherProperty());

        scrollBarMarker = new ScrollBarMarker(logsTable, Orientation.VERTICAL);
        scrollBarMarker.colorProperty().bind(config.getPreferences().searchMatchMarkColorProperty());
        scrollBarMarker.thicknessProperty().bind(config.getPreferences().searchMatchMarkThicknessProperty());
        scrollBarMarker.alignmentProperty().bind(config.getPreferences().searchMatchMarkAlignmentProperty());

        configureSearchFieldUpdates();
    }

    private Binding<Predicate<LogEntry>> createLogSearcherBinding(Binding<Predicate<String>> textMatcherBinding) {
        Callable<Predicate<LogEntry>> createLogMatcher = () -> {
            return createLogEntryMatcher(textMatcherBinding.getValue(), columnDefinitions.getValue());
        };

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

    void startSearch() {
        // first, pre-mark the former search, then re-add the listener (to avoid concurrent modification)
        scrollBarMarker.markAll(matchRows);
        logs.addListener(this);
        matchRows.addListener(scrollBarMarker);
        search.setActive(true);

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
        search.setActive(false);
    }

    private void recomputeMatchesAndGoToFirst() {
        matchRows.clear();
        addPotentialMatches(logs, 0);
        if (matchRows.size() > 0) {
            currentMatchRowIndex.set(0);
            scrollToMatch(0);
        } else {
            currentMatchRowIndex.set(null);
        }
    }

    private void addPotentialMatches(List<? extends LogEntry> newLogs, int indexOffset) {
        String textSearch = searchTextField.getText();
        if (textSearch.isEmpty()) {
            return;
        }
        Predicate<LogEntry> matchTest = logSearcherBinding.getValue();
        for (int i = 0; i < newLogs.size(); i++) {
            LogEntry log = newLogs.get(i);
            if (matchTest.test(log)) {
                matchRows.add(indexOffset + i);
            }
        }
    }

    private void removeMatches(int fromIndex, int toIndex) {
        for (int rowIndex = fromIndex; rowIndex < toIndex; rowIndex++) {
            matchRows.remove((Integer) rowIndex);
        }
    }

    @FXML
    void goToNextMatch() {
        moveCurrentMatch(+1);
        scrollToMatch(currentMatchRowIndex.get());
    }

    @FXML
    void goToPreviousMatch() {
        moveCurrentMatch(-1);
        scrollToMatch(currentMatchRowIndex.get());
    }

    private void moveCurrentMatch(int offset) {
        int newIndex = Math.floorMod(currentMatchRowIndex.get() + offset, matchRows.size());
        currentMatchRowIndex.set(newIndex);
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

    public Search getSearch() {
        return search;
    }
}
