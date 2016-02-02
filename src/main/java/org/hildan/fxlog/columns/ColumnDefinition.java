package org.hildan.fxlog.columns;

/**
 * Defines a column to hold part of log line.
 */
public class ColumnDefinition {

    private final String headerLabel;

    private final String capturingGroupName;

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
}
