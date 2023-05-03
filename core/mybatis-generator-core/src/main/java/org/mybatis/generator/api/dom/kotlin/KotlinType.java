package org.mybatis.generator.api.dom.kotlin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class KotlinType extends KotlinNamedItemContainer {

    private final List<KotlinProperty> constructorProperties = new ArrayList<>();
    private final Type type;
    private final Set<String> superTypes = new HashSet<>();

    public enum Type {
        CLASS("class"), //$NON-NLS-1$
        INTERFACE("interface"), //$NON-NLS-1$
        OBJECT("object"); //$NON-NLS-1$

        private final String value;

        Type(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    private KotlinType(Builder builder) {
        super(builder);
        this.type = Objects.requireNonNull(builder.type);
        constructorProperties.addAll(builder.constructorProperties);
        superTypes.addAll(builder.superTypes);
    }

    public List<KotlinProperty> getConstructorProperties() {
        return constructorProperties;
    }

    public Type getType() {
        return type;
    }

    public Set<String> getSuperTypes() {
        return superTypes;
    }

    public void addConstructorProperty(KotlinProperty property) {
        constructorProperties.add(property);
    }

    public void addSuperType(String superType) {
        superTypes.add(superType);
    }

    @Override
    public <R> R accept(KotlinNamedItemVisitor<R> visitor) {
        return visitor.visit(this);
    }

    public static Builder newClass(String name) {
        return new Builder(Type.CLASS, name);
    }

    public static Builder newInterface(String name) {
        return new Builder(Type.INTERFACE, name);
    }

    public static Builder newObject(String name) {
        return new Builder(Type.OBJECT, name);
    }

    public static class Builder extends NamedItemContainerBuilder<Builder> {
        private final Type type;
        private final List<KotlinProperty> constructorProperties = new ArrayList<>();
        private final List<String> superTypes = new ArrayList<>();

        private Builder(Type type, String name) {
            super(name);
            this.type = type;
        }

        public Builder withConstructorProperty(KotlinProperty kotlinProperty) {
            constructorProperties.add(kotlinProperty);
            return this;
        }

        public Builder withSuperType(String superType) {
            superTypes.add(superType);
            return this;
        }

        public Builder getThis() {
            return this;
        }

        public KotlinType build() {
            return new KotlinType(this);
        }
    }
}
