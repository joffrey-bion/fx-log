package org.hildan.fxlog.config;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.hildan.fxlog.coloring.Colorizer;
import org.hildan.fxlog.coloring.Style;
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
        config.getColumnizers().add(weblogicEasyTraceColumnizer());
        config.getColumnizers().add(apacheAccessColumnizer());
        config.getColumnizers().add(apacheErrorColumnizer());
        config.getColumnizers().add(amadeusInputLog());

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

        ObservableList<String> regexps = FXCollections.observableArrayList(//
                "####<(?<datetime>[^>]*?)> ?<(?<severity>[^>]*?)> ?<(?<subsystem>[^>]*?)> ?<(?<machine>[^>]*?)> ?"
                        + "<(?<server>[^>]*?)> ?<(?<thread>[^>]*?)> ?<(?<user>.*?)> ?<(?<transaction>[^>]*?)> ?"
                        + "<(?<context>[^>]*?)> ?<(?<timestamp>[^>]*?)> ?<(?<msgId>[^>]*?)>( ?<(?<class>[^>]*?)>)? ?"
                        + "<(?<msg>.*?)(;jsessionid=(?<sessionid>.*))?>?", // log beginning
                "(?<msg>[^>]*)(;jsessionid=(?<sid>.*?))?>", // end of message
                "(?<msg>.*)"); // middle of log message on new line
        return new Columnizer("Weblogic Server Log", columnDefinitions, regexps);
    }

    private static Columnizer weblogicEasyTraceColumnizer() {
        ObservableList<ColumnDefinition> columnDefinitions = FXCollections.observableArrayList();
        columnDefinitions.add(new ColumnDefinition("Date/Time", "datetime"));
        columnDefinitions.add(new ColumnDefinition("Severity", "severity"));
        columnDefinitions.add(new ColumnDefinition("Subsystem", "subsystem"));
        columnDefinitions.add(new ColumnDefinition("Machine Name", "machine"));
        columnDefinitions.add(new ColumnDefinition("Server Name", "server"));
        columnDefinitions.add(new ColumnDefinition("Thread ID", "thread"));
        columnDefinitions.add(new ColumnDefinition("Class", "class"));
        columnDefinitions.add(new ColumnDefinition("Message", "msg"));
        columnDefinitions.add(new ColumnDefinition("JSessionID", "sessionid"));

        ObservableList<String> regexps = FXCollections.observableArrayList(//
                "<(?<datetime>[^>]*?)> ?<(?<severity>[^>]*?)> ?<(?<subsystem>[^>]*?)> ?<(?<machine>[^>]*?)> ?"
                        + "<(?<server>[^>]*?)> ?<(?<thread>[^>]*?)>( ?<(?<class>[^>]*?)>)? ?"
                        + "<(?<msg>.*?)(;jsessionid=(?<sessionid>.*))?>?", // log beginning
                "(?<msg>[^>]*)(;jsessionid=(?<sid>.*?))?>", // end of message
                "(?<msg>.*)"); // middle of log message on new line
        return new Columnizer("Weblogic (processed by EasyTrace)", columnDefinitions, regexps);
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

        ObservableList<String> regexps = FXCollections.observableArrayList(
                "(?<client>\\S+) (?<user>\\S+) (?<username>\\S+) \\[(?<datetime>[^\\]]*?)\\] \"(?<request>[^\"]*?)\" "
                        + "(?<rstatus>\\S+) (?<rsize>\\S+)( \"(?<referer>[^\"]*?)\" \"(?<useragent>[^\"]*?)\")?");
        return new Columnizer("Apache Access Log", columnDefinitions, regexps);
    }

    private static Columnizer apacheErrorColumnizer() {
        ObservableList<ColumnDefinition> columnDefinitions = FXCollections.observableArrayList();
        columnDefinitions.add(new ColumnDefinition("Date/Time", "datetime"));
        columnDefinitions.add(new ColumnDefinition("Severity", "severity"));
        columnDefinitions.add(new ColumnDefinition("Client", "client"));
        columnDefinitions.add(new ColumnDefinition("Message", "msg"));

        String full = "\\[(?<datetime>[^\\]]*?)\\] \\[(?<severity>[^\\]]*?)\\] \\[(?<client>\\]*?)\\] (?<msg>.*)";
        String noClient = "\\[(?<datetime>[^\\]]*?)\\] \\[(?<severity>[^\\]]*?)\\] (?<msg>.*)";
        String noClientNoSev = "\\[(?<datetime>[^\\]]*?)\\] (?<msg>.*)";
        String defaultToMsg = "(?<msg>.*)";
        ObservableList<String> regexps = FXCollections.observableArrayList(full, noClient, noClientNoSev, defaultToMsg);
        return new Columnizer("Apache Error Log", columnDefinitions, regexps);
    }

    private static Columnizer amadeusInputLog() {
        ObservableList<ColumnDefinition> columnDefinitions = FXCollections.observableArrayList();
        columnDefinitions.add(new ColumnDefinition("HH", "hh"));
        columnDefinitions.add(new ColumnDefinition("MM", "mm"));
        columnDefinitions.add(new ColumnDefinition("SS", "ss"));
        columnDefinitions.add(new ColumnDefinition("MS", "ms"));
        columnDefinitions.add(new ColumnDefinition("Domain", "domain"));
        columnDefinitions.add(new ColumnDefinition("Action", "action"));
        columnDefinitions.add(new ColumnDefinition("Parameters", "params"));

        ObservableList<String> regexps = FXCollections.observableArrayList(
                "[0-9]{8}(?<hh>[0-9]{2})(?<mm>[0-9]{2})(?<ss>[0-9]{2})(?<ms>[0-9]{3}).*?(?<domain>http.*)/"
                        + "(?<action>.*?)\\?(?<params>.*)");
        return new Columnizer("Amadeus input.log", columnDefinitions, regexps);
    }

    private static final Filter WEBLOGIC_HIGHLIGHT =
            Filter.findInColumn("msg", "(Successfully completed deployment.*)|(EJB Deployed EJB with JNDI name.*)");

    private static Colorizer severityBasedColorizerLight() {
        StyleRule highlightRule = new StyleRule("Highlight", WEBLOGIC_HIGHLIGHT, Style.HIGHLIGHT);
        StyleRule errorRule = new StyleRule("Error", Filter.ERROR_SEVERITY, Style.DARK_RED);
        StyleRule warnRule = new StyleRule("Warn", Filter.WARN_SEVERITY, Style.DARK_ORANGE);
        StyleRule infoRule = new StyleRule("Info", Filter.INFO_SEVERITY, Style.DARK_GREEN);
        StyleRule debugRule = new StyleRule("Debug", Filter.DEBUG_SEVERITY, Style.DARK_BLUE);
        StyleRule noticeRule = new StyleRule("Notice", Filter.NOTICE_SEVERITY, Style.DEFAULT);
        StyleRule stackTraceHead = new StyleRule("Stacktrace Head", Filter.STACKTRACE_HEAD, new Style(Style.DARK_RED));
        StyleRule stackTraceBody = new StyleRule("Stacktrace Body", Filter.STACKTRACE_BODY, new Style(Style.DARK_RED));
        StyleRule defaultRule = new StyleRule("Default", Filter.MATCH_ALL, Style.BLACK);

        return new Colorizer("Severity (for light theme)",
                FXCollections.observableArrayList(highlightRule, errorRule, warnRule, infoRule, debugRule, noticeRule,
                        stackTraceHead, stackTraceBody, defaultRule));
    }

    private static Colorizer severityBasedColorizerDark() {
        StyleRule highlightRule = new StyleRule("Highlight", WEBLOGIC_HIGHLIGHT, Style.HIGHLIGHT);
        StyleRule errorRule = new StyleRule("Error", Filter.ERROR_SEVERITY, Style.RED);
        StyleRule warnRule = new StyleRule("Warn", Filter.WARN_SEVERITY, Style.ORANGE);
        StyleRule infoRule = new StyleRule("Info", Filter.INFO_SEVERITY, Style.GREEN);
        StyleRule debugRule = new StyleRule("Debug", Filter.DEBUG_SEVERITY, Style.BLUE);
        StyleRule noticeRule = new StyleRule("Notice", Filter.NOTICE_SEVERITY, Style.DEFAULT);
        StyleRule stackTraceHeadRule = new StyleRule("Stacktrace Head", Filter.STACKTRACE_HEAD, new Style(Style.RED));
        StyleRule stackTraceBodyRule = new StyleRule("Stacktrace Body", Filter.STACKTRACE_BODY, new Style(Style.RED));
        StyleRule defaultRule = new StyleRule("Default", Filter.MATCH_ALL, Style.LIGHT_GRAY);

        return new Colorizer("Severity (for dark theme)",
                FXCollections.observableArrayList(highlightRule, errorRule, warnRule, infoRule, debugRule, noticeRule,
                        stackTraceHeadRule, stackTraceBodyRule, defaultRule));
    }
}
