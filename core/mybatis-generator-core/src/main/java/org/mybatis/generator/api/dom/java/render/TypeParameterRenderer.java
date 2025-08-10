package org.mybatis.generator.api.dom.java.render;

import org.mybatis.generator.api.dom.java.CompilationUnit;
import org.mybatis.generator.api.dom.java.JavaDomUtils;
import org.mybatis.generator.api.dom.java.TypeParameter;
import org.mybatis.generator.internal.util.CustomCollectors;

/**
 * @author cen_c
 */
public class TypeParameterRenderer {
    public String render(TypeParameter typeParameter, CompilationUnit compilationUnit) {
        return typeParameter.getName() + typeParameter.getExtendsTypes().stream()
                .map(t -> JavaDomUtils.calculateTypeName(compilationUnit, t))
                .collect(CustomCollectors.joining(" & ", " extends ", ""));   
    }
}
