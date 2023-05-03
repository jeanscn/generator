package org.mybatis.generator.codegen.mybatis3.javamapper.elements.annotated;

import static org.mybatis.generator.api.dom.OutputUtilities.javaIndent;
import static org.mybatis.generator.internal.util.StringUtility.escapeStringForJava;

import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.codegen.mybatis3.javamapper.elements.SelectAllMethodGenerator;
import org.mybatis.generator.config.PropertyRegistry;
import org.mybatis.generator.internal.util.StringUtility;

public class AnnotatedSelectAllMethodGenerator extends SelectAllMethodGenerator {

    public AnnotatedSelectAllMethodGenerator() {
        super();
    }

    @Override
    public void addMapperAnnotations(Interface interfaze, Method method) {
        interfaze.addImportedType(new FullyQualifiedJavaType("org.apache.ibatis.annotations.Select")); //$NON-NLS-1$

        buildInitialSelectAnnotationStrings().forEach(method::addAnnotation);

        StringBuilder sb = new StringBuilder();
        javaIndent(sb, 1);
        sb.append("\"from "); //$NON-NLS-1$
        sb.append(escapeStringForJava(introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime()));
        sb.append('\"');

        String orderByClause = introspectedTable.getTableConfigurationProperty(
                PropertyRegistry.TABLE_SELECT_ALL_ORDER_BY_CLAUSE);
        boolean hasOrderBy = StringUtility.stringHasValue(orderByClause);
        if (hasOrderBy) {
            sb.append(',');
        }
        method.addAnnotation(sb.toString());

        if (hasOrderBy) {
            sb.setLength(0);
            javaIndent(sb, 1);
            sb.append("\"order by "); //$NON-NLS-1$
            sb.append(orderByClause);
            sb.append('\"');
            method.addAnnotation(sb.toString());
        }

        method.addAnnotation("})"); //$NON-NLS-1$

        addAnnotatedResults(interfaze, method, introspectedTable.getNonPrimaryKeyColumns());
    }

    @Override
    public void addExtraImports(Interface interfaze) {
        addAnnotatedSelectImports(interfaze);
    }
}
