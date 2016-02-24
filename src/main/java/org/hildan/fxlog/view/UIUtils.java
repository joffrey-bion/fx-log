package org.hildan.fxlog.view;

import java.io.IOException;

import org.hildan.fxlog.FXLog;
import org.hildan.fxlog.errors.ErrorDialog;
import org.hildan.fxlog.themes.Theme;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class UIUtils {

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
            Parent root = FXLog.loadView(fxmlFile);
            Scene scene = new Scene(root);
            stage.setScene(scene);
            theme.apply(scene);
        } catch (IOException e) {
            ErrorDialog.uncaughtException(e);
        }
        return stage;
    }

}
