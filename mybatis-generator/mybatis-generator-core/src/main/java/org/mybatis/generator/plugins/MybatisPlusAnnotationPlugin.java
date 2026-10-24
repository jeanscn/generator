package org.mybatis.generator.plugins;

import org.mybatis.generator.api.AbstractIntrospectedTable;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.custom.annotations.mybatisplus.TableField;
import org.mybatis.generator.custom.annotations.mybatisplus.TableName;

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

    /**
     * model类的@TableName注解
     */
    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, AbstractIntrospectedTable abstractIntrospectedTable) {
        buildModelAnnotation(abstractIntrospectedTable).addAnnotationToTopLevelClass(topLevelClass);
        return true;
    }

    /**
     * 属性注解@TableField
     */
    @Override
    public boolean modelFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, AbstractIntrospectedTable abstractIntrospectedTable, ModelClassType modelClassType) {
        // 添加TableField注解
        if (abstractIntrospectedTable.getRules().isIntegrateMybatisPlus()) {
            TableField tableField = buildModelPropertyAnnotation(field, abstractIntrospectedTable);
            tableField.addAnnotationToField(field,topLevelClass);
        }
        return true;
    }

    /**
     * 构造注解@TableField
     */
    private TableField buildModelPropertyAnnotation(final Field field, AbstractIntrospectedTable abstractIntrospectedTable) {
        final TableField tableField = new TableField();
        abstractIntrospectedTable.getAllColumns().stream()
                .filter(c->c.getJavaProperty().equals(field.getName()))
                .findFirst()
                .ifPresent(c-> tableField.setValue(c.getActualColumnName()));
        if (tableField.getValue() == null) {
            tableField.setExist(false);
        }
        return tableField;
    }

    /**
     * 构造注解@TableName
     */
    private TableName buildModelAnnotation(AbstractIntrospectedTable abstractIntrospectedTable) {
        return new TableName(abstractIntrospectedTable.getTableConfiguration().getTableName());
    }
}
