package org.mybatis.generator.plugins;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.config.VOViewGeneratorConfiguration;
import org.mybatis.generator.custom.annotations.ViewColumnMeta;

import java.util.List;

/**
 * 添加ViewMetaAnnotation
 */
public class ViewMetaAnnotationPlugin extends PluginAdapter {

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public boolean voAbstractFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable) {
        if (introspectedTable.getRules().isGenerateViewVO()) {
            ViewColumnMeta viewColumnMeta = ViewColumnMeta.create(introspectedColumn, introspectedTable);
            updateOrder(field, introspectedTable, viewColumnMeta);
            field.addAnnotation(viewColumnMeta.toAnnotation());
            topLevelClass.addImportedTypes(viewColumnMeta.getImportedTypes());
        }
        return true;
    }

    /**
     * viewVO类的ViewMetaAnnotation
     */
    @Override
    public boolean voViewFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable) {
        if (!introspectedTable.getRules().isGenerateViewVO()) {
            return true;
        }
        ViewColumnMeta viewColumnMeta;
        if (introspectedColumn != null) {
            viewColumnMeta = ViewColumnMeta.create(introspectedColumn, introspectedTable);
        }else{
            viewColumnMeta = new ViewColumnMeta(field, field.getRemark(), introspectedTable);
        }
        updateOrder(field, introspectedTable, viewColumnMeta);
        field.addAnnotation(viewColumnMeta.toAnnotation());
        topLevelClass.addImportedTypes(viewColumnMeta.getImportedTypes());
        return true;
    }

    private void updateOrder(Field field, IntrospectedTable introspectedTable, ViewColumnMeta viewColumnMeta) {
        //更新order
        if (introspectedTable.getRules().isGenerateViewVO()) {
            VOViewGeneratorConfiguration configuration = introspectedTable.getTableConfiguration().getVoGeneratorConfiguration().getVoViewConfiguration();
            List<String> displayFields = configuration.getDefaultDisplayFields();
            if (displayFields.size()>0) {
                if (displayFields.contains(field.getName())) {
                    viewColumnMeta.setOrder(displayFields.indexOf(field.getName())+100);
                }
            }
        }
    }
}
