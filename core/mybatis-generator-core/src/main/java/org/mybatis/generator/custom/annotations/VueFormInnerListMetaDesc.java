package org.mybatis.generator.custom.annotations;

import com.vgosoft.core.annotation.VueFormInnerListMeta;
import com.vgosoft.core.annotation.VueFormMeta;
import com.vgosoft.core.annotation.VueFormUploadMeta;
import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.config.HtmlElementInnerListConfiguration;
import org.mybatis.generator.internal.util.Mb3GenUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ElementPlus表单元数据描述注解
 *
 * @version 6.0
 */
public class VueFormInnerListMetaDesc extends AbstractAnnotation {

    public static final String ANNOTATION_NAME = "@" + VueFormInnerListMeta.class.getSimpleName();

    /**
     * 内置列表的id (moduleKeyword)_（sourceViewPath）(index)
     */
    private String value;
    /**
     * 列表标题（库表注释）
     */
    private String label = "";
    /**
     * 列表分组的key
     */
    private String listKey;
    /**
     * 列表所在的view类名
     */
    private String sourceListViewClass;
    /**
     * 列表所在的bean名
     */
    private String sourceBeanName;
    /**
     * 关联字段
     */
    private String relationField;
    /**
     * 关联key
     */
    private String relationKey = "id";
    /**
     * 列表所在列后
     */
    private String afterColumn;
    /**
     * 在vue中响应数据的属性名
     */
    private String tagId;
    /**
     * 数据字段
     */
    private String dataField;
    /**
     * 数据url
     */
    private String dataUrl;
    private String moduleKeyword;
    /**
     * restful请求中的根路径
     */
    private String restBasePath;
    private int span = 24;

    private int order = 10;

    public VueFormInnerListMetaDesc() {
        super();
        this.addImports(VueFormInnerListMeta.class.getCanonicalName());
    }

    public VueFormInnerListMetaDesc(HtmlElementInnerListConfiguration innerListConfiguration, IntrospectedTable introspectedTable, int index) {
        super();
        if(VStringUtil.stringHasValue(innerListConfiguration.getElementKey())){
            this.value = innerListConfiguration.getElementKey();
        }else{
            this.value =  innerListConfiguration.getModuleKeyword() +
                    "_" +  Mb3GenUtil.getDefaultHtmlKey(introspectedTable) +
                    "_" +  index;
        }
        this.label = innerListConfiguration.getLabel();
        this.listKey = innerListConfiguration.getListKey();
        this.sourceListViewClass = innerListConfiguration.getSourceViewVoClass();
        this.sourceBeanName = innerListConfiguration.getSourceBeanName();
        this.relationField = innerListConfiguration.getRelationField();
        this.relationKey = innerListConfiguration.getRelationKey();
        this.afterColumn = innerListConfiguration.getAfterColumn();
        this.tagId = innerListConfiguration.getTagId();
        this.dataField = innerListConfiguration.getDataField();
        this.dataUrl = innerListConfiguration.getDataUrl();
        this.span = innerListConfiguration.getSpan();
        this.moduleKeyword = innerListConfiguration.getModuleKeyword();
        this.restBasePath = innerListConfiguration.getRestBasePath();
        this.order = innerListConfiguration.getOrder();
        this.addImports(VueFormInnerListMeta.class.getCanonicalName());
    }

    @Override
    public String toAnnotation() {
        List<String> items = new ArrayList<>();
        items.add("value = \"" + value + "\"");
        if (VStringUtil.isNotBlank(label) ) {
            items.add("label = \"" + label + "\"");
        }
        if (VStringUtil.isNotBlank(listKey)) {
            items.add("listKey = \"" + listKey + "\"");
        }
        if (VStringUtil.isNotBlank(sourceListViewClass)) {
            items.add("sourceListViewClass = \"" + sourceListViewClass + "\"");
        }
        if (VStringUtil.isNotBlank(sourceBeanName)) {
            items.add("sourceBeanName = \"" + sourceBeanName + "\"");
        }
        if (VStringUtil.isNotBlank(relationField)) {
            items.add("relationField = \"" + relationField + "\"");
        }
        if (VStringUtil.isNotBlank(relationKey) && !"id".equals(relationKey)) {
            items.add("relationKey = \"" + relationKey + "\"");
        }
        if (VStringUtil.isNotBlank(afterColumn)) {
            items.add("afterColumn = \"" + afterColumn + "\"");
        }
        if (VStringUtil.isNotBlank(tagId)) {
            items.add("tagId = \"" + tagId + "\"");
        }
        if (VStringUtil.isNotBlank(dataField)) {
            items.add("dataField = \"" + dataField + "\"");
        }
        if (VStringUtil.isNotBlank(dataUrl)) {
            items.add("dataUrl = \"" + dataUrl + "\"");
        }
        if (VStringUtil.isNotBlank(restBasePath)) {
            items.add("restBasePath = \"" + restBasePath + "\"");
        }
        if(VStringUtil.stringHasValue(moduleKeyword)){
            items.add("moduleKeyword = \"" + moduleKeyword + "\"");
        }
        if (span != 24 && span != 0) {
            items.add("span = " + span);
        }
        if (order != 10) {
            items.add("order = " + order);
        }
        return ANNOTATION_NAME + "(" + String.join(", ", items.toArray(new String[0])) + ")";
    }

    public String getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getListKey() {
        return listKey;
    }

    public void setListKey(String listKey) {
        this.listKey = listKey;
    }

    public String getSourceListViewClass() {
        return sourceListViewClass;
    }

    public void setSourceListViewClass(String sourceListViewClass) {
        this.sourceListViewClass = sourceListViewClass;
    }

    public String getSourceBeanName() {
        return sourceBeanName;
    }

    public void setSourceBeanName(String sourceBeanName) {
        this.sourceBeanName = sourceBeanName;
    }

    public String getRelationField() {
        return relationField;
    }

    public void setRelationField(String relationField) {
        this.relationField = relationField;
    }

    public String getRelationKey() {
        return relationKey;
    }

    public void setRelationKey(String relationKey) {
        this.relationKey = relationKey;
    }

    public String getAfterColumn() {
        return afterColumn;
    }

    public void setAfterColumn(String afterColumn) {
        this.afterColumn = afterColumn;
    }

    public String getTagId() {
        return tagId;
    }

    public void setTagId(String tagId) {
        this.tagId = tagId;
    }

    public String getDataField() {
        return dataField;
    }

    public void setDataField(String dataField) {
        this.dataField = dataField;
    }

    public String getDataUrl() {
        return dataUrl;
    }

    public void setDataUrl(String dataUrl) {
        this.dataUrl = dataUrl;
    }

    public String getRestBasePath() {
        return restBasePath;
    }

    public void setRestBasePath(String restBasePath) {
        this.restBasePath = restBasePath;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getSpan() {
        return span;
    }

    public void setSpan(int span) {
        this.span = span;
    }

    public String getModuleKeyword() {
        return moduleKeyword;
    }

    public void setModuleKeyword(String moduleKeyword) {
        this.moduleKeyword = moduleKeyword;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}
