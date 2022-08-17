package org.mybatis.generator.plugins;

import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.config.PropertyRegistry;
import org.mybatis.generator.config.TableConfiguration;
import org.mybatis.generator.internal.util.JavaBeansUtil;
import org.mybatis.generator.internal.util.StringUtility;

import java.util.List;

import static org.mybatis.generator.custom.ConstantsUtil.ANNOTATION_COLUMN_META;
import static org.mybatis.generator.custom.ConstantsUtil.ANNOTATION_TABLE_META;
import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

/**
 * 添加TableMetaAnnotation
 */
public class TableMetaAnnotationPlugin extends PluginAdapter {

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    /**
     * model类的@apiModel
     */
    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        //添加实体对象元数据注解
        String tableMetaAnnotation = buildTableMetaAnnotation(introspectedTable, topLevelClass);
        if (stringHasValue(tableMetaAnnotation)) {
            topLevelClass.addAnnotation(tableMetaAnnotation);
            topLevelClass.addImportedType(ANNOTATION_TABLE_META);
        }
        return true;
    }

    @Override
    public boolean modelFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        String columnMetaAnnotation = buildColumnMetaAnnotation(field, introspectedTable, topLevelClass);
        if (stringHasValue(columnMetaAnnotation)) {
            field.addAnnotation(columnMetaAnnotation);
            topLevelClass.addImportedType(ANNOTATION_COLUMN_META);
        }
        return true;
    }

    @Override
    public boolean voModelAbstractClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        String tableMetaAnnotation = buildTableMetaAnnotation(introspectedTable, topLevelClass);
        if (stringHasValue(tableMetaAnnotation)) {
            topLevelClass.addAnnotation(tableMetaAnnotation);
            topLevelClass.addImportedType(ANNOTATION_TABLE_META);
        }
        return true;
    }

    @Override
    public boolean voAbstractFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable) {
        String columnMetaAnnotation = buildColumnMetaAnnotation(field, introspectedTable, topLevelClass);
        if (stringHasValue(columnMetaAnnotation)) {
            field.addAnnotation(columnMetaAnnotation);
            topLevelClass.addImportedType(ANNOTATION_COLUMN_META);
        }
        return true;
    }

    @Override
    public boolean voModelFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable) {
        String columnMetaAnnotation = buildColumnMetaAnnotation(field, introspectedTable, topLevelClass);
        if (stringHasValue(columnMetaAnnotation)) {
            field.addAnnotation(columnMetaAnnotation);
            topLevelClass.addImportedType(ANNOTATION_COLUMN_META);
        }
        return true;
    }

    private boolean isNoMetaAnnotation(IntrospectedTable introspectedTable) {
        return context.getAnyPropertyBoolean(PropertyRegistry.ANY_NO_META_ANNOTATION,
                "false",
                introspectedTable.getTableConfiguration().getJavaModelGeneratorConfiguration(),
                introspectedTable.getTableConfiguration(),
                context);
    }

    /**
     * 构造注解@ApiModelProperty
     */
    private String buildColumnMetaAnnotation(Field field, IntrospectedTable introspectedTable, TopLevelClass topLevelClass) {
        StringBuilder sb = new StringBuilder();
        if (isNoMetaAnnotation(introspectedTable)) {
            return sb.toString();
        }
        for (IntrospectedColumn column : introspectedTable.getAllColumns()) {
            if (column.getJavaProperty().equals(field.getName())) {
                sb.append("@ColumnMeta(").append("value = \"");
                sb.append(column.getActualColumnName()).append("\"");
                sb.append(",description = \"");
                if (StringUtils.isNotEmpty(column.getRemarks())) {
                    sb.append(StringUtility.remarkLeft(column.getRemarks()));
                } else {
                    sb.append(column.getActualColumnName());
                }
                sb.append("\"");
                if (column.getLength() != 255) {
                    sb.append(",size =");
                    sb.append(column.getLength());
                }
                sb.append(",order = ").append(column.getOrder());

                if (!"VARCHAR".equals(column.getJdbcTypeName())) {
                    sb.append(",type = JDBCType.").append(column.getJdbcTypeName());
                    topLevelClass.addImportedType("java.sql.JDBCType");
                }
                String datePattern = column.getDatePattern();
                if (column.getJdbcTypeName().equals("TIMESTAMP")) {
                    datePattern = "yyyy-MM-dd HH:mm:ss";
                }
                if (stringHasValue(datePattern)) {
                    sb.append(",dataFormat =\"");
                    sb.append(datePattern);
                    sb.append("\"");
                }
                sb.append(")");
                return sb.toString();
            }
        }
        if (sb.length() > 0) {
            return sb.toString();
        } else {
            return "";
        }
    }

    /**
     * model类的@apiModel
     */
    private String buildTableMetaAnnotation(IntrospectedTable introspectedTable, TopLevelClass topLevelClass) {
        StringBuilder sb = new StringBuilder();
        if (isNoMetaAnnotation(introspectedTable)) {
            return sb.toString();
        }
        TableConfiguration tableConfiguration = introspectedTable.getTableConfiguration();
        sb.append("@TableMeta(value = \"").append(tableConfiguration.getTableName()).append("\"");

        final String alias = introspectedTable.getFullyQualifiedTable().getAlias();
        if (StringUtils.isNotBlank(alias)) {
            sb.append(", alias =  \"");
            sb.append(alias);
            sb.append("\"");
        }
        if (introspectedTable.getRemarks() != null) {
            sb.append(", descript = \"");
            sb.append(StringUtility.remarkLeft(introspectedTable.getRemarks()));
            sb.append("\"");
        } else {
            sb.append(", descript = \"").append("\"");
        }
        if (introspectedTable.getRules().isNoMetaAnnotation()) {
            sb.append(", summary = false");
        }
        sb.append(", beanname = \"");
        sb.append(JavaBeansUtil.getFirstCharacterLowercase(introspectedTable.getControllerBeanName()));
        sb.append("\")");
        return sb.toString();
    }

}
