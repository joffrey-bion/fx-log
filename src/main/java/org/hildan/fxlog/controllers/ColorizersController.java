package org.hildan.fxlog.controllers;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;

import javafx.beans.binding.Binding;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.ListBinding;
import javafx.beans.property.Property;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.paint.Color;

import org.hildan.fxlog.coloring.Colorizer;
import org.hildan.fxlog.coloring.StyleRule;
import org.hildan.fxlog.config.Config;
import org.hildan.fxlog.filtering.Filter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Controller associated to the colorizers customization view.
 */
public class ColorizersController implements Initializable {

    private Config config;

    @FXML
    private ListView<Colorizer> colorizersList;

    @FXML
    private TextField newColorizerNameField;

    @FXML
    private SplitPane selectedColorizerPane;

    @FXML
    private ListView<StyleRule> rulesList;

    @FXML
    private TextField newRuleNameField;

    @FXML
    private ScrollPane selectedRulePane;

    @FXML
    private TextField ruleNameField;

    @FXML
    private TextField filterRegexField;

    @FXML
    private ToggleGroup filterType;

    @FXML
    private RadioButton matchRawButton;

    @FXML
    private RadioButton matchColumnButton;

    @FXML
    private TextField filterColumnNameField;

    @FXML
    private CheckBox overrideTextForeground;

    @FXML
    private CheckBox overrideTextBackground;

    @FXML
    private ColorPicker foregroundColorPicker;

    @FXML
    private ColorPicker backgroundColorPicker;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        config = Config.getInstance();
        initializeColorizersList();
        initializeSelectedColorizerPane();
        initializeSelectedRulePane();
    }

    private void initializeColorizersList() {
        colorizersList.setItems(config.getColorizers());
        colorizersList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    }

    private void initializeSelectedColorizerPane() {
        BooleanBinding isEmpty = Bindings.isEmpty(colorizersList.getSelectionModel().getSelectedItems());
        selectedColorizerPane.disableProperty().bind(isEmpty);
        ListBinding<StyleRule> rulesOfColorizer = new ListBinding<StyleRule>() {
            {
                bind(colorizersList.getSelectionModel().selectedItemProperty());
            }

            @Override
            protected ObservableList<StyleRule> computeValue() {
                Colorizer selectedColorizer = colorizersList.getSelectionModel().getSelectedItem();
                if (selectedColorizer != null) {
                    return selectedColorizer.getRules();
                } else {
                    return FXCollections.emptyObservableList();
                }
            }
        };
        rulesList.itemsProperty().bind(rulesOfColorizer);
        rulesList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    }

    private void initializeSelectedRulePane() {
        BooleanBinding isEmpty = Bindings.isEmpty(rulesList.getSelectionModel().getSelectedItems());
        selectedRulePane.disableProperty().bind(isEmpty);
        configureActivableColorPicker(foregroundColorPicker, overrideTextForeground);
        configureActivableColorPicker(backgroundColorPicker, overrideTextBackground);
        bindRuleToUI(null); // initialize pane with empty values
        rulesList.getSelectionModel().selectedItemProperty().addListener((obsRule, oldRule, newRule) -> {
            unbindRuleFromUI(oldRule);
            bindRuleToUI(newRule);
        });
        filterColumnNameField.disableProperty().bind(filterType.selectedToggleProperty().isEqualTo(matchRawButton));
    }

    private void configureActivableColorPicker(@NotNull ColorPicker picker, @NotNull CheckBox checkbox) {
        picker.disableProperty().bind(checkbox.selectedProperty().not());
    }

    private void bindRuleToUI(@Nullable StyleRule rule) {
        if (rule == null) {
            ruleNameField.setText("");
            overrideTextBackground.setSelected(false);
            overrideTextForeground.setSelected(false);
            backgroundColorPicker.setValue(null);
            foregroundColorPicker.setValue(null);
            filterType.selectToggle(matchRawButton);
            filterRegexField.setText("");
            filterColumnNameField.setText("");
            return;
        }
        ruleNameField.setText(rule.getName());
        rule.nameProperty().bind(ruleNameField.textProperty());

        boolean isRawFilter = rule.getFilter().getColumnName() == null;
        filterType.selectToggle(isRawFilter ? matchRawButton : matchColumnButton);
        filterRegexField.setText(rule.getFilter().getPattern().toString());
        filterColumnNameField.setText(isRawFilter ? "" : rule.getFilter().getColumnName());
        Callable<Filter> createFilter = () -> {
            if (filterType.getSelectedToggle() == matchRawButton) {
                return Filter.matchRawLog(filterRegexField.getText());
            } else {
                return Filter.matchColumn(filterColumnNameField.getText(), filterRegexField.getText());
            }
        };
        Binding<Filter> filterRegexBinding =
                Bindings.createObjectBinding(createFilter, filterColumnNameField.textProperty(),
                        filterRegexField.textProperty(), filterType.selectedToggleProperty());
        rule.filterProperty().bind(filterRegexBinding);

        bindActivableColorPicker(rule.backgroundProperty(), backgroundColorPicker, overrideTextBackground);
        bindActivableColorPicker(rule.foregroundProperty(), foregroundColorPicker, overrideTextForeground);
    }

    private void unbindRuleFromUI(@Nullable StyleRule rule) {
        if (rule == null) {
            return;
        }
        rule.nameProperty().unbind();
        rule.filterProperty().unbind();
        rule.backgroundProperty().unbind();
        rule.foregroundProperty().unbind();
    }

    private void bindActivableColorPicker(@NotNull Property<Color> colorProperty, @NotNull ColorPicker picker,
                                          @NotNull CheckBox checkbox) {
        // initial values
        Color currentValue = colorProperty.getValue();
        picker.setValue(currentValue == null ? Color.WHITE : currentValue);
        checkbox.setSelected(currentValue != null);

        // binding
        Callable<Color> getColor = () -> checkbox.isSelected() ? picker.getValue() : null;
        Binding<Color> colorBinding =
                Bindings.createObjectBinding(getColor, checkbox.selectedProperty(), picker.valueProperty());
        colorProperty.bind(colorBinding);
    }

    @FXML
    void createNewColorizer() {
        Colorizer newColorizer = new Colorizer(newColorizerNameField.getText());
        config.getColorizers().add(newColorizer);
        newColorizerNameField.setText("");
        colorizersList.getSelectionModel().select(newColorizer);
    }

    @FXML
    void createNewRule() {
        Colorizer selectedColorizer = colorizersList.getSelectionModel().getSelectedItem();
        StyleRule newRule = new StyleRule(newRuleNameField.getText());
        selectedColorizer.getRules().add(newRule);
        newRuleNameField.setText("");
        rulesList.getSelectionModel().select(newRule);
    }
}
