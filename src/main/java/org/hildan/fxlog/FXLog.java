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

public class FXLog extends Application {

    public static final String BASE_PACKAGE = '/' + FXLog.class.getPackage().getName().replace('.', '/');

    private static final String VIEWS_PATH = BASE_PACKAGE + "/view";

    @Override
    public void start(Stage stage) {
        // fail gracefully on any thread with a dialog
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> Platform.runLater(() -> ErrorDialog.uncaughtException(e)));
        Thread.currentThread().setUncaughtExceptionHandler((t, e) -> ErrorDialog.uncaughtException(e));
        try {
            URL url = getClass().getResource(BASE_PACKAGE + "/view/main.fxml");
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Config.getInstance().getCurrentTheme().apply(scene);
            stage.setTitle("FX Log");
            stage.setScene(scene);
            stage.show();
            MainController controller = loader.getController();
            autoOpenFile(controller);
        } catch (Exception e) {
            ErrorDialog.uncaughtException(e);
        }
    }

    /**
     * Loads the given view.
     *
     * @param viewFilename
     *         the name of the view. It can be a path relative to the views package.
     * @return the Parent returned by the FXMLLoader
     * @throws IOException
     *         if the resource couldn't be loaded for some reason
     */
    public static Parent loadView(String viewFilename) throws IOException {
        URL url = FXLog.class.getResource(VIEWS_PATH + '/' + viewFilename);
        return FXMLLoader.load(url);
    }

    private void autoOpenFile(MainController controller) {
        if (Config.getInstance().getColumnizers().isEmpty()) {
            return; // can't parse logs, better not open a file
        }
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
