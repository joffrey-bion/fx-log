package org.hildan.fxlog.coloring;

import java.util.List;

import javafx.scene.control.TableRow;

import org.hildan.fxlog.core.LogEntry;

/**
 * Colors a log entry based on a list of coloring rules.
 */
public class Colorizer {

    private final String name;

    private final List<StyleRule> styleRules;

    public Colorizer(String name, List<StyleRule> styleRules) {
        this.name = name;
        this.styleRules = styleRules;
    }

    void setStyle(TableRow row, LogEntry log) {
        row.setStyle(null);
        for (StyleRule rule : styleRules) {
            if (rule.appliesFor(log)) {
                rule.setStyle(row);
                return;
            }
        }
    }

    @Override
    public String toString() {
        return name;
    }
}
