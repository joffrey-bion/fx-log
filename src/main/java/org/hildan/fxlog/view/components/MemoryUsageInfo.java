package org.hildan.fxlog.view.components;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.*;

@SuppressWarnings("unused")
public class MemoryUsageInfo extends Label {

    public enum Unit {
        KB("KB", 1024), MB("MB", 1024 * 1024), GB("GB", 1024 * 1024 * 1024);

        private final double multiplier;

        private final String symbol;

        Unit(String symbol, double multiplier) {
            this.symbol = symbol;
            this.multiplier = multiplier;
        }

        public String getSymbol() {
            return symbol;
        }

        public double getMultiplier() {
            return multiplier;
        }
    }

    private static final String DEFAULT_TEMPLATE = "%%.2f %s";

    private static final String DEFAULT_TEMPLATE_WITH_TOTAL = "%%.2f of %%.2f %s";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final StringProperty template = new SimpleStringProperty(null);

    private final Property<Unit> unit = new SimpleObjectProperty<>(Unit.MB);

    private final BooleanProperty showTotal = new SimpleBooleanProperty(false);

    private final LongProperty refreshPeriod = new SimpleLongProperty(1000);

    private volatile boolean running;

    public MemoryUsageInfo() {
        updateText();
        setOnMouseClicked(e -> System.gc());
        visibleProperty().addListener((observable, wasVisible, nowVisible) -> {
            if (nowVisible) {
                startAutoUpdate();
            } else {
                stopAutoUpdate();
            }
        });
        if (isVisible()) {
            startAutoUpdate();
        }

    }

    private void stopAutoUpdate() {
        running = false;
    }

    private void startAutoUpdate() {
        running = true;
        executor.submit(() -> {
            while (running) {
                try {
                    updateText();
                    Thread.sleep(refreshPeriod.get());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
    }

    private void updateText() {
        Runtime runtime = Runtime.getRuntime();
        long total = runtime.totalMemory();
        long used = total - runtime.freeMemory();
        String text = formatText(used, total);
        Platform.runLater(() -> setText(text));
    }

    private String formatText(long usedMemory, long totalMemory) {
        double scale = unit.getValue().getMultiplier();
        if (showTotal.get()) {
            return String.format(computeTemplate(), usedMemory / scale, totalMemory / scale);
        } else {
            return String.format(computeTemplate(), usedMemory / scale);
        }
    }

    private String computeTemplate() {
        if (template.get() != null) {
            return template.get();
        }
        if (showTotal.get()) {
            return String.format(DEFAULT_TEMPLATE_WITH_TOTAL, unit.getValue().getSymbol());
        } else {
            return String.format(DEFAULT_TEMPLATE, unit.getValue().getSymbol());
        }
    }

    public String getTemplate() {
        return template.get();
    }

    public StringProperty templateProperty() {
        return template;
    }

    public void setTemplate(String template) {
        this.template.set(template);
    }

    public Unit getUnit() {
        return unit.getValue();
    }

    public Property<Unit> unitProperty() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit.setValue(unit);
    }

    public boolean isShowTotal() {
        return showTotal.get();
    }

    public BooleanProperty showTotalProperty() {
        return showTotal;
    }

    public void setShowTotal(boolean showTotal) {
        this.showTotal.set(showTotal);
    }

    public long getRefreshPeriod() {
        return refreshPeriod.get();
    }

    public LongProperty refreshPeriodProperty() {
        return refreshPeriod;
    }

    public void setRefreshPeriod(long refreshPeriod) {
        this.refreshPeriod.set(refreshPeriod);
    }
}
