package org.mybatis.generator.runtime.kotlin.elements;

import java.util.HashSet;
import java.util.Set;

import org.mybatis.generator.api.dom.kotlin.KotlinProperty;

public class KotlinPropertyAndImports {

    private final KotlinProperty property;
    private final Set<String> imports;

    private KotlinPropertyAndImports(Builder builder) {
        property = builder.property;
        imports = builder.imports;
    }

    public KotlinProperty getProperty() {
        return property;
    }

    public Set<String> getImports() {
        return imports;
    }

    public static Builder withProperty(KotlinProperty property) {
        return new Builder().withProperty(property);
    }

    public static class Builder {
        private KotlinProperty property;
        private final Set<String> imports = new HashSet<>();

        public Builder withProperty(KotlinProperty property) {
            this.property = property;
            return this;
        }

        public Builder withImports(Set<String> imports) {
            this.imports.addAll(imports);
            return this;
        }

        public KotlinPropertyAndImports build() {
            return new KotlinPropertyAndImports(this);
        }
    }
}
