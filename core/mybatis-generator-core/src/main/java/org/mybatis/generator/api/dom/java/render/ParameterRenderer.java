package org.mybatis.generator.api.dom.java.render;

import org.mybatis.generator.api.dom.java.CompilationUnit;
import org.mybatis.generator.api.dom.java.JavaDomUtils;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.internal.util.CustomCollectors;

/**
 * @author cen_c
 */
public class ParameterRenderer {

    public String render(Parameter parameter, CompilationUnit compilationUnit) {
        return renderAnnotations(parameter)
                + JavaDomUtils.calculateTypeName(compilationUnit, parameter.getType())
                + " "
                + (parameter.isVarargs() ? "... " : "")  
                + parameter.getName();
    }

    /**
     * Renders the annotations for a parameter.
     * @param parameter the parameter to render annotations for
     * @return a string containing the annotations, or an empty string if there are none
     */
    private String renderAnnotations(Parameter parameter) {
        return parameter.getAnnotations().stream()
                .collect(CustomCollectors.joining(" ", "", " "));   
    }
}
