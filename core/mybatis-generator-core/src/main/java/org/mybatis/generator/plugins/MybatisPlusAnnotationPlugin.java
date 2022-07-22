package org.mybatis.generator.plugins;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.internal.util.StringUtility;

import java.util.List;

/**
 * 添加日期属性的JsonFormat
 *
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-04-19 14:51
 * @version 3.0
 */
public class MybatisPlusAnnotationPlugin extends PluginAdapter {

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public void setContext(Context context) {
        super.setContext(context);
    }

    /**
     * model类的@apiModel
     */
    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        try {
            if (introspectedTable.getRules().isIntegrateMybatisPlus()) {
                String apiModelAnnotation = buildModelAnnotation(introspectedTable);
                topLevelClass.addAnnotation(apiModelAnnotation);
                topLevelClass.addImportedType("com.baomidou.mybatisplus.annotation.TableName");
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    /**
     * 属性注解@ApiModelProperty
     */
    @Override
    public boolean modelFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        //添加日期序列化格式注解
        try {
            if (introspectedTable.getRules().isIntegrateMybatisPlus()) {
                String modelPropertyAnnotation = buildModelPropertyAnnotation(field, introspectedTable);
                if (StringUtility.stringHasValue(modelPropertyAnnotation)) {
                    field.addAnnotation(modelPropertyAnnotation);
                    topLevelClass.addImportedType("com.baomidou.mybatisplus.annotation.TableField");
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
    private String buildModelPropertyAnnotation(Field field, IntrospectedTable introspectedTable) {
        StringBuilder sb = new StringBuilder();
        for (IntrospectedColumn column : introspectedTable.getAllColumns()) {
            if (column.getJavaProperty().equals(field.getName())) {
                sb.append("@TableField(").append("value = \"");
                sb.append(column.getActualColumnName()).append("\"");
                sb.append(")");
                return sb.toString();
            }
        }
        return "";
    }

    /**
     * model类的@apiModel
     */
    private String buildModelAnnotation(IntrospectedTable introspectedTable) {
        StringBuilder sb = new StringBuilder();
        FullyQualifiedJavaType fullyQualifiedJavaType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        sb.append("@TableName(value = \"").append(introspectedTable.getTableConfiguration().getTableName()).append("\"");
        sb.append(" )");
        return sb.toString();
    }
}
