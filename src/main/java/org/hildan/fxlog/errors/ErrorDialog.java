package org.hildan.fxlog.errors;

import java.io.PrintWriter;
import java.io.StringWriter;

import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

/**
 * Contains handy methods to create a show error dialogs.
 */
public class ErrorDialog {

    private static Alert createExceptionDialog(AlertType type, String title, String header, String content,
                                              Throwable e) {
        // useful to have it in the console output for debugging
        e.printStackTrace();

        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        if (content != null) {
            alert.setContentText(content);
        }

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

    public static void configReadException(String filename, Throwable e) {
        e.printStackTrace();
        String title = "Config Load Error";
        String header = "Messing up much with the config?";
        String content = String.format("There is an error in your config file '%s'.\n\n"
                + "The built-in config was used instead. Unfortunately, your dirty work will be erased.", filename);
        Alert alert = createExceptionDialog(AlertType.WARNING, title, header, content, e);
        alert.showAndWait();
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
        alert.setContentText(String.format("The file you selected %swas somehow deleted right before I opened it."
                + "Unlucky you.", pathMention));
        alert.showAndWait();
    }

    public static void recentFileNotFound(String path) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle("File Not Found");
        alert.setHeaderText(String.format("The file '%s' does not exist anymore.", path));
        alert.showAndWait();
    }
}
