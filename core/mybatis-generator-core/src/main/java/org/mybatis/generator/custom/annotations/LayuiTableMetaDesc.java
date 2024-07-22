package org.mybatis.generator.custom.annotations;

import com.vgosoft.core.annotation.CompositeQuery;
import com.vgosoft.core.annotation.LayuiTableMeta;
import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.custom.ViewVoUiFrameEnum;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.vgosoft.tool.core.VStringUtil.stringHasValue;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-06-02 19:50
 * @version 4.0
 */
public class LayuiTableMetaDesc extends AbstractAnnotation {

    public static final String ANNOTATION_NAME = "@" + LayuiTableMeta.class.getSimpleName();
    /**
     * listKey 为 table 的 key 值
     */
    private String value;
    private String title;
    private String size;
    private String indexColumn;
    private List<String> actionColumn = new ArrayList<>();
    /**
     * 设置表格尾部工具栏区域固定位置.可选值有：left 固定在左 right 固定在右 "false"或"" 不固定
     */
    private String actionColumnFixed = "";
    /**
     * 设置表格的索引列固定位置.可选值有：left 固定在左 right 固定在右 "false"或"" 不固定
     */
    private String indexColumnFixed = "";
    private List<String> toolbar = new ArrayList<>();
    private Set<String> batchUpdateColumns = new HashSet<>();
    private List<String> defaultToolbar = new ArrayList<>();
    private String parentMenuId;
    private String viewMenuElIcon;
    private String categoryTreeUrl;
    private boolean categoryTreeMultiple;
    private ViewVoUiFrameEnum uiFrameType;
    private String width;
    private String height;
    private boolean even;
    private String[] querys = new String[0];
    private String[] filters = new String[0];
    private String tableType = "default";
    private boolean totalRow;
    private Set<String> totalFields = new HashSet<>();
    private String totalText;

    private boolean enablePager = true;

    private String vxeListButtons = "";
    private String defaultFilterExpr;

    private boolean showRowNumber = true;
    private String editFormIn;
    private String detailFormIn;

    public LayuiTableMetaDesc() {
        super();
        this.addImports(LayuiTableMeta.class.getCanonicalName());
    }

    @Override
    public String toAnnotation() {
        items.clear();
        if (stringHasValue(value)) {
            items.add("value = \"" + value + "\"");
        }
        if (stringHasValue(title)) {
            items.add("title = \"" + title + "\"");
        }
        if (stringHasValue(width) && !"0".equals(width) && !"0px".equals(width) && !"0%".equals(width)) {
            items.add("width = " + width);
        }
        if (stringHasValue(height)) {
            items.add("height = \"" + height + "\"");
        }
        if (!batchUpdateColumns.isEmpty()) {
            items.add("batchUpdateColumns = \"" + String.join(",", batchUpdateColumns) + "\"");
        }
        if (stringHasValue(size) && !"md".equals(size)) {
            items.add("size = \"" + size + "\"");
        }
        if (!even) {
            items.add("even = false");
        }
        if (defaultToolbar.contains("NONE") && toolbar.contains("NONE")) {
            items.add("toolbar = \"false\"");
        } else {
            if (!defaultToolbar.isEmpty() && defaultToolbar.size() != 3) {
                items.add("defaultToolbar = \"" + String.join(",", defaultToolbar) + "\"");
            }
            if (!toolbar.isEmpty()) {
                items.add("toolbar = \"" + String.join(",", toolbar) + "\"");
            }
        }
        if (!actionColumn.isEmpty()) {
            items.add("actionColumn = \"" + String.join(",", actionColumn) + "\"");
        }
        if (stringHasValue(indexColumn) && !"CHECKBOX".equals(indexColumn)) {
            items.add("indexColumn = \"" + indexColumn + "\"");
        }
        if (this.querys.length > 0) {
            items.add(VStringUtil.format("querys = '{'{0}'}'", String.join("\n        , ", this.querys)));
            this.addImports(CompositeQuery.class.getCanonicalName());
        }
        if (this.filters.length > 0) {
            items.add(VStringUtil.format("filters = '{'{0}'}'", String.join("\n        , ", this.filters)));
            this.addImports(CompositeQuery.class.getCanonicalName());
        }
        if (stringHasValue(actionColumnFixed) && !"false".equalsIgnoreCase(actionColumnFixed)) {
            items.add("actionColumnFixed = \"" + actionColumnFixed + "\"");
        }
        if (stringHasValue(indexColumnFixed) && !"false".equalsIgnoreCase(indexColumnFixed)) {
            items.add("indexColumnFixed = \"" + indexColumnFixed + "\"");
        }
        if(parentMenuId != null && !parentMenuId.isEmpty()){
            items.add("parentMenuId = \"" + parentMenuId + "\"");
        }
        if(viewMenuElIcon != null && !viewMenuElIcon.isEmpty()){
            items.add("viewMenuElIcon = \"" + viewMenuElIcon + "\"");
        }
        if(categoryTreeUrl != null && !categoryTreeUrl.isEmpty()){
            items.add("categoryTreeUrl = \"" + categoryTreeUrl + "\"");
        }
        if(categoryTreeMultiple){
            items.add("categoryTreeMultiple = true");
        }
        if(uiFrameType != null){
            items.add("uiFrameType = \"" + uiFrameType.getCode() + "\"");
        }
        if (stringHasValue(tableType) && !"default".equalsIgnoreCase(tableType)) {
            items.add("tableType = \"" + tableType + "\"");
        }
        if (totalRow) {
            items.add("totalRow = true");
        }
        if (!totalFields.isEmpty()) {
            items.add("totalFields = \"" + String.join(",", totalFields) + "\"");
        }
        if (stringHasValue(totalText) && !"合计".equals(totalText)) {
            items.add("totalText = \"" + totalText + "\"");
        }
        if (stringHasValue(defaultFilterExpr)) {
            items.add("defaultFilterExpr = \"" + defaultFilterExpr + "\"");
        }
        if (stringHasValue(vxeListButtons) && !"innerAddBtn".equals(vxeListButtons)) {
            items.add("vxeListButtons = \"" + vxeListButtons + "\"");
        }
        if (!enablePager) {
            items.add("enablePager = false");
        }
        if (!showRowNumber) {
            items.add("showRowNumber = false");
        }
        if (stringHasValue(editFormIn) && !"dialog".equalsIgnoreCase(editFormIn)) {
            items.add("editFormIn = \"" + editFormIn + "\"");
        }
        if (stringHasValue(detailFormIn) && !"drawer".equalsIgnoreCase(detailFormIn)) {
            items.add("detailFormIn = \"" + detailFormIn + "\"");
        }
        return ANNOTATION_NAME + "(" + String.join(", ", items.toArray(new String[0])) + ")";
    }

    public List<String> getDefaultToolbar() {
        return defaultToolbar;
    }

    public void setDefaultToolbar(List<String> defaultToolbar) {
        this.defaultToolbar = defaultToolbar;
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public boolean isTotalRow() {
        return totalRow;
    }

    public void setTotalRow(boolean totalRow) {
        this.totalRow = totalRow;
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

    public boolean isEven() {
        return even;
    }

    public void setEven(boolean even) {
        this.even = even;
    }

    public List<String> getToolbar() {
        return toolbar;
    }

    public void setToolbar(List<String> toolbar) {
        this.toolbar = toolbar;
    }

    public List<String> getActionColumn() {
        return actionColumn;
    }

    public void setActionColumn(List<String> actionColumn) {
        this.actionColumn = actionColumn;
    }

    public String getIndexColumn() {
        return indexColumn;
    }

    public void setIndexColumn(String indexColumn) {
        this.indexColumn = indexColumn;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String[] getQuerys() {
        return querys;
    }

    public void setQuerys(String[] querys) {
        this.querys = querys;
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

    public String getTableType() {
        return tableType;
    }

    public void setTableType(String tableType) {
        this.tableType = tableType;
    }

    public Set<String> getTotalFields() {
        return totalFields;
    }

    public void setTotalFields(Set<String> totalFields) {
        this.totalFields = totalFields;
    }

    public Set<String> getBatchUpdateColumns() {
        return batchUpdateColumns;
    }

    public void setBatchUpdateColumns(Set<String> batchUpdateColumns) {
        this.batchUpdateColumns = batchUpdateColumns;
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

    public String getTotalText() {
        return totalText;
    }

    public void setTotalText(String totalText) {
        this.totalText = totalText;
    }

    public String[] getFilters() {
        return filters;
    }

    public void setFilters(String[] filters) {
        this.filters = filters;
    }

    public String getDefaultFilterExpr() {
        return defaultFilterExpr;
    }

    public void setDefaultFilterExpr(String defaultFilterExpr) {
        this.defaultFilterExpr = defaultFilterExpr;
    }

    public String getVxeListButtons() {
        return vxeListButtons;
    }

    public void setVxeListButtons(String vxeListButtons) {
        this.vxeListButtons = vxeListButtons;
    }

    public boolean isEnablePager() {
        return enablePager;
    }

    public void setEnablePager(boolean enablePager) {
        this.enablePager = enablePager;
    }

    public boolean isShowRowNumber() {
        return showRowNumber;
    }

    public void setShowRowNumber(boolean showRowNumber) {
        this.showRowNumber = showRowNumber;
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
}
