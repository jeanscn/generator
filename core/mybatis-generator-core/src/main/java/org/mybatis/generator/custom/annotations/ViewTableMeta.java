package org.mybatis.generator.custom.annotations;

import com.vgosoft.core.annotation.CompositeQuery;
import com.vgosoft.core.annotation.ViewColumnMeta;
import com.vgosoft.core.constant.GlobalConstant;
import com.vgosoft.core.constant.enums.ViewActionColumnEnum;
import com.vgosoft.core.constant.enums.ViewIndexColumnEnum;
import com.vgosoft.core.constant.enums.ViewToolBarsEnum;
import com.vgosoft.tool.core.VMD5Util;
import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.IntrospectedTable;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-10-07 04:48
 * @version 3.0
 */
public class ViewTableMeta  extends AbstractAnnotation{

    public static final String ANNOTATION_NAME = "@ViewTableMeta";

    private final String value;
    private final String listName;
    private final String beanName;
    private String dataUrl;
    private String createUrl;
    private String className;
    private String listType;
    private ViewIndexColumnEnum indexColumn;
    private ViewActionColumnEnum[] actionColumn = new ViewActionColumnEnum[0];
    private ViewToolBarsEnum[] toolbarActions = new ViewToolBarsEnum[0];
    private String[] ignoreFields = new String[0];
    private String[] columns = new String[0];
    private String[] querys = new String[0];
    private String indexColWidth;
    private String actionColWidth;
    private int dataFilterType = 0;
    private String categoryTreeUrl;
    private int wfStatus = 6;
    private String areaWidth;
    private String areaHeight;

    public static ViewTableMeta create(IntrospectedTable introspectedTable){
        return new ViewTableMeta(introspectedTable);
    }

    public ViewTableMeta(IntrospectedTable introspectedTable) {
        super();
        this.value = VMD5Util.MD5(introspectedTable.getControllerBeanName() + GlobalConstant.DEFAULT_VIEW_ID_SUFFIX);
        items.add(VStringUtil.format("value = \"{0}\"", this.getValue()));
        this.listName = introspectedTable.getRemarks(true);
        items.add(VStringUtil.format("listName = \"{0}\"", this.getListName()));
        this.beanName = introspectedTable.getControllerBeanName();
        items.add(VStringUtil.format("beanName = \"{0}\"", this.getBeanName()));
        this.dataUrl = "/viewmgr/getdtdata";
        this.listType = GlobalConstant.DEFAULT_VIEW_LIST_TYPE;
        this.indexColumn = ViewIndexColumnEnum.ROW_INDEX;
        this.dataFilterType = 0;
        this.addImports("com.vgosoft.core.annotation.ViewTableMeta");
    }

    @Override
    public String toAnnotation() {
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
            this.addImports("com.vgosoft.core.constant.enums.ViewIndexColumnEnum");
        }
        if (this.actionColumn.length>0) {
            List<ViewActionColumnEnum> viewActionColumnEnums = Arrays.asList(this.actionColumn);
            if (!(viewActionColumnEnums.size()==2 && viewActionColumnEnums.contains(ViewActionColumnEnum.VIEW) && viewActionColumnEnums.contains(ViewActionColumnEnum.EDIT))) {
                String collect = Arrays.stream(this.actionColumn).map(e -> "ViewActionColumnEnum." + e.name()).collect(Collectors.joining(","));
                items.add(VStringUtil.format("actionColumn = '{'{0}'}'", collect));
                this.addImports("com.vgosoft.core.constant.enums.ViewActionColumnEnum");
            }
        }
        if (this.toolbarActions.length>0) {
            List<ViewToolBarsEnum> viewToolBarsEnums = Arrays.asList(this.toolbarActions);
            if (!(viewToolBarsEnums.size()==2 && viewToolBarsEnums.contains(ViewToolBarsEnum.CREATE) && viewToolBarsEnums.contains(ViewToolBarsEnum.REMOVE))) {
                String collect = Arrays.stream(this.toolbarActions).map(e -> "ViewToolBarsEnum" + e.name()).collect(Collectors.joining(","));
                items.add(VStringUtil.format("toolbarActions = '{'{0}'}'", collect));
                this.addImports("com.vgosoft.core.constant.enums.ViewToolBarsEnum");
            }
        }
        if (this.ignoreFields.length>0) {
            String collect = Arrays.stream(this.ignoreFields).map(f -> "\"" + f + "\"").collect(Collectors.joining(","));
            items.add(VStringUtil.format("ignoreFields = '{'{0}'}'",collect));
        }
        if (this.columns.length>0) {
            items.add(VStringUtil.format("columns = '{'{0}'}'",String.join("\n        , ", this.columns)));
            this.addImports("com.vgosoft.core.annotation.ViewColumnMeta");
        }
        if (this.querys.length>0) {
            items.add(VStringUtil.format("querys = '{'{0}'}'",String.join("\n        , ", this.querys)));
            this.addImports("com.vgosoft.core.annotation.CompositeQuery");
        }
        if (VStringUtil.isNotBlank(this.indexColWidth)) {
            items.add(VStringUtil.format("indexColWidth = \"{0}\"",this.getIndexColWidth()));
        }
        if (VStringUtil.isNotBlank(this.actionColWidth)) {
            items.add(VStringUtil.format("actionColWidth = \"{0}\"",this.getActionColWidth()));
        }
        if (this.dataFilterType!=0) {
            items.add(VStringUtil.format("dataFilterType = {0}",this.getDataFilterType()));
        }
        if (VStringUtil.isNotBlank(this.categoryTreeUrl)) {
            items.add(VStringUtil.format("categoryTreeUrl = \"{0}\"",this.getCategoryTreeUrl()));
        }
        if (this.wfStatus!=6) {
            items.add(VStringUtil.format("wfStatus = {0}",this.getWfStatus()));
        }
        if (VStringUtil.isNotBlank(this.areaWidth)) {
            items.add(VStringUtil.format("areaWidth = \"{0}\"",this.getAreaWidth()));
        }
        if (VStringUtil.isNotBlank(this.areaHeight)) {
            items.add(VStringUtil.format("areaHeight = \"{0}\"",this.getAreaHeight()));
        }

        return ANNOTATION_NAME+"("+ String.join("\n       ,",items.toArray(new String[0])) +")";
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

    public ViewActionColumnEnum[] getActionColumn() {
        return actionColumn;
    }

    public void setActionColumn(ViewActionColumnEnum[] actionColumn) {
        this.actionColumn = actionColumn;
    }

    public ViewToolBarsEnum[] getToolbarActions() {
        return toolbarActions;
    }

    public void setToolbarActions(ViewToolBarsEnum[] toolbarActions) {
        this.toolbarActions = toolbarActions;
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
}