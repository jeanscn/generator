package org.mybatis.generator.plugins;

import com.vgosoft.core.db.enums.JDBCTypeTypeEnum;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.custom.DictTypeEnum;
import org.mybatis.generator.custom.annotations.AbstractAnnotation;
import org.mybatis.generator.custom.annotations.ExcelProperty;
import org.mybatis.generator.internal.util.JavaBeansUtil;
import org.mybatis.generator.internal.util.StringUtility;

import javax.annotation.Nullable;
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

    private void addExcelAnnotation(TopLevelClass topLevelClass,Field field,IntrospectedColumn introspectedColumn){
        String remark = introspectedColumn == null ? field.getRemark() : introspectedColumn.getRemarks(true);
        ExcelProperty excelProperty = new ExcelProperty(remark);
        if (introspectedColumn != null) {
            excelProperty.setOrder(introspectedColumn.getOrder());
        }
        if (field.getAnnotations().stream()
                .anyMatch(annotation -> annotation.contains("@Dict"))) {
            excelProperty.setConverter("ExportDictConverter.class");
            excelProperty.addImports("com.vgosoft.plugins.excel.converter.ExportDictConverter");
        }else if(field.getType().getShortName().equals("LocalDateTime")){
            excelProperty.setConverter("LocalDateTimeConverter.class");
            excelProperty.addImports("com.vgosoft.plugins.excel.converter.LocalDateTimeConverter");
        }else if (field.getType().getShortName().equals("LocalDate")){
            excelProperty.setConverter("LocalDateConverter.class");
            excelProperty.addImports("com.vgosoft.plugins.excel.converter.LocalDateConverter");
        }else if (field.getType().getShortName().equals("LocalTime")){
            excelProperty.setConverter("LocalTimeConverter.class");
            excelProperty.addImports("com.vgosoft.plugins.excel.converter.LocalTimeConverter");
        }else if (field.getType().getShortName().equals("Instant")){
            excelProperty.setConverter("InstantConverter.class");
            excelProperty.addImports("com.vgosoft.plugins.excel.converter.InstantConverter");
        }
        if (field.getInitializationString().isPresent()) {
            field.addAnnotation("@Builder.Default");
        }
        field.addAnnotation(excelProperty.toAnnotation());
        topLevelClass.addImportedTypes(excelProperty.getImportedTypes());
    }
}
