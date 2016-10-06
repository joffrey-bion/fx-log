package org.hildan.fxlog.columns;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Defines a column to hold part of a log line.
 */
public class ColumnDefinition {

    private static final double DEFAULT_WIDTH = 80d;

    private final StringProperty headerLabel;

    private final StringProperty capturingGroupName;

    private final DoubleProperty width;

    private final BooleanProperty visible;

    /**
     * Creates a new ColumnDefinition for the given field.
     *
     * @param headerLabel
     *         the label of this column
     * @param capturingGroupName
     *         the name of the capturing group of the regexp to put inside this column
     */
    public ColumnDefinition(String headerLabel, String capturingGroupName) {
        this(headerLabel, capturingGroupName, true, DEFAULT_WIDTH);
    }

    /**
     * Creates a new ColumnDefinition for the given field.
     *
     * @param headerLabel
     *         the label of this column
     * @param capturingGroupName
     *         the name of the capturing group of the regexp to put inside this column
     * @param visible
     *         whether this column is initially visible before the user customizes it
     */
    public ColumnDefinition(String headerLabel, String capturingGroupName, boolean visible) {
        this(headerLabel, capturingGroupName, visible, DEFAULT_WIDTH);
    }

    /**
     * Creates a new ColumnDefinition for the given field.
     *
     * @param headerLabel
     *         the label of this column
     * @param capturingGroupName
     *         the name of the capturing group of the regexp to put inside this column
     * @param initialWidth
     *         the initial width to give this column before the user customizes it
     */
    public ColumnDefinition(String headerLabel, String capturingGroupName, double initialWidth) {
        this(headerLabel, capturingGroupName, true, initialWidth);
    }

    /**
     * Creates a new ColumnDefinition for the given field.
     *
     * @param headerLabel
     *         the label of this column
     * @param capturingGroupName
     *         the name of the capturing group of the regexp to put inside this column
     * @param visible
     *         whether this column is initially visible before the user customizes it
     * @param initialWidth
     *         the initial width to give this column before the user customizes it
     */
    private ColumnDefinition(String headerLabel, String capturingGroupName, boolean visible, double initialWidth) {
        this.capturingGroupName = new SimpleStringProperty(capturingGroupName);
        this.headerLabel = new SimpleStringProperty(headerLabel);
        this.visible = new SimpleBooleanProperty(visible);
        this.width = new SimpleDoubleProperty(initialWidth);
    }

    /**
     * @return the header of this column
     */
    public String getHeaderLabel() {
        return headerLabel.get();
    }

    public StringProperty headerLabelProperty() {
        return headerLabel;
    }

    /**
     * @return the name of the capturing group to get the data from for this column
     */
    public String getCapturingGroupName() {
        return capturingGroupName.get();
    }

    public StringProperty capturingGroupNameProperty() {
        return capturingGroupName;
    }

    /**
     * @return the preferred width for this column
     */
    double getWidth() {
        return width.get();
    }

    /**
     * @return the preferred width for this column (property)
     */
    DoubleProperty widthProperty() {
        return width;
    }

    /**
     * @return whether this column is visible
     */
    public boolean isVisible() {
        return visible.get();
    }

    /**
     * @return whether this column is visible (property)
     */
    BooleanProperty visibleProperty() {
        return visible;
    }
}
