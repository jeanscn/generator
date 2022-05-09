package org.mybatis.generator.plugins;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.TopLevelClass;

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
        //添加日期序列化格式注解
        try {
            if (introspectedColumn.isJDBCDateColumn()) {
                field.addAnnotation("@JsonFormat(locale=\"zh\", timezone=\"GMT+8\", pattern=\"yyyy-MM-dd\")");
                topLevelClass.addImportedType("com.fasterxml.jackson.annotation.JsonFormat");
            }
            if (introspectedColumn.isJDBCTimeColumn() || introspectedColumn.isJDBCTimeStampColumn()) {
                field.addAnnotation("@JsonFormat(locale=\"zh\", timezone=\"GMT+8\", pattern=\"yyyy-MM-dd HH:mm:ss\")");
                topLevelClass.addImportedType("com.fasterxml.jackson.annotation.JsonFormat");
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
