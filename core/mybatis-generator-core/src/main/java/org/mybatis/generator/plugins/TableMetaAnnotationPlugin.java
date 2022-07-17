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
        try {
            boolean propertyBoolean = context.getAnyPropertyBoolean(PropertyRegistry.ANY_NO_META_ANNOTATION,
                    null,
                    introspectedTable.getTableConfiguration().getJavaModelGeneratorConfiguration(),
                    introspectedTable.getTableConfiguration(),
                    context);
            if (!propertyBoolean) {
                //添加实体对象元数据注解
                String tableMetaAnnotation = buildTableMetaAnnotation(introspectedTable, topLevelClass);
                topLevelClass.addAnnotation(tableMetaAnnotation);
                topLevelClass.addImportedType(ANNOTATION_TABLE_META);
                //添加属性元数据注解
                boolean added = false;
                for (int i = 0; i < topLevelClass.getFields().size(); i++) {
                    Field field = topLevelClass.getFields().get(i);
                    String columnMetaAnnotation = buildColumnMetaAnnotation(field, introspectedTable, topLevelClass, i + 10);
                    if (StringUtility.stringHasValue(columnMetaAnnotation)) {
                        field.addAnnotation(columnMetaAnnotation);
                        added = true;
                    }
                }
                if (added) {
                    topLevelClass.addImportedType(ANNOTATION_COLUMN_META);
                }

            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    /**
     * 构造注解@ApiModelProperty
     */
    private String buildColumnMetaAnnotation(Field field, IntrospectedTable introspectedTable, TopLevelClass topLevelClass, int i) {
        StringBuilder sb = new StringBuilder();
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
                sb.append(",size =");
                sb.append(column.getLength());
                sb.append(",order = ").append((i + 20));

                if (!"VARCHAR".equals(column.getJdbcTypeName())) {
                    sb.append(",type = JDBCType.").append(column.getJdbcTypeName());
                    topLevelClass.addImportedType("java.sql.JDBCType");
                }
                String datePattern = column.getDatePattern();
                if (column.getJdbcTypeName().equals("TIMESTAMP")) {
                    datePattern = "yyyy-MM-dd HH:mm:ss";
                }
                if (StringUtility.stringHasValue(datePattern)) {
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
