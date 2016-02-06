package org.hildan.fxlog.errors;

import java.io.PrintWriter;
import java.io.StringWriter;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

public class ErrorDialog {

    public static Alert createExceptionDialog(AlertType type, String title, String header, String content,
                                              Throwable e) {
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

    public static void configWriteException(Throwable e) {
        e.printStackTrace();
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
        alert.setContentText(String.format("The file you selected ('%s') was somehow deleted right before I opened it."
                + "Unlucky you.", path));
        alert.showAndWait();
    }

    public static void recentFileNotFound(String path) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle("File Not Found");
        alert.setHeaderText(String.format("The file '%s' does not exist anymore.", path));
        alert.showAndWait();
    }
}
