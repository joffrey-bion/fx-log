package org.hildan.fxlog.config.builtin;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.text.Font;

import org.hildan.fxlog.coloring.Colorizer;
import org.hildan.fxlog.coloring.Style;
import org.hildan.fxlog.coloring.StyleRule;
import org.hildan.fxlog.columns.ColumnDefinition;
import org.hildan.fxlog.columns.Columnizer;
import org.hildan.fxlog.config.Config;
import org.hildan.fxlog.filtering.Filter;
import org.hildan.fxlog.themes.Theme;
import org.intellij.lang.annotations.RegExp;

/**
 * A generator for the default configuration. It is used as fallback when the built-in config is not available as a
 * resource.
 * <p>
 * It makes it possible for a developer to generate a config programmatically with elements that are not customizable
 * yet via the UI. This is definitely easier than editing the JSON directly.
 */
public class DefaultConfig {

    private static final Filter WEBLOGIC_HIGHLIGHT = Filter.findInColumn("msg",
            "(Successfully completed deployment.*)|(EJB Deployed EJB with JNDI name.*)");

    @RegExp
    private static final String CLASS_NAME_REGEX = "((\\w+\\.)*?\\w+)?";

    @RegExp
    private static final String JSESSIONID_REGEX = "([\\w\\-!]+)";

    /**
     * Generates the default config programmatically.
     *
     * @return the default config
     */
    public static Config generate() {
        Config config = new Config();

        config.getPreferences().setLogsFont(Font.font("SansSerif", 14.0));

        config.getState().setCurrentTheme(Theme.DARK);
        config.getState().setSelectedColorizerIndex(0);
        config.getColorizers().add(severityBasedColorizerDark());
        config.getColorizers().add(severityBasedColorizerLight());

        config.getState().setSelectedColumnizerIndex(0);
        config.getColumnizers().add(weblogicMultilineColumnizer());
        config.getColumnizers().add(weblogicColumnizer());
        config.getColumnizers().add(weblogicEasyTraceColumnizer());
        config.getColumnizers().add(log4jColumnizer());
        config.getColumnizers().add(accessLogColumnizer());
        config.getColumnizers().add(apacheErrorColumnizer());
        config.getColumnizers().add(amadeusInputLog());

        return config;
    }

    private static Columnizer weblogicColumnizer() {
        ObservableList<ColumnDefinition> columns = FXCollections.observableArrayList();
        columns.add(new ColumnDefinition("Date/Time", "datetime", Description.Server.DATE, Width.DATE));
        columns.add(new ColumnDefinition("Severity", "severity", Description.Server.SEVERITY, Width.SEVERITY));
        columns.add(new ColumnDefinition("Subsystem", "subsystem", Description.Server.SUBSYSTEM, false));
        columns.add(new ColumnDefinition("Machine Name", "machine", Description.Server.MACHINE_NAME, false));
        columns.add(new ColumnDefinition("Server Name", "server", Description.Server.SERVER_NAME, false));
        columns.add(new ColumnDefinition("Thread ID", "thread", Description.Server.THREAD_ID, false));
        columns.add(new ColumnDefinition("User ID", "userId", Description.Server.USER_ID, false));
        columns.add(new ColumnDefinition("Transaction ID", "transaction", Description.Server.TRANSACTION, false));
        columns.add(new ColumnDefinition("Diagnostic Context ID", "context", Description.Server.DIAGNOSTIC_CTX_ID,
                false));
        columns.add(new ColumnDefinition("Timestamp", "timestamp", Description.Server.TIMESTAMP, false));
        columns.add(new ColumnDefinition("Message ID", "msgId", Description.Server.MSG_ID, false));
        columns.add(new ColumnDefinition("Class", "class", Description.Server.CLASS, Width.CLASS));
        columns.add(new ColumnDefinition("Message", "msg", Description.Server.MSG, Width.MSG));
        columns.add(new ColumnDefinition("JSessionID", "sessionid", Description.Server.JSESSIONID,
                Width.SESSION_ID));

        @RegExp
        String logStart = "####" //
                + "<(?<datetime>[^>]*?)>" //
                + " ?<(?<severity>\\w*?)>" //
                + " ?<(?<subsystem>[^>]*?)>" //
                + " ?<(?<machine>[^>]*?)>" //
                + " ?<(?<server>[^>]*?)>" //
                + " ?<(?<thread>[^>]*?)>" //
                + " ?<(?<userId>.*?)>" // might be wrapped into <>
                + " ?<(?<transaction>[^>]*?)>" //
                + " ?<(?<context>[^>]*?)>" //
                + " ?<(?<timestamp>[^>]*?)>" //
                + " ?<(?<msgId>BEA-\\d*?)>" //
                + "( ?<(?<class>" + CLASS_NAME_REGEX + ")>)?" //
                + " ?<(?<msg>.*?)" // message start
                + "(;jsessionid=(?<sessionid>" + JSESSIONID_REGEX + "))?" // optional session id
                + ">?\\s*"; // optional end of message
        @RegExp
        String logEnd = "(?<msg>.*?)(;jsessionid=(?<sessionid>" + JSESSIONID_REGEX
                + "))?>\\s*"; // end of msg and optional session id
        @RegExp
        String logCenter = "(?<msg>.*)";

        ObservableList<String> regexps = FXCollections.observableArrayList(logStart, logEnd, logCenter);
        return new Columnizer("Weblogic Server Log", columns, regexps);
    }

    private static Columnizer weblogicMultilineColumnizer() {
        ObservableList<ColumnDefinition> columns = FXCollections.observableArrayList();
        columns.add(new ColumnDefinition("Date/Time", "datetime", Description.Server.DATE, Width.DATE));
        columns.add(new ColumnDefinition("Severity", "severity", Description.Server.SEVERITY, Width.SEVERITY));
        columns.add(new ColumnDefinition("Subsystem", "subsystem", Description.Server.SUBSYSTEM, false));
        columns.add(new ColumnDefinition("Machine Name", "machine", Description.Server.MACHINE_NAME, false));
        columns.add(new ColumnDefinition("Server Name", "server", Description.Server.SERVER_NAME, false));
        columns.add(new ColumnDefinition("Thread ID", "thread", Description.Server.THREAD_ID, false));
        columns.add(new ColumnDefinition("User ID", "userId", Description.Server.USER_ID, false));
        columns.add(new ColumnDefinition("Transaction ID", "transaction", Description.Server.TRANSACTION, false));
        columns.add(new ColumnDefinition("Diagnostic Context ID", "context", Description.Server.DIAGNOSTIC_CTX_ID,
                false));
        columns.add(new ColumnDefinition("Timestamp", "timestamp", Description.Server.TIMESTAMP, false));
        columns.add(new ColumnDefinition("Message ID", "msgId", Description.Server.MSG_ID, false));
        columns.add(new ColumnDefinition("Class", "class", Description.Server.CLASS, Width.CLASS));
        columns.add(new ColumnDefinition("Message", "msg, stacktrace", Description.Server.MSG, Width.MSG));
        columns.add(new ColumnDefinition("JSessionID", "sessionid", Description.Server.JSESSIONID,
                Width.SESSION_ID));

        @RegExp
        String logFirstLine = "####" //
                + "<(?<datetime>[^>]*?)>" //
                + " ?<(?<severity>\\w*?)>" //
                + " ?<(?<subsystem>[^>]*?)>" //
                + " ?<(?<machine>[^>]*?)>" //
                + " ?<(?<server>[^>]*?)>" //
                + " ?<(?<thread>[^>]*?)>" //
                + " ?<(?<userId>.*?)>" //
                + " ?<(?<transaction>[^>]*?)>" //
                + " ?<(?<context>[^>]*?)>" //
                + " ?<(?<timestamp>[^>]*?)>" //
                + " ?<(?<msgId>BEA-\\d*?)>" //
                + "( ?<(?<class>" + CLASS_NAME_REGEX + ")>)?" //
                + " ?<(?<msg>.*?)" // message start
                + "(;jsessionid=(?<sessionid>" + JSESSIONID_REGEX + "))?" // optional session id
                + ">?\\s*"; // optional end of message
        @RegExp
        String logStart = "(?s)####" //
                + "<(?<datetime>[^>]*?)>" //
                + " ?<(?<severity>\\w*?)>" //
                + " ?<(?<subsystem>[^>]*?)>" //
                + " ?<(?<machine>[^>]*?)>" //
                + " ?<(?<server>[^>]*?)>" //
                + " ?<(?<thread>[^>]*?)>" //
                + " ?<(?<userId>.*?)>" //
                + " ?<(?<transaction>[^>]*?)>" //
                + " ?<(?<context>[^>]*?)>" //
                + " ?<(?<timestamp>[^>]*?)>" //
                + " ?<(?<msgId>BEA-\\d*?)>" //
                + "( ?<(?<class>" + CLASS_NAME_REGEX + ")>)?";
        @RegExp
        String withSessionId = logStart //
                + " ?<(?<msg>.*?)" // message start
                + ";jsessionid=(?<sessionid>" + JSESSIONID_REGEX + ")" // session id
                + "(\\n(?<stacktrace>[^>]*))?" // optional stacktrace
                + ">\\s*"; // end of log
        @RegExp
        String withoutSessionId = logStart + " ?<(?<msg>.*)>\\s*";

        ObservableList<String> regexps = FXCollections.observableArrayList(withSessionId, withoutSessionId);
        return new Columnizer("Weblogic Server Log (multi-line)", columns, regexps, logFirstLine);
    }

    private static Columnizer weblogicEasyTraceColumnizer() {
        ObservableList<ColumnDefinition> columns = FXCollections.observableArrayList();
        columns.add(new ColumnDefinition("Date/Time", "datetime", Description.Server.DATE, Width.DATE));
        columns.add(new ColumnDefinition("Severity", "severity", Description.Server.SEVERITY, Width.SEVERITY));
        columns.add(new ColumnDefinition("Subsystem", "subsystem", Description.Server.SUBSYSTEM, false));
        columns.add(new ColumnDefinition("Machine Name", "machine", Description.Server.MACHINE_NAME, false));
        columns.add(new ColumnDefinition("Server Name", "server", Description.Server.SERVER_NAME, false));
        columns.add(new ColumnDefinition("Thread ID", "thread", Description.Server.THREAD_ID, false));
        columns.add(new ColumnDefinition("Class", "class", Description.Server.CLASS, Width.CLASS));
        columns.add(new ColumnDefinition("Message", "msg", Description.Server.MSG, Width.MSG));
        columns.add(new ColumnDefinition("JSessionID", "sessionid", Description.Server.JSESSIONID,
                Width.SESSION_ID));

        @RegExp
        String logStart = "<(?<datetime>[^>]*?)>" //
                + " ?<(?<severity>\\w*?)>" //
                + " ?<(?<subsystem>[^>]*?)>" //
                + " ?<(?<machine>[^>]*?)>" //
                + " ?<(?<server>[^>]*?)>" //
                + " ?<(?<thread>[^>]*?)>" //
                + "( ?<(?<class>" + CLASS_NAME_REGEX + ")>)?" //
                + " ?<(?<msg>.*?)" // message start
                + "(;jsessionid=(?<sessionid>[^>]*))?" // optional session id
                + ">?\\s*"; // optional end of message
        @RegExp
        String logEnd = "(?<msg>.*?)(;jsessionid=(?<sessionid>[^>]*))?>\\s*"; // end of msg and optional session id
        @RegExp
        String logCenter = "(?<msg>.*)";

        ObservableList<String> regexps = FXCollections.observableArrayList(logStart, logEnd, logCenter);
        return new Columnizer("Weblogic (processed by EasyTrace)", columns, regexps);
    }

    private static Columnizer log4jColumnizer() {
        ObservableList<ColumnDefinition> columns = FXCollections.observableArrayList();
        columns.add(new ColumnDefinition("Date/Time", "datetime", Description.Server.DATE, Width.DATE));
        columns.add(new ColumnDefinition("Thread ID", "thread", Description.Server.THREAD_ID));
        columns.add(new ColumnDefinition("Severity", "severity", Description.Server.SEVERITY, Width.SEVERITY));
        columns.add(new ColumnDefinition("Class", "class", Description.Server.CLASS, Width.CLASS));
        columns.add(new ColumnDefinition("Message", "msg", Description.Server.MSG, Width.MSG));

        @RegExp
        String regex = "(?<datetime>\\S+)" //
                + " \\[(?<thread>[^]]*?)]" //
                + " (?<severity>\\S+)" //
                + "\\s+(?<class>" + CLASS_NAME_REGEX + ")" //
                + " - (?<msg>.*)";
        @RegExp
        String defaultToMsg = "(?<msg>.*)";

        ObservableList<String> regexps = FXCollections.observableArrayList(regex, defaultToMsg);
        return new Columnizer("Log4j / Logback", columns, regexps);
    }

    private static Columnizer accessLogColumnizer() {
        ObservableList<ColumnDefinition> columnDefinitions = FXCollections.observableArrayList();
        columnDefinitions.add(new ColumnDefinition("Client", "client", Description.Access.CLIENT, Width.DOMAIN));
        columnDefinitions.add(new ColumnDefinition("Indentity", "identd", Description.Access.IDENTD));
        columnDefinitions.add(new ColumnDefinition("User ID", "userid", Description.Access.USERID));
        columnDefinitions.add(new ColumnDefinition("Date/Time", "datetime", Description.Access.DATE, Width.DATE));
        columnDefinitions.add(new ColumnDefinition("Request", "request", Description.Access.REQUEST, Width.MSG));
        columnDefinitions.add(new ColumnDefinition("Rsp. Code", "rstatus", Description.Access.STATUS));
        columnDefinitions.add(new ColumnDefinition("Rsp. Size (B)", "rsize", Description.Access.RSIZE));
        columnDefinitions.add(new ColumnDefinition("Referer", "referer", Description.Access.REFERER));
        columnDefinitions.add(new ColumnDefinition("User-Agent", "useragent", Description.Access.USERAGENT));

        @RegExp
        String regex = "(?<client>\\S+)" //
                + " (?<identd>\\S+)" //
                + " (?<userid>\\S+)" //
                + " \\[(?<datetime>[^\\]]*?)\\]" //
                + " \"(?<request>[^\"]*?)\"" //
                + " (?<rstatus>\\S+)" //
                + " (?<rsize>\\S+)" //
                + "( \"(?<referer>[^\"]*?)\" \"(?<useragent>[^\"]*?)\")?";

        ObservableList<String> regexps = FXCollections.observableArrayList(regex);
        return new Columnizer("Common/Combined Log Format (access)", columnDefinitions, regexps);
    }

    private static Columnizer apacheErrorColumnizer() {
        ObservableList<ColumnDefinition> columnDefinitions = FXCollections.observableArrayList();
        columnDefinitions.add(new ColumnDefinition("Date/Time", "datetime", Width.DATE));
        columnDefinitions.add(new ColumnDefinition("Severity", "severity", Width.SEVERITY));
        columnDefinitions.add(new ColumnDefinition("Client", "client", Width.DOMAIN));
        columnDefinitions.add(new ColumnDefinition("Message", "msg", Width.MSG));

        @RegExp
        String full = "\\[(?<datetime>[^]]*?)] \\[(?<severity>[^]]*?)] \\[(?<client>[^]]*?)] (?<msg>.*)";
        @RegExp
        String noClient = "\\[(?<datetime>[^]]*?)] \\[(?<severity>[^]]*?)] (?<msg>.*)";
        @RegExp
        String noClientNoSev = "\\[(?<datetime>[^]]*?)] (?<msg>.*)";
        @RegExp
        String defaultToMsg = "(?<msg>.*)";

        ObservableList<String> regexps = FXCollections.observableArrayList(full, noClient, noClientNoSev, defaultToMsg);
        return new Columnizer("Apache Error Log", columnDefinitions, regexps);
    }

    private static Columnizer amadeusInputLog() {
        ObservableList<ColumnDefinition> columnDefinitions = FXCollections.observableArrayList();
        columnDefinitions.add(new ColumnDefinition("Date/Time", "date", Width.DATE));
        columnDefinitions.add(new ColumnDefinition("Domain", "domain", Width.URL));
        columnDefinitions.add(new ColumnDefinition("Action", "action", Width.ENDPOINT));
        columnDefinitions.add(new ColumnDefinition("Parameters", "params", Width.PARAMETER_LIST));

        @RegExp
        String regex = "(?<date>[0-9]{17}).*?(?<domain>http.*)/(?<action>.*?)\\?(?<params>.*)";

        ObservableList<String> regexps = FXCollections.observableArrayList(regex);
        return new Columnizer("Amadeus input.out", columnDefinitions, regexps);
    }

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

        ObservableList<StyleRule> rules = FXCollections.observableArrayList(highlightRule, errorRule, warnRule,
                infoRule, debugRule, noticeRule, stackTraceHead, stackTraceBody, defaultRule);
        return new Colorizer("Severity (for light theme)", rules);
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

        ObservableList<StyleRule> rules = FXCollections.observableArrayList(highlightRule, errorRule, warnRule,
                infoRule, debugRule, noticeRule, stackTraceHeadRule, stackTraceBodyRule, defaultRule);
        return new Colorizer("Severity (for dark theme)", rules);
    }
}
