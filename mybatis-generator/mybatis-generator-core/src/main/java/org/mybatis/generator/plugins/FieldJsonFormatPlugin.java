package org.mybatis.generator.plugins;

import org.mybatis.generator.api.AbstractIntrospectedTable;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.internal.util.StringUtility;

import java.util.List;

/**
 * 添加日期属性的JsonFormat
 *
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-04-19 14:51
 * @version 3.0
 */
public class FieldJsonFormatPlugin extends PluginAdapter {

    public static final String LOGICAL_TYPE_PROPERTY = "logicalType";
    public static final String JSON_NODE_TYPE = "tools.jackson.databind.JsonNode";
    public static final String JSON_TYPE_HANDLER = "com.vgosoft.mybatis.typehandler.JacksonJsonTypeHandler";

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public boolean modelFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, AbstractIntrospectedTable introspectedTable, ModelClassType modelClassType) {
        addJsonFieldFormat(field, topLevelClass, introspectedColumn);
        addDateTimeFormat(field, topLevelClass, introspectedColumn);
        if (!introspectedTable.getRules().isGenerateVoModel()) {
            addJsonSerialize(field, topLevelClass, introspectedColumn);
        }
        return true;
    }

    @Override
    public boolean voAbstractFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, AbstractIntrospectedTable introspectedTable) {
        addJsonFieldFormat(field, topLevelClass, introspectedColumn);
        addDateTimeFormat(field, topLevelClass, introspectedColumn);
        addJsonSerialize(field, topLevelClass, introspectedColumn);
        return true;
    }

    @Override
    public boolean voModelFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, AbstractIntrospectedTable introspectedTable) {
        addJsonFieldFormat(field, topLevelClass, introspectedColumn);
        addDateTimeFormat(field, topLevelClass, introspectedColumn);
        addJsonSerialize(field, topLevelClass, introspectedColumn);
        return true;
    }

    @Override
    public boolean voUpdateFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, AbstractIntrospectedTable introspectedTable) {
        addJsonFieldFormat(field, topLevelClass, introspectedColumn);
        return true;
    }

    @Override
    public boolean voCreateFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, AbstractIntrospectedTable introspectedTable) {
        addJsonFieldFormat(field, topLevelClass, introspectedColumn);
        addDateTimeFormat(field, topLevelClass, introspectedColumn);
        return true;
    }

    @Override
    public boolean voRequestFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, AbstractIntrospectedTable introspectedTable) {
        addDateTimeFormat(field, topLevelClass, introspectedColumn);
        return true;
    }

    private void addJsonFieldFormat(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn) {
        if (introspectedColumn == null) {
            return;
        }
        //添加日期序列化格式注解
        String datePattern = introspectedColumn.getDatePattern();
        if (StringUtility.stringHasValue(datePattern)) {
            topLevelClass.addImportedType("com.fasterxml.jackson.annotation.JsonFormat");
            field.addAnnotation("@JsonFormat(shape = JsonFormat.Shape.STRING,locale=\"zh\", timezone=\"GMT+8\", pattern=\"" + datePattern + "\")");
        }
    }

    private void addDateTimeFormat(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn) {
        if (introspectedColumn == null) {
            return;
        }
        //添加日期序列化格式注解
        String datePattern = introspectedColumn.getDatePattern();
        if (StringUtility.stringHasValue(datePattern)) {
            topLevelClass.addImportedType("org.springframework.format.annotation.DateTimeFormat");
            field.addAnnotation("@DateTimeFormat(pattern = \"" + datePattern + "\")");
        }
    }

    private void addJsonSerialize(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn) {
        if (introspectedColumn == null) {
            return;
        }
        String logicalType = introspectedColumn.getProperties().getProperty(LOGICAL_TYPE_PROPERTY);
        if (StringUtility.stringHasValue(logicalType) && !"JSON".equalsIgnoreCase(logicalType)) {
            throw new IllegalArgumentException("Unsupported logicalType for column "
                    + introspectedColumn.getActualColumnName() + ": " + logicalType);
        }
        boolean logicalJson = "JSON".equalsIgnoreCase(logicalType)
                || "JSON".equalsIgnoreCase(introspectedColumn.getActualTypeName());
        if (!logicalJson) {
            return;
        }
        if (!FullyQualifiedJavaType.getStringInstance().equals(introspectedColumn.getFullyQualifiedJavaType())) {
            ensureJsonTypeHandler(introspectedColumn);
            return;
        }
        topLevelClass.addImportedType("tools.jackson.databind.annotation.JsonSerialize");
        topLevelClass.addImportedType("com.vgosoft.system.parser.JsonRecordSerializer");
        field.addAnnotation("@JsonSerialize(using = JsonRecordSerializer.class)");
    }

    public static void ensureJsonTypeHandler(IntrospectedColumn introspectedColumn) {
        if (introspectedColumn == null
                || introspectedColumn.getFullyQualifiedJavaType() == null
                || FullyQualifiedJavaType.getStringInstance().equals(introspectedColumn.getFullyQualifiedJavaType())) {
            return;
        }
        if (!StringUtility.stringHasValue(introspectedColumn.getTypeHandler())) {
            introspectedColumn.setTypeHandler(JSON_TYPE_HANDLER);
        }
        if (JSON_TYPE_HANDLER.equals(introspectedColumn.getTypeHandler())
                && !isTextJdbcType(introspectedColumn.getJdbcTypeName())) {
            introspectedColumn.setJdbcTypeName("LONGVARCHAR");
        }
    }

    private static boolean isTextJdbcType(String jdbcType) {
        if (jdbcType == null) {
            return false;
        }
        return switch (jdbcType.toUpperCase(java.util.Locale.ROOT)) {
            case "CHAR", "VARCHAR", "LONGVARCHAR", "NCHAR", "NVARCHAR", "LONGNVARCHAR", "CLOB", "NCLOB" -> true;
            default -> false;
        };
    }
}
