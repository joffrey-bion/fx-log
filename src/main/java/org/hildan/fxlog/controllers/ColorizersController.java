package org.hildan.fxlog.controllers;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;
import java.util.regex.PatternSyntaxException;

import javafx.beans.binding.Binding;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.ListBinding;
import javafx.beans.property.Property;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.*;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;

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
        colorizersList.setCellFactory(TextFieldListCell.forListView(new StringConverter<Colorizer>() {
            @Override
            public String toString(Colorizer colorizer) {
                return colorizer.toString();
            }

            @Override
            public Colorizer fromString(String string) {
                // temporary object
                return new Colorizer(string);
            }
        }));
        colorizersList.setOnEditCommit(e -> {
            Colorizer editedColorizer = colorizersList.getItems().get(e.getIndex());
            editedColorizer.setName(e.getNewValue().getName());
        });
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
        rulesList.setCellFactory(TextFieldListCell.forListView(new StringConverter<StyleRule>() {
            @Override
            public String toString(StyleRule columnizer) {
                return columnizer.toString();
            }

            @Override
            public StyleRule fromString(String string) {
                // temporary object
                return new StyleRule(string);
            }
        }));
        rulesList.setOnEditCommit(e -> {
            StyleRule editedRule = rulesList.getItems().get(e.getIndex());
            editedRule.setName(e.getNewValue().getName());
        });

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
            overrideTextBackground.setSelected(false);
            overrideTextForeground.setSelected(false);
            backgroundColorPicker.setValue(null);
            foregroundColorPicker.setValue(null);
            filterType.selectToggle(matchRawButton);
            filterRegexField.setText("");
            filterColumnNameField.setText("");
            return;
        }

        boolean isRawFilter = rule.getFilter().getColumnName() == null;
        filterType.selectToggle(isRawFilter ? matchRawButton : matchColumnButton);
        filterRegexField.setText(rule.getFilter().getPattern().toString());
        filterColumnNameField.setText(isRawFilter ? "" : rule.getFilter().getColumnName());
        Callable<Filter> createFilter = () -> {
            PseudoClass errorClass = PseudoClass.getPseudoClass("error");
            try {
                filterRegexField.pseudoClassStateChanged(errorClass, false);
                if (filterType.getSelectedToggle() == matchRawButton) {
                    return Filter.matchRawLog(filterRegexField.getText());
                } else {
                    return Filter.matchColumn(filterColumnNameField.getText(), filterRegexField.getText());
                }
            } catch (PatternSyntaxException e) {
                filterRegexField.pseudoClassStateChanged(errorClass, true);
                return Filter.matchRawLog(".*");
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
    public void addNewColorizer() {
        Colorizer newColorizer = new Colorizer(newColorizerNameField.getText());
        config.getColorizers().add(newColorizer);
        newColorizerNameField.setText("");
        colorizersList.getSelectionModel().select(newColorizer);
    }

    @FXML
    public void addNewRule() {
        Colorizer selectedColorizer = colorizersList.getSelectionModel().getSelectedItem();
        StyleRule newRule = new StyleRule(newRuleNameField.getText());
        selectedColorizer.getRules().add(newRule);
        newRuleNameField.setText("");
        rulesList.getSelectionModel().select(newRule);
    }
}
