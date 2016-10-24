package org.hildan.fxlog.config;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import org.hildan.fxlog.coloring.Style;
import org.hildan.fxlog.view.scrollbarmarks.Alignment;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the user's preferences in terms of colors, fonts, size of things.
 */
@SuppressWarnings("unused")
public class Preferences {

    private final BooleanProperty openLastFileAtStartup;

    private final BooleanProperty checkForUpdates;

    private final BooleanProperty limitNumberOfLogs;

    private final Property<Integer> maxNumberOfLogs;

    private final BooleanProperty skipEmptyLogs;

    private final Property<Integer> tailingDelayInMillis;

    private final ObjectProperty<Integer> logBufferSize;

    private final BooleanProperty wrapLogsText;

    private final Property<Font> logsFont;

    private final Property<Style> searchHighlightStyle;

    private final Property<Color> searchMatchMarkColor;

    private final Property<Double> searchMatchMarkThickness;

    private final Property<Alignment> searchMatchMarkAlignment;

    Preferences() {
        this.openLastFileAtStartup = new SimpleBooleanProperty(true);
        this.checkForUpdates = new SimpleBooleanProperty(true);
        this.limitNumberOfLogs = new SimpleBooleanProperty(true);
        this.maxNumberOfLogs = new SimpleObjectProperty<>(100000);
        this.skipEmptyLogs = new SimpleBooleanProperty(true);
        this.tailingDelayInMillis = new SimpleObjectProperty<>(100);
        this.logBufferSize = new SimpleObjectProperty<>(1000);
        this.wrapLogsText = new SimpleBooleanProperty(false);
        this.logsFont = new SimpleObjectProperty<>(Font.getDefault());
        this.searchHighlightStyle = new SimpleObjectProperty<>(Style.HIGHLIGHT_SEARCH);
        this.searchMatchMarkColor = new SimpleObjectProperty<>(Color.ORANGE.deriveColor(0, 1, 0.7, 0.5));
        this.searchMatchMarkThickness = new SimpleObjectProperty<>(3.0);
        this.searchMatchMarkAlignment = new SimpleObjectProperty<>(Alignment.CENTER);
    }

    public boolean getOpenLastFileAtStartup() {
        return openLastFileAtStartup.get();
    }

    @NotNull
    public BooleanProperty openLastFileAtStartupProperty() {
        return openLastFileAtStartup;
    }

    public void setOpenLastFileAtStartup(boolean openLastFileAtStartup) {
        this.openLastFileAtStartup.set(openLastFileAtStartup);
    }

    public boolean isCheckForUpdates() {
        return checkForUpdates.get();
    }

    public BooleanProperty checkForUpdatesProperty() {
        return checkForUpdates;
    }

    public void setCheckForUpdates(boolean checkForUpdates) {
        this.checkForUpdates.set(checkForUpdates);
    }

    public boolean isLimitNumberOfLogs() {
        return limitNumberOfLogs.get();
    }

    public BooleanProperty limitNumberOfLogsProperty() {
        return limitNumberOfLogs;
    }

    public void setLimitNumberOfLogs(boolean limitNumberOfLogs) {
        this.limitNumberOfLogs.set(limitNumberOfLogs);
    }

    public int getMaxNumberOfLogs() {
        return maxNumberOfLogs.getValue();
    }

    public Property<Integer> maxNumberOfLogsProperty() {
        return maxNumberOfLogs;
    }

    public void setMaxNumberOfLogs(int maxNumberOfLogs) {
        this.maxNumberOfLogs.setValue(maxNumberOfLogs);
    }

    public boolean getSkipEmptyLogs() {
        return skipEmptyLogs.get();
    }

    @NotNull
    public BooleanProperty skipEmptyLogsProperty() {
        return skipEmptyLogs;
    }

    public void setSkipEmptyLogs(boolean skipEmptyLogs) {
        this.skipEmptyLogs.set(skipEmptyLogs);
    }

    public Integer getTailingDelayInMillis() {
        return tailingDelayInMillis.getValue();
    }

    public Property<Integer> tailingDelayInMillisProperty() {
        return tailingDelayInMillis;
    }

    public void setTailingDelayInMillis(Integer tailingDelayInMillis) {
        this.tailingDelayInMillis.setValue(tailingDelayInMillis);
    }

    public Integer getLogBufferSize() {
        return logBufferSize.get();
    }

    public Property<Integer> logBufferSizeProperty() {
        return logBufferSize;
    }

    public void setLogBufferSize(Integer logBufferSize) {
        this.logBufferSize.set(logBufferSize);
    }

    public boolean getWrapLogsText() {
        return wrapLogsText.get();
    }

    @NotNull
    public BooleanProperty wrapLogsTextProperty() {
        return wrapLogsText;
    }

    public void setWrapLogsText(boolean wrapLogsText) {
        this.wrapLogsText.set(wrapLogsText);
    }

    public Font getLogsFont() {
        return logsFont.getValue();
    }

    @NotNull
    public Property<Font> logsFontProperty() {
        return logsFont;
    }

    public void setLogsFont(@NotNull Font logsFont) {
        this.logsFont.setValue(logsFont);
    }

    public Style getSearchHighlightStyle() {
        return searchHighlightStyle.getValue();
    }

    public Property<Style> searchHighlightStyleProperty() {
        return searchHighlightStyle;
    }

    public void setSearchHighlightStyle(Style searchHighlightStyle) {
        this.searchHighlightStyle.setValue(searchHighlightStyle);
    }

    public Color getSearchMatchMarkColor() {
        return searchMatchMarkColor.getValue();
    }

    public Property<Color> searchMatchMarkColorProperty() {
        return searchMatchMarkColor;
    }

    public void setSearchMatchMarkColor(Color searchMatchMarkColor) {
        this.searchMatchMarkColor.setValue(searchMatchMarkColor);
    }

    public Double getSearchMatchMarkThickness() {
        return searchMatchMarkThickness.getValue();
    }

    public Property<Double> searchMatchMarkThicknessProperty() {
        return searchMatchMarkThickness;
    }

    public void setSearchMatchMarkThickness(Double searchMatchMarkThickness) {
        this.searchMatchMarkThickness.setValue(searchMatchMarkThickness);
    }

    public Alignment getSearchMatchMarkAlignment() {
        return searchMatchMarkAlignment.getValue();
    }

    public Property<Alignment> searchMatchMarkAlignmentProperty() {
        return searchMatchMarkAlignment;
    }

    public void setSearchMatchMarkAlignment(Alignment searchMatchMarkAlignment) {
        this.searchMatchMarkAlignment.setValue(searchMatchMarkAlignment);
    }
}
