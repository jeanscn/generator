package org.mybatis.generator.custom.annotations;

import com.vgosoft.core.annotation.CompositeQuery;
import com.vgosoft.core.annotation.HtmlButton;
import com.vgosoft.core.annotation.ViewColumnMeta;
import com.vgosoft.core.annotation.ViewTableMeta;
import com.vgosoft.core.constant.GlobalConstant;
import com.vgosoft.core.constant.enums.view.ViewIndexColumnEnum;
import com.vgosoft.tool.core.VMD5Util;
import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.codegen.mybatis3.htmlmapper.GenerateUtils;
import org.mybatis.generator.internal.util.Mb3GenUtil;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static com.vgosoft.tool.core.VStringUtil.stringHasValue;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-10-07 04:48
 * @version 3.0
 */
public class ViewTableMetaDesc extends AbstractAnnotation {

    public static final String ANNOTATION_NAME = "@" + ViewTableMeta.class.getSimpleName();
    private final String value;
    private String listKey;
    private String title;
    private String size;
    private final String listName;
    private final String beanName;
    private final String appKeyword;
    private String dataUrl;
    private String createUrl;
    private String className;
    private String listType;
    private final String tableName;
    private final String tableAlias;
    private String[] toolbar = new String[0];
    private ViewIndexColumnEnum indexColumn;
    private String indexColWidth;
    private String actionColumn;
    private String toolbarActions;
    private String[] ignoreFields = new String[0];
    private String[] columns = new String[0];
    private String[] querys = new String[0];
    private String[] filters = new String[0];
    private String[] fuzzyColumns = new String[0];
    private String actionColWidth;
    private int dataFilterType = 0;
    private String categoryTreeUrl;
    private int wfStatus = 6;
    private String areaWidth;
    private String areaHeight;
    private String restBasePath;
    private String tableType = "default";
    private int applyWorkflow = 0;
    private String moduleId;
    private boolean totalRow = false;
    private Set<String> totalFields = new HashSet<>();
    private String totalText = "合计";
    private String defaultFilterExpr;
    private boolean showRowNumber = true;

    private String showActionColumn = "default";
    private String editFormIn;
    private String detailFormIn;

    public static ViewTableMetaDesc create(IntrospectedTable introspectedTable) {
        return new ViewTableMetaDesc(introspectedTable);
    }

    public ViewTableMetaDesc(IntrospectedTable introspectedTable) {
        super();
        this.value = Mb3GenUtil.getDefaultViewId(introspectedTable);
        items.add(VStringUtil.format("value = \"{0}\"", this.getValue()));
        this.listName = introspectedTable.getRemarks(true);
        items.add(VStringUtil.format("listName = \"{0}\"", this.getListName()));
        this.beanName = introspectedTable.getControllerBeanName();
        items.add(VStringUtil.format("beanName = \"{0}\"", this.getBeanName()));
        this.appKeyword = introspectedTable.getContext().getAppKeyword();
        items.add(VStringUtil.format("appKeyword = \"{0}\"", this.getAppKeyword()));
        this.tableName = introspectedTable.getTableConfiguration().getTableName();
        items.add(VStringUtil.format("tableName = \"{0}\"", this.tableName));
        this.tableAlias = introspectedTable.getTableConfiguration().getAlias();
        items.add(VStringUtil.format("tableAlias = \"{0}\"", this.tableAlias));
        this.dataUrl = "/viewmgr/getdtdata";
        this.listType = VMD5Util.MD5_15(Mb3GenUtil.getModelKey(introspectedTable));
        this.indexColumn = ViewIndexColumnEnum.CHECKBOX;
        this.moduleId = Mb3GenUtil.getModelId(introspectedTable);
        this.applyWorkflow = GenerateUtils.isWorkflowInstance(introspectedTable) ? 1 : 0;
        this.addImports(ViewTableMeta.class.getCanonicalName());
    }

    @Override
    public String toAnnotation() {
        if (VStringUtil.isNotBlank(this.getListKey())) {
            items.add(VStringUtil.format("listKey = \"{0}\"", this.getListKey()));
        }
        if (this.toolbar.length>0) {
            items.add(VStringUtil.format("toolbar = '{'{0}'}'", String.join(", ", this.toolbar)));
        }
        if (VStringUtil.isNotBlank(this.getTitle())) {
            items.add(VStringUtil.format("title = \"{0}\"", this.getTitle()));
        }
        if (VStringUtil.isNotBlank(this.getSize()) && !"md".equals(this.getSize())) {
            items.add(VStringUtil.format("size = \"{0}\"", this.getSize()));
        }
        if (!this.getDataUrl().equals("/viewmgr/getdtdata")) {
            items.add(VStringUtil.format("dataUrl = \"{0}\"", this.getDataUrl()));
        }
        if (VStringUtil.isNotBlank(this.getCreateUrl())) {
            items.add(VStringUtil.format("createUrl = \"{0}\"", this.getCreateUrl()));
        }
        if (VStringUtil.isNotBlank(this.getClassName())) {
            items.add(VStringUtil.format("className = \"{0}\"", this.getClassName()));
        }
        if (!this.getListType().equals(GlobalConstant.DEFAULT_VIEW_LIST_TYPE)) {
            items.add(VStringUtil.format("listType = \"{0}\"", this.getListType()));
        }
        if (!this.getIndexColumn().equals(ViewIndexColumnEnum.ROW_INDEX)) {
            items.add(VStringUtil.format("indexColumn = ViewIndexColumnEnum.{0}", this.getIndexColumn().name()));
            this.addImports(ViewIndexColumnEnum.class.getCanonicalName());
        }
        if (this.actionColumn != null) {
            items.add(VStringUtil.format("actionColumn = '{'{0}'}'", this.actionColumn));
            this.addImports(HtmlButton.class.getCanonicalName());
        }
        if (this.toolbarActions != null) {
            items.add(VStringUtil.format("toolbarActions = '{'{0}'}'", this.toolbarActions));
            this.addImports(HtmlButton.class.getCanonicalName());
        }
        if (this.ignoreFields.length > 0) {
            String collect = Arrays.stream(this.ignoreFields).map(f -> "\"" + f + "\"").collect(Collectors.joining(","));
            items.add(VStringUtil.format("ignoreFields = '{'{0}'}'", collect));
        }
        if (this.columns.length > 0) {
            items.add(VStringUtil.format("columns = '{'{0}'}'", String.join("\n        , ", this.columns)));
            this.addImports(ViewColumnMeta.class.getCanonicalName());
        }
        if (this.querys.length > 0) {
            items.add(VStringUtil.format("querys = '{'{0}'}'", String.join("\n        , ", this.querys)));
            this.addImports(CompositeQuery.class.getCanonicalName());
        }
        if (this.filters.length > 0) {
            items.add(VStringUtil.format("filters = '{'{0}'}'", String.join("\n        , ", this.filters)));
            this.addImports(CompositeQuery.class.getCanonicalName());
        }
        if (VStringUtil.isNotBlank(this.indexColWidth)) {
            items.add(VStringUtil.format("indexColWidth = \"{0}\"", this.getIndexColWidth()));
        }
        if (VStringUtil.isNotBlank(this.actionColWidth)) {
            items.add(VStringUtil.format("actionColWidth = \"{0}\"", this.getActionColWidth()));
        }
        if (this.dataFilterType != 0) {
            items.add(VStringUtil.format("dataFilterType = {0}", this.getDataFilterType()));
        }
        if (VStringUtil.isNotBlank(this.categoryTreeUrl)) {
            items.add(VStringUtil.format("categoryTreeUrl = \"{0}\"", this.getCategoryTreeUrl()));
        }
        if (this.wfStatus != 6) {
            items.add(VStringUtil.format("wfStatus = {0}", this.getWfStatus()));
        }
        if (VStringUtil.isNotBlank(this.areaWidth)) {
            items.add(VStringUtil.format("areaWidth = \"{0}\"", this.getAreaWidth()));
        }
        if (VStringUtil.isNotBlank(this.areaHeight)) {
            items.add(VStringUtil.format("areaHeight = \"{0}\"", this.getAreaHeight()));
        }
        if (VStringUtil.isNotBlank(this.restBasePath)) {
            items.add(VStringUtil.format("restBasePath = \"{0}\"", this.getRestBasePath()));
        }
        if (stringHasValue(tableType) && !"default".equalsIgnoreCase(tableType)) {
            items.add(VStringUtil.format("tableType = \"{0}\"", this.getTableType()));
        }
        if (applyWorkflow != 0) {
            items.add(VStringUtil.format("applyWorkflow = {0}", this.getApplyWorkflow()));
        }
        if (VStringUtil.isNotBlank(this.moduleId)) {
            items.add(VStringUtil.format("moduleId = \"{0}\"", this.getModuleId()));
        }
        if (this.totalRow) {
            items.add("totalRow = true");
        }
        if (!this.totalFields.isEmpty()) {
            items.add(VStringUtil.format("totalFields = \"{0}\"", String.join(", ", this.totalFields)));
        }
        if (VStringUtil.isNotBlank(this.totalText) && !"合计".equals(this.totalText)) {
            items.add(VStringUtil.format("totalText = \"{0}\"", this.getTotalText()));
        }
        if (VStringUtil.isNotBlank(this.defaultFilterExpr)) {
            items.add(VStringUtil.format("defaultFilterExpr = \"{0}\"", this.getDefaultFilterExpr()));
        }
        if (!this.showRowNumber) {
            items.add("showRowNumber = false");
        }
        if (VStringUtil.isNotBlank(this.showActionColumn) && !"default".equals(this.showActionColumn)) {
            items.add(VStringUtil.format("showActionColumn = \"{0}\"", this.getShowActionColumn()));
        }
        if (this.fuzzyColumns.length > 0) {
            items.add(VStringUtil.format("fuzzyColumns = '{'{0}'}'", String.join(", ", this.fuzzyColumns)));
        }
        if (VStringUtil.isNotBlank(this.editFormIn) && !"dialog".equals(this.editFormIn)) {
            items.add(VStringUtil.format("editFormIn = \"{0}\"", this.getEditFormIn()));
        }
        if (VStringUtil.isNotBlank(this.detailFormIn) && !"drawer".equals(this.detailFormIn)) {
            items.add(VStringUtil.format("detailFormIn = \"{0}\"", this.getDetailFormIn()));
        }
        return ANNOTATION_NAME + "(" + String.join("\n       ,", items.toArray(new String[0])) + ")";
    }

    public String getValue() {
        return value;
    }

    public String getListName() {
        return listName;
    }

    public String getBeanName() {
        return beanName;
    }

    public String getAppKeyword() {
        return appKeyword;
    }

    public String getDataUrl() {
        return dataUrl;
    }

    public void setDataUrl(String dataUrl) {
        this.dataUrl = dataUrl;
    }

    public String getCreateUrl() {
        return createUrl;
    }

    public void setCreateUrl(String createUrl) {
        this.createUrl = createUrl;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getListType() {
        return listType;
    }

    public void setListType(String listType) {
        this.listType = listType;
    }

    public ViewIndexColumnEnum getIndexColumn() {
        return indexColumn;
    }

    public void setIndexColumn(ViewIndexColumnEnum indexColumn) {
        this.indexColumn = indexColumn;
    }

    public String[] getIgnoreFields() {
        return ignoreFields;
    }

    public void setIgnoreFields(String[] ignoreFields) {
        this.ignoreFields = ignoreFields;
    }

    public String[] getColumns() {
        return columns;
    }

    public void setColumns(String[] columns) {
        this.columns = columns;
    }

    public String[] getQuerys() {
        return querys;
    }

    public void setQuerys(String[] querys) {
        this.querys = querys;
    }

    public String getIndexColWidth() {
        return indexColWidth;
    }

    public void setIndexColWidth(String indexColWidth) {
        this.indexColWidth = indexColWidth;
    }

    public String getActionColWidth() {
        return actionColWidth;
    }

    public void setActionColWidth(String actionColWidth) {
        this.actionColWidth = actionColWidth;
    }

    public int getDataFilterType() {
        return dataFilterType;
    }

    public void setDataFilterType(int dataFilterType) {
        this.dataFilterType = dataFilterType;
    }

    public String getCategoryTreeUrl() {
        return categoryTreeUrl;
    }

    public void setCategoryTreeUrl(String categoryTreeUrl) {
        this.categoryTreeUrl = categoryTreeUrl;
    }

    public int getWfStatus() {
        return wfStatus;
    }

    public void setWfStatus(int wfStatus) {
        this.wfStatus = wfStatus;
    }

    public String getAreaWidth() {
        return areaWidth;
    }

    public void setAreaWidth(String areaWidth) {
        this.areaWidth = areaWidth;
    }

    public String getAreaHeight() {
        return areaHeight;
    }

    public void setAreaHeight(String areaHeight) {
        this.areaHeight = areaHeight;
    }

    public String getRestBasePath() {
        return restBasePath;
    }

    public void setRestBasePath(String restBasePath) {
        this.restBasePath = restBasePath;
    }

    public String getActionColumn() {
        return actionColumn;
    }

    public void setActionColumn(String actionColumn) {
        this.actionColumn = actionColumn;
    }

    public String getToolbarActions() {
        return toolbarActions;
    }

    public void setToolbarActions(String toolbarActions) {
        this.toolbarActions = toolbarActions;
    }

    public String getTableName() {
        return tableName;
    }

    public String getTableAlias() {
        return tableAlias;
    }

    public String[] getFilters() {
        return filters;
    }

    public void setFilters(String[] filters) {
        this.filters = filters;
    }

    public String getTableType() {
        return tableType;
    }

    public void setTableType(String tableType) {
        this.tableType = tableType;
    }

    public int getApplyWorkflow() {
        return applyWorkflow;
    }

    public void setApplyWorkflow(int applyWorkflow) {
        this.applyWorkflow = applyWorkflow;
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
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

    public String getListKey() {
        return listKey;
    }

    public void setListKey(String listKey) {
        this.listKey = listKey;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String[] getToolbar() {
        return toolbar;
    }

    public void setToolbar(String[] toolbar) {
        this.toolbar = toolbar;
    }

    public String[] getFuzzyColumns() {
        return fuzzyColumns;
    }

    public void setFuzzyColumns(String[] fuzzyColumns) {
        this.fuzzyColumns = fuzzyColumns;
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
}
