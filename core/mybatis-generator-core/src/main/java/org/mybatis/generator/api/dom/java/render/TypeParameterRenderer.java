package org.mybatis.generator.api.dom.java.render;

import org.mybatis.generator.api.dom.java.CompilationUnit;
import org.mybatis.generator.api.dom.java.JavaDomUtils;
import org.mybatis.generator.api.dom.java.TypeParameter;
import org.mybatis.generator.internal.util.CustomCollectors;

public class TypeParameterRenderer {
    public String render(TypeParameter typeParameter, CompilationUnit compilationUnit) {
        return typeParameter.getName() + typeParameter.getExtendsTypes().stream()
                .map(t -> JavaDomUtils.calculateTypeName(compilationUnit, t))
                .collect(CustomCollectors.joining(" & ", " extends ", "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
}
