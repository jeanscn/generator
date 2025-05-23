package org.mybatis.generator.config;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-06-12 13:43
 * @version 4.0
 */
public class HtmlElementInnerListConfiguration extends AbstractHtmlElementDescriptor {

    private String elementKey;
    private String label = "";
    private boolean showTitle;
    private String moduleKeyword;
    private String sourceViewPath = "";
    private String sourceBeanName;
    private String sourceBeanNameKebabCase;
    private String relationField;
    private String sourceListViewClass;
    private String relationKey = "id";
    private String tagId;
    private String dataField;
    private int span = 24;
    private String afterColumn;
    private String containerType;
    private int order = 10;
    private String editMode = "row";
    private Set<String> editableFields = new HashSet<>();
    private Set<String> batchUpdateColumns = new HashSet<>();
    private boolean showRowNumber = true;
    private boolean totalRow;
    private Set<String> totalFields = new HashSet<>();
    private String totalText = "合计";
    private String restBasePath;
    private String editFormIn;
    private String detailFormIn;
    private String defaultSort;
    private String showActionColumn = "default";
    private String enableEdit = "default";
    private String printMode = "table";
    private int printFormColumnsNum = 2;
    private List<String> printFields = new ArrayList<>();
    private List<String> verify = new ArrayList<>();

    private String actionColumnWidth;

    public HtmlElementInnerListConfiguration() {
    }

    public String getElementKey() {
        return elementKey;
    }

    public void setElementKey(String elementKey) {
        this.elementKey = elementKey;
    }

    public String getDataField() {
        return dataField;
    }

    public void setDataField(String dataField) {
        this.dataField = dataField;
    }

    public String getTagId() {
        return tagId;
    }

    public void setTagId(String tagId) {
        this.tagId = tagId;
    }

    public String getSourceViewPath() {
        if (sourceViewPath.contains("/")) {
            sourceViewPath = sourceViewPath.substring(sourceViewPath.lastIndexOf("/") + 1);
        }
        return sourceViewPath;
    }

    public void setSourceViewPath(String sourceViewPath) {
        this.sourceViewPath = sourceViewPath;
    }

    public String getSourceBeanName() {
        return sourceBeanName;
    }

    public void setSourceBeanName(String sourceBeanName) {
        this.sourceBeanName = sourceBeanName;
    }

    public String getModuleKeyword() {
        return moduleKeyword;
    }

    public void setModuleKeyword(String moduleKeyword) {
        this.moduleKeyword = moduleKeyword;
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

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getSpan() {
        return span;
    }

    public void setSpan(int span) {
        this.span = span;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public Set<String> getEditableFields() {
        return editableFields;
    }

    public void setEditableFields(Set<String> editableFields) {
        this.editableFields = editableFields;
    }

    public String getEditMode() {
        return editMode;
    }

    public void setEditMode(String editMode) {
        this.editMode = editMode;
    }

    public boolean isShowRowNumber() {
        return showRowNumber;
    }

    public void setShowRowNumber(boolean showRowNumber) {
        this.showRowNumber = showRowNumber;
    }

    public Set<String> getBatchUpdateColumns() {
        return batchUpdateColumns;
    }

    public void setBatchUpdateColumns(Set<String> batchUpdateColumns) {
        this.batchUpdateColumns = batchUpdateColumns;
    }

    public String getContainerType() {
        return containerType;
    }

    public void setContainerType(String containerType) {
        this.containerType = containerType;
    }

    public String getSourceListViewClass() {
        return sourceListViewClass;
    }

    public void setSourceListViewClass(String sourceListViewClass) {
        this.sourceListViewClass = sourceListViewClass;
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

    public String getRestBasePath() {
        return restBasePath;
    }

    public void setRestBasePath(String restBasePath) {
        this.restBasePath = restBasePath;
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

    public String getShowActionColumn() {
        return showActionColumn;
    }

    public void setShowActionColumn(String showActionColumn) {
        this.showActionColumn = showActionColumn;
    }

    public String getEnableEdit() {
        return enableEdit;
    }

    public void setEnableEdit(String enableEdit) {
        this.enableEdit = enableEdit;
    }

    public String getSourceBeanNameKebabCase() {
        return sourceBeanNameKebabCase;
    }

    public void setSourceBeanNameKebabCase(String sourceBeanNameKebabCase) {
        this.sourceBeanNameKebabCase = sourceBeanNameKebabCase;
    }

    public String getPrintMode() {
        return printMode;
    }

    public void setPrintMode(String printMode) {
        this.printMode = printMode;
    }

    public int getPrintFormColumnsNum() {
        return printFormColumnsNum;
    }

    public void setPrintFormColumnsNum(int printFormColumnsNum) {
        this.printFormColumnsNum = printFormColumnsNum;
    }

    public List<String> getPrintFields() {
        return printFields.stream().distinct().collect(Collectors.toList());
    }

    public void addPrintField(String printField) {
        this.printFields.add(printField);
    }

    public void setPrintFields(List<String> printFields) {
        this.printFields = printFields;
    }

    public List<String> getVerify() {
        return verify;
    }

    public void setVerify(List<String> verify) {
        this.verify = verify;
    }

    public String getActionColumnWidth() {
        return actionColumnWidth;
    }

    public void setActionColumnWidth(String actionColumnWidth) {
        this.actionColumnWidth = actionColumnWidth;
    }

    public boolean isShowTitle() {
        return showTitle;
    }

    public void setShowTitle(boolean showTitle) {
        this.showTitle = showTitle;
    }

    public String getDefaultSort() {
        return defaultSort;
    }

    public void setDefaultSort(String defaultSort) {
        this.defaultSort = defaultSort;
    }
}
