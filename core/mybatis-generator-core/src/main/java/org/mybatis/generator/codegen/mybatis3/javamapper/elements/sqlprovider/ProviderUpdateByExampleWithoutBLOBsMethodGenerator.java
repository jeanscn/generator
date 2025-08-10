package org.mybatis.generator.codegen.mybatis3.javamapper.elements.sqlprovider;

import static org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities.getAliasedEscapedColumnName;
import static org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities.getParameterClause;
import static org.mybatis.generator.internal.util.StringUtility.escapeStringForJava;

import java.util.List;
import java.util.Set;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.ListUtilities;

public class ProviderUpdateByExampleWithoutBLOBsMethodGenerator extends AbstractJavaProviderMethodGenerator {

    @Override
    public void addClassElements(TopLevelClass topLevelClass) {
        Method method = new Method(getMethodName());
        method.setReturnType(FullyQualifiedJavaType.getStringInstance());
        method.setVisibility(JavaVisibility.PUBLIC);
        method.addParameter(new Parameter(
                new FullyQualifiedJavaType("java.util.Map<java.lang.String, java.lang.Object>"),
                "parameter"));

        context.getCommentGenerator().addGeneralMethodComment(method, introspectedTable);

        method.addBodyLine("SQL sql = new SQL();");

        method.addBodyLine(String.format("sql.UPDATE(\"%s\");",
                escapeStringForJava(introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime())));
        method.addBodyLine("");

        for (IntrospectedColumn introspectedColumn : ListUtilities.removeGeneratedAlwaysColumns(getColumns())) {
            StringBuilder sb = new StringBuilder();
            sb.append(getParameterClause(introspectedColumn));
            sb.insert(2, "row.");

            method.addBodyLine(String.format("sql.SET(\"%s = %s\");",
                    escapeStringForJava(getAliasedEscapedColumnName(introspectedColumn)),
                    sb));
        }

        method.addBodyLine("");

        Set<FullyQualifiedJavaType> importedTypes = initializeImportedTypes("java.util.Map");
        FullyQualifiedJavaType example = new FullyQualifiedJavaType(introspectedTable.getExampleType());
        importedTypes.add(example);
        method.addBodyLine(String.format("%s example = (%s) parameter.get(\"example\");",
                example.getShortName(), example.getShortName()));

        method.addBodyLine("applyWhere(sql, example, true);");
        method.addBodyLine("return sql.toString();");

        if (callPlugins(method, topLevelClass)) {
            topLevelClass.addImportedTypes(importedTypes);
            topLevelClass.addMethod(method);
        }
    }

    public String getMethodName() {
        return introspectedTable.getUpdateByExampleStatementId();
    }

    public List<IntrospectedColumn> getColumns() {
        return introspectedTable.getNonBLOBColumns();
    }

    public boolean callPlugins(Method method, TopLevelClass topLevelClass) {
        return context.getPlugins()
                .providerUpdateByExampleWithoutBLOBsMethodGenerated(method, topLevelClass, introspectedTable);
    }
}
