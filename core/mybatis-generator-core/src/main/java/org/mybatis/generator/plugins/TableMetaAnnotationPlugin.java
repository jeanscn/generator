package org.mybatis.generator.plugins;

import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.config.PropertyRegistry;
import org.mybatis.generator.config.TableConfiguration;
import org.mybatis.generator.custom.annotations.ColumnMeta;
import org.mybatis.generator.custom.annotations.TableMeta;
import org.mybatis.generator.internal.util.JavaBeansUtil;

import java.util.Arrays;
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
       addTableMetaAnnotation(introspectedTable, topLevelClass);
       return true;
    }

    @Override
    public boolean modelFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        addColumnMetaAnnotation(field, topLevelClass,introspectedTable, introspectedColumn);
        return true;
    }

    @Override
    public boolean voAbstractFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable) {
        addColumnMetaAnnotation(field, topLevelClass,introspectedTable, introspectedColumn);
        return true;
    }

    @Override
    public boolean voModelFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable) {
        addColumnMetaAnnotation(field, topLevelClass, introspectedTable,introspectedColumn);
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
     * 属性@ColumnMeta
     */
    private void addColumnMetaAnnotation(Field field,
                                         TopLevelClass topLevelClass,
                                         IntrospectedTable introspectedTable,
                                         IntrospectedColumn introspectedColumn) {

        if (isNoMetaAnnotation(introspectedTable)) {
            return;
        }
        ColumnMeta columnMeta = new ColumnMeta(introspectedColumn);
        field.addAnnotation(columnMeta.toAnnotation());
        topLevelClass.addMultipleImports(columnMeta.multipleImports());
    }

    /**
     * 类的@TableMeta
     */
    private void addTableMetaAnnotation(IntrospectedTable introspectedTable, TopLevelClass topLevelClass) {
        if (isNoMetaAnnotation(introspectedTable)) {
            return;
        }
        TableMeta tableMeta = new TableMeta(introspectedTable);
        topLevelClass.addAnnotation(tableMeta.toAnnotation());
        topLevelClass.addMultipleImports(tableMeta.multipleImports());
    }

}
