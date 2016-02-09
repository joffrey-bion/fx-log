package org.hildan.fxlog.config;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;

import org.hildan.fxlog.coloring.Colorizer;
import org.hildan.fxlog.coloring.StyleRule;
import org.hildan.fxlog.columns.ColumnDefinition;
import org.hildan.fxlog.columns.Columnizer;
import org.hildan.fxlog.filtering.Filter;

/**
 * A generator for the default configuration. It is used as fallback when the built-in config is not available as a
 * resource.
 * <p>
 * It makes it possible for a developer to generate a config programmatically with elements that are not customizable
 * yet via the UI. This is definitely easier than editing the JSON directly.
 */
class DefaultConfig {

    /**
     * Generates the default config programmatically.
     *
     * @return the default config
     */
    static Config generate() {
        Config config = new Config();
        config.getColorizers().add(severityBasedColorizerDark());
        config.getColorizers().add(severityBasedColorizerLight());
        config.getColumnizers().add(weblogicColumnizer());
        config.getColumnizers().add(apacheAccessColumnizer());
        config.getColumnizers().add(apacheErrorColumnizer());
        return config;
    }

    private static Columnizer weblogicColumnizer() {
        ObservableList<ColumnDefinition> columnDefinitions = FXCollections.observableArrayList();
        columnDefinitions.add(new ColumnDefinition("Date/Time", "datetime"));
        columnDefinitions.add(new ColumnDefinition("Severity", "severity"));
        columnDefinitions.add(new ColumnDefinition("Subsystem", "subsystem"));
        columnDefinitions.add(new ColumnDefinition("Machine Name", "machine"));
        columnDefinitions.add(new ColumnDefinition("Server Name", "server"));
        columnDefinitions.add(new ColumnDefinition("Thread ID", "thread"));
        columnDefinitions.add(new ColumnDefinition("User ID", "user"));
        columnDefinitions.add(new ColumnDefinition("Transaction ID", "transaction"));
        columnDefinitions.add(new ColumnDefinition("Diagnostic Context ID", "context"));
        columnDefinitions.add(new ColumnDefinition("Timestamp", "timestamp"));
        columnDefinitions.add(new ColumnDefinition("Message ID", "msgId"));
        columnDefinitions.add(new ColumnDefinition("Class", "class"));
        columnDefinitions.add(new ColumnDefinition("Message", "msg"));
        columnDefinitions.add(new ColumnDefinition("JSessionID", "sessionid"));
        String weblogicLogStart =
                "####<(?<datetime>.*?)> <(?<severity>.*?)> <(?<subsystem>.*?)> <(?<machine>.*?)> <(?<server>.*?)> <(?<thread>.*?)> <(?<user>.*?)> <(?<transaction>.*?)> <(?<context>.*?)> <(?<timestamp>.*?)> <(?<msgId>.*?)>";

        ObservableList<String> regexps = FXCollections.observableArrayList(//
                weblogicLogStart + " <(?<class>.*?)> <(?<msg>.*?);jsessionid=(?<sessionid>.*?)>", // with session ID
                weblogicLogStart + " <(?<class>.*?)> <(?<msg>.*?)>", // without session ID
                weblogicLogStart + " <(?<class>.*?)> <(?<msg>.*?)", // without session ID and continued on next line
                weblogicLogStart + " <(?<msg>.*?);jsessionid=(?<sid>.*?)>", // without class but with session ID
                weblogicLogStart + " <(?<msg>.*?)>", // without class
                weblogicLogStart + " <(?<msg>.*?)", // without class and continued on next line
                "(?<msg>.*);jsessionid=(?<sid>.*?)>", // end of log message on new line with session ID
                "(?<msg>.*)>", // end of log message on new line
                "(?<msg>.*)"); // middle of log message on new line
        return new Columnizer("Weblogic", columnDefinitions, regexps);
    }

    private static Columnizer apacheAccessColumnizer() {
        ObservableList<ColumnDefinition> columnDefinitions = FXCollections.observableArrayList();
        columnDefinitions.add(new ColumnDefinition("Client", "client"));
        columnDefinitions.add(new ColumnDefinition("User", "user"));
        columnDefinitions.add(new ColumnDefinition("Username", "username"));
        columnDefinitions.add(new ColumnDefinition("Date/Time", "datetime"));
        columnDefinitions.add(new ColumnDefinition("Request", "request"));
        columnDefinitions.add(new ColumnDefinition("Resp. Code", "rstatus"));
        columnDefinitions.add(new ColumnDefinition("Resp. Size (bytes)", "rsize"));
        columnDefinitions.add(new ColumnDefinition("Referer", "referer"));
        columnDefinitions.add(new ColumnDefinition("User-Agent", "useragent"));
        String apacheCommon = "(?<client>\\S+) (?<user>\\S+) (?<username>\\S+) \\[(?<datetime>.*?)\\]"
                + " \"(?<request>.*?)\" (?<rstatus>\\S+) (?<rsize>\\S+)";
        String apacheCombined = apacheCommon + " \"(?<referer>.*?)\" \"(?<useragent>.*?)\"";
        ObservableList<String> regexps = FXCollections.observableArrayList(apacheCombined, apacheCommon);
        return new Columnizer("Apache Access Log", columnDefinitions, regexps);
    }

    private static Columnizer apacheErrorColumnizer() {
        ObservableList<ColumnDefinition> columnDefinitions = FXCollections.observableArrayList();
        columnDefinitions.add(new ColumnDefinition("Date/Time", "datetime"));
        columnDefinitions.add(new ColumnDefinition("Severity", "severity"));
        columnDefinitions.add(new ColumnDefinition("Client", "client"));
        columnDefinitions.add(new ColumnDefinition("Message", "msg"));
        String full = "\\[(?<datetime>.*?)\\] \\[(?<severity>.*?)\\] \\[(?<client>.*?)\\] (?<msg>.*)";
        String noClient = "\\[(?<datetime>.*?)\\] \\[(?<severity>.*?)\\] (?<msg>.*)";
        String noClientNoSev = "\\[(?<datetime>.*?)\\] (?<msg>.*)";
        String defaultToMsg = "(?<msg>.*)";
        ObservableList<String> regexps = FXCollections.observableArrayList(full, noClient, noClientNoSev, defaultToMsg);
        return new Columnizer("Apache Error Log", columnDefinitions, regexps);
    }

    private static Colorizer severityBasedColorizerLight() {
        Filter errorFilter = Filter.matchColumn("severity", "[Ee]rror");
        Filter warnFilter = Filter.matchColumn("severity", "[Ww]arn(ing)?");
        Filter infoFilter = Filter.matchColumn("severity", "[Ii]nfo");
        Filter debugFilter = Filter.matchColumn("severity", "[Dd]ebug");
        Filter noticeFilter = Filter.matchColumn("severity", "[Nn]otice");
        StyleRule errorRule = new StyleRule("Error", errorFilter, Color.web("#AA0000"), null);
        StyleRule warnRule = new StyleRule("Warn", warnFilter, Color.web("#AA8800"), null);
        StyleRule infoRule = new StyleRule("Info", infoFilter, Color.web("#00AA00"), null);
        StyleRule debugRule = new StyleRule("Debug", debugFilter, Color.web("#0000BB"), null);
        StyleRule noticeRule = new StyleRule("Notice", noticeFilter, null, null);
        return new Colorizer("Severity (light)",
                FXCollections.observableArrayList(errorRule, warnRule, infoRule, debugRule, noticeRule));
    }

    private static Colorizer severityBasedColorizerDark() {
        Filter errorFilter = Filter.matchColumn("severity", "[Ee]rror");
        Filter warnFilter = Filter.matchColumn("severity", "[Ww]arn(ing)?");
        Filter infoFilter = Filter.matchColumn("severity", "[Ii]nfo");
        Filter debugFilter = Filter.matchColumn("severity", "[Dd]ebug");
        Filter noticeFilter = Filter.matchColumn("severity", "[Nn]otice");
        StyleRule errorRule = new StyleRule("Error", errorFilter, Color.web("#AA0000"), null);
        StyleRule warnRule = new StyleRule("Warn", warnFilter, Color.web("#AA8800"), null);
        StyleRule infoRule = new StyleRule("Info", infoFilter, Color.web("#00AA00"), null);
        StyleRule debugRule = new StyleRule("Debug", debugFilter, Color.web("#0000BB"), null);
        StyleRule noticeRule = new StyleRule("Notice", noticeFilter, null, null);
        return new Colorizer("Severity (dark)",
                FXCollections.observableArrayList(errorRule, warnRule, infoRule, debugRule, noticeRule));
    }
}
