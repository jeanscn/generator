package org.mybatis.generator.codegen.mybatis3.javamapper.elements.sqlprovider;

import static org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities.getAliasedEscapedColumnName;
import static org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities.getParameterClause;
import static org.mybatis.generator.internal.util.JavaBeansUtil.getGetterMethodName;
import static org.mybatis.generator.internal.util.StringUtility.escapeStringForJava;

import java.util.Set;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.ListUtilities;

public class ProviderUpdateByExampleSelectiveMethodGenerator extends AbstractJavaProviderMethodGenerator {

    @Override
    public void addClassElements(TopLevelClass topLevelClass) {
        Method method = new Method(introspectedTable.getUpdateByExampleSelectiveStatementId());
        method.setReturnType(FullyQualifiedJavaType.getStringInstance());
        method.setVisibility(JavaVisibility.PUBLIC);
        method.addParameter(new Parameter(
                new FullyQualifiedJavaType("java.util.Map<java.lang.String, java.lang.Object>"),
                "parameter"));

        Set<FullyQualifiedJavaType> importedTypes = initializeImportedTypes("java.util.Map");

        FullyQualifiedJavaType recordClass = introspectedTable.getRules().calculateAllFieldsClass();
        importedTypes.add(recordClass);
        method.addBodyLine(String.format("%s row = (%s) parameter.get(\"row\");",
                recordClass.getShortName(), recordClass.getShortName()));

        FullyQualifiedJavaType example = new FullyQualifiedJavaType(introspectedTable.getExampleType());
        importedTypes.add(example);
        method.addBodyLine(String.format("%s example = (%s) parameter.get(\"example\");",
                example.getShortName(), example.getShortName()));

        context.getCommentGenerator().addGeneralMethodComment(method, introspectedTable);

        method.addBodyLine("");

        method.addBodyLine("SQL sql = new SQL();");

        method.addBodyLine(String.format("sql.UPDATE(\"%s\");",
                escapeStringForJava(introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime())));
        method.addBodyLine("");

        for (IntrospectedColumn introspectedColumn :
                ListUtilities.removeGeneratedAlwaysColumns(introspectedTable.getAllColumns())) {
            if (!introspectedColumn.getFullyQualifiedJavaType().isPrimitive()) {
                method.addBodyLine(String.format("if (row.%s() != null) {",
                        getGetterMethodName(introspectedColumn.getJavaProperty(),
                                introspectedColumn.getFullyQualifiedJavaType())));
            }

            StringBuilder sb = new StringBuilder();
            sb.append(getParameterClause(introspectedColumn));
            sb.insert(2, "row.");

            method.addBodyLine(String.format("sql.SET(\"%s = %s\");",
                    escapeStringForJava(getAliasedEscapedColumnName(introspectedColumn)),
                    sb.toString()));

            if (!introspectedColumn.getFullyQualifiedJavaType().isPrimitive()) {
                method.addBodyLine("}");
            }

            method.addBodyLine("");
        }

        method.addBodyLine("applyWhere(sql, example, true);");
        method.addBodyLine("return sql.toString();");

        if (context.getPlugins()
                .providerUpdateByExampleSelectiveMethodGenerated(method, topLevelClass, introspectedTable)) {
            topLevelClass.addImportedTypes(importedTypes);
            topLevelClass.addMethod(method);
        }
    }
}
