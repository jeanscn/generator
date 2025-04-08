package org.mybatis.generator.config;

import com.vgosoft.core.constant.GlobalConstant;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.custom.ViewVoUiFrameEnum;

import java.util.*;

public class VOViewGeneratorConfiguration extends AbstractTableListCommonConfiguration {

    private List<String> includeColumns = new ArrayList<>();

    private String viewMenuIcon;

    private Set<HtmlButtonGeneratorConfiguration> htmlButtons = new HashSet<>();

    private List<InnerListViewConfiguration> innerListViewConfigurations = new ArrayList<>();

    private final List<QueryColumnConfiguration> queryColumnConfigurations = new ArrayList<>();

    private final List<FilterColumnConfiguration> filterColumnsConfigurations = new ArrayList<>();

    private List<ViewFieldOverrideConfiguration> viewFieldOverrideConfigurations = new ArrayList<>();

    private TableConfiguration tableConfiguration;

    {
        this.listKey = "";
        this.size = "mg";
        this.actionColumn = new ArrayList<>();
        this.toolbar = new ArrayList<>();
        this.queryColumns = new ArrayList<>();
        this.fuzzyColumns = new ArrayList<>();
        this.filterColumns = new ArrayList<>();
        this.totalRow = false;
        this.totalFields = new HashSet<>();
        this.defaultDisplayFields = new ArrayList<>();
        this.defaultHiddenFields = new HashSet<>();
        this.defaultToolbar = new ArrayList<>();
        this.viewMenuElIcon = GlobalConstant.VIEW_VO_DEFAULT_EL_ICON;
        this.categoryTreeMultiple = false;
        this.uiFrameType = ViewVoUiFrameEnum.EL_PLUS_TABLE;
        this.tableType = "default";
        this.actionColumnFixed = "right";
        this.indexColumnFixed = "left";
    }

    public VOViewGeneratorConfiguration() {
        super();
    }

    public VOViewGeneratorConfiguration(Context context) {
        super(context);
    }

    public VOViewGeneratorConfiguration(Context context, TableConfiguration tc) {
        super(context);
        this.generate = false;
        targetPackage = String.join(".", baseTargetPackage, "vo");
        fullyQualifiedJavaType = new FullyQualifiedJavaType(String.join(".", targetPackage, tc.getDomainObjectName() + "ViewVO"));
        this.tableConfiguration = tc;
    }

    @Override
    public void validate(List<String> errors, String contextId) {
        super.validate(errors, contextId, "VOViewGeneratorConfiguration");
    }
    public List<String> getIncludeColumns() {
        return includeColumns;
    }

    public void setIncludeColumns(List<String> includeColumns) {
        this.includeColumns = includeColumns;
    }

    public String getViewMenuIcon() {
        return viewMenuIcon;
    }

    public void setViewMenuIcon(String viewMenuIcon) {
        this.viewMenuIcon = viewMenuIcon;
    }

    public List<InnerListViewConfiguration> getInnerListViewConfigurations() {
        return innerListViewConfigurations;
    }

    public void setInnerListViewConfigurations(List<InnerListViewConfiguration> innerListViewConfigurations) {
        this.innerListViewConfigurations = innerListViewConfigurations;
    }
    public Set<HtmlButtonGeneratorConfiguration> getHtmlButtons() {
        return htmlButtons;
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

    public List<ViewFieldOverrideConfiguration> getViewFieldOverrideConfigurations() {
        return viewFieldOverrideConfigurations;
    }

    public void setViewFieldOverrideConfigurations(List<ViewFieldOverrideConfiguration> viewFieldOverrideConfigurations) {
        this.viewFieldOverrideConfigurations = viewFieldOverrideConfigurations;
    }

    public void addViewFieldOverrideConfiguration(ViewFieldOverrideConfiguration viewFieldOverrideConfiguration) {
        this.viewFieldOverrideConfigurations.add(viewFieldOverrideConfiguration);
    }

    public TableConfiguration getTableConfiguration() {
        return tableConfiguration;
    }

    public void setTableConfiguration(TableConfiguration tableConfiguration) {
        this.tableConfiguration = tableConfiguration;
    }

    public List<FilterColumnConfiguration> getFilterColumnsConfigurations() {
        return filterColumnsConfigurations;
    }

    public void addFilterColumnsConfigurations(FilterColumnConfiguration filterColumnConfiguration) {
        this.filterColumnsConfigurations.add(filterColumnConfiguration);
    }
}
