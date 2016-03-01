package org.hildan.fxlog.coloring;

import org.hildan.fxlog.core.LogEntry;
import org.hildan.fxlog.filtering.Filter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javafx.beans.binding.Binding;
import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Labeled;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;

/**
 * A rule that can apply a style to a {@link Node} based on a log {@link Filter}.
 */
public class StyleRule {

    private final StringProperty name;

    private final Property<Filter> filter;

    private final Property<Color> foregroundColor;

    private final Property<Color> backgroundColor;

    private transient Binding<Background> backgroundBinding;

    /**
     * Creates a StyleRule with no style override.
     *
     * @param name
     *         a name for this rule
     */
    public StyleRule(@NotNull String name) {
        this(name, Filter.findInRawLog(""), null, null);
    }

    /**
     * Creates a StyleRule with the given filter and style.
     *
     * @param name
     *         a name for this rule
     * @param filter
     *         the filter to use to match logs
     * @param foregroundColor
     *         the foreground color to apply
     * @param backgroundColor
     *         the background color to apply
     */
    public StyleRule(@NotNull String name, @NotNull Filter filter, @Nullable Color foregroundColor,
            @Nullable Color backgroundColor) {
        this.name = new SimpleStringProperty(name);
        this.filter = new SimpleObjectProperty<>(filter);
        this.foregroundColor = new SimpleObjectProperty<>(foregroundColor);
        this.backgroundColor = new SimpleObjectProperty<>(backgroundColor);
    }

    private Binding<Background> getBackgroundBinding() {
        if (backgroundBinding == null) {
            backgroundBinding = Bindings.createObjectBinding(this::createColoredBackground, this.backgroundColor);
        }
        return backgroundBinding;
    }

    private Background createColoredBackground() {
        Color color = backgroundColor.getValue();
        if (color == null) {
            return null;
        }
        BackgroundFill fill = new BackgroundFill(color, CornerRadii.EMPTY, Insets.EMPTY);
        return new Background(fill);
    }

    /**
     * Binds this node's style to this rule's style if the given log matches this rule's filter. Removes bindings if the
     * log doesn't match.
     *
     * @param node
     *         the node to style
     * @param log
     *         the log to test
     *
     * @return true if the log matched this rule
     */
    boolean bindStyleIfMatches(@NotNull Node node, @NotNull LogEntry log) {
        boolean match = filter.getValue().test(log);
        if (match) {
            bindNodeForeground(node);
            bindNodeBackground(node);
        } else {
            unbindNodeForeground(node);
            unbindNodeBackground(node);
        }
        return match;
    }

    private void bindNodeForeground(@NotNull Node node) {
        if (node instanceof Labeled) {
            // this includes javafx.scene.control.Label
            ((Labeled)node).textFillProperty().bind(foregroundColor);
        } else if (node instanceof Shape) {
            // this includes javafx.scene.Text
            ((Shape)node).fillProperty().bind(foregroundColor);
        }
    }

    private void unbindNodeForeground(@NotNull Node node) {
        if (node instanceof Labeled) {
            // this includes javafx.scene.control.Label
            ((Labeled)node).textFillProperty().unbind();
            ((Labeled)node).setTextFill(null);
        } else if (node instanceof Shape) {
            // this includes javafx.scene.Text
            ((Shape)node).fillProperty().unbind();
            ((Shape)node).setFill(null);
        }
    }

    private void bindNodeBackground(@NotNull Node node) {
        if (node instanceof Region) {
            // this includes javafx.scene.control.Label
            ((Region)node).backgroundProperty().bind(getBackgroundBinding());
        }
    }

    private void unbindNodeBackground(@NotNull Node node) {
        if (node instanceof Region) {
            // this includes javafx.scene.control.Label
            ((Region)node).backgroundProperty().unbind();
            ((Region)node).setBackground(null);
        }
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

    public Color getForegroundColor() {
        return foregroundColor.getValue();
    }

    public Property<Color> foregroundColorProperty() {
        return foregroundColor;
    }

    public void setForegroundColor(Color foregroundColor) {
        this.foregroundColor.setValue(foregroundColor);
    }

    public Color getBackgroundColor() {
        return backgroundColor.getValue();
    }

    public Property<Color> backgroundColorProperty() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor.setValue(backgroundColor);
    }

    @Override
    public String toString() {
        return name.get();
    }
}
