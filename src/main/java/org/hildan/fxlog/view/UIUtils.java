package org.hildan.fxlog.view;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Consumer;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.IntegerExpression;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.Window;

import com.sun.javafx.scene.control.skin.TableViewSkin;
import com.sun.javafx.scene.control.skin.VirtualFlow;
import org.controlsfx.control.textfield.CustomTextField;
import org.controlsfx.control.textfield.TextFields;
import org.hildan.fxlog.FXLog;
import org.hildan.fxlog.errors.ErrorDialog;
import org.hildan.fxlog.themes.Theme;

public class UIUtils {

    /**
     * Creates a new Stage with the given parameters.
     * <p>
     * This method does not throw an exception if the view is not found, but shows an error dialog instead.
     *
     * @param fxmlFile
     *         the FXML filename (with extension) of the view to load
     * @param title
     *         the title of the new stage
     * @param theme
     *         the CSS theme to use for the new stage
     *
     * @return a new Stage with the given title. If an error occurred while loading the view, the returned stage has no
     * attached Scene. Otherwise, a new Scene is created for the view and attached to returned Stage.
     */
    public static Stage createStage(String fxmlFile, String title, Theme theme) {
        Stage stage = new Stage();
        stage.setTitle(title);
        try {
            Parent root = FXLog.loadView(fxmlFile);
            Scene scene = new Scene(root);
            stage.setScene(scene);
            InputStream icon = UIUtils.class.getResourceAsStream(FXLog.APP_ICON_PATH);
            stage.getIcons().add(new Image(icon));
            theme.apply(scene);
        } catch (IOException e) {
            ErrorDialog.uncaughtException(e);
        }
        return stage;
    }

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

    public static BooleanBinding noItemIsSelected(ListView listView) {
        return listView.getSelectionModel().selectedItemProperty().isNull();
    }

    public static BooleanBinding firstItemIsSelected(ListView listView) {
        return listView.getSelectionModel().selectedIndexProperty().isEqualTo(0);
    }

    public static BooleanBinding lastItemIsSelected(ListView listView) {
        IntegerExpression lastIndex =
                Bindings.createIntegerBinding(() -> listView.getItems().size() - 1, listView.itemsProperty());
        return listView.getSelectionModel().selectedIndexProperty().isEqualTo(lastIndex);
    }

    public static BooleanBinding noItemIsSelected(TableView listView) {
        return listView.getSelectionModel().selectedItemProperty().isNull();
    }

    public static BooleanBinding firstItemIsSelected(TableView listView) {
        return listView.getSelectionModel().selectedIndexProperty().isEqualTo(0);
    }

    public static BooleanBinding lastItemIsSelected(TableView listView) {
        IntegerExpression lastIndex =
                Bindings.createIntegerBinding(() -> listView.getItems().size() - 1, listView.itemsProperty());
        return listView.getSelectionModel().selectedIndexProperty().isEqualTo(lastIndex);
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
}
