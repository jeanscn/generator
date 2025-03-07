package org.mybatis.generator.plugins;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.custom.annotations.ExcelPropertyDesc;

import java.util.List;
import java.util.Set;

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
    public boolean voExcelFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable,int index) {
        addExcelAnnotation(topLevelClass,field,introspectedColumn,index);
        Set<String> ignoreFields = introspectedTable.getTableConfiguration().getVoGeneratorConfiguration().getVoExcelConfiguration().getExportIgnoreFields();
        if (ignoreFields.contains(field.getName())) {
            field.addAnnotation("@ExcelIgnore");
            topLevelClass.addImportedType("com.alibaba.excel.annotation.ExcelIgnore");
        }
        return true;
    }

    @Override
    public boolean voExcelImportFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable,int index) {
        addExcelAnnotation(topLevelClass,field,introspectedColumn,index);
        if (field.getInitializationString().isPresent()) {
            field.addAnnotation("@Builder.Default");
        }
        Set<String> ignoreFields = introspectedTable.getTableConfiguration().getVoGeneratorConfiguration().getVoExcelConfiguration().getImportIgnoreFields();
        if (ignoreFields.contains(field.getName())) {
            field.addAnnotation("@ExcelIgnore");
            topLevelClass.addImportedType("com.alibaba.excel.annotation.ExcelIgnore");
        }
        return true;
    }

    private void addExcelAnnotation(TopLevelClass topLevelClass,Field field,IntrospectedColumn introspectedColumn,int index){
        String remark = introspectedColumn == null ? field.getRemark() : introspectedColumn.getRemarks(true);
        ExcelPropertyDesc excelPropertyDesc = new ExcelPropertyDesc(remark);
        excelPropertyDesc.setOrder(index);
        if (field.getAnnotations().stream()
                .anyMatch(annotation -> annotation.contains("@Dict"))) {
            excelPropertyDesc.setConverter("ExportDictConverter.class");
            excelPropertyDesc.addImports("com.vgosoft.plugins.excel.converter.ExportDictConverter");
        }else if(field.getType().getShortName().equals("LocalDateTime")){
            excelPropertyDesc.setConverter("LocalDateTimeConverter.class");
            excelPropertyDesc.addImports("com.vgosoft.plugins.excel.converter.LocalDateTimeConverter");
        }else if (field.getType().getShortName().equals("LocalDate")){
            excelPropertyDesc.setConverter("LocalDateConverter.class");
            excelPropertyDesc.addImports("com.vgosoft.plugins.excel.converter.LocalDateConverter");
        }else if (field.getType().getShortName().equals("LocalTime")){
            excelPropertyDesc.setConverter("LocalTimeConverter.class");
            excelPropertyDesc.addImports("com.vgosoft.plugins.excel.converter.LocalTimeConverter");
        }else if (field.getType().getShortName().equals("Instant")){
            excelPropertyDesc.setConverter("InstantConverter.class");
            excelPropertyDesc.addImports("com.vgosoft.plugins.excel.converter.InstantConverter");
        }else if (field.getType().getShortName().equals("Date")){
            excelPropertyDesc.setConverter("DateConverter.class");
            excelPropertyDesc.addImports("com.vgosoft.plugins.excel.converter.DateConverter");
        }else if (field.getType().getShortName().equals("BigDecimal")){
            excelPropertyDesc.setConverter("BigDecimalConverter.class");
            excelPropertyDesc.addImports("com.vgosoft.plugins.excel.converter.BigDecimalConverter");
        }else if (field.getType().getShortName().equals("Integer")){
            excelPropertyDesc.setConverter("IntegerConverter.class");
            excelPropertyDesc.addImports("com.vgosoft.plugins.excel.converter.IntegerConverter");
        }
        field.addAnnotation(excelPropertyDesc.toAnnotation());
        topLevelClass.addImportedTypes(excelPropertyDesc.getImportedTypes());
    }
}
