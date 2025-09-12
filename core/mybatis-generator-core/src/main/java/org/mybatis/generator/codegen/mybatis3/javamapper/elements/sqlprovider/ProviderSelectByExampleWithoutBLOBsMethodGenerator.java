package org.mybatis.generator.codegen.mybatis3.javamapper.elements.sqlprovider;

import static org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities.getSelectListPhrase;
import static org.mybatis.generator.internal.util.StringUtility.escapeStringForJava;

import java.util.List;
import java.util.Set;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;

public class ProviderSelectByExampleWithoutBLOBsMethodGenerator extends AbstractJavaProviderMethodGenerator {

    @Override
    public void addClassElements(TopLevelClass topLevelClass) {
        FullyQualifiedJavaType fqjt = new FullyQualifiedJavaType(introspectedTable.getExampleType());
        Set<FullyQualifiedJavaType> importedTypes = initializeImportedTypes(fqjt);

        Method method = new Method(getMethodName());
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(FullyQualifiedJavaType.getStringInstance());
        method.addParameter(new Parameter(fqjt, "example"));

        context.getCommentGenerator().addGeneralMethodComment(method, introspectedTable);

        method.addBodyLine("SQL sql = new SQL();");

        boolean distinctCheck = true;
        for (IntrospectedColumn introspectedColumn : getColumns()) {
            if (distinctCheck) {
                method.addBodyLine("if (example != null && example.isDistinct()) {");
                method.addBodyLine(String.format("sql.SELECT_DISTINCT(\"%s\");",
                        escapeStringForJava(getSelectListPhrase(introspectedColumn))));
                method.addBodyLine("} else {");
                method.addBodyLine(String.format("sql.SELECT(\"%s\");",
                        escapeStringForJava(getSelectListPhrase(introspectedColumn))));
                method.addBodyLine("}");
            } else {
                method.addBodyLine(String.format("sql.SELECT(\"%s\");",
                        escapeStringForJava(getSelectListPhrase(introspectedColumn))));
            }

            distinctCheck = false;
        }

        method.addBodyLine(String.format("sql.FROM(\"%s\");",
                escapeStringForJava(introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime())));
        method.addBodyLine("applyWhere(sql, example, false);");

        method.addBodyLine("");
        method.addBodyLine("if (example != null && example.getOrderByClause() != null) {");
        method.addBodyLine("sql.ORDER_BY(example.getOrderByClause());");
        method.addBodyLine("}");

        method.addBodyLine("");
        method.addBodyLine("return sql.toString();");

        if (callPlugins(method, topLevelClass)) {
            topLevelClass.addImportedTypes(importedTypes);
            topLevelClass.addMethod(method);
        }
    }

    public List<IntrospectedColumn> getColumns() {
        return introspectedTable.getNonBlobColumns();
    }

    public String getMethodName() {
        return introspectedTable.getSelectByExampleStatementId();
    }

    public boolean callPlugins(Method method, TopLevelClass topLevelClass) {
        return context.getPlugins()
                .providerSelectByExampleWithoutBLOBsMethodGenerated(method, topLevelClass, introspectedTable);
    }
}
