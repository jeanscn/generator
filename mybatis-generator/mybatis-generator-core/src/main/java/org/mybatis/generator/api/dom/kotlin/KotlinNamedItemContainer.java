package org.mybatis.generator.api.dom.kotlin;

import java.util.ArrayList;
import java.util.List;

public abstract class KotlinNamedItemContainer extends KotlinNamedItem {
    private final List<KotlinNamedItem> namedItems = new ArrayList<>();

    protected KotlinNamedItemContainer(NamedItemContainerBuilder<?> builder) {
        super(builder);
        namedItems.addAll(builder.namedItems);
    }

    public void addNamedItem(KotlinNamedItem namedItem) {
        namedItems.add(namedItem);
    }

    public List<KotlinNamedItem> getNamedItems() {
        return namedItems;
    }

    public abstract static class NamedItemContainerBuilder<T extends NamedItemContainerBuilder<T>>
            extends AbstractBuilder<T> {

        private final List<KotlinNamedItem> namedItems = new ArrayList<>();

        protected NamedItemContainerBuilder(String name) {
            super(name);
        }

        public T withNamedItem(KotlinNamedItem namedItem) {
            namedItems.add(namedItem);
            return getThis();
        }

        protected abstract T getThis();
    }
}
