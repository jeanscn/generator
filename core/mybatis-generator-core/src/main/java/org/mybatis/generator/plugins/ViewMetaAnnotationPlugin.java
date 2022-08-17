package org.mybatis.generator.plugins;

import com.vgosoft.tool.core.VStringUtil;
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
            topLevelClass.addMultipleImports("ViewColumnMeta");
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
            topLevelClass.addMultipleImports("ViewColumnMeta");
        }
        return true;
    }

    /**
     * 构造注解@ViewColumnMeta
     */
    private String buildViewColumnMetaAnnotation(IntrospectedColumn introspectedColumn) {
        String value = VStringUtil.format("value = \"{0}\"", introspectedColumn.getJavaProperty());
        String title = VStringUtil.format("title = \"{0}\""
                , StringUtils.isNotEmpty(introspectedColumn.getRemarks())?introspectedColumn.getRemarks():introspectedColumn.getActualColumnName());
        String order = VStringUtil.format("order = {0}", introspectedColumn.getOrder());
        if (introspectedColumn.getOrder()!=20) {
            return VStringUtil.format("@ViewColumnMeta({0})",String.join(",", value,title,order));
        }else{
            return VStringUtil.format("@ViewColumnMeta({0})",String.join(",", value,title));
        }
    }

}
