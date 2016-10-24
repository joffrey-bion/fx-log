package org.hildan.fxlog.controllers;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;

import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory.DoubleSpinnerValueFactory;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.text.Font;

import org.controlsfx.dialog.FontSelectorDialog;
import org.hildan.fxlog.coloring.Style;
import org.hildan.fxlog.config.Config;
import org.hildan.fxlog.config.Preferences;
import org.hildan.fxlog.view.scrollbarmarks.Alignment;

/**
 * Controller associated to the colorizers customization view.
 */
public class PreferencesController implements Initializable {

    private static final double MIN_THICKNESS = 1;

    private static final double MAX_THICKNESS = 20;

    private static final int MIN_NB_OF_LOGS = 1;

    private static final int MAX_NB_OF_LOGS = Integer.MAX_VALUE;

    private static final int MIN_TAILING_DELAY = 1;

    private static final int MAX_TAILING_DELAY = Integer.MAX_VALUE;

    private static final int MIN_LOG_BUFFER_SIZE = 1;

    private static final int MAX_LOG_BUFFER_SIZE = Integer.MAX_VALUE;

    private Preferences prefs;

    @FXML
    private CheckBox reopenLastFile;

    @FXML
    private CheckBox checkForUpdates;

    @FXML
    private CheckBox limitNumberOfLogs;

    @FXML
    private Spinner<Integer> maxNumberOfLogs;

    @FXML
    private CheckBox skipEmptyLogs;

    @FXML
    private Spinner<Integer> tailingDelay;

    @FXML
    private Spinner<Integer> logBufferSize;

    @FXML
    private CheckBox wrapLogsText;

    @FXML
    private TextField logsFontField;

    @FXML
    private ColorPicker searchMatchForegroundColor;

    @FXML
    private ColorPicker searchMatchBackgroundColor;

    @FXML
    private ColorPicker searchMatchMarkColor;

    @FXML
    private Spinner<Double> searchMatchMarkThickness;

    @FXML
    private ChoiceBox<Alignment> searchMatchMarkAlignment;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        prefs = Config.getInstance().getPreferences();
        // FIXME this override is necessary while SearchableText has not been fixed and plugged in
        prefs.setWrapLogsText(false);
        wrapLogsText.setDisable(true);

        reopenLastFile.selectedProperty().bindBidirectional(prefs.openLastFileAtStartupProperty());
        checkForUpdates.selectedProperty().bindBidirectional(prefs.checkForUpdatesProperty());
        skipEmptyLogs.selectedProperty().bindBidirectional(prefs.skipEmptyLogsProperty());
        wrapLogsText.selectedProperty().bindBidirectional(prefs.wrapLogsTextProperty());
        limitNumberOfLogs.selectedProperty().bindBidirectional(prefs.limitNumberOfLogsProperty());

        Style searchHighlightStyle = prefs.getSearchHighlightStyle();
        searchMatchForegroundColor.valueProperty().bindBidirectional(searchHighlightStyle.foregroundColorProperty());
        searchMatchBackgroundColor.valueProperty().bindBidirectional(searchHighlightStyle.backgroundColorProperty());
        searchMatchMarkColor.valueProperty().bindBidirectional(prefs.searchMatchMarkColorProperty());

        configureSearchMatchMarkThicknessSpinner();
        configureSearchMatchMarkAlignmentBox();
        configureLogLimitSpinner();
        configureTailingDelaySpinner();
        configureLogBufferSizeSpinner();
        configureFontSelector();
    }

    private void configureSearchMatchMarkThicknessSpinner() {
        configureDoubleSpinner(searchMatchMarkThickness, prefs.searchMatchMarkThicknessProperty(), MIN_THICKNESS,
                MAX_THICKNESS);
    }

    private void configureSearchMatchMarkAlignmentBox() {
        searchMatchMarkAlignment.getItems().addAll(Alignment.values());
        searchMatchMarkAlignment.valueProperty().bindBidirectional(prefs.searchMatchMarkAlignmentProperty());
    }

    private void configureLogLimitSpinner() {
        maxNumberOfLogs.disableProperty().bind(limitNumberOfLogs.selectedProperty().not());
        configureIntegerSpinner(maxNumberOfLogs, prefs.maxNumberOfLogsProperty(), MIN_NB_OF_LOGS, MAX_NB_OF_LOGS);
    }

    private void configureTailingDelaySpinner() {
        configureIntegerSpinner(tailingDelay, prefs.tailingDelayInMillisProperty(), MIN_TAILING_DELAY,
                MAX_TAILING_DELAY);
    }

    private void configureLogBufferSizeSpinner() {
        configureIntegerSpinner(logBufferSize, prefs.logBufferSizeProperty(), MIN_LOG_BUFFER_SIZE, MAX_LOG_BUFFER_SIZE);
    }

    private void configureIntegerSpinner(Spinner<Integer> spinner, Property<Integer> prop, int min, int max) {
        IntegerSpinnerValueFactory factory = new IntegerSpinnerValueFactory(min, max);
        factory.valueProperty().bindBidirectional(prop);
        spinner.setValueFactory(factory);
    }

    private void configureDoubleSpinner(Spinner<Double> spinner, Property<Double> prop, double min, double max) {
        DoubleSpinnerValueFactory factory = new DoubleSpinnerValueFactory(min, max);
        factory.valueProperty().bindBidirectional(prop);
        spinner.setValueFactory(factory);
    }

    private void configureFontSelector() {
        Callable<String> configFontString = () -> {
            Font font = prefs.getLogsFont();
            return font.getName() + ", " + font.getSize();
        };
        logsFontField.textProperty().bind(Bindings.createStringBinding(configFontString, prefs.logsFontProperty()));
        logsFontField.setDisable(true);
    }

    @FXML
    public void chooseLogsTextFont() {
        FontSelectorDialog fontDialog = new FontSelectorDialog(prefs.getLogsFont());
        fontDialog.showAndWait().ifPresent(prefs::setLogsFont);
    }
}
