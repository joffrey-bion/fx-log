package org.hildan.fxlog.core;

import org.apache.commons.io.input.TailerListenerAdapter;
import org.hildan.fxlog.columns.Columnizer;

import java.util.List;

public class LogTailListener extends TailerListenerAdapter {

    private Columnizer columnizer;

    private List<LogEntry> logs;

    public LogTailListener(Columnizer columnizer, List<LogEntry> logs) {
        this.columnizer = columnizer;
        this.logs = logs;
    }

    @Override
    public void fileRotated() {
        logs.clear();
    }

    @Override
    public void handle(String line) {
        logs.add(columnizer.parse(line));
    }

    @Override
    public void handle(Exception ex) {
        System.err.println("Exception while reading the file: " + ex);
    }
}
