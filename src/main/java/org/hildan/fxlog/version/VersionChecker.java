package org.hildan.fxlog.version;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.hildan.fxlog.version.bintray.BintrayApi;

/**
 * Provides version-related utilities.
 */
public class VersionChecker {

    private static final String VERSION_PROPERTIES = "version.properties";

    /**
     * Returns the current version of FX Log. The version is taken from the resource version.properties, which in
     * turn is fed by gradle at build time.
     *
     * @return the current version of FX Log, as a SemVer string
     */
    public static String getCurrentVersion() {
        Properties prop = new Properties();
        try {
            InputStream is = VersionChecker.class.getResourceAsStream(VERSION_PROPERTIES);
            if (is == null) {
                throw new RuntimeException("Couldn't find " + VERSION_PROPERTIES);
            }
            prop.load(is);
            return prop.getProperty("version");
        } catch (IOException e) {
            throw new RuntimeException("Couldn't read " + VERSION_PROPERTIES, e);
        }
    }

    /**
     * Gets the latest version of FX Log from bintray. This makes an HTTP call to the bintray REST API.
     *
     * @return the latest version of FX Log, as a SemVer string
     */
    public static String getLatestVersion() {
        return BintrayApi.getFXLogPackage().getLatestVersion();
    }

    /**
     * Checks for updates.
     *
     * @return true if there is an update available, false if the current version is the latest
     */
    public static boolean isUpdateAvailable() {
        String current = getCurrentVersion();
        String latest = getLatestVersion();
        // no need for a smart comparison, as users can't have a future version
        return !current.equals(latest);
    }
}
