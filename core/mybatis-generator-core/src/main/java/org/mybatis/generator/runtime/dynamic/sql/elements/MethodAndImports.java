package org.mybatis.generator.runtime.dynamic.sql.elements;

import java.util.HashSet;
import java.util.Set;

import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;

public class MethodAndImports {

    private final Method method;
    private final Set<FullyQualifiedJavaType> imports;
    private final Set<String> staticImports;

    private MethodAndImports(Builder builder) {
        method = builder.method;
        imports = builder.imports;
        staticImports = builder.staticImports;
    }

    public Method getMethod() {
        return method;
    }

    public Set<FullyQualifiedJavaType> getImports() {
        return imports;
    }

    public Set<String> getStaticImports() {
        return staticImports;
    }

    public static Builder withMethod(Method method) {
        return new Builder().withMethod(method);
    }

    public static class Builder {
        private Method method;
        private final Set<FullyQualifiedJavaType> imports = new HashSet<>();
        private final Set<String> staticImports = new HashSet<>();

        public Builder withMethod(Method method) {
            this.method = method;
            return this;
        }

        public Builder withImport(FullyQualifiedJavaType importedType) {
            this.imports.add(importedType);
            return this;
        }

        public Builder withImports(Set<FullyQualifiedJavaType> imports) {
            this.imports.addAll(imports);
            return this;
        }

        public Builder withStaticImport(String staticImport) {
            this.staticImports.add(staticImport);
            return this;
        }

        public Builder withStaticImports(Set<String> staticImports) {
            this.staticImports.addAll(staticImports);
            return this;
        }

        public MethodAndImports build() {
            return new MethodAndImports(this);
        }
    }
}
