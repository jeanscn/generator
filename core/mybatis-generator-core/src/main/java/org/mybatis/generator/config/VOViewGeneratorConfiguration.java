package org.mybatis.generator.config;

import com.vgosoft.core.constant.GlobalConstant;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.custom.ViewVoUiFrameEnum;

import java.util.*;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
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

    public void addQueryColumnConfigurations(QueryColumnConfiguration queryColumnConfiguration) {
        this.queryColumnConfigurations.add(queryColumnConfiguration);
    }

    public void addViewFieldOverrideConfiguration(ViewFieldOverrideConfiguration viewFieldOverrideConfiguration) {
        this.viewFieldOverrideConfigurations.add(viewFieldOverrideConfiguration);
    }

    public void addFilterColumnsConfigurations(FilterColumnConfiguration filterColumnConfiguration) {
        this.filterColumnsConfigurations.add(filterColumnConfiguration);
    }
}
