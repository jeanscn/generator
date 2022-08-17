package org.mybatis.generator.plugins;

import com.vgosoft.core.db.enums.JDBCTypeTypeEnum;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.internal.util.JavaBeansUtil;
import org.mybatis.generator.internal.util.StringUtility;

import java.sql.JDBCType;
import java.util.List;

/**
 * 添加日期属性的JsonFormat
 *
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-04-19 14:51
 * @version 3.0
 */
public class EasyExcelAnnotationPlugin extends PluginAdapter {

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public boolean voExcelFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable) {
        addExcelAnnotation(topLevelClass,field,introspectedColumn);
        return true;
    }

    @Override
    public boolean voAbstractFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable) {
        addExcelAnnotation(topLevelClass,field,introspectedColumn);
        return true;
    }

    private void addExcelAnnotation(TopLevelClass topLevelClass,Field field,IntrospectedColumn introspectedColumn){
        field.addAnnotation("@ExcelProperty(\""+ StringUtility.remarkLeft(introspectedColumn.getRemarks()) +"\")");
        topLevelClass.addMultipleImports("ExcelProperty");
    }
}
