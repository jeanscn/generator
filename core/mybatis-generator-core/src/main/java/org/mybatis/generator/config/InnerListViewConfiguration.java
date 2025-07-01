package org.mybatis.generator.config;

import com.vgosoft.core.constant.GlobalConstant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.mybatis.generator.codegen.mybatis3.freeMaker.html.layui.InnerListEditTemplate;
import org.mybatis.generator.custom.enums.ViewVoUiFrameEnum;

import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class InnerListViewConfiguration extends AbstractTableListCommonConfiguration {

    private String height;
    private String width = "";
    private boolean even = true;
    private List<String> enableEditFields = new ArrayList<>();
    private String editExtendsForm;
    private List<HtmlElementDescriptor> htmlElements = new ArrayList<>();
    private HtmlGeneratorConfiguration htmlGeneratorConfiguration;
    private final Map<String, HtmlElementDescriptor> elementDescriptorMap = new HashMap<>();
    private final List<InnerListEditTemplate> innerListEditTemplate = new ArrayList<>();
    private final List<ListColumnConfiguration> listColumnConfigurations = new ArrayList<>();
    private final Set<String> readonlyFields = new HashSet<>();
    private final Set<String> requiredColumns = new HashSet<>();
    private final Set<HtmlButtonGeneratorConfiguration> htmlButtons = new HashSet<>();
    private final List<QueryColumnConfiguration> queryColumnConfigurations = new ArrayList<>();
    private List<String> vxeListButtons = new ArrayList<>();

    {
        this.listKey = "";
        this.size = "mg";
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

    public Map<String, HtmlElementDescriptor> getElementDescriptorMap() {
        if (!elementDescriptorMap.isEmpty()) {
            return elementDescriptorMap;
        }
        Map<String, HtmlElementDescriptor> map = htmlElements.stream().collect(Collectors.toMap(h -> h.getColumn().getJavaProperty(), h -> h, (h1, h2) -> h1));
        elementDescriptorMap.putAll(map);
        return elementDescriptorMap;
    }
}
