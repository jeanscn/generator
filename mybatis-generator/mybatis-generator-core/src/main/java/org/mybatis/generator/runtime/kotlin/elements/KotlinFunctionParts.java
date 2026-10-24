package org.mybatis.generator.runtime.kotlin.elements;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.mybatis.generator.api.dom.kotlin.KotlinArg;

public class KotlinFunctionParts {

    private final List<String> annotations;
    private final List<String> codeLines;
    private final Set<String> imports;
    private final List<KotlinArg> arguments;

    private KotlinFunctionParts(Builder builder) {
        imports = builder.imports;
        codeLines = builder.codeLines;
        arguments = builder.arguments;
        annotations = builder.annotations;
    }

    public Set<String> getImports() {
        return imports;
    }

    public List<String> getAnnotations() {
        return annotations;
    }

    public List<String> getCodeLines() {
        return codeLines;
    }

    public List<KotlinArg> getArguments() {
        return arguments;
    }

    public static class Builder {
        private final List<String> codeLines = new ArrayList<>();
        private final Set<String> imports = new HashSet<>();
        private final List<KotlinArg> arguments = new ArrayList<>();
        private final List<String> annotations = new ArrayList<>();

        public Builder withAnnotation(String annotation) {
            annotations.add(annotation);
            return this;
        }

        public Builder withCodeLine(String codeLine) {
            this.codeLines.add(codeLine);
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

        public Builder withArgument(KotlinArg argument) {
            arguments.add(argument);
            return this;
        }

        public KotlinFunctionParts build() {
            return new KotlinFunctionParts(this);
        }
    }
}
