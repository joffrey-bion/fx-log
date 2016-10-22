package org.hildan.fxlog.view;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Consumer;
import java.util.function.Function;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.IntegerExpression;
import javafx.beans.binding.ListBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Window;

import com.sun.javafx.scene.control.skin.TableViewSkin;
import com.sun.javafx.scene.control.skin.VirtualFlow;
import org.controlsfx.control.textfield.CustomTextField;
import org.controlsfx.control.textfield.TextFields;

public class UIUtils {

    /**
     * Adds a clear button to the right of the text field. The little cross appears only when the field is not empty.
     *
     * @param customTextField
     *         the text field to decorate
     */
    public static void makeClearable(CustomTextField customTextField) {
        try {
            Method method =
                    TextFields.class.getDeclaredMethod("setupClearButtonField", TextField.class, ObjectProperty.class);
            method.setAccessible(true);
            method.invoke(null, customTextField, customTextField.rightProperty());
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public static BooleanBinding noItemIsSelected(TableView<?> tableView) {
        return tableView.getSelectionModel().selectedItemProperty().isNull();
    }

    public static BooleanBinding firstItemIsSelected(TableView<?> tableView) {
        return tableView.getSelectionModel().selectedIndexProperty().isEqualTo(0);
    }

    public static BooleanBinding lastItemIsSelected(TableView<?> tableView) {
        IntegerExpression lastIndex =
                Bindings.createIntegerBinding(() -> tableView.getItems().size() - 1, tableView.itemsProperty());
        return tableView.getSelectionModel().selectedIndexProperty().isEqualTo(lastIndex);
    }

    /**
     * Scrolls the given table view to the given item. This is nicer than {@link TableView#scrollTo(int)} because it
     * doesn't put the target item at the top, but rather at about a third of the screen.
     *
     * @param table
     *         the table view to scroll
     * @param index
     *         the index to scroll to
     */
    @SuppressWarnings("WeakerAccess")
    public static void scrollTo(TableView table, int index) {
        int numberOfVisibleItems = getNumberOfVisibleItems(table);
        table.scrollTo(index - numberOfVisibleItems / 3);
    }

    @SuppressWarnings("WeakerAccess")
    public static int getNumberOfVisibleItems(TableView table) {
        return getLastVisibleRowIndex(table) - getFirstVisibleRowIndex(table);
    }

    public static int getFirstVisibleRowIndex(TableView table) {
        VirtualFlow<?> flow = getVirtualFlow(table);
        if (flow == null || flow.getFirstVisibleCellWithinViewPort() == null) {
            return 0;
        }
        int indexFirst = flow.getFirstVisibleCellWithinViewPort().getIndex();
        if (indexFirst >= table.getItems().size()) {
            return table.getItems().size() - 1;
        } else {
            return indexFirst;
        }
    }

    @SuppressWarnings("WeakerAccess")
    public static int getLastVisibleRowIndex(TableView table) {
        VirtualFlow<?> flow = getVirtualFlow(table);
        if (flow == null || flow.getLastVisibleCellWithinViewPort() == null) {
            return 0;
        }
        int index = flow.getLastVisibleCellWithinViewPort().getIndex();
        if (index >= table.getItems().size()) {
            return table.getItems().size() - 1;
        } else {
            return index;
        }
    }

    private static VirtualFlow<?> getVirtualFlow(TableView table) {
        TableViewSkin<?> skin = (TableViewSkin) table.getSkin();
        if (skin == null) {
            return null;
        }
        return (VirtualFlow) skin.getChildren().get(1);
    }

    public static void whenWindowReady(Node anyChild, Consumer<? super Window> handler) {
        ChangeListener<Scene> onNewScene = (obsScene, oldScene, newScene) -> {
            ChangeListener<? super Window> oneShotWindowListener = asOneShotListener(asListener(handler));
            newScene.windowProperty().addListener(oneShotWindowListener);
        };

        anyChild.sceneProperty().addListener(asOneShotListener(onNewScene));
    }

    private static <T> ChangeListener<T> asListener(Consumer<T> handler) {
        return (obs, oldValue, newValue) -> handler.accept(newValue);
    }

    private static <T> ChangeListener<T> asOneShotListener(ChangeListener<T> handler) {
        return new ChangeListener<T>() {
            public void changed(ObservableValue<? extends T> obs, T oldValue, T newValue) {
                handler.changed(obs, oldValue, newValue);
                obs.removeListener(this);
            }
        };
    }

    public static <S, C> ListBinding<C> selectList(ObservableValue<S> source, Function<S, ObservableList<C>> getList) {
        return new ListBinding<C>() {
            {
                bind(source);
            }

            @Override
            protected ObservableList<C> computeValue() {
                S sourceValue = source.getValue();
                if (sourceValue != null) {
                    return getList.apply(sourceValue);
                } else {
                    return FXCollections.emptyObservableList();
                }
            }
        };
    }
}
