package org.hildan.fxlog.controllers;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.control.SpinnerValueFactory.DoubleSpinnerValueFactory;
import javafx.scene.text.Font;

import org.controlsfx.dialog.FontSelectorDialog;
import org.hildan.fxlog.coloring.Style;
import org.hildan.fxlog.config.Config;
import org.hildan.fxlog.config.Preferences;

/**
 * Controller associated to the colorizers customization view.
 */
public class PreferencesController implements Initializable {

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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        prefs = Config.getInstance().getPreferences();
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
        configureLogLimitSpinner();
        configureTailingDelaySpinner();
        configureFontSelector();
    }

    private void configureSearchMatchMarkThicknessSpinner() {
        DoubleSpinnerValueFactory factory = new DoubleSpinnerValueFactory(1, 20);
        factory.valueProperty().bindBidirectional(prefs.searchMatchMarkThicknessProperty());
        searchMatchMarkThickness.setValueFactory(factory);
    }

    private void configureLogLimitSpinner() {
        maxNumberOfLogs.disableProperty().bind(limitNumberOfLogs.selectedProperty().not());
        IntegerSpinnerValueFactory factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Integer.MAX_VALUE);
        factory.valueProperty().bindBidirectional(prefs.maxNumberOfLogsProperty());
        maxNumberOfLogs.setValueFactory(factory);
    }

    private void configureTailingDelaySpinner() {
        IntegerSpinnerValueFactory factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(50, Integer.MAX_VALUE);
        factory.valueProperty().bindBidirectional(prefs.tailingDelayInMillisProperty());
        tailingDelay.setValueFactory(factory);
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
