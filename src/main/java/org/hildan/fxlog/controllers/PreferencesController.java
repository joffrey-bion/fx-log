package org.hildan.fxlog.controllers;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.text.Font;

import org.controlsfx.dialog.FontSelectorDialog;
import org.hildan.fxlog.config.Config;

/**
 * Controller associated to the colorizers customization view.
 */
public class PreferencesController implements Initializable {

    private Config config;

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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        config = Config.getInstance();
        reopenLastFile.selectedProperty().bindBidirectional(config.openLastFileAtStartupProperty());
        checkForUpdates.selectedProperty().bindBidirectional(config.checkForUpdatesProperty());
        skipEmptyLogs.selectedProperty().bindBidirectional(config.skipEmptyLogsProperty());
        wrapLogsText.selectedProperty().bindBidirectional(config.wrapLogsTextProperty());
        limitNumberOfLogs.selectedProperty().bindBidirectional(config.limitNumberOfLogsProperty());
        configureLogLimitSpinner();
        configureTailingDelaySpinner();
        configureFontSelector();
    }

    private void configureLogLimitSpinner() {
        maxNumberOfLogs.disableProperty().bind(limitNumberOfLogs.selectedProperty().not());
        IntegerSpinnerValueFactory factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Integer.MAX_VALUE);
        factory.valueProperty().bindBidirectional(config.maxNumberOfLogsProperty());
        maxNumberOfLogs.setValueFactory(factory);
    }

    private void configureTailingDelaySpinner() {
        IntegerSpinnerValueFactory factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(50, Integer.MAX_VALUE);
        factory.valueProperty().bindBidirectional(config.tailingDelayInMillisProperty());
        tailingDelay.setValueFactory(factory);
    }

    private void configureFontSelector() {
        Callable<String> configFontString = () -> {
            Font font = config.getLogsFont();
            return font.getName() + ", " + font.getSize();
        };
        logsFontField.textProperty().bind(Bindings.createStringBinding(configFontString, config.logsFontProperty()));
        logsFontField.setDisable(true);
    }

    @FXML
    public void chooseLogsTextFont() {
        FontSelectorDialog fontDialog = new FontSelectorDialog(config.getLogsFont());
        fontDialog.showAndWait().ifPresent(config::setLogsFont);
    }
}
