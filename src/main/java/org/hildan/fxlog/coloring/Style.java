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

    static final Style DEFAULT = new Style("DEFAULT", Color.LIGHTGRAY, null);

    private final StringProperty nameProperty;

    private final Property<Color> foregroundColor;

    private final Property<Color> backgroundColor;

    private transient Binding<Background> backgroundBinding;

    public Style(String name, Color foregroundColor, Color backgroundColor) {
        this.nameProperty = new SimpleStringProperty(name);
        this.foregroundColor = new SimpleObjectProperty<>(foregroundColor);
        this.backgroundColor = new SimpleObjectProperty<>(backgroundColor);
    }

    /**
     * Binds the given {@link Node}'s style to this style.
     *
     * @param node
     *         the node to drive the style of
     */
    void bindNode(@NotNull Node node) {
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

    public String getNameProperty() {
        return nameProperty.get();
    }

    public StringProperty namePropertyProperty() {
        return nameProperty;
    }

    public void setNameProperty(String nameProperty) {
        this.nameProperty.set(nameProperty);
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
