package org.mybatis.generator.api.dom.kotlin;

import java.util.ArrayList;
import java.util.List;

public abstract class KotlinNamedItem {
    private final String name;
    private final List<KotlinModifier> modifiers = new ArrayList<>();
    private final List<String> annotations = new ArrayList<>();

    protected KotlinNamedItem(AbstractBuilder<?> builder) {
        name = builder.name;
        modifiers.addAll(builder.modifiers);
        annotations.addAll(builder.annotations);
    }

    public String getName() {
        return name;
    }

    public List<KotlinModifier> getModifiers() {
        return modifiers;
    }

    public void addAnnotation(String annotation) {
        annotations.add(annotation);
    }

    public List<String> getAnnotations() {
        return annotations;
    }

    public abstract <R> R accept(KotlinNamedItemVisitor<R> visitor);

    public abstract static class AbstractBuilder<T extends AbstractBuilder<T>> {
        private final String name;
        private final List<KotlinModifier> modifiers = new ArrayList<>();
        private final List<String> annotations = new ArrayList<>();

        protected AbstractBuilder(String name) {
            this.name = name;
        }

        public T withModifier(KotlinModifier modifier) {
            modifiers.add(modifier);
            return getThis();
        }

        public T withAnnotation(String annotation) {
            annotations.add(annotation);
            return getThis();
        }

        protected abstract T getThis();
    }
}
