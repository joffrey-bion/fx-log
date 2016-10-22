package org.hildan.fxlog;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

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
import org.hildan.fxlog.themes.Theme;
import org.hildan.fxlog.version.VersionChecker;
import org.hildan.fxlog.view.UIUtils;
import org.jetbrains.annotations.NotNull;

public class FXLog extends Application {

    public static final String APP_NAME = "FX Log";

    public static final String LAST_VERSION_URL = "https://bintray.com/joffrey-bion/applications/fx-log/_latestVersion";

    public static final String GITHUB_URL = "https://github.com/joffrey-bion/fx-log";

    private static final String BASE_PACKAGE = FXLog.class.getPackage().getName();

    private static final String BASE_PACKAGE_PATH = '/' + BASE_PACKAGE.replace('.', '/');

    private static final String APP_ICON_PATH = BASE_PACKAGE_PATH + "/fx-log.png";

    private static final String VIEWS_PATH = BASE_PACKAGE_PATH + "/view";

    @Override
    public void start(Stage stage) {
        // fail gracefully on any thread with a dialog
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> Platform.runLater(() -> ErrorDialog.uncaughtException(e)));
        Thread.currentThread().setUncaughtExceptionHandler((t, e) -> ErrorDialog.uncaughtException(e));
        try {
            URL url = getClass().getResource(VIEWS_PATH + "/main.fxml");
            Locale locale = new Locale("en", "US");
            ResourceBundle resources = ResourceBundle.getBundle(BASE_PACKAGE + ".strings", locale);
            FXMLLoader loader = new FXMLLoader(url, resources);
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Config.getInstance().getState().getCurrentTheme().apply(scene);
            stage.setTitle(APP_NAME);
            InputStream icon = getClass().getResourceAsStream(APP_ICON_PATH);
            stage.getIcons().add(new Image(icon));
            stage.setScene(scene);
            stage.show();

            if (Config.getInstance().getPreferences().isCheckForUpdates()) {
                VersionChecker.checkForUpdates(false);
            }

            MainController controller = loader.getController();
            configureDragAndDrop(scene, controller);
            autoOpenFile(controller);
        } catch (Exception e) {
            ErrorDialog.uncaughtException(e);
        }
    }

    /**
     * Creates a new Stage with the given parameters.
     * <p>
     * This method does not throw an exception if the view is not found, but shows an error dialog instead.
     *
     * @param fxmlFile
     *         the FXML filename (with extension) of the view to load
     * @param title
     *         the title of the new stage
     * @param theme
     *         the CSS theme to use for the new stage
     *
     * @return a new Stage with the given title. If an error occurred while loading the view, the returned stage has no
     * attached Scene. Otherwise, a new Scene is created for the view and attached to returned Stage.
     */
    public static Stage createStage(String fxmlFile, String title, Theme theme) {
        Stage stage = new Stage();
        stage.setTitle(title);
        try {
            URL url = FXLog.class.getResource(VIEWS_PATH + '/' + fxmlFile);
            Locale locale = new Locale("en", "US");
            ResourceBundle resources = ResourceBundle.getBundle(BASE_PACKAGE + ".strings", locale);
            Parent root = FXMLLoader.load(url, resources);
            Scene scene = new Scene(root);
            stage.setScene(scene);
            InputStream icon = UIUtils.class.getResourceAsStream(APP_ICON_PATH);
            stage.getIcons().add(new Image(icon));
            theme.apply(scene);
        } catch (IOException e) {
            ErrorDialog.uncaughtException(e);
        }
        return stage;
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
        } else if (config.getPreferences().getOpenLastFileAtStartup()) {
            List<String> recentFiles = config.getState().getRecentFiles();
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
            System.out.println("Config saved");
            System.exit(0);
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
