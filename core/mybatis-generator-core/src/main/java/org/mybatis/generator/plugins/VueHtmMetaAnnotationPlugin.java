package org.mybatis.generator.plugins;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.config.HtmlElementDescriptor;
import org.mybatis.generator.config.HtmlGeneratorConfiguration;
import org.mybatis.generator.config.HtmlLayoutDescriptor;
import org.mybatis.generator.custom.annotations.VueFormItemMetaDesc;
import org.mybatis.generator.custom.annotations.VueFormMetaDesc;

import java.util.List;

/**
 * 添加VueHtmMetaAnnotationPlugin
 */
public class VueHtmMetaAnnotationPlugin extends PluginAdapter {

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }


    @Override
    public boolean voModelRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        HtmlGeneratorConfiguration htmlGeneratorConfiguration = getHtmlGeneratorConfiguration(introspectedTable);
        if (htmlGeneratorConfiguration != null) {
            new VueFormMetaDesc(introspectedTable).addAnnotationToTopLevelClass(topLevelClass);
        }
        return true;
    }

    /**
     * VO抽象父类的ColumnMetaAnnotation
     */
    @Override
    public boolean voAbstractFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable) {
        addVueFormItemMetaDescAnnotation(field, topLevelClass, introspectedColumn, introspectedTable);
        return true;
    }

    @Override
    public boolean voModelFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable) {
        addVueFormItemMetaDescAnnotation(field, topLevelClass, introspectedColumn, introspectedTable);
        return true;
    }

    private void addVueFormItemMetaDescAnnotation(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable) {
        if (introspectedColumn == null) {
            return;
        }
        HtmlGeneratorConfiguration htmlGeneratorConfiguration = getHtmlGeneratorConfiguration(introspectedTable);
        if (htmlGeneratorConfiguration != null) {
            HtmlElementDescriptor elementDescriptor = htmlGeneratorConfiguration.getElementDescriptors().stream()
                    .filter(e -> e.getColumn().getActualColumnName().equals(introspectedColumn.getActualColumnName()))
                    .findFirst().orElse(null);
            VueFormItemMetaDesc vueFormItemMetaDesc = VueFormItemMetaDesc.create(introspectedColumn);
            //设置span
            HtmlLayoutDescriptor layoutDescriptor = htmlGeneratorConfiguration.getLayoutDescriptor();
            if (layoutDescriptor.getExclusiveColumns().contains(introspectedColumn.getActualColumnName())) {
                vueFormItemMetaDesc.setSpan(24);
            }else{
                vueFormItemMetaDesc.setSpan(24/layoutDescriptor.getPageColumnsNum());
            }
            //设置
            if (elementDescriptor != null) {
                vueFormItemMetaDesc.setComponent(elementDescriptor.getTagType());
                vueFormItemMetaDesc.setMultiple(Boolean.valueOf(elementDescriptor.getMultiple()));
                //todo 更多设置

            }else{
                vueFormItemMetaDesc.setComponent("input");
            }
            vueFormItemMetaDesc.getImportedTypes().forEach(topLevelClass::addImportedType);
            field.addAnnotation(vueFormItemMetaDesc.toAnnotation());
        }
    }

    private HtmlGeneratorConfiguration getHtmlGeneratorConfiguration(IntrospectedTable introspectedTable) {
        List<HtmlGeneratorConfiguration> htmlMapGeneratorConfigurations = introspectedTable.getTableConfiguration().getHtmlMapGeneratorConfigurations();
        if (htmlMapGeneratorConfigurations.isEmpty()) {
            return null;
        }else{
            return htmlMapGeneratorConfigurations.stream().filter(HtmlGeneratorConfiguration::isDefaultConfig).findFirst().orElse(htmlMapGeneratorConfigurations.get(0));
        }
    }

}
