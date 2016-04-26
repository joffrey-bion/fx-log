package org.hildan.fxlog.version.bintray;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;

public class BintrayApi {

    private static final String FXLOG_URL = "https://api.bintray.com/packages/joffrey-bion/applications/fx-log";

    private static final GsonBuilder gsonBuilder =
            new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);

    public static Package getFXLogPackage() {
        try {
            URL bintrayPackageUrl = new URL(FXLOG_URL);
            HttpURLConnection conn = (HttpURLConnection) bintrayPackageUrl.openConnection();
            int resultCode = conn.getResponseCode();
            if (resultCode != 200) {
                throw new RuntimeException("Call to bintray failed, code " + resultCode + " received");
            }
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            return gsonBuilder.create().fromJson(in, Package.class);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Internal error", e);
        } catch (IOException e) {
            throw new RuntimeException("Call to bintray failed", e);
        }
    }
}
