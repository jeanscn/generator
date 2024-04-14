package org.mybatis.generator.config;

import com.vgosoft.core.constant.GlobalConstant;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.custom.ViewVoUiFrameEnum;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VOViewGeneratorConfiguration extends AbstractModelGeneratorConfiguration {

    private String title;

    private Set<String> includeColumns = new HashSet<>();

    private List<String> toolbar = new ArrayList<>();

    private String indexColumn;

    private  List<String> actionColumn = new ArrayList<>();

    private Set<HtmlButtonGeneratorConfiguration> htmlButtons = new HashSet<>();

    private List<String> queryColumns = new ArrayList<>();

    private List<String> defaultDisplayFields = new ArrayList<>();

    private Set<String> defaultHiddenFields = new HashSet<>();

    private String parentMenuId;

    private String viewMenuIcon;

    private String viewMenuElIcon = GlobalConstant.VIEW_VO_DEFAULT_EL_ICON;

    private String categoryTreeUrl;

    private String categoryTreeMultiple = "true";

    private ViewVoUiFrameEnum uiFrameType = ViewVoUiFrameEnum.EL_PLUS_TABLE;

    private List<InnerListViewConfiguration> innerListViewConfigurations = new ArrayList<>();

    private final List<QueryColumnConfiguration> queryColumnConfigurations = new ArrayList<>();

    private List<ViewFieldOverrideConfiguration> viewFieldOverrideConfigurations = new ArrayList<>();

    public VOViewGeneratorConfiguration(Context context,TableConfiguration tc) {
        super(context);
        this.generate = false;
        targetPackage = String.join(".", baseTargetPackage,"vo");
        fullyQualifiedJavaType = new FullyQualifiedJavaType(String.join(".",targetPackage,tc.getDomainObjectName()+"ViewVO"));
    }

    @Override
    public void validate(List<String> errors, String contextId) {
        super.validate(errors, contextId, "VOViewGeneratorConfiguration");
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIndexColumn() {
        return indexColumn;
    }

    public void setIndexColumn(String indexColumn) {
        this.indexColumn = indexColumn;
    }

    public List<String> getQueryColumns() {
        return queryColumns;
    }

    public void setQueryColumns(List<String> queryColumns) {
        this.queryColumns = queryColumns;
    }

    public void setDefaultHiddenFields(Set<String> defaultHiddenFields) {
        this.defaultHiddenFields = defaultHiddenFields;
    }

    public Set<String> getDefaultHiddenFields() {
        return defaultHiddenFields;
    }

    public Set<String> getIncludeColumns() {
        return includeColumns;
    }

    public void setIncludeColumns(Set<String> includeColumns) {
        this.includeColumns = includeColumns;
    }

    public String getParentMenuId() {
        return parentMenuId;
    }

    public void setParentMenuId(String parentMenuId) {
        this.parentMenuId = parentMenuId;
    }

    public List<String> getDefaultDisplayFields() {
        return defaultDisplayFields;
    }

    public void setDefaultDisplayFields(List<String> defaultDisplayFields) {
        this.defaultDisplayFields = defaultDisplayFields;
    }
    public String getViewMenuIcon() {
        return viewMenuIcon;
    }

    public void setViewMenuIcon(String viewMenuIcon) {
        this.viewMenuIcon = viewMenuIcon;
    }

    public String getCategoryTreeUrl() {
        return categoryTreeUrl;
    }

    public void setCategoryTreeUrl(String categoryTreeUrl) {
        this.categoryTreeUrl = categoryTreeUrl;
    }

    public List<InnerListViewConfiguration> getInnerListViewConfigurations() {
        return innerListViewConfigurations;
    }

    public void setInnerListViewConfigurations(List<InnerListViewConfiguration> innerListViewConfigurations) {
        this.innerListViewConfigurations = innerListViewConfigurations;
    }

    public String getCategoryTreeMultiple() {
        return categoryTreeMultiple;
    }

    public void setCategoryTreeMultiple(String categoryTreeMultiple) {
        this.categoryTreeMultiple = categoryTreeMultiple;
    }

    public List<String> getToolbar() {
        return toolbar;
    }

    public List<String> getActionColumn() {
        return actionColumn;
    }

    public Set<HtmlButtonGeneratorConfiguration> getHtmlButtons() {
        return htmlButtons;
    }

    public void setToolbar(List<String> toolbar) {
        this.toolbar = toolbar;
    }

    public void setActionColumn(List<String> actionColumn) {
        this.actionColumn = actionColumn;
    }

    public void setHtmlButtons(Set<HtmlButtonGeneratorConfiguration> htmlButtons) {
        this.htmlButtons = htmlButtons;
    }

    public List<QueryColumnConfiguration> getQueryColumnConfigurations() {
        return queryColumnConfigurations;
    }

    public void addQueryColumnConfigurations(QueryColumnConfiguration queryColumnConfiguration) {
        this.queryColumnConfigurations.add(queryColumnConfiguration);
    }

    public ViewVoUiFrameEnum getUiFrameType() {
        return uiFrameType;
    }

    public void setUiFrameType(ViewVoUiFrameEnum uiFrameType) {
        this.uiFrameType = uiFrameType;
    }

    public String getViewMenuElIcon() {
        return viewMenuElIcon;
    }

    public void setViewMenuElIcon(String viewMenuElIcon) {
        this.viewMenuElIcon = viewMenuElIcon;
    }

    public List<ViewFieldOverrideConfiguration> getViewFieldOverrideConfigurations() {
        return viewFieldOverrideConfigurations;
    }

    public void setViewFieldOverrideConfigurations(List<ViewFieldOverrideConfiguration> viewFieldOverrideConfigurations) {
        this.viewFieldOverrideConfigurations = viewFieldOverrideConfigurations;
    }

    public void addViewFieldOverrideConfiguration(ViewFieldOverrideConfiguration viewFieldOverrideConfiguration) {
        this.viewFieldOverrideConfigurations.add(viewFieldOverrideConfiguration);
    }
}
