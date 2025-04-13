package org.mybatis.generator.custom.annotations;

import com.vgosoft.core.annotation.HtmlButton;
import com.vgosoft.core.annotation.VueFormInnerListMeta;
import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.config.HtmlElementInnerListConfiguration;
import org.mybatis.generator.internal.util.Mb3GenUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
     * 列表分组的key
     */
    private String listKey;
    /**
     * 列表标题（库表注释）
     */
    private String label = "";

    private boolean showTitle = true;

    private String moduleKeyword;
    /**
     * 列表所在的bean名
     */
    private String sourceBeanName;
    /**
     * 关联字段
     */
    private String relationField;
    /**
     * 列表所在的view类名
     */
    private String sourceListViewClass;
    /**
     * 关联key
     */
    private String relationKey = "id";
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
    private int span = 24;
    /**
     * 列表所在列后
     */
    private String afterColumn;
    private String containerType;
    private int order = 10;
    private String editMode = "row";
    private String editableFields;
    private boolean enablePager;
    private String defaultFilterExpr;
    private List<String> batchUpdateColumns = new ArrayList<>();
    private boolean showRowNumber = true;
    private boolean totalRow = false;
    private Set<String> totalFields = new HashSet<>();
    private String totalText = "合计";

    private String editFormIn;
    private String detailFormIn;
    private String hideExpression;
    private String disabledExpression;
    /**
     * restful请求中的根路径
     */
    private String restBasePath;

    private String enableEdit = "default";

    private String verify = "none";

    private String toolbarActions;
    private String columnActions;

    private String actionColumnWidth;

    public VueFormInnerListMetaDesc() {
        super();
        this.addImports(VueFormInnerListMeta.class.getCanonicalName());
    }

    public VueFormInnerListMetaDesc(HtmlElementInnerListConfiguration innerListConfiguration, IntrospectedTable introspectedTable, int index) {
        super();
        if (VStringUtil.stringHasValue(innerListConfiguration.getElementKey())) {
            this.value = innerListConfiguration.getElementKey();
        } else {
            this.value = innerListConfiguration.getModuleKeyword() +
                    "_" + Mb3GenUtil.getDefaultHtmlKey(introspectedTable) +
                    "_" + index;
        }
        this.listKey = innerListConfiguration.getListKey();
        if (VStringUtil.stringHasValue(innerListConfiguration.getLabel())) {
            this.label = innerListConfiguration.getLabel();
        } else {
            this.label = introspectedTable.getRemarks(true);
        }
        this.showTitle = innerListConfiguration.isShowTitle();
        this.moduleKeyword = innerListConfiguration.getModuleKeyword();
        this.sourceBeanName = innerListConfiguration.getSourceBeanName();
        this.relationField = innerListConfiguration.getRelationField();
        this.sourceListViewClass = innerListConfiguration.getSourceListViewClass();
        this.relationKey = innerListConfiguration.getRelationKey();
        this.tagId = innerListConfiguration.getTagId();
        this.dataField = innerListConfiguration.getDataField();
        this.dataUrl = innerListConfiguration.getDataUrl();
        this.span = innerListConfiguration.getSpan();
        this.afterColumn = innerListConfiguration.getAfterColumn();
        this.containerType = innerListConfiguration.getContainerType();
        this.order = innerListConfiguration.getOrder();
        this.editMode = innerListConfiguration.getEditMode();
        this.editableFields = String.join(",", innerListConfiguration.getEditableFields());
        this.enablePager = innerListConfiguration.isEnablePager();
        this.defaultFilterExpr = innerListConfiguration.getDefaultFilterExpr();
        this.batchUpdateColumns = new ArrayList<>(innerListConfiguration.getBatchUpdateColumns());
        this.showRowNumber = innerListConfiguration.isShowRowNumber();
        this.totalRow = innerListConfiguration.isTotalRow();
        this.totalFields = new HashSet<>(innerListConfiguration.getTotalFields());
        this.totalText = innerListConfiguration.getTotalText();
        this.restBasePath = innerListConfiguration.getRestBasePath();
        this.editFormIn = innerListConfiguration.getEditFormIn();
        this.detailFormIn = innerListConfiguration.getDetailFormIn();
        this.hideExpression = innerListConfiguration.getHideExpression();
        this.disabledExpression = innerListConfiguration.getDisabledExpression();
        this.enableEdit = innerListConfiguration.getEnableEdit();
        this.verify = innerListConfiguration.getVerify().stream().distinct().collect(Collectors.joining(","));
        this.addImports(VueFormInnerListMeta.class.getCanonicalName());
    }

    @Override
    public String toAnnotation() {
        List<String> items = new ArrayList<>();
        items.add("value = \"" + value + "\"");
        if (VStringUtil.isNotBlank(listKey)) {
            items.add("listKey = \"" + listKey + "\"");
        }
        if (VStringUtil.isNotBlank(label)) {
            items.add("label = \"" + label + "\"");
        }
        if (!showTitle) {
            items.add("showTitle = false");
        }
        if (VStringUtil.stringHasValue(moduleKeyword)) {
            items.add("moduleKeyword = \"" + moduleKeyword + "\"");
        }
        if (VStringUtil.isNotBlank(sourceBeanName)) {
            items.add("sourceBeanName = \"" + sourceBeanName + "\"");
        }
        if (VStringUtil.isNotBlank(relationField)) {
            items.add("relationField = \"" + relationField + "\"");
        }
        if (VStringUtil.isNotBlank(sourceListViewClass)) {
            items.add("sourceListViewClass = \"" + sourceListViewClass + "\"");
        }
        if (VStringUtil.isNotBlank(relationKey) && !"id".equals(relationKey)) {
            items.add("relationKey = \"" + relationKey + "\"");
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
        if (span != 24 && span != 0) {
            items.add("span = " + span);
        }
        if (VStringUtil.isNotBlank(afterColumn)) {
            items.add("afterColumn = \"" + afterColumn + "\"");
        }
        if (VStringUtil.isNotBlank(containerType)) {
            items.add("containerType = \"" + containerType + "\"");
        }
        if (order != 10) {
            items.add("order = " + order);
        }
        if (VStringUtil.stringHasValue(editMode) && !"row".equals(editMode)) {
            items.add("editMode = \"" + editMode + "\"");
        }
        if (VStringUtil.stringHasValue(editableFields)) {
            items.add("\n                        editableFields = \"" + editableFields + "\"");
        }
        if (enablePager) {
            items.add("enablePager = true");
        }
        if (this.toolbarActions != null) {
            items.add(VStringUtil.format("\n                        toolbarActions = '{'{0}'}'", this.toolbarActions));
            this.addImports(HtmlButton.class.getCanonicalName());
        }
        if (this.columnActions != null) {
            items.add(VStringUtil.format("\n                        columnActions = '{'{0}'}'", this.columnActions));
            this.addImports(HtmlButton.class.getCanonicalName());
        }
        if (VStringUtil.stringHasValue(defaultFilterExpr)) {
            items.add("defaultFilterExpr = \"" + defaultFilterExpr + "\"");
        }
        if (!batchUpdateColumns.isEmpty()) {
            items.add("batchUpdateColumns  = \"" + String.join(",", this.batchUpdateColumns) + "\"");
        }
        if (!showRowNumber) {
            items.add("showRowNumber = false");
        }
        if (totalRow) {
            items.add("\n                        totalRow = true");
        }
        if (!totalFields.isEmpty()) {
            items.add("totalFields = \"" + String.join(",", this.totalFields) + "\"");
        }
        if (VStringUtil.stringHasValue(totalText) && !"合计".equals(totalText)) {
            items.add("totalText = \"" + totalText + "\"");
        }
        if (VStringUtil.isNotBlank(restBasePath)) {
            items.add("restBasePath = \"" + restBasePath + "\"");
        }
        if (VStringUtil.isNotBlank(editFormIn) && !"dialog".equals(editFormIn)) {
            items.add("editFormIn = \"" + editFormIn + "\"");
        }
        if (VStringUtil.isNotBlank(detailFormIn) && !"drawer".equals(detailFormIn)) {
            items.add("detailFormIn = \"" + detailFormIn + "\"");
        }
        if (VStringUtil.isNotBlank(hideExpression)) {
            items.add("hideExpression = \"" + hideExpression + "\"");
        }
        if (VStringUtil.isNotBlank(disabledExpression)) {
            items.add("disabledExpression = \"" + disabledExpression + "\"");
        }
        if (VStringUtil.isNotBlank(enableEdit) && !"default".equals(enableEdit)) {
            items.add("enableEdit = \"" + enableEdit + "\"");
        }
        if (VStringUtil.isNotBlank(verify) && !verify.contains("none")) {
            items.add("verify = \"" + verify + "\"");
        }
        if (VStringUtil.isNotBlank(actionColumnWidth)) {
            items.add("actionColumnWidth = \"" + actionColumnWidth + "\"");
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

    public String getEditableFields() {
        return editableFields;
    }

    public void setEditableFields(String editableFields) {
        this.editableFields = editableFields;
    }

    public String getEditMode() {
        return editMode;
    }

    public void setEditMode(String editMode) {
        this.editMode = editMode;
    }

    public boolean isEnablePager() {
        return enablePager;
    }

    public void setEnablePager(boolean enablePager) {
        this.enablePager = enablePager;
    }

    public String getDefaultFilterExpr() {
        return defaultFilterExpr;
    }

    public void setDefaultFilterExpr(String defaultFilterExpr) {
        this.defaultFilterExpr = defaultFilterExpr;
    }

    public boolean isShowRowNumber() {
        return showRowNumber;
    }

    public void setShowRowNumber(boolean showRowNumber) {
        this.showRowNumber = showRowNumber;
    }

    public List<String> getBatchUpdateColumns() {
        return batchUpdateColumns;
    }

    public void setBatchUpdateColumns(List<String> batchUpdateColumns) {
        this.batchUpdateColumns = batchUpdateColumns;
    }

    public String getContainerType() {
        return containerType;
    }

    public void setContainerType(String containerType) {
        this.containerType = containerType;
    }

    public boolean isTotalRow() {
        return totalRow;
    }

    public void setTotalRow(boolean totalRow) {
        this.totalRow = totalRow;
    }

    public Set<String> getTotalFields() {
        return totalFields;
    }

    public void setTotalFields(Set<String> totalFields) {
        this.totalFields = totalFields;
    }

    public String getTotalText() {
        return totalText;
    }

    public void setTotalText(String totalText) {
        this.totalText = totalText;
    }

    public String getEditFormIn() {
        return editFormIn;
    }

    public void setEditFormIn(String editFormIn) {
        this.editFormIn = editFormIn;
    }

    public String getDetailFormIn() {
        return detailFormIn;
    }

    public void setDetailFormIn(String detailFormIn) {
        this.detailFormIn = detailFormIn;
    }

    public String getHideExpression() {
        return hideExpression;
    }

    public void setHideExpression(String hideExpression) {
        this.hideExpression = hideExpression;
    }

    public String getDisabledExpression() {
        return disabledExpression;
    }

    public void setDisabledExpression(String disabledExpression) {
        this.disabledExpression = disabledExpression;
    }

    public String getEnableEdit() {
        return enableEdit;
    }

    public void setEnableEdit(String enableEdit) {
        this.enableEdit = enableEdit;
    }

    public String getVerify() {
        return verify;
    }

    public void setVerify(String verify) {
        this.verify = verify;
    }

    public String getColumnActions() {
        return columnActions;
    }

    public void setColumnActions(String columnActions) {
        this.columnActions = columnActions;
    }

    public String getActionColumnWidth() {
        return actionColumnWidth;
    }

    public void setActionColumnWidth(String actionColumnWidth) {
        this.actionColumnWidth = actionColumnWidth;
    }

    public String getToolbarActions() {
        return toolbarActions;
    }

    public void setToolbarActions(String toolbarActions) {
        this.toolbarActions = toolbarActions;
    }

    public boolean isShowTitle() {
        return showTitle;
    }

    public void setShowTitle(boolean showTitle) {
        this.showTitle = showTitle;
    }
}
