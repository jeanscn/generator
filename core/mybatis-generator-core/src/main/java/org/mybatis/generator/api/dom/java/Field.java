package org.mybatis.generator.api.dom.java;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;
import java.util.Optional;

/**
 * @author cen_c
 */
@Setter
@Getter
public class Field extends JavaElement {

    private FullyQualifiedJavaType type;

    private String name;

    private String initializationString;

    private boolean isTransient;

    private boolean isVolatile;

    private boolean isFinal;

    private String remark;

    private String sourceColumnName;

    private Boolean isRequired;

    public Field(String name, FullyQualifiedJavaType type) {
        this.name = name;
        this.type = type;
    }

    public Field(Field field) {
        super(field);
        this.type = field.type;
        this.name = field.name;
        this.initializationString = field.initializationString;
        this.isTransient = field.isTransient;
        this.isVolatile = field.isVolatile;
        this.isFinal = field.isFinal;
        this.remark = field.remark;
        this.sourceColumnName = field.sourceColumnName;
        this.isRequired = field.isRequired;
    }

    public Optional<String> getInitializationString() {
        return Optional.ofNullable(initializationString);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (!(o instanceof Field field)) { return false; }
        return Objects.equals(getName(), field.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName());
    }
}
