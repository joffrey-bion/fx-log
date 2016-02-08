package org.hildan.fxlog.coloring;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.paint.Color;

import org.hildan.fxlog.core.LogEntry;
import org.hildan.fxlog.filtering.Filter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A rule that can apply a style to a {@link Node} based on a log {@link Filter}.
 */
public class StyleRule {

    private final StringProperty name;

    private final Property<Filter> filter;

    private final Property<Color> foreground;

    private final Property<Color> background;

    /**
     * Creates a StyleRule with no style override.
     *
     * @param name
     *         a name for this rule
     */
    public StyleRule(@NotNull String name) {
        this(name, Filter.matchRawLog(""), null, null);
    }

    /**
     * Creates a StyleRule with the given filter and style.
     *
     * @param name
     *         a name for this rule
     * @param filter
     *         the filter to use to match logs
     * @param foreground
     *         the foreground color to apply
     * @param background
     *         the background color to apply
     */
    public StyleRule(@NotNull String name, @NotNull Filter filter, @Nullable Color foreground,
                     @Nullable Color background) {
        this.name = new SimpleStringProperty(name);
        this.filter = new SimpleObjectProperty<>(filter);
        this.foreground = new SimpleObjectProperty<>(foreground);
        this.background = new SimpleObjectProperty<>(background);
    }

    /**
     * Applies this rule's style to the given node if the given log matches this rule's filter.
     *
     * @param node
     *         the node to style
     * @param log
     *         the log to test
     * @return true if the style of the node was changed
     */
    boolean applyTo(@NotNull Node node, @NotNull LogEntry log) {
        if (filter.getValue().test(log)) {
            String style = "";
            if (foreground.getValue() != null) {
                // surprisingly, this is the property affecting the text's *foreground* color in a TableRow
                style += "-fx-text-background-color: " + toString(foreground.getValue()) + "; ";
            }
            if (background.getValue() != null) {
                style += "-fx-background-color: " + toString(background.getValue()) + "; ";
            }
            node.setStyle(style);
            return true;
        }
        return false;
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public Filter getFilter() {
        return filter.getValue();
    }

    public Property<Filter> filterProperty() {
        return filter;
    }

    public void setFilter(Filter filter) {
        this.filter.setValue(filter);
    }

    public Color getForeground() {
        return foreground.getValue();
    }

    public Property<Color> foregroundProperty() {
        return foreground;
    }

    public void setForeground(Color foreground) {
        this.foreground.setValue(foreground);
    }

    public Color getBackground() {
        return background.getValue();
    }

    public Property<Color> backgroundProperty() {
        return background;
    }

    public void setBackground(Color background) {
        this.background.setValue(background);
    }

    /**
     * Converts the given color into a String in the format #FFFFFF.
     *
     * @param color
     *         the Color to convert
     * @return the color as a String
     */
    private static String toString(Color color) {
        return "#" + color.toString().substring(2);
    }

    @Override
    public String toString() {
        return name.get();
    }
}
