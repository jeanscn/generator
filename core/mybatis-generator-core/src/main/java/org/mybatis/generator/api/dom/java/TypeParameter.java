package org.mybatis.generator.api.dom.java;

import java.util.ArrayList;
import java.util.List;

import org.mybatis.generator.api.dom.java.render.TypeParameterRenderer;

public class TypeParameter {

    private final String name;

    private final List<FullyQualifiedJavaType> extendsTypes = new ArrayList<>();

    public TypeParameter(String name) {
        super();
        this.name = name;
    }

    public TypeParameter(String name, List<FullyQualifiedJavaType> extendsTypes) {
        super();
        this.name = name;
        this.extendsTypes.addAll(extendsTypes);
    }

    public String getName() {
        return name;
    }

    public List<FullyQualifiedJavaType> getExtendsTypes() {
        return extendsTypes;
    }

    @Override
    public String toString() {
        return new TypeParameterRenderer().render(this, null);
    }
}
