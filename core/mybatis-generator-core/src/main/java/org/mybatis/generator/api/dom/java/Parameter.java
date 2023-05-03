package org.mybatis.generator.api.dom.java;

import java.util.ArrayList;
import java.util.List;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.render.ParameterRenderer;

public class Parameter {
    private final String name;
    private final FullyQualifiedJavaType type;
    private final boolean isVarargs;

    private String remark = "";

    private final List<String> annotations = new ArrayList<>();

    public Parameter(FullyQualifiedJavaType type, String name) {
        this(type, name, false);
    }

    public Parameter(FullyQualifiedJavaType type, String name, boolean isVarargs) {
        this.name = name;
        this.type = type;
        this.isVarargs = isVarargs;
    }

    public Parameter(FullyQualifiedJavaType type, String name, String annotation) {
        this(type, name, false);
        addAnnotation(annotation);
    }

    public Parameter(FullyQualifiedJavaType type, String name, String annotation, boolean isVarargs) {
        this(type, name, isVarargs);
        addAnnotation(annotation);
    }

    public Parameter(IntrospectedColumn column){
        this(column.getFullyQualifiedJavaType(),column.getJavaProperty());
        this.remark = column.getRemarks(false);
    }

    public String getName() {
        return name;
    }

    public FullyQualifiedJavaType getType() {
        return type;
    }

    public List<String> getAnnotations() {
        return annotations;
    }

    public void addAnnotation(String annotation) {
        annotations.add(annotation);
    }

    @Override
    public String toString() {
        return new ParameterRenderer().render(this, null);
    }

    public boolean isVarargs() {
        return isVarargs;
    }

    public String getRemark() {
        return remark;
    }

    public Parameter setRemark(String remark) {
        this.remark = remark;
        return this;
    }
}
