package org.hildan.fxlog.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javafx.scene.paint.Color;

import org.hildan.fxlog.coloring.Colorizer;
import org.hildan.fxlog.coloring.StyleRule;
import org.hildan.fxlog.columns.ColumnDefinition;
import org.hildan.fxlog.columns.Columnizer;
import org.hildan.fxlog.filtering.Filter;

class DefaultConfig {

    static Config generate() {
        Config config = new Config();
        config.getColorizers().add(severityBasedColorizerDark());
        config.getColorizers().add(severityBasedColorizerLight());
        config.getColumnizers().add(weblogicColumnizer());
        return config;
    }

    private static Columnizer weblogicColumnizer() {
        List<ColumnDefinition> columnDefinitions = new ArrayList<>(5);
        columnDefinitions.add(new ColumnDefinition("Date", "date"));
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
        columnDefinitions.add(new ColumnDefinition("JSessionID", "sid"));
        String weblogicLogStart =
                "####<(?<date>.*?)> <(?<severity>.*?)> <(?<subsystem>.*?)> <(?<machine>.*?)> <(?<server>.*?)> <(?<thread>.*?)> <(?<user>.*?)> <(?<transaction>.*?)> <(?<context>.*?)> <(?<timestamp>.*?)> <(?<msgId>.*?)>";
        List<String> regexps = Arrays.asList(//
                weblogicLogStart + " <(?<class>.*?)> <(?<msg>.*?);jsessionid=(?<sid>.*?)>", // with session ID
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
        return new Colorizer("Severity (light)", Arrays.asList(errorRule, warnRule, infoRule, debugRule, noticeRule));
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
        return new Colorizer("Severity (dark)", Arrays.asList(errorRule, warnRule, infoRule, debugRule, noticeRule));
    }
}
