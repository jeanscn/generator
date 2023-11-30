package org.mybatis.generator.plugins;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
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

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public boolean modelFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
       addJsonFieldFormat(field, topLevelClass, introspectedColumn);
        addDateTimeFormat(field, topLevelClass, introspectedColumn);
       return true;
    }

    @Override
    public boolean voAbstractFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable) {
        addJsonFieldFormat(field, topLevelClass, introspectedColumn);
        addDateTimeFormat(field, topLevelClass, introspectedColumn);
        return true;
    }

    @Override
    public boolean voModelFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable) {
        addJsonFieldFormat(field, topLevelClass, introspectedColumn);
        addDateTimeFormat(field, topLevelClass, introspectedColumn);
        return true;
    }

    @Override
    public boolean voUpdateFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable) {
        addJsonFieldFormat(field, topLevelClass, introspectedColumn);
        return true;
    }

    @Override
    public boolean voCreateFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable) {
        addJsonFieldFormat(field, topLevelClass, introspectedColumn);
        addDateTimeFormat(field, topLevelClass, introspectedColumn);
        return true;
    }

    @Override
    public boolean voRequestFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable) {
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
            topLevelClass.addImportedType("com.alibaba.fastjson2.annotation.JSONField");
            field.addAnnotation("@JSONField(format = \"" + datePattern + "\")");
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
}
