package org.hildan.fxlog.config;

import java.io.FileNotFoundException;
import java.net.URL;
import java.nio.file.Paths;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import org.hildan.fxlog.coloring.Colorizer;
import org.hildan.fxlog.columns.Columnizer;

import static org.hildan.fxlog.Main.createExceptionDialog;

class ConfigLoader {

    static final String USER_CONFIG_PATH = System.getProperty("user.home") + "/.fxlog/config.json";

    private static final String BUILTIN_RESOURCE = "default_config.json";

    /**
     * Retrieves the user config, or creates it based on the built-in config if it does not exist.
     *
     * @return the Config object corresponding to the current user config
     * @throws JsonSyntaxException
     *         if there is a JSON syntax error in the user config file
     */
    static Config getUserConfig() throws JsonSyntaxException {
        try {
            return Config.readFrom(USER_CONFIG_PATH);
        } catch (FileNotFoundException e) {
            System.out.println("User config not found, falling back to built-in config");
        } catch (JsonIOException e) {
            System.out.println("IO error while reading user config, falling back to built-in config");
        } catch (JsonSyntaxException e) {
            showConfigErrorDialog(e);
        }
        return getBuiltinConfig();
    }

    private static Config getBuiltinConfig() {
        URL builtInConfigUrl = ConfigLoader.class.getResource(BUILTIN_RESOURCE);
        if (builtInConfigUrl == null) {
            System.err.println("Something's wrong: built-in config not found!");
            return getDefaultConfig();
        }
        try {
            return Config.readFrom(builtInConfigUrl.toString());
        } catch (FileNotFoundException e) {
            System.err.println("Impossible error: built-in config URL was not null, but the file was not found");
        } catch (JsonSyntaxException e) {
            System.err.println("Syntax error in built-in config");
        } catch (JsonIOException e) {
            System.err.println("IO error while reading built-in config");
        }
        System.err.println("Falling back to default config");
        return getDefaultConfig();
    }

    private static Config getDefaultConfig() {
        Config config = new Config();
        config.getColorizers().add(Colorizer.WEBLOGIC);
        config.getColumnizers().add(Columnizer.WEBLOGIC);
        return config;
    }

    private static void showConfigErrorDialog(JsonSyntaxException e) {
        e.printStackTrace();
        String title = "Config Load Error";
        String header = "Messing up much?";
        String content = String.format("There is a JSON syntax error in your config file '%s'.\n\n"
                        + "The built-in config was used instead. Unfortunately, your dirty work will be erased.",
                Paths.get(USER_CONFIG_PATH));
        Alert alert = createExceptionDialog(AlertType.WARNING, title, header, content, e);
        alert.showAndWait();
    }
}
