package org.mybatis.generator.runtime.kotlin.elements;

import static org.mybatis.generator.api.dom.OutputUtilities.kotlinIndent;
import static org.mybatis.generator.internal.util.StringUtility.escapeStringForKotlin;
import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.mybatis.generator.api.AbstractIntrospectedTable;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.kotlin.FullyQualifiedKotlinType;
import org.mybatis.generator.api.dom.kotlin.JavaToKotlinTypeConverter;
import org.mybatis.generator.api.dom.kotlin.KotlinArg;
import org.mybatis.generator.codegen.mybatis3.ListUtilities;
import org.mybatis.generator.config.GeneratedKey;
import org.mybatis.generator.runtime.kotlin.KotlinDynamicSqlSupportClassGenerator;

/**
 * @author cen_c
 */
public class KotlinFragmentGenerator {

    private final AbstractIntrospectedTable introspectedTable;
    private final String resultMapId;
    private final String supportObjectImport;
    private final String tableFieldName;

    private KotlinFragmentGenerator(Builder builder) {
        introspectedTable = builder.introspectedTable;
        resultMapId = builder.resultMapId;
        supportObjectImport = builder.supportObjectImport;
        tableFieldName = builder.tableFieldName;
    }

    public KotlinFunctionParts getPrimaryKeyWhereClauseAndParameters() {
        KotlinFunctionParts.Builder builder = new KotlinFunctionParts.Builder();

        boolean first = true;
        for (IntrospectedColumn column : introspectedTable.getPrimaryKeyColumns()) {
            FullyQualifiedKotlinType kt = JavaToKotlinTypeConverter.convert(column.getFullyQualifiedJavaType());

            AbstractKotlinFunctionGenerator.FieldNameAndImport fieldNameAndImport =
                    AbstractKotlinFunctionGenerator.calculateFieldNameAndImport(tableFieldName,
                    supportObjectImport, column);
            String argName = column.getJavaProperty() + "_";

            builder.withImport(fieldNameAndImport.importString());
            builder.withImports(kt.getImportList());
            builder.withArgument(KotlinArg.newArg(argName)
                    .withDataType(kt.getShortNameWithTypeArguments())
                    .build());
            if (first) {
                builder.withCodeLine("    where { " + fieldNameAndImport.fieldName()
                        + " isEqualTo " + argName
                        + " }");
                first = false;
            } else {
                builder.withCodeLine("    and { " + fieldNameAndImport.fieldName()
                        + " isEqualTo " + argName
                        + " }");
            }
        }
        builder.withCodeLine("}");

        return builder.build();
    }

    public KotlinFunctionParts getPrimaryKeyWhereClauseForUpdate() {
        KotlinFunctionParts.Builder builder = new KotlinFunctionParts.Builder();

        boolean first = true;
        for (IntrospectedColumn column : introspectedTable.getPrimaryKeyColumns()) {
            AbstractKotlinFunctionGenerator.FieldNameAndImport fieldNameAndImport =
                    AbstractKotlinFunctionGenerator.calculateFieldNameAndImport(tableFieldName,
                            supportObjectImport, column);

            builder.withImport(fieldNameAndImport.importString());
            if (first) {
                builder.withCodeLine("    where { " + fieldNameAndImport.fieldName()
                        + " isEqualTo row." + column.getJavaProperty()
                        + "!! }");
                first = false;
            } else {
                builder.withCodeLine("    and {" + fieldNameAndImport.fieldName()
                        + " isEqualTo row." + column.getJavaProperty()
                        + "!! }");
            }
        }
        builder.withCodeLine("}");

        return builder.build();
    }

    public KotlinFunctionParts getAnnotatedResults() {
        KotlinFunctionParts.Builder builder = new KotlinFunctionParts.Builder();

        builder.withImport("org.apache.ibatis.type.JdbcType");
        builder.withImport("org.apache.ibatis.annotations.Result");
        builder.withImport("org.apache.ibatis.annotations.Results");

        builder.withAnnotation("@Results(id=\"" + resultMapId + "\", value = [");

        StringBuilder sb = new StringBuilder();

        Set<String> imports = new HashSet<>();
        Iterator<IntrospectedColumn> iterPk = introspectedTable.getPrimaryKeyColumns().iterator();
        Iterator<IntrospectedColumn> iterNonPk = introspectedTable.getNonPrimaryKeyColumns().iterator();
        while (iterPk.hasNext()) {
            IntrospectedColumn introspectedColumn = iterPk.next();
            sb.setLength(0);
            kotlinIndent(sb, 1);
            sb.append(getResultAnnotation(imports, introspectedColumn, true));

            if (iterPk.hasNext() || iterNonPk.hasNext()) {
                sb.append(',');
            }

            builder.withAnnotation(sb.toString());
        }

        while (iterNonPk.hasNext()) {
            IntrospectedColumn introspectedColumn = iterNonPk.next();
            sb.setLength(0);
            kotlinIndent(sb, 1);
            sb.append(getResultAnnotation(imports, introspectedColumn, false));

            if (iterNonPk.hasNext()) {
                sb.append(',');
            }

            builder.withAnnotation(sb.toString());
        }

        builder.withAnnotation("])")
                .withImports(imports);

        return builder.build();
    }

    private String getResultAnnotation(Set<String> imports, IntrospectedColumn introspectedColumn,
            boolean idColumn) {
        StringBuilder sb = new StringBuilder();
        sb.append("Result(column=\"");
        sb.append(escapeStringForKotlin(introspectedColumn.getActualColumnName()));
        sb.append("\", property=\"");
        sb.append(introspectedColumn.getJavaProperty());
        sb.append('\"');

        if (stringHasValue(introspectedColumn.getTypeHandler())) {
            FullyQualifiedKotlinType fqjt =
                    new FullyQualifiedKotlinType(introspectedColumn.getTypeHandler());
            imports.add(introspectedColumn.getTypeHandler());
            sb.append(", typeHandler=");
            sb.append(fqjt.getShortNameWithoutTypeArguments());
            sb.append("::class");
        }

        sb.append(", jdbcType=JdbcType.");
        sb.append(introspectedColumn.getJdbcTypeName());
        if (idColumn) {
            sb.append(", id=true");
        }
        sb.append(')');

        return sb.toString();
    }

    public KotlinFunctionParts getGeneratedKeyAnnotation(GeneratedKey gk) {
        KotlinFunctionParts.Builder builder = new KotlinFunctionParts.Builder();

        StringBuilder sb = new StringBuilder();
        introspectedTable.getColumn(gk.getColumn()).ifPresent(introspectedColumn -> {
            if (gk.isJdbcStandard()) {
                builder.withImport("org.apache.ibatis.annotations.Options");
                sb.append("@Options(useGeneratedKeys=true,keyProperty=\"row.");
                sb.append(introspectedColumn.getJavaProperty());
                sb.append("\")");
                builder.withAnnotation(sb.toString());
            } else {
                builder.withImport("org.apache.ibatis.annotations.SelectKey");
                FullyQualifiedKotlinType kt =
                        JavaToKotlinTypeConverter.convert(introspectedColumn.getFullyQualifiedJavaType());

                sb.append("@SelectKey(statement=[\"");
                sb.append(gk.getRuntimeSqlStatement());
                sb.append("\"], keyProperty=\"row.");
                sb.append(introspectedColumn.getJavaProperty());
                sb.append("\", before=");
                sb.append(gk.isIdentity() ? "false" : "true");
                sb.append(", resultType=");
                sb.append(kt.getShortNameWithoutTypeArguments());
                sb.append("::class)");
                builder.withAnnotation(sb.toString());
            }
        });

        return builder.build();
    }

    public KotlinFunctionParts getSetEqualLines(List<IntrospectedColumn> columnList) {

        KotlinFunctionParts.Builder builder = new KotlinFunctionParts.Builder();

        List<IntrospectedColumn> columns = ListUtilities.removeIdentityAndGeneratedAlwaysColumns(columnList);
        for (IntrospectedColumn column : columns) {
            AbstractKotlinFunctionGenerator.FieldNameAndImport fieldNameAndImport =
                    AbstractKotlinFunctionGenerator.calculateFieldNameAndImport(tableFieldName,
                            supportObjectImport, column);
            builder.withImport(fieldNameAndImport.importString());

            builder.withCodeLine("    set(" + fieldNameAndImport.fieldName()
                    + ") equalToOrNull row::" + column.getJavaProperty());
        }

        return builder.build();
    }

    public KotlinFunctionParts getSetEqualWhenPresentLines(List<IntrospectedColumn> columnList) {

        KotlinFunctionParts.Builder builder = new KotlinFunctionParts.Builder();

        List<IntrospectedColumn> columns = ListUtilities.removeIdentityAndGeneratedAlwaysColumns(columnList);
        for (IntrospectedColumn column : columns) {
            AbstractKotlinFunctionGenerator.FieldNameAndImport fieldNameAndImport =
                    AbstractKotlinFunctionGenerator.calculateFieldNameAndImport(tableFieldName,
                            supportObjectImport, column);
            builder.withImport(fieldNameAndImport.importString());

            builder.withCodeLine("    set(" + fieldNameAndImport.fieldName()
                    + ") equalToWhenPresent row::" + column.getJavaProperty());
        }

        return builder.build();
    }

    public static class Builder {
        private AbstractIntrospectedTable introspectedTable;
        private String resultMapId;
        private String supportObjectImport;
        private String tableFieldName;

        public Builder withIntrospectedTable(AbstractIntrospectedTable introspectedTable) {
            this.introspectedTable = introspectedTable;
            return this;
        }

        public Builder withResultMapId(String resultMapId) {
            this.resultMapId = resultMapId;
            return this;
        }

        public Builder withDynamicSqlSupportClassGenerator(KotlinDynamicSqlSupportClassGenerator generator) {
            supportObjectImport = generator.getSupportObjectImport();
            return this;
        }

        public Builder withTableFieldName(String tableFieldName) {
            this.tableFieldName = tableFieldName;
            return this;
        }

        public KotlinFragmentGenerator build() {
            return new KotlinFragmentGenerator(this);
        }
    }
}
