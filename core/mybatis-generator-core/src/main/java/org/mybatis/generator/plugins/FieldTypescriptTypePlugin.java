package org.mybatis.generator.plugins;

import org.mybatis.generator.api.AbstractIntrospectedTable;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.config.ColumnOverride;
import org.mybatis.generator.config.TableConfiguration;
import org.mybatis.generator.custom.FieldItem;
import org.mybatis.generator.internal.util.Mb3GenUtil;
import org.mybatis.generator.internal.util.StringUtility;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 添加Vo类TypeScript的属性
 *
 * @author cen_c
 */
public class FieldTypescriptTypePlugin extends PluginAdapter {

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public boolean modelFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, AbstractIntrospectedTable introspectedTable, ModelClassType modelClassType) {
        FieldItem fieldItem = getFieldItem(introspectedTable, introspectedColumn, field);
        if (introspectedColumn != null && introspectedColumn.isPrimaryKey()) {
            fieldItem.setRequired(false);
        }
        addTypeScriptType(topLevelClass, introspectedTable, fieldItem, introspectedColumn);
        return true;
    }

    @Override
    public boolean voAbstractFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, AbstractIntrospectedTable introspectedTable) {
        FieldItem fieldItem = getFieldItem(introspectedTable, introspectedColumn, field);
        fieldItem.setRequired(false);
        addTypeScriptType(topLevelClass, introspectedTable, fieldItem, introspectedColumn);
        return true;
    }

    @Override
    public boolean voModelFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, AbstractIntrospectedTable introspectedTable) {
        FieldItem fieldItem = getFieldItem(introspectedTable, introspectedColumn, field);
        addTypeScriptType(topLevelClass, introspectedTable, fieldItem, introspectedColumn);
        return true;
    }

    @Override
    public boolean voUpdateFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, AbstractIntrospectedTable introspectedTable) {
        FieldItem fieldItem = getFieldItem(introspectedTable, introspectedColumn, field);
        if (introspectedColumn != null && introspectedColumn.isPrimaryKey()) {
            fieldItem.setRequired(true);
        }
        addTypeScriptType(topLevelClass, introspectedTable, fieldItem, introspectedColumn);
        return true;
    }

    @Override
    public boolean voCreateFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, AbstractIntrospectedTable introspectedTable) {
        FieldItem fieldItem = getFieldItem(introspectedTable, introspectedColumn, field);
        if (introspectedColumn != null && introspectedColumn.isPrimaryKey()) {
            fieldItem.setRequired(false);
        }
        addTypeScriptType(topLevelClass, introspectedTable, fieldItem, introspectedColumn);
        return true;
    }

    @Override
    public boolean voRequestFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, AbstractIntrospectedTable introspectedTable) {
        FieldItem fieldItem = getFieldItem(introspectedTable, introspectedColumn, field);
        fieldItem.setRequired(false);
        addTypeScriptType(topLevelClass, introspectedTable, fieldItem, introspectedColumn);
        return true;
    }

    private void addTypeScriptType(TopLevelClass topLevelClass, AbstractIntrospectedTable introspectedTable, FieldItem fieldItem, IntrospectedColumn introspectedColumn) {
        // 生成typescript的类型描述
        if (topLevelClass.getSuperClass().isPresent()) {
            Mb3GenUtil.addTypeScriptTypeDescriptor(introspectedTable, topLevelClass, fieldItem, topLevelClass.getSuperClass().get().getShortName());
        } else {
            Mb3GenUtil.addTypeScriptTypeDescriptor(introspectedTable, topLevelClass, fieldItem, null);
        }
    }

    private FieldItem getFieldItem(AbstractIntrospectedTable introspectedTable, IntrospectedColumn introspectedColumn, Field field) {
        FieldItem fieldItem = new FieldItem(field.getName(), field.getType().getShortName());
        fieldItem.setRemarks(field.getRemark());
        fieldItem.setRequired(isRequired(introspectedTable, introspectedColumn, field));
        if (introspectedColumn != null) {
            fieldItem.setRequired(introspectedColumn.isRequired());
        }
        return fieldItem;
    }

    private boolean isRequired(AbstractIntrospectedTable introspectedTable, IntrospectedColumn introspectedColumn, Field field) {
        Set<String> notRequiredFields = new HashSet<>(Arrays.asList(
                "deleteFlag", "version", "created", "modified", "createdId", "modifiedId"
        ));
        if (introspectedColumn == null) {
            return field.getIsRequired() != null && field.getIsRequired();
        } else if (notRequiredFields.contains(introspectedColumn.getJavaProperty())) {
            return false;
        }
        TableConfiguration tc = introspectedTable.getTableConfiguration();
        Set<String> ignoreColumns = tc.getValidateIgnoreColumns();
        ColumnOverride override = introspectedTable.getTableConfiguration().getColumnOverrides().stream().filter(columnOverride -> columnOverride.getColumnName().equals(introspectedColumn.getActualColumnName())).findFirst().orElse(null);
        boolean isOverrideRequired = override != null && override.isRequired();
        boolean contains = ignoreColumns.contains(introspectedColumn.getActualColumnName());
        return !contains && (!introspectedColumn.isNullable() || introspectedColumn.isForeignKey() || isOverrideRequired);
    }
}
