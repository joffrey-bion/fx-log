package org.hildan.fxlog.version;

import java.awt.Desktop;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;

import org.hildan.fxlog.errors.ErrorDialog;
import org.hildan.fxlog.version.bintray.BintrayApi;

/**
 * Provides version-related utilities.
 */
public class VersionChecker {

    private static final String VERSION_PROPERTIES = "version.properties";

    private static final ButtonType BTN_UPDATE = new ButtonType("Download", ButtonData.YES);

    private static final ButtonType BTN_NOT_TODAY = new ButtonType("Not today", ButtonData.NO);

    private static final ButtonType BTN_CHANGELOG = new ButtonType("Changelog...", ButtonData.HELP_2);

    /**
     * Compares the current version of FX Log to the latest version on bintray. If updates are available, a dialog is
     * shown, offering to update. If the application is up-to-date, a dialog may or may not be shown to say so.
     *
     * @param showUpToDateDialog
     *         if FX Log is up-to-date, a dialog is shown only if this parameter is true
     */
    public static void checkForUpdates(boolean showUpToDateDialog) {
        ExecutorService service = Executors.newSingleThreadExecutor();
        service.submit(() -> {
            String current = getCurrentVersion();
            String latest = getLatestVersion();
            if (!current.equals(latest)) {
                Platform.runLater(() -> showUpdateAvailableDialog(current, latest));
            } else if (showUpToDateDialog) {
                Platform.runLater(VersionChecker::showUpToDateDialog);
            }
        });
    }

    /**
     * Returns the current version of FX Log. The version is taken from the resource version.properties, which in turn
     * is fed by gradle at build time.
     *
     * @return the current version of FX Log, as a SemVer string
     */
    private static String getCurrentVersion() {
        Properties prop = new Properties();
        try {
            InputStream is = VersionChecker.class.getResourceAsStream(VERSION_PROPERTIES);
            if (is == null) {
                throw new RuntimeException("Couldn't find " + VERSION_PROPERTIES);
            }
            prop.load(is);
            return prop.getProperty("version");
        } catch (IOException e) {
            throw new RuntimeException("Couldn't read " + VERSION_PROPERTIES, e);
        }
    }

    /**
     * Gets the latest version of FX Log from bintray. This makes an HTTP call to the bintray REST API.
     *
     * @return the latest version of FX Log, as a SemVer string
     */
    private static String getLatestVersion() {
        return BintrayApi.getFXLogPackage().getLatestVersion();
    }

    private static void showUpToDateDialog() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Up-to-date");
        alert.setHeaderText("You have the latest version of FX Log.");
        alert.setContentText("Feel free to contribute if you can't wait for updates!");
        alert.showAndWait();
    }

    private static void showUpdateAvailableDialog(String currentVersion, String latestVersion) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Updates Available");
        alert.setHeaderText("A new version of FX-Log is available.");
        alert.setContentText("You are using version " + currentVersion + " of FX-Log, while version " + latestVersion
                + " is available. If you are not sure whether to update, please have a look at the changelog.");
        alert.getButtonTypes().clear();
        alert.getButtonTypes().addAll(BTN_UPDATE, BTN_NOT_TODAY, BTN_CHANGELOG);

        // prevent Changelog button from closing the dialog
        Button btnChangelog = (Button) alert.getDialogPane().lookupButton(BTN_CHANGELOG);
        btnChangelog.addEventFilter(ActionEvent.ACTION, event -> {
            openChangelog();
            event.consume();
        });

        alert.showAndWait().ifPresent(response -> {
            if (response == BTN_UPDATE) {
                openBintray();
                System.exit(0);
            }
        });
    }

    private static void openBintray() {
        try {
            Desktop.getDesktop().browse(new URI("https://bintray.com/joffrey-bion/applications/fx-log/_latestVersion"));
        } catch (IOException | URISyntaxException e) {
            ErrorDialog.uncaughtException(e);
        }
    }

    private static void openChangelog() {
        try {
            Desktop.getDesktop().browse(new URI("https://github.com/joffrey-bion/fx-log/blob/master/CHANGELOG.md"));
        } catch (IOException | URISyntaxException e) {
            ErrorDialog.uncaughtException(e);
        }
    }
}
