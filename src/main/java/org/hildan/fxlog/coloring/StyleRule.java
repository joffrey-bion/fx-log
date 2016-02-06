package org.hildan.fxlog.coloring;

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

    private final String name;

    private final Filter filter;

    private final Color foreground;

    private final Color background;

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
        this.name = name;
        this.filter = filter;
        this.foreground = foreground;
        this.background = background;
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
        if (filter.test(log)) {
            String style = "";
            if (foreground != null) {
                // surprisingly, this is the property affecting the text's *foreground* color in a TableRow
                style += "-fx-text-background-color: " + toString(foreground) + "; ";
            }
            if (background != null) {
                style += "-fx-background-color: " + toString(background) + "; ";
            }
            node.setStyle(style);
            return true;
        }
        return false;
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
        return name;
    }
}
