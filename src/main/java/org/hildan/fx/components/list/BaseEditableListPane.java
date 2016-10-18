package org.hildan.fx.components.list;

import java.util.function.Function;

public class BaseEditableListPane<T extends Named> extends EditableListPane<T> {

    public BaseEditableListPane() {
        super();
        setNewItemValidator(s -> !s.isEmpty());
    }

    public void setItemFactory(Function<String, T> itemFactory) {
        super.setItemFactory(itemFactory);
        getList().setConverter(itemFactory, T::getName, s -> !s.isEmpty());
        getList().setUpdater(T::setName);
    }
}
