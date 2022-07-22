package org.mybatis.generator.plugins;

import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.internal.util.StringUtility;

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
        String s = buildViewColumnMetaAnnotation(introspectedColumn);
        if (StringUtility.stringHasValue(s)) {
            field.addAnnotation(s);
            topLevelClass.addImportedType("com.vgosoft.core.annotation.ViewColumnMeta");
        }
        return true;
    }

    /**
     * viewVO类的ViewMetaAnnotation
     */
    @Override
    public boolean voViewFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable) {
        String s = buildViewColumnMetaAnnotation(introspectedColumn);
        if (StringUtility.stringHasValue(s)) {
            field.addAnnotation(s);
            topLevelClass.addImportedType("com.vgosoft.core.annotation.ViewColumnMeta");
        }
        return true;
    }

    /**
     * 构造注解@ViewColumnMeta
     */
    private String buildViewColumnMetaAnnotation(IntrospectedColumn introspectedColumn) {
        StringBuilder sb = new StringBuilder();
        sb.append("@ViewColumnMeta(").append("value = \"");
        sb.append(introspectedColumn.getJavaProperty()).append("\"");
        sb.append(",title = \"");
        if (StringUtils.isNotEmpty(introspectedColumn.getRemarks())) {
            sb.append(StringUtility.remarkLeft(introspectedColumn.getRemarks()));
        } else {
            sb.append(introspectedColumn.getActualColumnName());
        }
        sb.append("\"");
        sb.append(")");
        return sb.toString();
    }

}
