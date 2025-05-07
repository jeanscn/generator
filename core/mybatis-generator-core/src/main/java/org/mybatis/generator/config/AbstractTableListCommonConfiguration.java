package org.mybatis.generator.config;

import org.mybatis.generator.custom.ViewVoUiFrameEnum;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AbstractTableListCommonConfiguration extends AbstractModelGeneratorConfiguration{

    protected String title;
    private boolean showTitle;
    protected String size;

    protected String listKey;
    protected String indexColumn;
    protected String indexColumnFixed;

    protected List<String> toolbar = new ArrayList<>();
    protected List<String> actionColumn = new ArrayList<>();

    protected String actionColumnWidth;
    protected String actionColumnFixed;

    protected List<String> queryColumns = new ArrayList<>();
    protected List<String> fuzzyColumns = new ArrayList<>();
    protected List<String> filterColumns = new ArrayList<>();
    protected List<String> defaultDisplayFields = new ArrayList<>();

    protected Set<String> defaultHiddenFields = new HashSet<>();
    protected List<String> defaultToolbar = new ArrayList<>();
    protected boolean enablePager = true;
    protected String parentMenuId;
    protected String viewMenuElIcon;
    protected String categoryTreeUrl;
    protected boolean categoryTreeMultiple;
    protected ViewVoUiFrameEnum uiFrameType;
    protected String tableType;
    protected boolean totalRow;
    protected Set<String> totalFields;
    protected String totalText = "合计";

    protected String defaultFilterExpr;

    protected String defaultSort;

    protected boolean showRowNumber = true;
    protected String showActionColumn = "default";
    protected String editFormIn;
    protected String detailFormIn;

    public AbstractTableListCommonConfiguration() {
        super();
    }
    public AbstractTableListCommonConfiguration(Context context) {
        super(context);
    }
    @Override
    void validate(List<String> errors, String contextId) {
        super.validate(errors, contextId, "AbstractTableListCommonConfiguration");
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

    public String getIndexColumn() {
        return indexColumn;
    }

    public void setIndexColumn(String indexColumn) {
        this.indexColumn = indexColumn;
    }

    public List<String> getActionColumn() {
        return actionColumn;
    }

    public void setActionColumn(List<String> actionColumn) {
        this.actionColumn = actionColumn;
    }

    public String getActionColumnFixed() {
        return actionColumnFixed;
    }

    public void setActionColumnFixed(String actionColumnFixed) {
        this.actionColumnFixed = actionColumnFixed;
    }

    public String getIndexColumnFixed() {
        return indexColumnFixed;
    }

    public void setIndexColumnFixed(String indexColumnFixed) {
        this.indexColumnFixed = indexColumnFixed;
    }

    public List<String> getToolbar() {
        return toolbar;
    }

    public void setToolbar(List<String> toolbar) {
        this.toolbar = toolbar;
    }

    public List<String> getQueryColumns() {
        return queryColumns;
    }

    public void setQueryColumns(List<String> queryColumns) {
        this.queryColumns = queryColumns;
    }

    public List<String> getFuzzyColumns() {
        return fuzzyColumns;
    }

    public void setFuzzyColumns(List<String> fuzzyColumns) {
        this.fuzzyColumns = fuzzyColumns;
    }

    public List<String> getFilterColumns() {
        return filterColumns;
    }

    public void setFilterColumns(List<String> filterColumns) {
        this.filterColumns = filterColumns;
    }

    public boolean isTotalRow() {
        return totalRow;
    }

    public void setTotalRow(boolean totalRow) {
        this.totalRow = totalRow;
    }

    public void setDefaultToolbar(List<String> defaultToolbar) {
        this.defaultToolbar = defaultToolbar;
    }
    public List<String> getDefaultToolbar() {
        return defaultToolbar;
    }
    public Set<String> getTotalFields() {
        return totalFields;
    }

    public void setTotalFields(Set<String> totalFields) {
        this.totalFields = totalFields;
    }

    public List<String> getDefaultDisplayFields() {
        return defaultDisplayFields;
    }

    public void setDefaultDisplayFields(List<String> defaultDisplayFields) {
        this.defaultDisplayFields = defaultDisplayFields;
    }

    public Set<String> getDefaultHiddenFields() {
        return defaultHiddenFields;
    }

    public void setDefaultHiddenFields(Set<String> defaultHiddenFields) {
        this.defaultHiddenFields = defaultHiddenFields;
    }
    public String getParentMenuId() {
        return parentMenuId;
    }

    public void setParentMenuId(String parentMenuId) {
        this.parentMenuId = parentMenuId;
    }

    public String getViewMenuElIcon() {
        return viewMenuElIcon;
    }

    public void setViewMenuElIcon(String viewMenuElIcon) {
        this.viewMenuElIcon = viewMenuElIcon;
    }

    public String getCategoryTreeUrl() {
        return categoryTreeUrl;
    }

    public void setCategoryTreeUrl(String categoryTreeUrl) {
        this.categoryTreeUrl = categoryTreeUrl;
    }

    public boolean isCategoryTreeMultiple() {
        return categoryTreeMultiple;
    }

    public void setCategoryTreeMultiple(boolean categoryTreeMultiple) {
        this.categoryTreeMultiple = categoryTreeMultiple;
    }

    public ViewVoUiFrameEnum getUiFrameType() {
        return uiFrameType;
    }

    public void setUiFrameType(ViewVoUiFrameEnum uiFrameType) {
        this.uiFrameType = uiFrameType;
    }

    public String getTableType() {
        return tableType;
    }

    public void setTableType(String tableType) {
        this.tableType = tableType;
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

    public boolean isEnablePager() {
        return enablePager;
    }

    public void setEnablePager(boolean enablePager) {
        this.enablePager = enablePager;
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
