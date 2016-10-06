package org.hildan.fxlog.config;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import org.hildan.fxlog.errors.ErrorDialog;

/**
 * The ConfigLoader is responsible for reading and writing configuration files.
 */
class ConfigLoader {

    /**
     * The default location of the user's config.
     */
    private static final String USER_CONFIG_PATH =
            Paths.get(System.getProperty("user.home") + "/.fxlog/config.json").toAbsolutePath().toString();

    /**
     * The resource path of the default config.
     */
    private static final String BUILTIN_RESOURCE = "default_config.json";

    private static final class ConfigVersion {
        @SuppressWarnings("unused")
        private int version;
    }

    /**
     * Retrieves the user config, or the built-in config if the user config does not exist.
     *
     * @return the Config object corresponding to the current user config (or the default config)
     * @throws JsonSyntaxException
     *         if there is a JSON syntax error in the user config file
     */
    static Config getUserConfig() throws JsonSyntaxException {
        try {
            int version = readConfigVersionFrom(new FileReader(USER_CONFIG_PATH));
            if (version == Config.FORMAT_VERSION) {
                return readConfigFrom(new FileReader(USER_CONFIG_PATH));
            } else if (version < Config.FORMAT_VERSION) {
                ErrorDialog.configOutdated(USER_CONFIG_PATH, version, Config.FORMAT_VERSION);
            } else {
                ErrorDialog.configTooRecent(USER_CONFIG_PATH, version, Config.FORMAT_VERSION);
            }
        } catch (FileNotFoundException e) {
            System.out.println("User config not found, falling back to built-in config");
        } catch (JsonSyntaxException e) {
            // the user will decide whether to stop here or continue with default (and overwrite)
            ErrorDialog.configReadException(USER_CONFIG_PATH, e);
        }
        return getBuiltinConfig();
    }

    private static Config getBuiltinConfig() {
        InputStream jsonConfigStream = ConfigLoader.class.getResourceAsStream(BUILTIN_RESOURCE);
        if (jsonConfigStream == null) {
            System.err.println("Something's wrong: built-in config not found!");
            return DefaultConfig.generate();
        }
        try {
            int version = readConfigVersionFrom(new InputStreamReader(jsonConfigStream));
            if (version == Config.FORMAT_VERSION) {
                jsonConfigStream = ConfigLoader.class.getResourceAsStream(BUILTIN_RESOURCE);
                return readConfigFrom(new InputStreamReader(jsonConfigStream));
            } else if (version < Config.FORMAT_VERSION) {
                ErrorDialog.configOutdated(BUILTIN_RESOURCE, version, Config.FORMAT_VERSION);
            } else {
                ErrorDialog.configTooRecent(BUILTIN_RESOURCE, version, Config.FORMAT_VERSION);
            }
        } catch (JsonSyntaxException e) {
            System.err.println("Syntax error in built-in config. SHAME.");
        } catch (JsonIOException e) {
            System.err.println("IO error while reading built-in config");
        }
        System.err.println("Falling back to default config");
        return DefaultConfig.generate();
    }

    static void writeUserConfig(Config config) throws IOException {
        writeConfigTo(USER_CONFIG_PATH, config);
    }

    private static int readConfigVersionFrom(Reader source) throws JsonIOException, JsonSyntaxException {
        ConfigVersion configVersion = new GsonBuilder().create().fromJson(source, ConfigVersion.class);
        return configVersion.version;
    }

    private static Config readConfigFrom(Reader source) throws JsonIOException, JsonSyntaxException {
        return ConfigGson.create().fromJson(source, Config.class);
    }

    private static void writeConfigTo(String filename, Config config) throws IOException {
        Path filePath = Paths.get(filename);
        Path parentDir = filePath.getParent();
        if (parentDir != null) {
            Files.createDirectories(parentDir);
        }
        // html escaping disabled to see <> in regexps in the JSON
        Gson gson = ConfigGson.builder().disableHtmlEscaping().setPrettyPrinting().create();
        List<String> lines = Collections.singletonList(gson.toJson(config));
        Files.write(filePath, lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING);
    }
}
