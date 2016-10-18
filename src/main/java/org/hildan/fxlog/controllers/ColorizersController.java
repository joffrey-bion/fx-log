package org.hildan.fxlog.controllers;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import javafx.beans.binding.Binding;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ListBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableObjectValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.paint.Color;

import impl.org.controlsfx.autocompletion.AutoCompletionTextFieldBinding;
import org.hildan.fx.bindings.lists.MappedList;
import org.hildan.fx.bindings.lists.UnorderedMergedList;
import org.hildan.fx.components.list.BaseEditableListPane;
import org.hildan.fxlog.coloring.Colorizer;
import org.hildan.fxlog.coloring.StyleRule;
import org.hildan.fxlog.columns.ColumnDefinition;
import org.hildan.fxlog.config.Config;
import org.hildan.fxlog.filtering.Filter;
import org.hildan.fxlog.themes.Css;
import org.hildan.fxlog.view.UIUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Controller associated to the colorizers customization view.
 */
public class ColorizersController implements Initializable {

    private Config config;

    @FXML
    private BaseEditableListPane<Colorizer> colorizersPane;

    @FXML
    private SplitPane selectedColorizerPane;

    @FXML
    private BaseEditableListPane<StyleRule> rulesPane;

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

    private Property<Pattern> filterRegexFieldPatternBinding;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        config = Config.getInstance();
        initializeColorizersPane();
        initializeRulesPane();
        initializeStyleEditionPane();
    }

    private void initializeColorizersPane() {
        colorizersPane.setItemFactory(Colorizer::new);
        colorizersPane.setItemDuplicator(Colorizer::new);
        colorizersPane.getList().setItems(config.getColorizers());
        colorizersPane.itemInUseIndexProperty().bindBidirectional(config.getState().selectedColorizerIndexProperty());
    }

    private void initializeRulesPane() {
        ObservableObjectValue<Colorizer> selectedColorizer = colorizersPane.selectedItemProperty();
        selectedColorizerPane.disableProperty().bind(Bindings.isNull(selectedColorizer));

        ListBinding<StyleRule> selectedColorizerRules = UIUtils.selectList(selectedColorizer, Colorizer::getRules);
        rulesPane.setItemFactory(StyleRule::new);
        rulesPane.setItemDuplicator(StyleRule::new);
        rulesPane.getList().itemsProperty().bind(selectedColorizerRules);
    }

    private void initializeStyleEditionPane() {
        selectedRulePane.disableProperty().bind(rulesPane.selectedItemProperty().isNull());

        configureActivableColorPicker(foregroundColorPicker, overrideTextForeground);
        configureActivableColorPicker(backgroundColorPicker, overrideTextBackground);

        filterRegexFieldPatternBinding = new SimpleObjectProperty<>();
        createRegexFieldPatternBinding(filterRegexField, filterRegexFieldPatternBinding);

        bindRuleToUI(null); // initialize pane with empty values
        rulesPane.selectedItemProperty().addListener((obsRule, oldRule, newRule) -> {
            unbindRuleFromUI(oldRule);
            bindRuleToUI(newRule);
        });

        filterColumnNameField.disableProperty().bind(filterType.selectedToggleProperty().isEqualTo(matchRawButton));

        ObservableList<ObservableList<String>> columnGroupNames = new MappedList<>(config.getColumnizers(), c -> {
            return new MappedList<>(c.getColumnDefinitions(), ColumnDefinition::getCapturingGroupName);
        });
        ObservableList<String> autoCompleteList = new UnorderedMergedList<>(columnGroupNames);
        new AutoCompletionTextFieldBinding<>(filterColumnNameField, param -> {
            return autoCompleteList.stream().filter(s -> s.contains(param.getUserText())).collect(Collectors.toSet());
        });
    }

    private static void configureActivableColorPicker(@NotNull ColorPicker picker, @NotNull CheckBox checkbox) {
        picker.disableProperty().bind(checkbox.selectedProperty().not());
    }

    private static void createRegexFieldPatternBinding(TextField regexField, Property<Pattern> patternProperty) {
        ChangeListener<String> patternCreationListener = (obs, oldVal, newVal) -> {
            regexField.pseudoClassStateChanged(Css.INVALID, false);
            try {
                patternProperty.setValue(Pattern.compile(newVal));
            } catch (PatternSyntaxException e) {
                System.out.println("Invalid pattern, using the old one");
                regexField.pseudoClassStateChanged(Css.INVALID, true);
            }
        };
        regexField.textProperty().addListener(patternCreationListener);
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

        Filter ruleFilter = rule.getMatcher();
        boolean isRawFilter = ruleFilter.getColumnName() == null;
        filterType.selectToggle(isRawFilter ? matchRawButton : matchColumnButton);
        filterRegexField.setText(ruleFilter.getPattern().pattern());
        filterColumnNameField.setText(isRawFilter ? "" : rule.getMatcher().getColumnName());
        ruleFilter.patternProperty().bind(filterRegexFieldPatternBinding);

        Callable<String> getColumnName = () -> {
            if (filterType.getSelectedToggle() == matchRawButton) {
                return null;
            } else {
                return filterColumnNameField.getText();
            }
        };

        StringBinding filterColumnBinding =
                Bindings.createStringBinding(getColumnName, filterColumnNameField.textProperty(),
                        filterType.selectedToggleProperty());
        ruleFilter.columnNameProperty().bind(filterColumnBinding);

        bindActivableColorPicker(rule.getResult().backgroundColorProperty(), backgroundColorPicker,
                overrideTextBackground);
        bindActivableColorPicker(rule.getResult().foregroundColorProperty(), foregroundColorPicker,
                overrideTextForeground);
    }

    private static void unbindRuleFromUI(@Nullable StyleRule rule) {
        if (rule == null) {
            return;
        }
        rule.nameProperty().unbind();
        rule.getResult().backgroundColorProperty().unbind();
        rule.getResult().foregroundColorProperty().unbind();
        rule.getMatcher().columnNameProperty().unbind();
        rule.getMatcher().patternProperty().unbind();
    }

    private static void bindActivableColorPicker(@NotNull Property<Color> colorProperty, @NotNull ColorPicker picker,
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
}
