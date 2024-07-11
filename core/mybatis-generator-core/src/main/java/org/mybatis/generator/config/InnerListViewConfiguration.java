package org.mybatis.generator.config;

import com.vgosoft.core.constant.GlobalConstant;
import org.mybatis.generator.codegen.mybatis3.freeMaker.html.layui.InnerListEditTemplate;
import org.mybatis.generator.custom.ViewVoUiFrameEnum;

import java.util.*;
import java.util.stream.Collectors;

public class InnerListViewConfiguration extends AbstractTableListCommonConfiguration{

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
    private final  Set<String> readonlyFields = new HashSet<>();
    private final Set<String> requiredColumns = new HashSet<>();
    private final List<HtmlButtonGeneratorConfiguration> htmlButtons = new ArrayList<>();
    private final List<QueryColumnConfiguration> queryColumnConfigurations = new ArrayList<>();

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

    /**
     * 构造器
     */
    public InnerListViewConfiguration() {
       super();
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public boolean isEven() {
        return even;
    }

    public void setEven(boolean even) {
        this.even = even;
    }

    public List<String> getEnableEditFields() {
        return enableEditFields;
    }

    public void setEnableEditFields(List<String> enableEditFields) {
        this.enableEditFields = enableEditFields;
    }

    public String getEditExtendsForm() {
        return editExtendsForm;
    }

    public void setEditExtendsForm(String editExtendsForm) {
        this.editExtendsForm = editExtendsForm;
    }

    public List<HtmlElementDescriptor> getHtmlElements() {
        return htmlElements;
    }

    public void setHtmlElements(List<HtmlElementDescriptor> htmlElements) {
        this.htmlElements = htmlElements;
    }

    public HtmlGeneratorConfiguration getHtmlGeneratorConfiguration() {
        return htmlGeneratorConfiguration;
    }

    public void setHtmlGeneratorConfiguration(HtmlGeneratorConfiguration htmlGeneratorConfiguration) {
        this.htmlGeneratorConfiguration = htmlGeneratorConfiguration;
    }
    public Set<String> getRequiredColumns() {
        return requiredColumns;
    }

    public Map<String, HtmlElementDescriptor> getElementDescriptorMap() {
        if (!elementDescriptorMap.isEmpty()) {
            return elementDescriptorMap;
        }
        Map<String, HtmlElementDescriptor> map = htmlElements.stream().collect(Collectors.toMap(h -> h.getColumn().getJavaProperty(), h -> h, (h1, h2) -> h1));
        elementDescriptorMap.putAll(map);
        return elementDescriptorMap;
    }
    public List<InnerListEditTemplate> getInnerListEditTemplate() {
        return innerListEditTemplate;
    }
    public Set<String> getReadonlyFields() {
        return readonlyFields;
    }

    public List<ListColumnConfiguration> getListColumnConfigurations() {
        return listColumnConfigurations;
    }

    public List<HtmlButtonGeneratorConfiguration> getHtmlButtons() {
        return htmlButtons;
    }

    public List<QueryColumnConfiguration> getQueryColumnConfigurations() {
        return queryColumnConfigurations;
    }

}
