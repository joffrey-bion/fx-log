package org.hildan.fxlog.controllers;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.text.Font;

import org.controlsfx.dialog.FontSelectorDialog;
import org.hildan.fxlog.config.Config;

/**
 * Controller associated to the colorizers customization view.
 */
public class PreferencesController implements Initializable {

    private Config config;

    @FXML
    private CheckBox reopenLastFileCheckbox;

    @FXML
    private CheckBox skipEmptyLogsCheckbox;

    @FXML
    private TextField logsFontField;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        config = Config.getInstance();
        reopenLastFileCheckbox.selectedProperty().bindBidirectional(config.openLastFileAtStartupProperty());
        skipEmptyLogsCheckbox.selectedProperty().bindBidirectional(config.skipEmptyLogsProperty());

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
