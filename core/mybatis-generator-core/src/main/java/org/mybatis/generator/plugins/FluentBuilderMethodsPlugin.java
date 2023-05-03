package org.mybatis.generator.plugins;

import java.util.List;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.IntrospectedTable.TargetRuntime;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;

/**
 * This plugin adds fluent builder methods to the generated model classes.
 *
 * <p>Example:
 *
 * <p>Given the domain class <code>MyDomainClass</code> with setter-method <code>setValue(Object v)</code>
 *
 * <p>The plugin will create the additional Method <code>public MyDomainClass withValue(Object v)</code>
 *
 *
 * @author Stefan Lack
 */
public class FluentBuilderMethodsPlugin extends PluginAdapter {

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public boolean modelSetterMethodGenerated(Method method,
            TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn,
            IntrospectedTable introspectedTable,
            ModelClassType modelClassType) {

        Method fluentMethod = new Method("with" + method.getName().substring(3)); //$NON-NLS-1$
        fluentMethod.setVisibility(JavaVisibility.PUBLIC);
        fluentMethod.setReturnType(topLevelClass.getType());
        fluentMethod.getParameters().addAll(method.getParameters());

        if (introspectedTable.getTargetRuntime() == TargetRuntime.MYBATIS3_DSQL) {
            context.getCommentGenerator().addGeneralMethodAnnotation(fluentMethod,
                    introspectedTable, topLevelClass.getImportedTypes());
        } else {
            context.getCommentGenerator().addGeneralMethodComment(fluentMethod,
                    introspectedTable);
        }

        String s = "this." //$NON-NLS-1$
                + method.getName()
                + '('
                + introspectedColumn.getJavaProperty()
                + ");"; //$NON-NLS-1$
        fluentMethod.addBodyLine(s); //$NON-NLS-1$
        fluentMethod.addBodyLine("return this;"); //$NON-NLS-1$

        topLevelClass.addMethod(fluentMethod);

        return super.modelSetterMethodGenerated(method, topLevelClass, introspectedColumn,
                introspectedTable, modelClassType);
    }
}
