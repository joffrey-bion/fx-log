package org.hildan.fxlog.view.components;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.*;

public class MemoryUsageInfo extends ProportionLabel<Double> {

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

    private static final String TEMPLATE = "%%,.2f%s";

    private static final String TEMPLATE_WITH_TOTAL = "%%,.2f of %%,.0f%s";

    private static final String TOOLTIP_TEMPLATE = "%%,.2f%1$s used out of %%,.0f%1$s allocated (max %%,.0f%1$s)";

    private final StringProperty template = new SimpleStringProperty(TEMPLATE);

    private final StringProperty templateWithTotal = new SimpleStringProperty(TEMPLATE_WITH_TOTAL);

    private final StringProperty tooltipTemplate = new SimpleStringProperty(TOOLTIP_TEMPLATE);

    private final ObjectProperty<Unit> unit = new SimpleObjectProperty<>(Unit.MB);

    private final LongProperty refreshPeriod = new SimpleLongProperty(1000);

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    // defined as a field to avoid garbage collection
    @SuppressWarnings("FieldCanBeLocal")
    private final StringBinding effectiveTemplate;

    // defined as a field to avoid garbage collection
    @SuppressWarnings("FieldCanBeLocal")
    private final StringBinding effectiveTemplateWithTotal;

    // defined as a field to avoid garbage collection
    @SuppressWarnings("FieldCanBeLocal")
    private final StringBinding effectiveTooltipTemplate;

    private volatile boolean running;

    public MemoryUsageInfo() {
        effectiveTemplate = createTemplateBinding(template);
        effectiveTemplateWithTotal = createTemplateBinding(templateWithTotal);
        effectiveTooltipTemplate = createTemplateBinding(tooltipTemplate);

        super.templateProperty().bind(effectiveTemplate);
        super.templateWithTotalProperty().bind(effectiveTemplateWithTotal);
        setTooltip(new Tooltip());

        setOnMouseClicked(e -> System.gc());

        updateText();

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

    private StringBinding createTemplateBinding(StringProperty templateToBind) {
        Callable<String> replaceUnit = () -> String.format(templateToBind.get(), unit.get().getSymbol());
        return Bindings.createStringBinding(replaceUnit, templateToBind, unit);
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
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long usedMemory = totalMemory - runtime.freeMemory();
        double scale = unit.getValue().getMultiplier();
        double maxScaled = maxMemory / scale;
        double totalScaled = totalMemory / scale;
        double usedScaled = usedMemory / scale;
        Platform.runLater(() -> {
            setCurrentCount(usedScaled);
            setTotalCount(totalScaled);
            getTooltip().setText(String.format(effectiveTooltipTemplate.get(), usedScaled, totalScaled, maxScaled));
        });
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

    public long getRefreshPeriod() {
        return refreshPeriod.get();
    }

    public LongProperty refreshPeriodProperty() {
        return refreshPeriod;
    }

    public void setRefreshPeriod(long refreshPeriod) {
        this.refreshPeriod.set(refreshPeriod);
    }

    @Override
    public String getTemplate() {
        return template.get();
    }

    @Override
    public StringProperty templateProperty() {
        return template;
    }

    public void setTemplate(String template) {
        this.template.set(template);
    }

    @Override
    public String getTemplateWithTotal() {
        return templateWithTotal.get();
    }

    @Override
    public StringProperty templateWithTotalProperty() {
        return templateWithTotal;
    }

    public void setTemplateWithTotal(String templateWithTotal) {
        this.templateWithTotal.set(templateWithTotal);
    }

    public String getTooltipTemplate() {
        return tooltipTemplate.get();
    }

    public StringProperty tooltipTemplateProperty() {
        return tooltipTemplate;
    }

    public void setTooltipTemplate(String tooltipTemplate) {
        this.tooltipTemplate.set(tooltipTemplate);
    }
}
