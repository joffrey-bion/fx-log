package org.hildan.fxlog.coloring;

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

import org.jetbrains.annotations.NotNull;

public class Style {

    public static final Style DEFAULT = new Style("DEFAULT", Color.LIGHTGRAY);

    public static final Style HIGHLIGHT = new Style("Highlight", Color.web("#50e6ffff"), Color.web("#002042ff"));
    public static final Style RED = new Style("Red", Color.web("#ca1d1dff"));
    public static final Style ORANGE = new Style("Orange", Color.web("#e6994dff"));
    public static final Style GREEN = new Style("Green", Color.web("#10c14bff"));
    public static final Style BLUE = new Style("Blue", Color.web("#334db3ff"));
    public static final Style LIGHT_GRAY = new Style("Light Gray", Color.LIGHTGRAY);

    public static final Style DARK_RED = new Style("Dark Red", Color.web("#aa0000ff"));
    public static final Style DARK_ORANGE = new Style("Dark Orange", Color.web("#b27200ff"));
    public static final Style DARK_GREEN = new Style("Dark Green", Color.web("#008100ff"));
    public static final Style DARK_BLUE = new Style("Dark Blue", Color.web("#0000bbff"));
    public static final Style BLACK = new Style("Black", Color.web("#000000ff"));

    public static final Style HIGHLIGHT_SEARCH = new Style("Search highlight", Color.BLACK, Color.ORANGE);

    private final StringProperty name;

    private final Property<Color> foregroundColor;

    private final Property<Color> backgroundColor;

    private transient Binding<Background> backgroundBinding;

    public Style(String name, Color foregroundColor) {
        this.name = new SimpleStringProperty(name);
        this.foregroundColor = new SimpleObjectProperty<>(foregroundColor);
        this.backgroundColor = new SimpleObjectProperty<>();
    }

    public Style(String name, Color foregroundColor, Color backgroundColor) {
        this.name = new SimpleStringProperty(name);
        this.foregroundColor = new SimpleObjectProperty<>(foregroundColor);
        this.backgroundColor = new SimpleObjectProperty<>(backgroundColor);
    }

    public Style(Style source) {
        this(source.getName(), source.getForegroundColor(), source.getBackgroundColor());
    }

    /**
     * Binds the given {@link Node}s' styles to this style.
     *
     * @param nodes
     *         the nodes to drive the style of
     */
    public void bindNodes(@NotNull Node... nodes) {
        for (Node node : nodes) {
            bindNode(node);
        }
    }

    /**
     * Binds the given {@link Node}'s style to this style.
     *
     * @param node
     *         the node to drive the style of
     */
    private void bindNode(@NotNull Node node) {
        bindNodeForeground(node, foregroundColorProperty());
        bindNodeBackground(node, backgroundBinding());
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

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String nameProperty) {
        this.name.set(nameProperty);
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

    public Background getBackground() {
        return backgroundBinding().getValue();
    }

    private Binding<Background> backgroundBinding() {
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
}
