package org.mybatis.generator.runtime.dynamic.sql.elements;

import static org.mybatis.generator.api.dom.OutputUtilities.javaIndent;
import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.codegen.mybatis3.ListUtilities;
import org.mybatis.generator.config.GeneratedKey;
import org.mybatis.generator.internal.util.JavaBeansUtil;

public class FragmentGenerator {

    private final IntrospectedTable introspectedTable;
    private final String resultMapId;
    private final String tableFieldName;

    private FragmentGenerator(Builder builder) {
        this.introspectedTable = builder.introspectedTable;
        this.resultMapId = builder.resultMapId;
        tableFieldName = builder.tableFieldName;
    }

    public String getSelectList() {
        return introspectedTable.getAllColumns().stream()
                .map(c -> AbstractMethodGenerator.calculateFieldName(tableFieldName, c))
                .collect(Collectors.joining(", "));
    }

    public MethodParts getPrimaryKeyWhereClauseAndParameters() {
        MethodParts.Builder builder = new MethodParts.Builder();

        boolean first = true;
        for (IntrospectedColumn column : introspectedTable.getPrimaryKeyColumns()) {
            String fieldName = AbstractMethodGenerator.calculateFieldName(tableFieldName, column);
            builder.withImport(column.getFullyQualifiedJavaType());
            builder.withParameter(new Parameter(
                    column.getFullyQualifiedJavaType(), column.getJavaProperty() + "_"));
            if (first) {
                builder.withBodyLine("    c.where(" + fieldName
                        + ", isEqualTo(" + column.getJavaProperty()
                        + "_))");
                first = false;
            } else {
                builder.withBodyLine("    .and(" + fieldName
                        + ", isEqualTo(" + column.getJavaProperty()
                        + "_))");
            }
        }
        builder.withBodyLine(");");

        return builder.build();
    }

    public List<String> getPrimaryKeyWhereClauseForUpdate(String prefix) {
        List<String> lines = new ArrayList<>();

        boolean first = true;
        for (IntrospectedColumn column : introspectedTable.getPrimaryKeyColumns()) {
            String fieldName = AbstractMethodGenerator.calculateFieldName(tableFieldName, column);
            String methodName = JavaBeansUtil.getGetterMethodName(
                    column.getJavaProperty(), column.getFullyQualifiedJavaType());
            if (first) {
                lines.add(prefix + ".where(" + fieldName
                        + ", isEqualTo(row::" + methodName
                        + "))");
                first = false;
            } else {
                lines.add(prefix + ".and(" + fieldName
                        + ", isEqualTo(row::" + methodName
                        + "))");
            }
        }

        return lines;
    }

    public MethodParts getAnnotatedConstructorArgs() {
        MethodParts.Builder builder = new MethodParts.Builder();

        builder.withImport(new FullyQualifiedJavaType("org.apache.ibatis.type.JdbcType"));
        builder.withImport(new FullyQualifiedJavaType("org.apache.ibatis.annotations.ConstructorArgs"));
        builder.withImport(new FullyQualifiedJavaType("org.apache.ibatis.annotations.Arg"));

        builder.withAnnotation("@ConstructorArgs({");

        StringBuilder sb = new StringBuilder();

        Set<FullyQualifiedJavaType> imports = new HashSet<>();
        Iterator<IntrospectedColumn> iterPk = introspectedTable.getPrimaryKeyColumns().iterator();
        Iterator<IntrospectedColumn> iterNonPk = introspectedTable.getNonPrimaryKeyColumns().iterator();
        while (iterPk.hasNext()) {
            IntrospectedColumn introspectedColumn = iterPk.next();
            sb.setLength(0);
            javaIndent(sb, 1);
            sb.append(getArgAnnotation(imports, introspectedColumn, true));

            if (iterPk.hasNext() || iterNonPk.hasNext()) {
                sb.append(',');
            }

            builder.withAnnotation(sb.toString());
        }

        while (iterNonPk.hasNext()) {
            IntrospectedColumn introspectedColumn = iterNonPk.next();
            sb.setLength(0);
            javaIndent(sb, 1);
            sb.append(getArgAnnotation(imports, introspectedColumn, false));

            if (iterNonPk.hasNext()) {
                sb.append(',');
            }

            builder.withAnnotation(sb.toString());
        }

        builder.withAnnotation("})")
                .withImports(imports);

        return builder.build();
    }

    public MethodParts getAnnotatedResults() {
        MethodParts.Builder builder = new MethodParts.Builder();

        builder.withImport(new FullyQualifiedJavaType("org.apache.ibatis.type.JdbcType"));
        builder.withImport(new FullyQualifiedJavaType("org.apache.ibatis.annotations.Result"));
        builder.withImport(new FullyQualifiedJavaType("org.apache.ibatis.annotations.Results"));

        builder.withAnnotation("@Results(id=\"" + resultMapId + "\", value = {");  

        StringBuilder sb = new StringBuilder();

        Set<FullyQualifiedJavaType> imports = new HashSet<>();
        Iterator<IntrospectedColumn> iterPk = introspectedTable.getPrimaryKeyColumns().iterator();
        Iterator<IntrospectedColumn> iterNonPk = introspectedTable.getNonPrimaryKeyColumns().iterator();
        while (iterPk.hasNext()) {
            IntrospectedColumn introspectedColumn = iterPk.next();
            sb.setLength(0);
            javaIndent(sb, 1);
            sb.append(getResultAnnotation(imports, introspectedColumn, true));

            if (iterPk.hasNext() || iterNonPk.hasNext()) {
                sb.append(',');
            }

            builder.withAnnotation(sb.toString());
        }

        while (iterNonPk.hasNext()) {
            IntrospectedColumn introspectedColumn = iterNonPk.next();
            sb.setLength(0);
            javaIndent(sb, 1);
            sb.append(getResultAnnotation(imports, introspectedColumn, false));

            if (iterNonPk.hasNext()) {
                sb.append(',');
            }

            builder.withAnnotation(sb.toString());
        }

        builder.withAnnotation("})")
                .withImports(imports);

        return builder.build();
    }

    private String getArgAnnotation(Set<FullyQualifiedJavaType> imports, IntrospectedColumn introspectedColumn,
                                    boolean idColumn) {
        imports.add(introspectedColumn.getFullyQualifiedJavaType());

        return "@Arg(column=\""
                + introspectedColumn.getActualColumnName()
                + "\", javaType="
                + introspectedColumn.getFullyQualifiedJavaType().getShortName()
                + ".class"
                + generateAdditionalItems(imports, introspectedColumn, idColumn)
                + ')';
    }

    private String getResultAnnotation(Set<FullyQualifiedJavaType> imports, IntrospectedColumn introspectedColumn,
            boolean idColumn) {
        return "@Result(column=\""
                + introspectedColumn.getActualColumnName()
                + "\", property=\""
                + introspectedColumn.getJavaProperty()
                + '\"'
                + generateAdditionalItems(imports, introspectedColumn, idColumn)
                + ')';
    }

    private String generateAdditionalItems(Set<FullyQualifiedJavaType> imports, IntrospectedColumn introspectedColumn,
                                           boolean idColumn) {
        StringBuilder sb = new StringBuilder();

        if (stringHasValue(introspectedColumn.getTypeHandler())) {
            FullyQualifiedJavaType fqjt =
                    new FullyQualifiedJavaType(introspectedColumn.getTypeHandler());
            imports.add(fqjt);
            sb.append(", typeHandler=");
            sb.append(fqjt.getShortName());
            sb.append(".class");
        }

        sb.append(", jdbcType=JdbcType.");
        sb.append(introspectedColumn.getJdbcTypeName());
        if (idColumn) {
            sb.append(", id=true");
        }

        return sb.toString();
    }

    public MethodParts getGeneratedKeyAnnotation(GeneratedKey gk) {
        MethodParts.Builder builder = new MethodParts.Builder();

        StringBuilder sb = new StringBuilder();
        introspectedTable.getColumn(gk.getColumn()).ifPresent(introspectedColumn -> {
            if (gk.isJdbcStandard()) {
                builder.withImport(new FullyQualifiedJavaType("org.apache.ibatis.annotations.Options"));
                sb.append("@Options(useGeneratedKeys=true,keyProperty=\"row.");
                sb.append(introspectedColumn.getJavaProperty());
                sb.append("\")");
            } else {
                builder.withImport(new FullyQualifiedJavaType("org.apache.ibatis.annotations.SelectKey"));
                FullyQualifiedJavaType fqjt = introspectedColumn.getFullyQualifiedJavaType();
                sb.append("@SelectKey(statement=\"");
                sb.append(gk.getRuntimeSqlStatement());
                sb.append("\", keyProperty=\"row.");
                sb.append(introspectedColumn.getJavaProperty());
                sb.append("\", before=");
                sb.append(gk.isIdentity() ? "false" : "true");  
                sb.append(", resultType=");
                sb.append(fqjt.getShortName());
                sb.append(".class)");
            }
            builder.withAnnotation(sb.toString());
        });

        return builder.build();
    }

    public List<String> getSetEqualLines(List<IntrospectedColumn> columnList, String firstLinePrefix,
                                         String subsequentLinePrefix, boolean terminate) {
        return getSetLines(columnList, firstLinePrefix, subsequentLinePrefix, terminate,
                "equalTo");
    }

    public List<String> getSetEqualWhenPresentLines(List<IntrospectedColumn> columnList, String firstLinePrefix,
                                                    String subsequentLinePrefix, boolean terminate) {
        return getSetLines(columnList, firstLinePrefix, subsequentLinePrefix, terminate,
                "equalToWhenPresent");
    }

    private List<String> getSetLines(List<IntrospectedColumn> columnList, String firstLinePrefix,
                                     String subsequentLinePrefix, boolean terminate, String fragment) {
        List<String> lines = new ArrayList<>();
        List<IntrospectedColumn> columns = ListUtilities.removeIdentityAndGeneratedAlwaysColumns(columnList);
        Iterator<IntrospectedColumn> iter = columns.iterator();
        boolean first = true;
        while (iter.hasNext()) {
            IntrospectedColumn column = iter.next();
            String fieldName = AbstractMethodGenerator.calculateFieldName(tableFieldName, column);
            String methodName = JavaBeansUtil.getGetterMethodName(column.getJavaProperty(),
                    column.getFullyQualifiedJavaType());

            String start;
            if (first) {
                start = firstLinePrefix;
                first = false;
            } else {
                start = subsequentLinePrefix;
            }

            String line = start
                    + ".set("
                    + fieldName
                    + ")."
                    + fragment
                    + "(row::"
                    + methodName
                    + ")";

            if (terminate && !iter.hasNext()) {
                line += ";";
            }

            lines.add(line);
        }

        return lines;
    }

    public static class Builder {
        private IntrospectedTable introspectedTable;
        private String resultMapId;
        private String tableFieldName;

        public Builder withIntrospectedTable(IntrospectedTable introspectedTable) {
            this.introspectedTable = introspectedTable;
            return this;
        }

        public Builder withResultMapId(String resultMapId) {
            this.resultMapId = resultMapId;
            return this;
        }

        public Builder withTableFieldName(String tableFieldName) {
            this.tableFieldName = tableFieldName;
            return this;
        }

        public FragmentGenerator build() {
            return new FragmentGenerator(this);
        }
    }
}
