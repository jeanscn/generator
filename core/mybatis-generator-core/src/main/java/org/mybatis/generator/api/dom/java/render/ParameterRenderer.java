package org.mybatis.generator.api.dom.java.render;

import org.mybatis.generator.api.dom.java.CompilationUnit;
import org.mybatis.generator.api.dom.java.JavaDomUtils;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.internal.util.CustomCollectors;

public class ParameterRenderer {

    public String render(Parameter parameter, CompilationUnit compilationUnit) {
        return renderAnnotations(parameter)
                + JavaDomUtils.calculateTypeName(compilationUnit, parameter.getType())
                + " " //$NON-NLS-1$
                + (parameter.isVarargs() ? "... " : "") //$NON-NLS-1$ //$NON-NLS-2$
                + parameter.getName();
    }

    // should return empty string if no annotations
    private String renderAnnotations(Parameter parameter) {
        return parameter.getAnnotations().stream()
                .collect(CustomCollectors.joining(" ", "", " ")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
}
