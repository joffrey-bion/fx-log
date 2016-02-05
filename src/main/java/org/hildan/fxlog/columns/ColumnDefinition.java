package org.hildan.fxlog.columns;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;

/**
 * Defines a column to hold part of log line.
 */
public class ColumnDefinition {

    private final String headerLabel;

    private final String capturingGroupName;

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
        this.capturingGroupName = capturingGroupName;
        this.headerLabel = headerLabel;
        this.width = new SimpleDoubleProperty(80d);
        this.visible = new SimpleBooleanProperty(true);
    }

    /**
     * @return the header of this column
     */
    String getHeaderLabel() {
        return headerLabel;
    }

    /**
     * @return the name of the capturing group to get the data from for this column
     */
    String getCapturingGroupName() {
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

    boolean isVisible() {
        return visible.get();
    }

    /**
     * @return the preferred width for this column (property)
     */
    BooleanProperty visibleProperty() {
        return visible;
    }
}
