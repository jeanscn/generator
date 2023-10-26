package org.mybatis.generator.plugins;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.config.PropertyRegistry;
import org.mybatis.generator.config.VOViewGeneratorConfiguration;
import org.mybatis.generator.custom.annotations.ColumnMetaDesc;
import org.mybatis.generator.custom.annotations.TableMetaDesc;

import java.util.List;

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

    @Override
    public boolean voViewFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable) {
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

        if (isNoMetaAnnotation(introspectedTable) ||introspectedColumn==null) {
            return;
        }
        ColumnMetaDesc columnMetaDesc = new ColumnMetaDesc(introspectedColumn);
        field.addAnnotation(columnMetaDesc.toAnnotation());
        topLevelClass.addImportedTypes(columnMetaDesc.getImportedTypes());
    }

    /**
     * 类的@TableMeta
     */
    private void addTableMetaAnnotation(IntrospectedTable introspectedTable, TopLevelClass topLevelClass) {
        if (isNoMetaAnnotation(introspectedTable)) {
            return;
        }
        TableMetaDesc tableMetaDesc = new TableMetaDesc(introspectedTable);
        topLevelClass.getSuperClass().ifPresent(superClass -> {
            tableMetaDesc.setSuperClass(superClass.getShortNameWithoutTypeArguments()+".class");
        });
        topLevelClass.getSuperInterfaceTypes().forEach(superInterface -> {
            tableMetaDesc.addSuperInterface(superInterface.getShortNameWithoutTypeArguments()+".class");
        });
        topLevelClass.addAnnotation(tableMetaDesc.toAnnotation());
        topLevelClass.addImportedTypes(tableMetaDesc.getImportedTypes());
    }

}
