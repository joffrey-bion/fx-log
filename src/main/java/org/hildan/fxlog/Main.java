package org.hildan.fxlog;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import org.hildan.fxlog.config.Config;
import org.hildan.fxlog.controllers.MainController;
import org.hildan.fxlog.errors.ErrorDialog;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        // fail gracefully on any thread with a dialog
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> Platform.runLater(() -> ErrorDialog.uncaughtException(e)));
        Thread.currentThread().setUncaughtExceptionHandler((t, e) -> ErrorDialog.uncaughtException(e));
        try {
            URL url = getClass().getResource("view/main.fxml");
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("dark_theme.css").toExternalForm());
            stage.setTitle("FX Log");
            stage.setScene(scene);
            stage.show();
            MainController controller = loader.getController();
            autoOpenFile(controller);
        } catch (Exception e) {
            ErrorDialog.uncaughtException(e);
        }
    }

    private void autoOpenFile(MainController controller) {
        List<String> params = getParameters().getRaw();
        if (!params.isEmpty()) {
            String filename = params.get(0);
            try {
                controller.startTailingFile(filename);
            } catch (FileNotFoundException e) {
                ErrorDialog.fileNotFound(filename);
            }
        } else {
            List<String> recentFiles = Config.getInstance().getRecentFiles();
            if (!recentFiles.isEmpty()) {
                controller.openRecentFile(recentFiles.get(0));
            }
        }
    }

    @Override
    public void stop() {
        try {
            Config.getInstance().persist();
        } catch (IOException e) {
            ErrorDialog.configWriteException(e);
        } catch (Exception e) {
            ErrorDialog.uncaughtException(e);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
