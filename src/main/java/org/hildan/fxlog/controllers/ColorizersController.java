package org.hildan.fxlog.controllers;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javafx.beans.binding.Binding;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.IntegerExpression;
import javafx.beans.binding.ListBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;

import org.hildan.fxlog.coloring.Colorizer;
import org.hildan.fxlog.coloring.StyleRule;
import org.hildan.fxlog.config.Config;
import org.hildan.fxlog.filtering.Filter;
import org.hildan.fxlog.themes.Css;
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

    @FXML
    private Button removeColorizerButton;

    @FXML
    private Button removeRuleButton;

    private Property<Pattern> filterRegexFieldPatternBinding;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        config = Config.getInstance();
        initializeColorizersList();
        initializeSelectedColorizerPane();
        initializeSelectedRulePane();
        initializeDeleteButtons();
    }

    private void initializeColorizersList() {
        colorizersList.setItems(config.getColorizers());
        colorizersList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        colorizersList.setCellFactory(TextFieldListCell.forListView(new StringConverter<Colorizer>() {
            @Override
            public String toString(Colorizer colorizer) {
                return colorizer.getName();
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
            public String toString(StyleRule styleRule) {
                return styleRule.getName();
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
        BooleanBinding noRuleSelected = Bindings.isEmpty(rulesList.getSelectionModel().getSelectedItems());
        selectedRulePane.disableProperty().bind(noRuleSelected);

        configureActivableColorPicker(foregroundColorPicker, overrideTextForeground);
        configureActivableColorPicker(backgroundColorPicker, overrideTextBackground);

        filterRegexFieldPatternBinding = new SimpleObjectProperty<>();
        createRegexFieldPatternBinding(filterRegexField, filterRegexFieldPatternBinding);

        bindRuleToUI(null); // initialize pane with empty values
        rulesList.getSelectionModel().selectedItemProperty().addListener((obsRule, oldRule, newRule) -> {
            unbindRuleFromUI(oldRule);
            bindRuleToUI(newRule);
        });

        filterColumnNameField.disableProperty().bind(filterType.selectedToggleProperty().isEqualTo(matchRawButton));
    }

    private static void configureActivableColorPicker(@NotNull ColorPicker picker, @NotNull CheckBox checkbox) {
        picker.disableProperty().bind(checkbox.selectedProperty().not());
    }

    private static void createRegexFieldPatternBinding(TextField regexField, Property<Pattern> patternProperty) {
        ChangeListener<String> patternCreationListener = (obs, oldVal, newVal) -> {
            System.out.println("Creating new pattern from '" + newVal + "'");
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

    private void initializeDeleteButtons() {
        IntegerExpression currentlyUsedColorizer = config.selectedColorizerIndexProperty();
        IntegerExpression selectedColorizer = colorizersList.getSelectionModel().selectedIndexProperty();
        BooleanBinding selectedColorizerIsUsed = selectedColorizer.isEqualTo(currentlyUsedColorizer);
        BooleanBinding noColorizerSelected = colorizersList.getSelectionModel().selectedItemProperty().isNull();
        removeColorizerButton.disableProperty().bind(noColorizerSelected.or(selectedColorizerIsUsed));

        BooleanBinding noRuleSelected = rulesList.getSelectionModel().selectedItemProperty().isNull();
        removeRuleButton.disableProperty().bind(noRuleSelected);
    }

    @FXML
    public void addNewColorizer() {
        Colorizer newColorizer = new Colorizer(newColorizerNameField.getText());
        config.getColorizers().add(newColorizer);
        newColorizerNameField.setText("");
        colorizersList.getSelectionModel().select(newColorizer);
    }

    @FXML
    public void removeSelectedColorizer() {
        Colorizer selectedColorizer = colorizersList.getSelectionModel().getSelectedItem();
        config.getColorizers().remove(selectedColorizer);
    }

    @FXML
    public void addNewRule() {
        Colorizer selectedColorizer = colorizersList.getSelectionModel().getSelectedItem();
        StyleRule newRule = new StyleRule(newRuleNameField.getText());
        selectedColorizer.getRules().add(newRule);
        newRuleNameField.setText("");
        rulesList.getSelectionModel().select(newRule);
    }

    @FXML
    public void removeSelectedRule() {
        StyleRule selectedPattern = rulesList.getSelectionModel().getSelectedItem();
        Colorizer selectedColumnizer = colorizersList.getSelectionModel().getSelectedItem();
        selectedColumnizer.getRules().remove(selectedPattern);
    }
}
