package org.mybatis.generator.codegen.mybatis3.javamapper.elements;

import static org.mybatis.generator.api.dom.OutputUtilities.javaIndent;
import static org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities.getEscapedColumnName;
import static org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities.getParameterClause;
import static org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities.getRenamedColumnNameForResultMap;
import static org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities.getSelectListPhrase;
import static org.mybatis.generator.internal.util.StringUtility.escapeStringForJava;
import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.codegen.AbstractGenerator;
import org.mybatis.generator.codegen.mybatis3.ListUtilities;
import org.mybatis.generator.config.GeneratedKey;
import org.mybatis.generator.config.SelectByTableGeneratorConfiguration;

public abstract class AbstractJavaMapperMethodGenerator extends AbstractGenerator {
    public abstract void addInterfaceElements(Interface interfaze);

    protected AbstractJavaMapperMethodGenerator() {
        super();
    }

    protected static String getResultAnnotation(Interface interfaze, IntrospectedColumn introspectedColumn,
            boolean idColumn, boolean constructorBased) {
        StringBuilder sb = new StringBuilder();
        if (constructorBased) {
            interfaze.addImportedType(introspectedColumn.getFullyQualifiedJavaType());
            sb.append("@Arg(column=\"");
            sb.append(getRenamedColumnNameForResultMap(introspectedColumn));
            sb.append("\", javaType=");
            sb.append(introspectedColumn.getFullyQualifiedJavaType().getShortName());
            sb.append(".class");
        } else {
            sb.append("@Result(column=\"");
            sb.append(getRenamedColumnNameForResultMap(introspectedColumn));
            sb.append("\", property=\"");
            sb.append(introspectedColumn.getJavaProperty());
            sb.append('\"');
        }

        if (stringHasValue(introspectedColumn.getTypeHandler())) {
            FullyQualifiedJavaType fqjt = new FullyQualifiedJavaType(introspectedColumn.getTypeHandler());
            interfaze.addImportedType(fqjt);
            sb.append(", typeHandler=");
            sb.append(fqjt.getShortName());
            sb.append(".class");
        }

        sb.append(", jdbcType=JdbcType.");
        sb.append(introspectedColumn.getJdbcTypeName());
        if (idColumn) {
            sb.append(", id=true");
        }
        sb.append(')');

        return sb.toString();
    }

    protected Optional<String> buildGeneratedKeyAnnotation() {
        return introspectedTable.getGeneratedKey().flatMap(this::buildGeneratedKeyAnnotation);
    }

    private Optional<String> buildGeneratedKeyAnnotation(GeneratedKey gk) {
        return introspectedTable.getColumn(gk.getColumn()).map(ic -> buildGeneratedKeyAnnotation(gk, ic));
    }

    private String buildGeneratedKeyAnnotation(GeneratedKey gk, IntrospectedColumn introspectedColumn) {
        StringBuilder sb = new StringBuilder();
        if (gk.isJdbcStandard()) {
            sb.append("@Options(useGeneratedKeys=true,keyProperty=\"");
            sb.append(introspectedColumn.getJavaProperty());
            sb.append("\")");
        } else {
            sb.append("@SelectKey(statement=\"");
            sb.append(gk.getRuntimeSqlStatement());
            sb.append("\", keyProperty=\"");
            sb.append(introspectedColumn.getJavaProperty());
            sb.append("\", before=");
            sb.append(gk.isIdentity() ? "false" : "true");  //$NON-NLS-2$
            sb.append(", resultType=");
            sb.append(introspectedColumn.getFullyQualifiedJavaType().getShortName());
            sb.append(".class)");
        }
        return sb.toString();
    }

    protected Set<FullyQualifiedJavaType> buildGeneratedKeyImportsIfRequired() {
        return introspectedTable.getGeneratedKey().map(this::buildGeneratedKeyImportsIfRequired)
                .orElseGet(Collections::emptySet);
    }

    private Set<FullyQualifiedJavaType> buildGeneratedKeyImportsIfRequired(GeneratedKey gk) {
        return introspectedTable.getColumn(gk.getColumn()).map(ic -> buildGeneratedKeyImports(gk, ic))
                .orElseGet(Collections::emptySet);
    }

    private Set<FullyQualifiedJavaType> buildGeneratedKeyImports(GeneratedKey gk,
                                                                 IntrospectedColumn introspectedColumn) {
        Set<FullyQualifiedJavaType> answer = new HashSet<>();
        if (gk.isJdbcStandard()) {
            answer.add(new FullyQualifiedJavaType("org.apache.ibatis.annotations.Options"));
        } else {
            answer.add(new FullyQualifiedJavaType("org.apache.ibatis.annotations.SelectKey"));
            answer.add(introspectedColumn.getFullyQualifiedJavaType());
        }

        return answer;
    }

    protected void addAnnotatedSelectImports(Interface interfaze) {
        interfaze.addImportedType(new FullyQualifiedJavaType("org.apache.ibatis.type.JdbcType"));

        if (introspectedTable.isConstructorBased()) {
            interfaze.addImportedType(new FullyQualifiedJavaType("org.apache.ibatis.annotations.Arg"));
            interfaze.addImportedType(
                    new FullyQualifiedJavaType("org.apache.ibatis.annotations.ConstructorArgs"));
        } else {
            interfaze.addImportedType(
                    new FullyQualifiedJavaType("org.apache.ibatis.annotations.Result"));
            interfaze.addImportedType(
                    new FullyQualifiedJavaType("org.apache.ibatis.annotations.Results"));
        }
    }

    protected List<String> buildByPrimaryKeyWhereClause() {
        List<String> answer = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean and = false;
        Iterator<IntrospectedColumn> iter = introspectedTable.getPrimaryKeyColumns().iterator();
        while (iter.hasNext()) {
            sb.setLength(0);
            javaIndent(sb, 1);
            if (and) {
                sb.append("  \"and ");
            } else {
                sb.append("\"where ");
                and = true;
            }

            IntrospectedColumn introspectedColumn = iter.next();
            sb.append(escapeStringForJava(getEscapedColumnName(introspectedColumn)));
            sb.append(" = ");
            sb.append(getParameterClause(introspectedColumn));
            sb.append('\"');
            if (iter.hasNext()) {
                sb.append(',');
            }
            answer.add(sb.toString());
        }

        return answer;
    }

    protected List<String> buildUpdateByPrimaryKeyAnnotations(List<IntrospectedColumn> columnList) {
        List<String> answer = new ArrayList<>();
        answer.add("@Update({");

        StringBuilder sb = new StringBuilder();
        javaIndent(sb, 1);
        sb.append("\"update ");
        sb.append(escapeStringForJava(introspectedTable.getFullyQualifiedTableNameAtRuntime()));
        sb.append("\",");
        answer.add(sb.toString());

        // set up for first column
        sb.setLength(0);
        javaIndent(sb, 1);
        sb.append("\"set ");

        Iterator<IntrospectedColumn> iter = ListUtilities.removeGeneratedAlwaysColumns(columnList).iterator();
        while (iter.hasNext()) {
            IntrospectedColumn introspectedColumn = iter.next();

            sb.append(escapeStringForJava(getEscapedColumnName(introspectedColumn)));
            sb.append(" = ");
            sb.append(getParameterClause(introspectedColumn));

            if (iter.hasNext()) {
                sb.append(',');
            }

            sb.append("\",");
            answer.add(sb.toString());

            // set up for the next column
            if (iter.hasNext()) {
                sb.setLength(0);
                javaIndent(sb, 1);
                sb.append("  \"");
            }
        }

        answer.addAll(buildByPrimaryKeyWhereClause());

        answer.add("})");
        return answer;
    }

    protected void addPrimaryKeyMethodParameters(boolean isSimple, Method method,
                                                 Set<FullyQualifiedJavaType> importedTypes) {
        if (!isSimple && introspectedTable.getRules().generatePrimaryKeyClass()) {
            FullyQualifiedJavaType type = new FullyQualifiedJavaType(introspectedTable.getPrimaryKeyType());
            importedTypes.add(type);
            Parameter parameter = new Parameter(type, "key");
            parameter.setRemark("唯一标识");
            method.addParameter(parameter);
        } else {
            // no primary key class - fields are in the base class
            // if more than one PK field, then we need to annotate the
            // parameters for MyBatis3
            List<IntrospectedColumn> introspectedColumns = introspectedTable
                    .getPrimaryKeyColumns();
            boolean annotate = introspectedColumns.size() > 1;
            if (annotate) {
                importedTypes.add(new FullyQualifiedJavaType("org.apache.ibatis.annotations.Param"));
            }
            StringBuilder sb = new StringBuilder();
            for (IntrospectedColumn introspectedColumn : introspectedColumns) {
                FullyQualifiedJavaType type = introspectedColumn.getFullyQualifiedJavaType();
                importedTypes.add(type);
                Parameter parameter = new Parameter(type, introspectedColumn.getJavaProperty());
                parameter.setRemark("主键标识");
                if (annotate) {
                    sb.setLength(0);
                    sb.append("@Param(\"");
                    sb.append(introspectedColumn.getJavaProperty());
                    sb.append("\")");
                    parameter.addAnnotation(sb.toString());
                }
                method.addParameter(parameter);
            }
        }
    }

    protected void addAnnotatedResults(Interface interfaze, Method method,
                                       List<IntrospectedColumn> nonPrimaryKeyColumns) {

        if (introspectedTable.isConstructorBased()) {
            method.addAnnotation("@ConstructorArgs({");
        } else {
            method.addAnnotation("@Results({");
        }

        StringBuilder sb = new StringBuilder();

        Iterator<IntrospectedColumn> iterPk = introspectedTable.getPrimaryKeyColumns().iterator();
        Iterator<IntrospectedColumn> iterNonPk = nonPrimaryKeyColumns.iterator();
        while (iterPk.hasNext()) {
            IntrospectedColumn introspectedColumn = iterPk.next();
            sb.setLength(0);
            javaIndent(sb, 1);
            sb.append(getResultAnnotation(interfaze, introspectedColumn, true,
                    introspectedTable.isConstructorBased()));

            if (iterPk.hasNext() || iterNonPk.hasNext()) {
                sb.append(',');
            }

            method.addAnnotation(sb.toString());
        }

        while (iterNonPk.hasNext()) {
            IntrospectedColumn introspectedColumn = iterNonPk.next();
            sb.setLength(0);
            javaIndent(sb, 1);
            sb.append(getResultAnnotation(interfaze, introspectedColumn, false,
                    introspectedTable.isConstructorBased()));

            if (iterNonPk.hasNext()) {
                sb.append(',');
            }

            method.addAnnotation(sb.toString());
        }

        method.addAnnotation("})");
    }

    protected Method buildBasicUpdateByExampleMethod(String statementId, FullyQualifiedJavaType parameterType,
                                                     Set<FullyQualifiedJavaType> importedTypes) {
        Method method = new Method(statementId);
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setAbstract(true);
        method.setReturnType(FullyQualifiedJavaType.getIntInstance());

        method.addParameter(new Parameter(parameterType, "row", "@Param(\"row\")"));  //$NON-NLS-2$

        importedTypes.add(parameterType);

        FullyQualifiedJavaType exampleType = new FullyQualifiedJavaType(introspectedTable.getExampleType());
        method.addParameter(new Parameter(exampleType,"example", "@Param(\"example\")"));  //$NON-NLS-2$
        importedTypes.add(exampleType);

        importedTypes.add(new FullyQualifiedJavaType("org.apache.ibatis.annotations.Param"));

        context.getCommentGenerator().addGeneralMethodComment(method, introspectedTable);

        return method;
    }

    protected Method buildBasicUpdateByPrimaryKeyMethod(String statementId, FullyQualifiedJavaType parameterType) {
        Method method = new Method(statementId);
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setAbstract(true);
        method.setReturnType(FullyQualifiedJavaType.getIntInstance());
        method.addParameter(new Parameter(parameterType, "row"));

        context.getCommentGenerator().addGeneralMethodComment(method, introspectedTable);
        return method;
    }

    protected Method buildInsertOrDeleteByTableMethod(boolean isInsert, SelectByTableGeneratorConfiguration configuration) {
        Method method = new Method(isInsert?configuration.getUnionMethodName():configuration.getSplitMethodName());
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setAbstract(true);
        method.setReturnType(FullyQualifiedJavaType.getIntegerInstance());
        Parameter p1 = new Parameter(configuration.getThisColumn().getFullyQualifiedJavaType(), configuration.getThisColumn().getJavaProperty());
        p1.setRemark(configuration.getThisColumn().getRemarks(false));
        method.addParameter(p1);
        FullyQualifiedJavaType listInstance = FullyQualifiedJavaType.getNewListInstance();
        listInstance.addTypeArgument(configuration.getOtherColumn().getFullyQualifiedJavaType());
        Parameter p2 = new Parameter(listInstance, configuration.getOtherColumn().getJavaProperty() + "s");
        p2.setRemark(configuration.getOtherColumn().getRemarks(false));
        method.addParameter(p2);
        method.setReturnRemark("影响行数");
        return method;
    }

    protected List<String> buildInitialSelectAnnotationStrings() {
        List<String> answer = new ArrayList<>();
        answer.add("@Select({");
        StringBuilder sb = new StringBuilder();
        javaIndent(sb, 1);
        sb.append("\"select\",");
        answer.add(sb.toString());

        sb.setLength(0);
        javaIndent(sb, 1);
        sb.append('"');
        boolean hasColumns = false;
        Iterator<IntrospectedColumn> iter = introspectedTable.getAllColumns().iterator();
        while (iter.hasNext()) {
            sb.append(escapeStringForJava(getSelectListPhrase(iter.next())));
            hasColumns = true;

            if (iter.hasNext()) {
                sb.append(", ");
            }

            if (sb.length() > 80) {
                sb.append("\",");
                answer.add(sb.toString());

                sb.setLength(0);
                javaIndent(sb, 1);
                sb.append('"');
                hasColumns = false;
            }
        }

        if (hasColumns) {
            sb.append("\",");
            answer.add(sb.toString());
        }

        return answer;
    }
}
