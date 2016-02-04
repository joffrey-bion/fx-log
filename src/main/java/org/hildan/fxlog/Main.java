package org.hildan.fxlog;

import java.net.URL;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        URL url = null;
        try {
            url = getClass().getResource("view/main.fxml");
            Parent root = FXMLLoader.load(url);
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("dark_theme.css").toExternalForm());
            stage.setTitle("FX Log");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            System.err.println("Exception on FXMLLoader.load()");
            System.err.println("View url: " + url);
            System.err.println("----------------------------------------\n");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
