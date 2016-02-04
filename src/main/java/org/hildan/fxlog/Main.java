package org.hildan.fxlog;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

import org.hildan.fxlog.config.Config;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> Platform.runLater(() -> showUncaughtExceptionDialog(e)));
        Thread.currentThread().setUncaughtExceptionHandler((t, e) -> showUncaughtExceptionDialog(e));
        try {
            URL url = getClass().getResource("view/main.fxml");
            Parent root = FXMLLoader.load(url);
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("dark_theme.css").toExternalForm());
            stage.setTitle("FX Log");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            showUncaughtExceptionDialog(e);
        }
    }

    @Override
    public void stop() {
        try {
            Config.getInstance().persist();
            System.out.println("Configuration saved");
        } catch (IOException e) {
            showConfigWriteExceptionDialog(e);
        } catch (Exception e) {
            showUncaughtExceptionDialog(e);
        }
    }

    private static void showConfigWriteExceptionDialog(Throwable e) {
        e.printStackTrace();
        String title = "Config Save Error";
        String header = "Error when saving your configuration";
        String content = "The next time your start FX Log, you might not find all your settings as you left them.";
        Alert alert = createExceptionDialog(AlertType.ERROR, title, header, content, e);
        alert.showAndWait();
    }

    private static void showUncaughtExceptionDialog(Throwable e) {
        e.printStackTrace();
        String title = "Uncaught Exception";
        String header = "Oops! We have a bug here...";
        String content = "An uncaught exception occurred. To help solve the problem, "
                + "please take a look a the stacktrace below.";
        Alert alert = createExceptionDialog(AlertType.ERROR, title, header, content, e);
        alert.showAndWait();
    }

    public static Alert createExceptionDialog(AlertType type, String title, String header, String content, Throwable e) {
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

    public static void main(String[] args) {
        launch(args);
    }
}
