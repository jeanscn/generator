package org.mybatis.generator.runtime.dynamic.sql.elements;

import java.util.HashSet;
import java.util.Set;

import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;

public class GeneralCountMethodGenerator extends AbstractMethodGenerator {

    private GeneralCountMethodGenerator(Builder builder) {
        super(builder);
    }

    @Override
    public MethodAndImports generateMethodAndImports() {
        Set<FullyQualifiedJavaType> imports = new HashSet<>();

        FullyQualifiedJavaType parameterType = new FullyQualifiedJavaType(
                "org.mybatis.dynamic.sql.select.CountDSLCompleter");
        imports.add(parameterType);
        imports.add(new FullyQualifiedJavaType("org.mybatis.dynamic.sql.util.mybatis3.MyBatis3Utils"));

        Method method = new Method("count");
        method.setDefault(true);
        method.addParameter(new Parameter(parameterType, "completer"));
        context.getCommentGenerator().addGeneralMethodAnnotation(method, introspectedTable, imports);

        method.setReturnType(new FullyQualifiedJavaType("long"));

        method.addBodyLine("return MyBatis3Utils.countFrom(this::count, " + tableFieldName
                + ", completer);");

        return MethodAndImports.withMethod(method)
                .withImports(imports)
                .build();
    }

    @Override
    public boolean callPlugins(Method method, Interface interfaze) {
        return context.getPlugins().clientGeneralCountMethodGenerated(method, interfaze, introspectedTable);
    }

    public static class Builder extends BaseBuilder<Builder> {
        @Override
        public Builder getThis() {
            return this;
        }

        public GeneralCountMethodGenerator build() {
            return new GeneralCountMethodGenerator(this);
        }
    }
}
