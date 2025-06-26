package org.mybatis.generator.api.dom.java;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public abstract class JavaElement {

    private final List<String> javaDocLines = new ArrayList<>();

    private JavaVisibility visibility = JavaVisibility.DEFAULT;

    private boolean isStatic;

    private final List<String> annotations = new ArrayList<>();

    protected JavaElement() {
        super();
    }

    protected JavaElement(JavaElement original) {
        this.annotations.addAll(original.annotations);
        this.isStatic = original.isStatic;
        this.javaDocLines.addAll(original.javaDocLines);
        this.visibility = original.visibility;
    }

    public void addJavaDocLine(String javaDocLine) {
        javaDocLines.add(javaDocLine);
    }

    public void addAnnotation(String annotation) {
        if (annotation != null && !annotations.contains(annotation)) {
            annotations.add(annotation);
        }
    }

    public void addSuppressTypeWarningsAnnotation() {
        addAnnotation("@SuppressWarnings(\"unchecked\")");
    }

}
