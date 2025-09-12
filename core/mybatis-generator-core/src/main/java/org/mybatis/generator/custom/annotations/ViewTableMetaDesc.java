package org.mybatis.generator.custom.annotations;

import com.vgosoft.core.annotation.CompositeQuery;
import com.vgosoft.core.annotation.HtmlButton;
import com.vgosoft.core.annotation.ViewColumnMeta;
import com.vgosoft.core.annotation.ViewTableMeta;
import com.vgosoft.core.constant.GlobalConstant;
import com.vgosoft.core.constant.enums.view.ViewIndexColumnEnum;
import com.vgosoft.tool.core.VMD5Util;
import com.vgosoft.tool.core.VStringUtil;
import lombok.Getter;
import lombok.Setter;
import org.mybatis.generator.api.AbstractIntrospectedTable;
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
@Getter
@Setter
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

    private ViewIndexColumnEnum indexColumn;

    private String indexColWidth;

    private String[] toolbar = new String[0];

    private String columnActions;

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

    private String defaultSort = "";

    private String showActionColumn = "default";

    private String editFormIn;

    private String detailFormIn;

    public static ViewTableMetaDesc create(AbstractIntrospectedTable abstractIntrospectedTable) {
        return new ViewTableMetaDesc(abstractIntrospectedTable);
    }

    public ViewTableMetaDesc(AbstractIntrospectedTable abstractIntrospectedTable) {
        super();
        this.value = Mb3GenUtil.getDefaultViewId(abstractIntrospectedTable);
        items.add(VStringUtil.format("value = \"{0}\"", this.getValue()));
        this.listName = abstractIntrospectedTable.getRemarks(true);
        items.add(VStringUtil.format("listName = \"{0}\"", this.getListName()));
        this.beanName = abstractIntrospectedTable.getControllerBeanName();
        items.add(VStringUtil.format("beanName = \"{0}\"", this.getBeanName()));
        this.appKeyword = abstractIntrospectedTable.getContext().getAppKeyword();
        items.add(VStringUtil.format("appKeyword = \"{0}\"", this.getAppKeyword()));
        this.tableName = abstractIntrospectedTable.getTableConfiguration().getTableName();
        items.add(VStringUtil.format("tableName = \"{0}\"", this.tableName));
        this.tableAlias = abstractIntrospectedTable.getTableConfiguration().getAlias();
        items.add(VStringUtil.format("tableAlias = \"{0}\"", this.tableAlias));
        this.dataUrl = "/viewmgr/getdtdata";
        this.listType = VMD5Util.MD5_15(Mb3GenUtil.getModelKey(abstractIntrospectedTable));
        this.indexColumn = ViewIndexColumnEnum.CHECKBOX;
        this.moduleId = Mb3GenUtil.getModelId(abstractIntrospectedTable);
        this.applyWorkflow = GenerateUtils.isWorkflowInstance(abstractIntrospectedTable) ? 1 : 0;
        this.addImports(ViewTableMeta.class.getCanonicalName());
    }

    @Override
    public String toAnnotation() {
        if (VStringUtil.isNotBlank(this.getListKey())) {
            items.add(VStringUtil.format("listKey = \"{0}\"", this.getListKey()));
        }
        if (this.toolbar.length > 0) {
            items.add(VStringUtil.format("toolbar = '{'{0}'}'", String.join(", ", this.toolbar)));
        }
        if (VStringUtil.isNotBlank(this.getTitle())) {
            items.add(VStringUtil.format("title = \"{0}\"", this.getTitle()));
        }
        if (VStringUtil.isNotBlank(this.getSize()) && !"md".equals(this.getSize())) {
            items.add(VStringUtil.format("size = \"{0}\"", this.getSize()));
        }
        if (!"/viewmgr/getdtdata".equals(this.getDataUrl())) {
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
        if (this.columnActions != null) {
            items.add(VStringUtil.format("columnActions = '{'{0}'}'", this.columnActions));
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
        if (VStringUtil.stringHasValue(this.defaultSort)) {
            items.add(VStringUtil.format("defaultSort = \"{0}\"", this.getDefaultSort()));
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
        return ANNOTATION_NAME + "(" + String.join("\n        , ", items.toArray(new String[0])) + ")";
    }

}
