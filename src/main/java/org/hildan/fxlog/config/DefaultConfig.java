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

    private static final Filter ERROR_SEVERITY = Filter.findInColumn("severity", "[Ee]rror");

    private static final Filter WARN_SEVERITY = Filter.findInColumn("severity", "[Ww]arn(ing)?");

    private static final Filter INFO_SEVERITY = Filter.findInColumn("severity", "[Ii]nfo");

    private static final Filter DEBUG_SEVERITY = Filter.findInColumn("severity", "[Dd]ebug");

    private static final Filter NOTICE_SEVERITY = Filter.findInColumn("severity", "[Nn]otice");

    private static final Filter STACKTRACE = Filter.findInColumn("severity", "(at \\S.*)|(Caused By.*)");

    private static final Filter DEFAULT = Filter.findInRawLog(".*");

    private static Colorizer severityBasedColorizerLight() {
        StyleRule errorRule = new StyleRule("Error", ERROR_SEVERITY, Color.web("#aa0000ff"), null);
        StyleRule warnRule = new StyleRule("Warn", WARN_SEVERITY, Color.web("#b27200ff"), null);
        StyleRule infoRule = new StyleRule("Info", INFO_SEVERITY, Color.web("#008100ff"), null);
        StyleRule debugRule = new StyleRule("Debug", DEBUG_SEVERITY, Color.web("#0000bbff"), null);
        StyleRule noticeRule = new StyleRule("Notice", NOTICE_SEVERITY, null, null);
        StyleRule stackTraceRule = new StyleRule("Stacktrace", STACKTRACE, Color.web("#990000ff"), null);
        return new Colorizer("Severity (light)",
                FXCollections.observableArrayList(errorRule, warnRule, infoRule, debugRule, noticeRule,
                        stackTraceRule));
    }

    private static Colorizer severityBasedColorizerDark() {
        StyleRule errorRule = new StyleRule("Error", ERROR_SEVERITY, Color.web("#ca1d1dff"), Color.web("#1a1a1aff"));
        StyleRule warnRule = new StyleRule("Warn", WARN_SEVERITY, Color.web("#e6994dff"), Color.web("#1a1a1aff"));
        StyleRule infoRule = new StyleRule("Info", INFO_SEVERITY, Color.web("#10c14bff"), Color.web("#1a1a1aff"));
        StyleRule debugRule = new StyleRule("Debug", DEBUG_SEVERITY, Color.web("#334db3ff"), Color.web("#1a1a1aff"));
        StyleRule noticeRule = new StyleRule("Notice", NOTICE_SEVERITY, Color.web("#ccccccff"), Color.web("#1a1a1aff"));
        StyleRule stackTraceRule =
                new StyleRule("Stacktrace", STACKTRACE, Color.web("#990000ff"), Color.web("#1a1a1aff"));
        StyleRule defaultRule = new StyleRule("Stacktrace", DEFAULT, null, Color.web("#1a1a1aff"));
        return new Colorizer("Severity (dark)",
                FXCollections.observableArrayList(errorRule, warnRule, infoRule, debugRule, noticeRule, stackTraceRule,
                        defaultRule));
    }
}
