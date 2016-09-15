package org.hildan.fxlog.errors;

import java.awt.Desktop;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

/**
 * Contains handy methods to create a show error dialogs.
 */
public class ErrorDialog {

    private static final ButtonType BTN_OVERWRITE = new ButtonType("Overwrite", ButtonData.YES);

    private static final ButtonType BTN_I_WILL_FIX = new ButtonType("I'll fix it", ButtonData.NO);

    private static final ButtonType BTN_LATER = new ButtonType("Later", ButtonData.CANCEL_CLOSE);

    private static Alert createBaseDialog(AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        if (content != null) {
            alert.setContentText(content);
        }
        return alert;
    }

    private static Alert createExceptionDialog(AlertType type, String title, String header, String content,
            Throwable e) {
        // useful to have it in the console output for debugging
        e.printStackTrace();

        Alert alert = createBaseDialog(type, title, header, content);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String exceptionText = sw.toString();

        Label label = new Label("The exception stacktrace was:");
        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);

        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);
        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);

        // Set expandable Exception into the dialog pane.
        alert.getDialogPane().setExpandableContent(expContent);
        return alert;
    }

    private static void showConfigOverwriteDialog(AlertType type, String filename, String title, String header,
            String content, Throwable e) {
        Alert alert;
        if (e == null) {
            alert = createBaseDialog(type, title, header, content);
        } else {
            alert = createExceptionDialog(type, title, header, content, e);
        }
        alert.getButtonTypes().clear();
        alert.getButtonTypes().add(BTN_OVERWRITE);
        alert.getButtonTypes().add(BTN_I_WILL_FIX);
        alert.getButtonTypes().add(BTN_LATER);
        alert.showAndWait().ifPresent(response -> {
            if (response == BTN_I_WILL_FIX) {
                exitAndOpenConfig(filename);
            } else if (response == BTN_LATER) {
                System.exit(0);
            }
        });
    }

    public static void configOutdated(String filename, int actualVersion, int expectedVersion) {
        String title = "Outdated Configuration";
        String header = "The format of your config file is too old for this version of FX-Log.";
        String contentTemplate = "Your config file %s is in version %d, while the current version is %d."
                + " Your config file needs to be deleted and replaced by the new default config,"
                + " or you can exit and change it manually to try and keep your custom stuff.";
        String content = String.format(contentTemplate, filename, actualVersion, expectedVersion);
        showConfigOverwriteDialog(AlertType.WARNING, filename, title, header, content, null);
    }

    public static void configReadException(String filename, Throwable e) {
        e.printStackTrace();
        String title = "Config Load Error";
        String header = "Messing up much with the config?";
        String content = String.format("There was an error while reading your config file '%s'. You can either "
                + "replace your config with the default, or exit and fix the config file yourself", filename);
        showConfigOverwriteDialog(AlertType.WARNING, filename, title, header, content, e);
    }

    private static void exitAndOpenConfig(String configFilename) {
        System.out.println("Opening config file " + configFilename);
        File configFileToOpen = new File(configFilename);
        if (configFileToOpen.exists()) {
            try {
                Desktop.getDesktop().open(configFileToOpen);
            } catch (Exception e1) {
                System.err.println("Failed to open config file " + configFilename);
            }
        }
        System.exit(0);
    }

    public static void configWriteException(Throwable e) {
        String title = "Config Save Error";
        String header = "Error when saving your configuration";
        String content = "The next time your start FX Log, you might not find all your settings as you left them.";
        Alert alert = createExceptionDialog(AlertType.ERROR, title, header, content, e);
        alert.showAndWait();
    }

    public static void uncaughtException(Throwable e) {
        e.printStackTrace();
        String title = "Uncaught Exception";
        String header = "Oops! We have a bug here...";
        String content = "An uncaught exception occurred. To help solve the problem, "
                + "please take a look a the stacktrace below.";
        Alert alert = createExceptionDialog(AlertType.ERROR, title, header, content, e);
        alert.showAndWait();
    }

    public static void fileNotFound(String path) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("File Not Found");
        alert.setHeaderText(String.format("The file '%s' does not exist.", path));
        alert.showAndWait();
    }

    public static void selectedFileNotFound(String path) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("File Not Found");
        alert.setHeaderText("Your file has disappeared!");
        String pathMention = path != null ? String.format("('%s') ", path) : "";
        alert.setContentText(
                String.format("The file you selected %swas somehow deleted right before I opened it. Unlucky you.",
                        pathMention));
        alert.showAndWait();
    }

    public static void recentFileNotFound(String path) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle("File Not Found");
        alert.setHeaderText(String.format("The file '%s' does not exist anymore.", path));
        alert.showAndWait();
    }
}
