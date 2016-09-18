package org.hildan.fxlog.coloring;

import java.util.regex.Pattern;

import javafx.beans.binding.Binding;
import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Shape;

import org.fxmisc.easybind.EasyBind;
import org.hildan.fxlog.core.LogEntry;
import org.hildan.fxlog.filtering.Filter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A rule that can apply a style to a {@link Node} based on a log {@link Filter}.
 */
public class StyleRule {

    static final StyleRule DEFAULT = new StyleRule("DEFAULT", Filter.findInRawLog("."), Color.LIGHTGRAY, null);

    private final StringProperty name;

    private final Property<Filter> filter;

    private final Property<Color> foregroundColor;

    private final Property<Color> backgroundColor;

    private transient Binding<Background> backgroundBinding;

    private transient Binding[] matchingInternalsObservable;

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
        // cannot instantiate the transient bindings here as Gson does not use this constructor
    }

    private Binding<Background> getBackgroundBinding() {
        if (backgroundBinding == null) {
            backgroundBinding = createBackgroundBinding(this.backgroundColor);
        }
        return backgroundBinding;
    }

    private static Binding<Background> createBackgroundBinding(@NotNull ObservableValue<? extends Paint> color) {
        return Bindings.createObjectBinding(() -> createColoredBackground(color.getValue()), color);
    }

    private static Background createColoredBackground(Paint color) {
        if (color == null) {
            return null;
        }
        BackgroundFill fill = new BackgroundFill(color, CornerRadii.EMPTY, Insets.EMPTY);
        return new Background(fill);
    }

    /**
     * Returns whether the given log matches this rule.
     *
     * @param log
     *         the log to test
     *
     * @return true if the log matched this rule
     */
    boolean matches(@NotNull LogEntry log) {
        return filter.getValue().test(log);
    }

    /**
     * Binds this rule's style to the given {@link Node}'s style.
     *
     * @param node
     *         the node to drive the style of
     */
    void bindNodeStyle(@NotNull Node node) {
        bindNodeForeground(node, foregroundColor);
        bindNodeBackground(node, getBackgroundBinding());
    }

    private static void bindNodeForeground(@NotNull Node node,
                                           @NotNull ObservableValue<? extends Paint> observableColor) {
        if (node instanceof Labeled) {
            // this includes javafx.scene.control.Label
            ((Labeled) node).textFillProperty().bind(observableColor);
        } else if (node instanceof Shape) {
            // this includes javafx.scene.Text
            ((Shape) node).fillProperty().bind(observableColor);
        }
    }

    private static void bindNodeBackground(@NotNull Node node,
                                           @NotNull ObservableValue<? extends Background> observableBackground) {
        if (node instanceof Region) {
            // this includes javafx.scene.control.Label
            ((Region) node).backgroundProperty().bind(observableBackground);
        }
    }

    Binding[] getMatchingInternalsObservable() {
        if (matchingInternalsObservable == null) {
            matchingInternalsObservable = createMatchingInternalsObservable();
        }
        return matchingInternalsObservable;
    }

    private Binding[] createMatchingInternalsObservable() {
        Binding<Pattern> pat = EasyBind.select(filterProperty()).selectObject(Filter::patternProperty);
        Binding<String> col = EasyBind.select(filterProperty()).selectObject(Filter::columnNameProperty);
        return new Binding[] {pat, col};
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
        return getClass().getSimpleName() + " '" + name.get() + "'";
    }
}
