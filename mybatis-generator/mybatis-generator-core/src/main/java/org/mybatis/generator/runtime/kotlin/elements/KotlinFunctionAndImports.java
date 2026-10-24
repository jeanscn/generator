package org.mybatis.generator.runtime.kotlin.elements;

import java.util.HashSet;
import java.util.Set;

import org.mybatis.generator.api.dom.kotlin.KotlinFunction;

public class KotlinFunctionAndImports {

    private final KotlinFunction function;
    private final Set<String> imports;

    private KotlinFunctionAndImports(Builder builder) {
        function = builder.function;
        imports = builder.imports;
    }

    public KotlinFunction getFunction() {
        return function;
    }

    public Set<String> getImports() {
        return imports;
    }

    public static Builder withFunction(KotlinFunction function) {
        return new Builder().withFunction(function);
    }

    public static class Builder {
        private KotlinFunction function;
        private final Set<String> imports = new HashSet<>();

        public Builder withFunction(KotlinFunction function) {
            this.function = function;
            return this;
        }

        public Builder withImport(String im) {
            this.imports.add(im);
            return this;
        }

        public Builder withImports(Set<String> imports) {
            this.imports.addAll(imports);
            return this;
        }

        public KotlinFunctionAndImports build() {
            return new KotlinFunctionAndImports(this);
        }
    }
}
