package org.mybatis.generator.api.dom.java;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.render.ParameterRenderer;

/**
 * @author cen_c
 */
@Getter
@Setter
public class Parameter {

    private FullyQualifiedJavaType type;
    private String remark = "";

    private final String name;
    private final boolean isVarargs;
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

    public void addAnnotation(String annotation) {
        annotations.add(annotation);
    }

    @Override
    public String toString() {
        return new ParameterRenderer().render(this, null);
    }

    public Parameter setRemark(String remark) {
        this.remark = remark;
        return this;
    }
}
