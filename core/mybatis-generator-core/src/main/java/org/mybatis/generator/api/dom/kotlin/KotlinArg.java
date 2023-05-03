package org.mybatis.generator.api.dom.kotlin;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class KotlinArg {

    private final String name;
    private final String dataType;
    private final String initializationString;
    private final List<String> annotations;

    private KotlinArg(Builder builder) {
        name = Objects.requireNonNull(builder.name);
        dataType = builder.dataType;
        initializationString = builder.initializationString;
        annotations = builder.annotations;
    }

    public String getName() {
        return name;
    }

    public Optional<String> getInitializationString() {
        return Optional.ofNullable(initializationString);
    }

    public Optional<String> getDataType() {
        return Optional.ofNullable(dataType);
    }

    public List<String> getAnnotations() {
        return annotations;
    }

    public static Builder newArg(String name) {
        return new Builder(name);
    }

    public static class Builder {
        private final String name;
        private String dataType;
        private String initializationString;
        private final List<String> annotations = new ArrayList<>();

        private Builder(String name) {
            this.name = name;
        }

        public Builder withInitializationString(String initializationString) {
            this.initializationString = initializationString;
            return this;
        }

        public Builder withDataType(String dataType) {
            this.dataType = dataType;
            return this;
        }

        public Builder withAnnotation(String annotation) {
            annotations.add(annotation);
            return this;
        }

        public KotlinArg build() {
            return new KotlinArg(this);
        }
    }
}
