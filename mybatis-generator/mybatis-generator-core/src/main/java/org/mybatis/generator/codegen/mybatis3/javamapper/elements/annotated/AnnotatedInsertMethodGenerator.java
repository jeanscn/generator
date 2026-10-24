package org.mybatis.generator.codegen.mybatis3.javamapper.elements.annotated;

import static org.mybatis.generator.api.dom.OutputUtilities.javaIndent;
import static org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities.getEscapedColumnName;
import static org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities.getParameterClause;
import static org.mybatis.generator.internal.util.StringUtility.escapeStringForJava;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.codegen.mybatis3.ListUtilities;
import org.mybatis.generator.codegen.mybatis3.javamapper.elements.InsertMethodGenerator;

public class AnnotatedInsertMethodGenerator extends InsertMethodGenerator {

    public AnnotatedInsertMethodGenerator(boolean isSimple) {
        super(isSimple);
    }

    @Override
    public void addMapperAnnotations(Method method) {

        method.addAnnotation("@Insert({");
        StringBuilder insertClause = new StringBuilder();
        StringBuilder valuesClause = new StringBuilder();

        javaIndent(insertClause, 1);
        javaIndent(valuesClause, 1);

        insertClause.append("\"insert into ");
        insertClause.append(escapeStringForJava(introspectedTable
                .getFullyQualifiedTableNameAtRuntime()));
        insertClause.append(" (");

        valuesClause.append("\"values (");

        List<String> valuesClauses = new ArrayList<>();
        Iterator<IntrospectedColumn> iter =
                ListUtilities.removeIdentityAndGeneratedAlwaysColumns(introspectedTable.getAllColumns())
                .iterator();
        boolean hasFields = false;
        while (iter.hasNext()) {
            IntrospectedColumn introspectedColumn = iter.next();

            insertClause.append(escapeStringForJava(getEscapedColumnName(introspectedColumn)));
            valuesClause.append(getParameterClause(introspectedColumn));
            hasFields = true;
            if (iter.hasNext()) {
                insertClause.append(", ");
                valuesClause.append(", ");
            }

            if (valuesClause.length() > 60) {
                if (!iter.hasNext()) {
                    insertClause.append(')');
                    valuesClause.append(')');
                }
                insertClause.append("\",");
                valuesClause.append('\"');
                if (iter.hasNext()) {
                    valuesClause.append(',');
                }

                method.addAnnotation(insertClause.toString());
                insertClause.setLength(0);
                javaIndent(insertClause, 1);
                insertClause.append('\"');

                valuesClauses.add(valuesClause.toString());
                valuesClause.setLength(0);
                javaIndent(valuesClause, 1);
                valuesClause.append('\"');
                hasFields = false;
            }
        }

        if (hasFields) {
            insertClause.append(")\",");
            method.addAnnotation(insertClause.toString());

            valuesClause.append(")\"");
            valuesClauses.add(valuesClause.toString());
        }

        for (String clause : valuesClauses) {
            method.addAnnotation(clause);
        }

        method.addAnnotation("})");

        buildGeneratedKeyAnnotation().ifPresent(method::addAnnotation);
    }

    @Override
    public void addExtraImports(Interface interfaze) {
        interfaze.addImportedTypes(buildGeneratedKeyImportsIfRequired());
        interfaze.addImportedType(new FullyQualifiedJavaType("org.apache.ibatis.annotations.Insert"));
    }
}
