package org.mybatis.generator.custom.annotations;

import com.vgosoft.core.annotation.CompositeQuery;
import com.vgosoft.core.annotation.HtmlButton;
import com.vgosoft.core.annotation.LayuiTableMeta;
import com.vgosoft.tool.core.VStringUtil;
import lombok.Getter;
import lombok.Setter;
import org.mybatis.generator.custom.enums.ViewVoUiFrameEnum;

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
@Getter
@Setter
public class LayuiTableMetaDesc extends AbstractAnnotation {

    public static final String ANNOTATION_NAME = "@" + LayuiTableMeta.class.getSimpleName();
    /**
     * listKey 为 table 的 key 值
     */
    private String value;
    private String title;
    private boolean showTitle = true;
    private String size;
    private String indexColumn;
    /**
     * 设置表格尾部工具栏区域固定位置.可选值有：left 固定在左 right 固定在右 "false"或"" 不固定
     */
    private String actionColumnFixed = "";
    /**
     * 设置表格的索引列固定位置.可选值有：left 固定在左 right 固定在右 "false"或"" 不固定
     */
    private String indexColumnFixed = "";
    private String actionColumnWidth;
    private List<String> toolbar = new ArrayList<>();
    private String toolbarActions;
    private String columnActions;
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

    private String enablePage = "true";

    private String defaultFilterExpr;
    private String defaultSort;
    private boolean showRowNumber = true;
    private String showActionColumn = "default";

    private String editFormIn;
    private String detailFormIn;
    private List<String> editableFields = new ArrayList<>();

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
        if (!showTitle) {
            items.add("showTitle = false");
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
        if (VStringUtil.stringHasValue(this.columnActions)) {
            items.add(VStringUtil.format("\n                        columnActions = '{'{0}'}'", this.columnActions));
            this.addImports(HtmlButton.class.getCanonicalName());
        }
        if (VStringUtil.stringHasValue(this.toolbarActions)) {
            items.add(VStringUtil.format("\n                        toolbarActions = '{'{0}'}'", this.toolbarActions));
            this.addImports(HtmlButton.class.getCanonicalName());
        }
        if (stringHasValue(indexColumn) && !"CHECKBOX".equals(indexColumn)) {
            items.add("indexColumn = \"" + indexColumn + "\"");
        }
        if (this.querys.length > 0) {
            items.add(VStringUtil.format("\n        querys = '{'{0}'}'", String.join("\n                , ", this.querys)));
            this.addImports(CompositeQuery.class.getCanonicalName());
        }
        if (this.filters.length > 0) {
            items.add(VStringUtil.format("\n        filters = '{'{0}'}'", String.join("\n                , ", this.filters)));
            this.addImports(CompositeQuery.class.getCanonicalName());
        }
        if (stringHasValue(actionColumnFixed) && !"false".equalsIgnoreCase(actionColumnFixed)) {
            items.add("actionColumnFixed = \"" + actionColumnFixed + "\"");
        }
        if (stringHasValue(actionColumnWidth)) {
            items.add("actionColumnWidth = \"" + actionColumnWidth + "\"");
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
        if (enablePage.equals("false")) {
           items.add("enablePage = \"false\"");
        }
        if (stringHasValue(defaultSort)) {
            items.add("defaultSort = \"" + defaultSort + "\"");
        }
        if (!showRowNumber) {
            items.add("showRowNumber = false");
        }
        if (stringHasValue(showActionColumn) && !"default".equalsIgnoreCase(showActionColumn)) {
            items.add("showActionColumn = \"" + showActionColumn + "\"");
        }
        if (stringHasValue(editFormIn) && !"dialog".equalsIgnoreCase(editFormIn)) {
            items.add("editFormIn = \"" + editFormIn + "\"");
        }
        if (stringHasValue(detailFormIn) && !"drawer".equalsIgnoreCase(detailFormIn)) {
            items.add("detailFormIn = \"" + detailFormIn + "\"");
        }
        if (!editableFields.isEmpty()) {
            items.add("editableFields = \"" + String.join(",", editableFields) + "\"");
        }
        return ANNOTATION_NAME + "(" + String.join(", ", items.toArray(new String[0])) + ")";
    }

}
