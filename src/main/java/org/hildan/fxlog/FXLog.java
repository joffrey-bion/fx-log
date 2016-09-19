package org.hildan.fxlog;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.stage.Stage;

import org.hildan.fxlog.config.Config;
import org.hildan.fxlog.controllers.MainController;
import org.hildan.fxlog.errors.ErrorDialog;
import org.hildan.fxlog.version.VersionChecker;
import org.jetbrains.annotations.NotNull;

public class FXLog extends Application {

    public static final String APP_NAME = "FX Log";

    public static final String LAST_VERSION_URL = "https://bintray.com/joffrey-bion/applications/fx-log/_latestVersion";

    public static final String GITHUB_URL = "https://github.com/joffrey-bion/fx-log";

    public static final String BASE_PACKAGE = '/' + FXLog.class.getPackage().getName().replace('.', '/');

    private static final String VIEWS_PATH = BASE_PACKAGE + "/view";

    @Override
    public void start(Stage stage) {
        // fail gracefully on any thread with a dialog
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> Platform.runLater(() -> ErrorDialog.uncaughtException(e)));
        Thread.currentThread().setUncaughtExceptionHandler((t, e) -> ErrorDialog.uncaughtException(e));
        try {
            URL url = getClass().getResource(BASE_PACKAGE + "/view/main.fxml");
            InputStream icon = getClass().getResourceAsStream(BASE_PACKAGE + "/fx-log.png");
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Config.getInstance().getCurrentTheme().apply(scene);
            stage.setTitle(APP_NAME);
            stage.getIcons().add(new Image(icon));
            stage.setScene(scene);
            stage.setOnCloseRequest(event -> {
                Platform.exit();
                System.exit(0);
            });
            stage.show();

            VersionChecker.checkForUpdates(false);

            MainController controller = loader.getController();
            configureDragAndDrop(scene, controller);
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
     *
     * @return the Parent returned by the FXMLLoader
     * @throws IOException
     *         if the resource couldn't be loaded for some reason
     */
    public static Parent loadView(@NotNull String viewFilename) throws IOException {
        URL url = FXLog.class.getResource(VIEWS_PATH + '/' + viewFilename);
        return FXMLLoader.load(url);
    }

    private void autoOpenFile(@NotNull MainController controller) {
        Config config = Config.getInstance();
        if (config.getColumnizers().isEmpty()) {
            return; // can't parse logs, better not open a file
        }
        List<String> params = getParameters().getRaw();
        if (!params.isEmpty()) {
            String filename = params.get(0);
            try {
                controller.startTailingFile(new File(filename));
            } catch (FileNotFoundException e) {
                ErrorDialog.fileNotFound(filename);
            }
        } else if (config.getOpenLastFileAtStartup()) {
            List<String> recentFiles = config.getRecentFiles();
            if (!recentFiles.isEmpty()) {
                controller.openRecentFile(recentFiles.get(0));
            }
        }
    }

    private static void configureDragAndDrop(@NotNull Scene scene, @NotNull MainController controller) {
        scene.setOnDragOver(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasFiles()) {
                event.acceptTransferModes(TransferMode.ANY);
            } else {
                event.consume();
            }
        });

        // Dropping over surface
        scene.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                success = true;
                for (File file : db.getFiles()) {
                    try {
                        controller.startTailingFile(file);
                    } catch (FileNotFoundException e) {
                        ErrorDialog.fileNotFound(file.getAbsolutePath());
                    }
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });
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
