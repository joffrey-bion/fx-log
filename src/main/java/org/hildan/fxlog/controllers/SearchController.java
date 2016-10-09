package org.hildan.fxlog.controllers;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;
import java.util.function.Predicate;

import javafx.beans.binding.Binding;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;

import org.controlsfx.control.textfield.CustomTextField;
import org.hildan.fxlog.config.Config;
import org.hildan.fxlog.core.LogEntry;
import org.hildan.fxlog.view.UIUtils;
import org.hildan.fxlog.view.scrollbarmarks.ScrollBarMarker;

public class SearchController implements Initializable {

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

    private final ObservableList<Integer> matches = FXCollections.observableArrayList();

    private Binding<Predicate<LogEntry>> matchTestBinding;

    private ScrollBarMarker scrollBarMarker;

    private final IntegerProperty currentMatchIndex = new SimpleIntegerProperty(-1);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        UIUtils.makeClearable(searchTextField);
    }

    void configure(Config config, ObservableList<? extends LogEntry> logs, TableView<LogEntry> logsTable) {
        this.logs = logs;
        this.logsTable = logsTable;

        matchTestBinding = createMatcherBinding();

        scrollBarMarker = new ScrollBarMarker(logsTable, Orientation.VERTICAL);
        scrollBarMarker.colorProperty().bind(config.getPreferences().searchMatchMarkColorProperty());
        scrollBarMarker.thicknessProperty().bind(config.getPreferences().searchMatchMarkThicknessProperty());
        scrollBarMarker.alignmentProperty().bind(config.getPreferences().searchMatchMarkAlignmentProperty());

        BooleanBinding disableMatchBrowsing = Bindings.createBooleanBinding(matches::isEmpty, matches);
        nextButton.disableProperty().bind(disableMatchBrowsing);
        previousButton.disableProperty().bind(disableMatchBrowsing);

        // feature not ready yet
        regexCheckBox.setDisable(true);

        configureSearchFieldUpdates();
        configureLogsListUpdates();
    }

    private Binding<Predicate<LogEntry>> createMatcherBinding() {
        Binding<Predicate<String>> textMatcherBinding =
                Bindings.createObjectBinding(this::createTextMatcher, matchCaseCheckBox.selectedProperty(),
                        searchTextField.textProperty());

        Callable<Predicate<LogEntry>> createLogMatcher = () -> createLogEntryMatcher(textMatcherBinding.getValue());

        return Bindings.createObjectBinding(createLogMatcher, textMatcherBinding);
    }

    private static Predicate<LogEntry> createLogEntryMatcher(Predicate<String> textMatcher) {
        return log -> textMatcher.test(log.rawLine());
    }

    private Predicate<String> createTextMatcher() {
        boolean matchCase = matchCaseCheckBox.isSelected();
        String searchText = searchTextField.getText();
        if (matchCase) {
            return s -> s.contains(searchText);
        } else {
            String searchTextLowercase = searchText.toLowerCase();
            return s -> s.toLowerCase().contains(searchTextLowercase);
        }
    }

    private void configureSearchFieldUpdates() {
        searchTextField.textProperty().addListener((observable, oldSearch, newSearch) -> {
            if (newSearch.length() >= MIN_QUERY_LENGTH_TO_TRIGGER) {
                recomputeMatches();
            } else {
                matches.clear();
            }
        });

        // to search anyway even below 3 characters
        searchTextField.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                if (matches.size() > 0) {
                    goToNextMatch();
                } else {
                    recomputeMatches();
                }
            }
        });
    }

    private void configureLogsListUpdates() {
        logs.addListener((Change<? extends LogEntry> c) -> {
            while (c.next() && !searchTextField.getText().isEmpty()) {
                if (c.wasAdded()) {
                    addPotentialMatches(c.getAddedSubList(), c.getFrom());
                } else if (c.wasRemoved()) {
                    removeMatches(c.getFrom(), c.getFrom() + c.getRemovedSize());
                }
            }
        });
    }

    TextField getSearchField() {
        return searchTextField;
    }

    void startSearch() {
        searchTextField.requestFocus();
        searchTextField.selectAll();
        matches.addListener(scrollBarMarker);
    }

    private void hideSearch() {
        matches.removeListener(scrollBarMarker);
        scrollBarMarker.clear();
        searchPanel.setVisible(false);
    }

    private void recomputeMatches() {
        matches.clear();
        addPotentialMatches(logs, 0);
        if (matches.size() > 0) {
            currentMatchIndex.set(0);
            goToMatch(0);
        } else {
            currentMatchIndex.set(-1);
        }
    }

    private void addPotentialMatches(List<? extends LogEntry> newLogs, int indexOffset) {
        String textSearch = searchTextField.getText();
        if (textSearch.isEmpty()) {
            return;
        }
        Predicate<LogEntry> matchTest = matchTestBinding.getValue();
        for (int i = 0; i < newLogs.size(); i++) {
            LogEntry log = newLogs.get(i);
            if (matchTest.test(log)) {
                matches.add(indexOffset + i);
            }
        }
    }

    private void removeMatches(int fromIndex, int toIndex) {
        for (int i = fromIndex; i < toIndex; i++) {
            matches.remove((Integer) i);
        }
    }

    @FXML
    void goToNextMatch() {
        currentMatchIndex.set((currentMatchIndex.get() + 1) % matches.size());
        goToMatch(currentMatchIndex.get());
    }

    @FXML
    void goToPreviousMatch() {
        currentMatchIndex.set((currentMatchIndex.get() - 1) % matches.size());
        goToMatch(currentMatchIndex.get());
    }

    private void goToMatch(int matchIndex) {
        UIUtils.scrollTo(logsTable, matches.get(matchIndex));
    }

    @FXML
    void exitSearch() {
        hideSearch();
    }
}
