package org.mybatis.generator.api.dom.kotlin;

import java.util.Objects;
import java.util.Optional;

public class KotlinProperty extends KotlinNamedItem {

    private final String dataType;
    private final String initializationString;
    private final Type type;

    public enum Type {
        VAL("val"), //$NON-NLS-1$
        VAR("var"); //$NON-NLS-1$

        private final String value;

        Type(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    private KotlinProperty(Builder builder) {
        super(builder);
        dataType = builder.dataType;
        initializationString = builder.initializationString;
        type = Objects.requireNonNull(builder.type);
    }

    public Optional<String> getInitializationString() {
        return Optional.ofNullable(initializationString);
    }

    public Optional<String> getDataType() {
        return Optional.ofNullable(dataType);
    }

    public Type getType() {
        return type;
    }

    @Override
    public <R> R accept(KotlinNamedItemVisitor<R> visitor) {
        return visitor.visit(this);
    }

    public static Builder newVal(String name) {
        return new Builder(Type.VAL, name);
    }

    public static Builder newVar(String name) {
        return new Builder(Type.VAR, name);
    }

    public static class Builder extends AbstractBuilder<Builder> {
        private String dataType;
        private String initializationString;
        private final Type type;

        private Builder(Type type, String name) {
            super(name);
            this.type = type;
        }

        public Builder withInitializationString(String initializationString) {
            this.initializationString = initializationString;
            return this;
        }

        public Builder withDataType(String dataType) {
            this.dataType = dataType;
            return this;
        }

        public Builder getThis() {
            return this;
        }

        public KotlinProperty build() {
            return new KotlinProperty(this);
        }
    }
}
