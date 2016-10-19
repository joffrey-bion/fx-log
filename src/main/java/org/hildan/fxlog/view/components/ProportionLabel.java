package org.hildan.fxlog.view.components;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.*;

public class ProportionLabel<T extends Number> extends Label {

    private static final String DEFAULT_TEMPLATE = "%d";

    private static final String DEFAULT_TEMPLATE_WITH_TOTAL = "%d / %d";

    private final StringProperty template = new SimpleStringProperty(DEFAULT_TEMPLATE);

    private final StringProperty templateWithTotal = new SimpleStringProperty(DEFAULT_TEMPLATE_WITH_TOTAL);

    private final BooleanProperty showTotal = new SimpleBooleanProperty(false);

    private final ObjectProperty<T> currentCount = new SimpleObjectProperty<>();

    private final ObjectProperty<T> totalCount = new SimpleObjectProperty<>();

    private final StringBinding effectiveTemplate;

    // defined as a field to avoid garbage collection
    @SuppressWarnings("FieldCanBeLocal")
    private final StringBinding text;

    public ProportionLabel() {
        effectiveTemplate = Bindings.createStringBinding(this::computeTemplate, template, templateWithTotal, showTotal);
        text = Bindings.createStringBinding(this::computeText, currentCount, totalCount, effectiveTemplate, showTotal);
        textProperty().bind(text);
    }

    private String computeTemplate() {
        if (showTotal.get()) {
            return templateWithTotal.get();
        } else {
            return template.get();
        }
    }

    private String computeText() {
        if (showTotal.get()) {
            return String.format(effectiveTemplate.get(), currentCount.get(), totalCount.get());
        } else {
            return String.format(effectiveTemplate.get(), currentCount.get());
        }
    }

    public String getTemplate() {
        return template.get();
    }

    public StringProperty templateProperty() {
        return template;
    }

    public void setTemplate(String template) {
        this.template.set(template);
    }

    public String getTemplateWithTotal() {
        return templateWithTotal.get();
    }

    public StringProperty templateWithTotalProperty() {
        return templateWithTotal;
    }

    public void setTemplateWithTotal(String templateWithTotal) {
        this.templateWithTotal.set(templateWithTotal);
    }

    public boolean getShowTotal() {
        return showTotal.get();
    }

    public BooleanProperty showTotalProperty() {
        return showTotal;
    }

    public void setShowTotal(boolean showTotal) {
        this.showTotal.set(showTotal);
    }

    public T getCurrentCount() {
        return currentCount.get();
    }

    public ObjectProperty<T> currentCountProperty() {
        return currentCount;
    }

    public void setCurrentCount(T currentCount) {
        this.currentCount.set(currentCount);
    }

    public T getTotalCount() {
        return totalCount.get();
    }

    public ObjectProperty<T> totalCountProperty() {
        return totalCount;
    }

    public void setTotalCount(T totalCount) {
        this.totalCount.set(totalCount);
    }
}
