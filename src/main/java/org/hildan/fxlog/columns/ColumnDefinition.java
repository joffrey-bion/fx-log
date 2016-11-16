package org.hildan.fxlog.columns;

import java.util.concurrent.Callable;

import javafx.beans.binding.Binding;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Tooltip;

import org.hildan.fxlog.data.LogEntry;

/**
 * Defines a column to hold part of a log line.
 */
public class ColumnDefinition {

    private static final double DEFAULT_WIDTH = 80d;

    private final StringProperty headerLabel;

    private final StringProperty capturingGroupName;

    private final DoubleProperty width;

    private final BooleanProperty visible;

    private final StringProperty description;

    /**
     * Creates a new ColumnDefinition for the given field.
     *
     * @param headerLabel
     *         the label of this column
     * @param capturingGroupName
     *         the name of the capturing group of the regexp to put inside this column
     */
    public ColumnDefinition(String headerLabel, String capturingGroupName) {
        this(headerLabel, capturingGroupName, null, true, DEFAULT_WIDTH);
    }

    /**
     * Creates a new ColumnDefinition for the given field.
     *
     * @param headerLabel
     *         the label of this column
     * @param capturingGroupName
     *         the name of the capturing group of the regexp to put inside this column
     * @param description
     *         the description of this column for the user
     */
    public ColumnDefinition(String headerLabel, String capturingGroupName, String description) {
        this(headerLabel, capturingGroupName, description, true, DEFAULT_WIDTH);
    }

    /**
     * Creates a new ColumnDefinition for the given field.
     *
     * @param headerLabel
     *         the label of this column
     * @param capturingGroupName
     *         the name of the capturing group of the regexp to put inside this column
     * @param description
     *         the description of this column for the user
     * @param visible
     *         whether this column is initially visible before the user customizes it
     */
    public ColumnDefinition(String headerLabel, String capturingGroupName, String description, boolean visible) {
        this(headerLabel, capturingGroupName, description, visible, DEFAULT_WIDTH);
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
        this(headerLabel, capturingGroupName, null, true, initialWidth);
    }

    /**
     * Creates a new ColumnDefinition for the given field.
     *
     * @param headerLabel
     *         the label of this column
     * @param capturingGroupName
     *         the name of the capturing group of the regexp to put inside this column
     * @param description
     *         the description of this column for the user
     * @param initialWidth
     *         the initial width to give this column before the user customizes it
     */
    public ColumnDefinition(String headerLabel, String capturingGroupName, String description, double initialWidth) {
        this(headerLabel, capturingGroupName, description, true, initialWidth);
    }

    /**
     * Creates a new ColumnDefinition for the given field.
     *
     * @param headerLabel
     *         the label of this column
     * @param capturingGroupName
     *         the name of the capturing group of the regexp to put inside this column
     * @param description
     *         the description of this column for the user
     * @param visible
     *         whether this column is initially visible before the user customizes it
     * @param initialWidth
     *         the initial width to give this column before the user customizes it
     */
    private ColumnDefinition(String headerLabel, String capturingGroupName, String description, boolean visible, double
            initialWidth) {
        this.capturingGroupName = new SimpleStringProperty(capturingGroupName);
        this.headerLabel = new SimpleStringProperty(headerLabel);
        this.visible = new SimpleBooleanProperty(visible);
        this.width = new SimpleDoubleProperty(initialWidth);
        this.description = new SimpleStringProperty(description);
    }

    TableColumn<LogEntry, String> createColumn() {
        TableColumn<LogEntry, String> col = new TableColumn<>();
        // we need to keep the original text to avoid breaking the table visibility menu
        col.textProperty().bind(headerLabel);
        col.setGraphic(createBoundHeaderLabel());
        col.setVisible(this.isVisible());
        col.setPrefWidth(width.get());
        visible.bindBidirectional(col.visibleProperty());
        width.bind(col.widthProperty());
        col.setCellValueFactory(data -> {
            LogEntry log = data.getValue();
            String cellValue = log.getColumnValues().get(this.getCapturingGroupName());
            return new ReadOnlyStringWrapper(cellValue);
        });
        return col;
    }

    private Label createBoundHeaderLabel() {
        Label header = new Label();
        header.textProperty().bind(headerLabel);
        header.tooltipProperty().bind(createTooltipBinding());
        // allows to show this label while hiding the default header text
        header.getStyleClass().add("column-header-label");
        // makes it take up the full width of the table column header so that the tooltip is shown more easily
        header.setMaxWidth(Double.MAX_VALUE);
        return header;
    }

    private Binding<Tooltip> createTooltipBinding() {
        Tooltip tooltip = new Tooltip();
        tooltip.textProperty().bind(description);

        BooleanBinding descriptionIsNull = description.isNull();
        Callable<Tooltip> tooltipCallable = () -> {
            if (descriptionIsNull.get()) {
                // no tooltip when no description
                return null;
            }
            return tooltip;
        };
        return Bindings.createObjectBinding(tooltipCallable, descriptionIsNull);
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
     * @return whether this column is visible
     */
    public boolean isVisible() {
        return visible.get();
    }

    /**
     * @return whether this column is visible (property)
     */
    public BooleanProperty visibleProperty() {
        return visible;
    }

    public StringProperty descriptionProperty() {
        return description;
    }
}
