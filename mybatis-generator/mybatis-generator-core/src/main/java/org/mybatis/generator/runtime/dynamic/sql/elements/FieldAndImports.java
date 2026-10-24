package org.mybatis.generator.runtime.dynamic.sql.elements;

import java.util.HashSet;
import java.util.Set;

import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;

public class FieldAndImports {

    private final Field field;
    private final Set<FullyQualifiedJavaType> imports;

    private FieldAndImports(Builder builder) {
        field = builder.field;
        imports = builder.imports;
    }

    public Field getField() {
        return field;
    }

    public Set<FullyQualifiedJavaType> getImports() {
        return imports;
    }

    public static Builder withField(Field field) {
        return new Builder().withField(field);
    }

    public static class Builder {
        private Field field;
        private final Set<FullyQualifiedJavaType> imports = new HashSet<>();

        public Builder withField(Field field) {
            this.field = field;
            return this;
        }

        public Builder withImports(Set<FullyQualifiedJavaType> imports) {
            this.imports.addAll(imports);
            return this;
        }

        public FieldAndImports build() {
            return new FieldAndImports(this);
        }
    }
}
