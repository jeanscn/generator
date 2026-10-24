package org.mybatis.generator.plugins;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.AbstractIntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.config.PropertyRegistry;
import org.mybatis.generator.custom.annotations.ColumnMetaDesc;
import org.mybatis.generator.custom.annotations.TableMetaDesc;

import java.util.List;

/**
 * 添加TableMetaAnnotation
 * @author cen_c
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
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, AbstractIntrospectedTable abstractIntrospectedTable) {
        //添加实体对象元数据注解
       addTableMetaAnnotation(abstractIntrospectedTable, topLevelClass);
       return true;
    }

    @Override
    public boolean modelFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, AbstractIntrospectedTable abstractIntrospectedTable, ModelClassType modelClassType) {
        addColumnMetaAnnotation(field, topLevelClass, abstractIntrospectedTable, introspectedColumn);
        return true;
    }

    @Override
    public boolean voAbstractFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, AbstractIntrospectedTable abstractIntrospectedTable) {
        addColumnMetaAnnotation(field, topLevelClass, abstractIntrospectedTable, introspectedColumn);
        return true;
    }

    @Override
    public boolean voModelFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, AbstractIntrospectedTable abstractIntrospectedTable) {
        addColumnMetaAnnotation(field, topLevelClass, abstractIntrospectedTable,introspectedColumn);
        return true;
    }

    @Override
    public boolean voViewFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, AbstractIntrospectedTable abstractIntrospectedTable) {
        addColumnMetaAnnotation(field, topLevelClass, abstractIntrospectedTable,introspectedColumn);
        return true;
    }

    private boolean isNoMetaAnnotation(AbstractIntrospectedTable abstractIntrospectedTable) {
        return context.getAnyPropertyBoolean(PropertyRegistry.ANY_NO_META_ANNOTATION,
                "false",
                abstractIntrospectedTable.getTableConfiguration().getJavaModelGeneratorConfiguration(),
                abstractIntrospectedTable.getTableConfiguration(),
                context);
    }

    /**
     * 属性@ColumnMeta
     */
    private void addColumnMetaAnnotation(Field field,
                                         TopLevelClass topLevelClass,
                                         AbstractIntrospectedTable abstractIntrospectedTable,
                                         IntrospectedColumn introspectedColumn) {

        if (isNoMetaAnnotation(abstractIntrospectedTable) ||introspectedColumn==null) {
            return;
        }
        ColumnMetaDesc columnMetaDesc = new ColumnMetaDesc(introspectedColumn);
        field.addAnnotation(columnMetaDesc.toAnnotation());
        topLevelClass.addImportedTypes(columnMetaDesc.getImportedTypes());
    }

    /**
     * 类的@TableMeta
     */
    private void addTableMetaAnnotation(AbstractIntrospectedTable abstractIntrospectedTable, TopLevelClass topLevelClass) {
        if (isNoMetaAnnotation(abstractIntrospectedTable)) {
            return;
        }
        TableMetaDesc tableMetaDesc = new TableMetaDesc(abstractIntrospectedTable);
        topLevelClass.getSuperClass().ifPresent(superClass -> tableMetaDesc.setSuperClass(superClass.getShortNameWithoutTypeArguments()+".class"));
        topLevelClass.getSuperInterfaceTypes().forEach(superInterface -> tableMetaDesc.addSuperInterface(superInterface.getShortNameWithoutTypeArguments()+".class"));
        topLevelClass.addAnnotation(tableMetaDesc.toAnnotation());
        topLevelClass.addImportedTypes(tableMetaDesc.getImportedTypes());
    }

}
