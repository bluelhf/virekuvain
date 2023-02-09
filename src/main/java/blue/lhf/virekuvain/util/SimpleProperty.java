package blue.lhf.virekuvain.util;

import javafx.beans.property.*;

public class SimpleProperty<T> extends ObjectPropertyBase<T> {
    private final Object bean;
    private final String name;

    public SimpleProperty(final Object bean, final String name) {
        this.bean = bean;
        this.name = name;
    }

    @Override
    public Object getBean() {
        return bean;
    }

    @Override
    public String getName() {
        return name;
    }
}
