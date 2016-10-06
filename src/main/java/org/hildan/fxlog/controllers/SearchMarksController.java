package org.hildan.fxlog.controllers;

import java.util.List;

import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;

import org.hildan.fxlog.config.Config;
import org.hildan.fxlog.core.LogEntry;
import org.hildan.fxlog.view.scrollbarmarks.ScrollBarMarker;

class SearchMarksController {

    private final ObservableList<? extends LogEntry> logs;

    private final TextField searchField;

    private final ScrollBarMarker scrollBarMarker;

    SearchMarksController(Config config, ObservableList<? extends LogEntry> logs, TableView<LogEntry> logsTable,
                                 TextField searchField) {
        this.logs = logs;
        this.searchField = searchField;

        scrollBarMarker = new ScrollBarMarker(logsTable, Orientation.VERTICAL);
        scrollBarMarker.colorProperty().bind(config.getPreferences().searchMatchMarkColorProperty());
        scrollBarMarker.thicknessProperty().bind(config.getPreferences().searchMatchMarkThicknessProperty());
        scrollBarMarker.alignmentProperty().bind(config.getPreferences().searchMatchMarkAlignmentProperty());

        configureSearchFieldUpdates();
        configureLogsListUpdates();
    }

    private void configureSearchFieldUpdates() {
        searchField.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                refreshSearch();
            }
        });
        searchField.textProperty().addListener((observable, oldSearch, newSearch) -> {
            if (newSearch.length() > 2) {
                refreshSearch();
            } else {
                scrollBarMarker.clear();
            }
        });
    }

    private void configureLogsListUpdates() {
        logs.addListener((Change<? extends LogEntry> c) -> {
            while (c.next() && !searchField.getText().isEmpty()) {
                if (c.wasAdded()) {
                    updateSearch(c.getAddedSubList(), c.getFrom());
                } else if (c.wasRemoved()) {
                    clearSearch(c.getFrom(), c.getFrom() + c.getRemovedSize());
                }
            }
        });
    }

    private void refreshSearch() {
        scrollBarMarker.clear();
        updateSearch(logs, 0);
    }

    private void updateSearch(List<? extends LogEntry> logs, int fromIndex) {
        String textSearch = searchField.getText();
        for (int i = fromIndex; i < fromIndex + logs.size(); i++) {
            LogEntry log = logs.get(i - fromIndex);
            if (!textSearch.isEmpty() && log.rawLine().contains(textSearch)) {
                scrollBarMarker.mark(i);
            }
        }
    }

    private void clearSearch(int fromIndex, int toIndex) {
        for (int i = fromIndex; i < toIndex; i++) {
            scrollBarMarker.unmark(i);
        }
    }
}
