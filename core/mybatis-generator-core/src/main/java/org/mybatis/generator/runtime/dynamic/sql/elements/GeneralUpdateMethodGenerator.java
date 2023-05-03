package org.mybatis.generator.runtime.dynamic.sql.elements;

import java.util.HashSet;
import java.util.Set;

import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;

public class GeneralUpdateMethodGenerator extends AbstractMethodGenerator {

    private GeneralUpdateMethodGenerator(Builder builder) {
        super(builder);
    }

    @Override
    public MethodAndImports generateMethodAndImports() {
        Set<FullyQualifiedJavaType> imports = new HashSet<>();

        FullyQualifiedJavaType parameterType = new FullyQualifiedJavaType(
                "org.mybatis.dynamic.sql.update.UpdateDSLCompleter"); //$NON-NLS-1$

        imports.add(parameterType);
        imports.add(new FullyQualifiedJavaType("org.mybatis.dynamic.sql.util.mybatis3.MyBatis3Utils")); //$NON-NLS-1$

        FullyQualifiedJavaType returnType = FullyQualifiedJavaType.getIntInstance();

        imports.add(returnType);

        Method method = new Method("update"); //$NON-NLS-1$
        method.setDefault(true);
        method.addParameter(new Parameter(parameterType, "completer")); //$NON-NLS-1$

        context.getCommentGenerator().addGeneralMethodAnnotation(method, introspectedTable, imports);

        method.setReturnType(returnType);
        method.addBodyLine("return MyBatis3Utils.update(this::update, " //$NON-NLS-1$
                + tableFieldName + ", completer);"); //$NON-NLS-1$

        return MethodAndImports.withMethod(method)
                .withImports(imports)
                .build();
    }

    @Override
    public boolean callPlugins(Method method, Interface interfaze) {
        return context.getPlugins().clientGeneralUpdateMethodGenerated(method, interfaze, introspectedTable);
    }

    public static class Builder extends BaseBuilder<Builder> {
        @Override
        public Builder getThis() {
            return this;
        }

        public GeneralUpdateMethodGenerator build() {
            return new GeneralUpdateMethodGenerator(this);
        }
    }
}
