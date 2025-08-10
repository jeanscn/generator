package org.mybatis.generator.api.dom.java.render;

import java.util.ArrayList;
import java.util.List;

import org.mybatis.generator.api.dom.java.CompilationUnit;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.JavaDomUtils;

/**
 * @author cen_c
 */
public class FieldRenderer {

    public List<String> render(Field field, CompilationUnit compilationUnit) {
        List<String> lines = new ArrayList<>();

        lines.addAll(field.getJavaDocLines());
        lines.addAll(field.getAnnotations());
        lines.add(renderField(field, compilationUnit));

        return lines;
    }

    private String renderField(Field field, CompilationUnit compilationUnit) {
        StringBuilder sb = new StringBuilder();
        sb.append(field.getVisibility().getValue());

        if (field.isStatic()) {
            sb.append("static ");
        }

        if (field.isFinal()) {
            sb.append("final ");
        }

        if (field.isTransient()) {
            sb.append("transient ");
        }

        if (field.isVolatile()) {
            sb.append("volatile ");
        }

        sb.append(JavaDomUtils.calculateTypeName(compilationUnit, field.getType()));
        sb.append(' ');
        sb.append(field.getName());
        sb.append(renderInitializationString(field));
        sb.append(';');

        return sb.toString();
    }

    private String renderInitializationString(Field field) {
        return field.getInitializationString()
                .map(is -> " = " + is)
                .orElse("");
    }
}
