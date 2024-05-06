package org.mybatis.generator.plugins;

import com.vgosoft.core.constant.enums.core.EntityAbstractParentEnum;
import com.vgosoft.tool.core.VArrayUtil;
import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.htmlmapper.GenerateUtils;
import org.mybatis.generator.codegen.mybatis3.vue.VueFormGenerateUtil;
import org.mybatis.generator.config.*;
import org.mybatis.generator.custom.annotations.VueFormInnerListMetaDesc;
import org.mybatis.generator.custom.annotations.VueFormItemMetaDesc;
import org.mybatis.generator.custom.annotations.VueFormMetaDesc;
import org.mybatis.generator.custom.annotations.VueFormUploadMetaDesc;
import org.mybatis.generator.internal.util.Mb3GenUtil;

import java.util.ArrayList;
import java.util.List;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

/**
 * 添加VueHtmMetaAnnotationPlugin
 */
public class VueHtmMetaAnnotationPlugin extends PluginAdapter {

    private List<IntrospectedColumn> hiddenColumns = new ArrayList<>();
    private List<IntrospectedColumn> displayColumns = new ArrayList<>();

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }


    @Override
    public boolean voModelRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        HtmlGeneratorConfiguration htmlGeneratorConfiguration = getHtmlGeneratorConfiguration(introspectedTable);
        if (htmlGeneratorConfiguration != null) {
            VueFormMetaDesc vueFormMetaDesc = new VueFormMetaDesc(introspectedTable);
            HtmlLayoutDescriptor layoutDescriptor = htmlGeneratorConfiguration.getLayoutDescriptor();
            vueFormMetaDesc.setLabelWidth(layoutDescriptor.getLabelWidth());
            vueFormMetaDesc.setLabelPosition(layoutDescriptor.getLabelPosition());
            vueFormMetaDesc.setSize(layoutDescriptor.getSize());
            vueFormMetaDesc.setPopSize(layoutDescriptor.getPopSize());
            vueFormMetaDesc.setAttachmentsContainer(layoutDescriptor.getAttachmentsContainer());
            vueFormMetaDesc.setInnerListContainer(layoutDescriptor.getInnerListContainer());
            vueFormMetaDesc.setRestBasePath(Mb3GenUtil.getControllerBaseMappingPath(introspectedTable));

            //附件注解
            if (!htmlGeneratorConfiguration.getHtmlFileAttachmentConfiguration().isEmpty()) {
                htmlGeneratorConfiguration.getHtmlFileAttachmentConfiguration().forEach(e -> {
                    vueFormMetaDesc.getUploadMeta().add(new VueFormUploadMetaDesc(e));
                });
            }

            //内置列表注解
            if (introspectedTable.getRules().isAdditionInnerList(htmlGeneratorConfiguration)) {
                List<HtmlElementInnerListConfiguration> listConfigurations = htmlGeneratorConfiguration.getHtmlElementInnerListConfiguration();
                for (int i = 0; i <listConfigurations.size(); i++) {
                    vueFormMetaDesc.getInnerListMeta().add(new VueFormInnerListMetaDesc(listConfigurations.get(i),introspectedTable,i+1));
                }
            }

            vueFormMetaDesc.addAnnotationToTopLevelClass(topLevelClass);
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
            //获取显示的字段
            for (IntrospectedColumn baseColumn : introspectedTable.getNonBLOBColumns()) {
                if (introspectedTable.getRules().isGenerateVoModel()) {
                    if (isIgnore(baseColumn, introspectedTable.getTableConfiguration().getVoGeneratorConfiguration().getVoModelConfiguration())
                            && !baseColumn.isPrimaryKey()
                            && !baseColumn.getActualColumnName().equalsIgnoreCase("version_")) {
                        continue;
                    }
                }
                if (GenerateUtils.isHiddenColumn(introspectedTable, baseColumn, htmlGeneratorConfiguration)) {
                    hiddenColumns.add(baseColumn);
                } else {
                    displayColumns.add(baseColumn);
                }
            }

            boolean match = displayColumns.stream().anyMatch(e -> e.getActualColumnName().equals(introspectedColumn.getActualColumnName()));
            if (!match) {
                return;
            }
            //获取html字段配置
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
            //设置placeholder
            vueFormItemMetaDesc.setPlaceholder(VueFormGenerateUtil.getDefaultPlaceholder(elementDescriptor, introspectedColumn));
            //设置component
            VueFormGenerateUtil.setComponentName(vueFormItemMetaDesc,elementDescriptor, introspectedColumn);
            //设置日期时间属性
            VueFormGenerateUtil.setDateTimeTypFormat(vueFormItemMetaDesc, elementDescriptor, introspectedColumn);
            if (elementDescriptor != null) { //存在字段配置的内容
                //设置multiple
                vueFormItemMetaDesc.setMultiple(Boolean.valueOf(elementDescriptor.getMultiple()));
                vueFormItemMetaDesc.setOtherFieldName(elementDescriptor.getOtherFieldName());
                //设置dataSource
                if (VStringUtil.stringHasValue(elementDescriptor.getDataSource())) {
                    vueFormItemMetaDesc.setDataSource(elementDescriptor.getDataSource());
                }
                //设置beanName
                if (VStringUtil.stringHasValue(elementDescriptor.getBeanName())) {
                    vueFormItemMetaDesc.setBeanName(elementDescriptor.getBeanName());
                }
                if (VStringUtil.stringHasValue(elementDescriptor.getApplyProperty())) {
                    vueFormItemMetaDesc.setApplyProperty(elementDescriptor.getApplyProperty());
                }
                if (VStringUtil.stringHasValue(elementDescriptor.getEnumClassName())) {
                    vueFormItemMetaDesc.setEnumClassFullName(elementDescriptor.getEnumClassName());
                }
                if (VStringUtil.stringHasValue(elementDescriptor.getDictCode())) {
                    vueFormItemMetaDesc.setDictCode(elementDescriptor.getDictCode());
                }
                if (VStringUtil.stringHasValue(elementDescriptor.getCallback())) {
                    vueFormItemMetaDesc.setCallback(elementDescriptor.getCallback());
                }
                if (VStringUtil.stringHasValue(elementDescriptor.getLabelCss())) {
                    vueFormItemMetaDesc.setLabelCss(elementDescriptor.getLabelCss());
                }
                if (VStringUtil.stringHasValue(elementDescriptor.getElementCss())) {
                    vueFormItemMetaDesc.setElementCss(elementDescriptor.getElementCss());
                }
                if (elementDescriptor.isDateRange()) {
                    vueFormItemMetaDesc.setDateRange(true);
                }
                if (VStringUtil.stringHasValue(elementDescriptor.getSwitchText())) {
                    vueFormItemMetaDesc.setSwitchText(elementDescriptor.getSwitchText());
                }
                if (VStringUtil.stringHasValue(elementDescriptor.getDataUrl())) {
                    vueFormItemMetaDesc.setDataUrl(elementDescriptor.getDataUrl());
                }
                if (VStringUtil.stringHasValue(elementDescriptor.getKeyMapValue())) {
                    vueFormItemMetaDesc.setKeyMapValue(elementDescriptor.getKeyMapValue());
                }
                if (VStringUtil.stringHasValue(elementDescriptor.getKeyMapLabel())) {
                    vueFormItemMetaDesc.setKeyMapLabel(elementDescriptor.getKeyMapLabel());
                }
                if (elementDescriptor.isRemoteApiParse()) {
                    vueFormItemMetaDesc.setRemoteApiParse(true);
                }
                if (elementDescriptor.isRemoteToTree()) {
                    vueFormItemMetaDesc.setRemoteToTree(true);
                }
                if (elementDescriptor.isRemoteAsync()) {
                    vueFormItemMetaDesc.setRemoteAsync(true);
                }
                if (elementDescriptor.isExcludeSelf()) {
                    vueFormItemMetaDesc.setExcludeSelf(true);
                }
                //根据字段类型设置valueType
                VueFormGenerateUtil.setRemoteValueType(vueFormItemMetaDesc, introspectedColumn, elementDescriptor);
            }else{
                vueFormItemMetaDesc.setOtherFieldName(introspectedColumn.getJavaProperty());
            }
            //设置rules
            String rules = VueFormGenerateUtil.getRules(vueFormItemMetaDesc, introspectedColumn, elementDescriptor);
            if (stringHasValue(rules)) {
                vueFormItemMetaDesc.addImports("com.vgosoft.core.annotation.VueFormItemRule");
                vueFormItemMetaDesc.setRules(rules);
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

    private boolean isIgnore(IntrospectedColumn introspectedColumn, VOModelGeneratorConfiguration configuration) {
        List<String> allFields = new ArrayList<>(EntityAbstractParentEnum.ABSTRACT_PERSISTENCE_LOCK_ENTITY.fields());
        allFields.add("tenantId");
        String property = configuration.getProperty(PropertyRegistry.ELEMENT_IGNORE_COLUMNS);
        boolean ret = false;
        if (stringHasValue(property)) {
            ret = VArrayUtil.contains(property.split(","), introspectedColumn.getActualColumnName());
        }
        return ret || allFields.contains(introspectedColumn.getJavaProperty());
    }

}
