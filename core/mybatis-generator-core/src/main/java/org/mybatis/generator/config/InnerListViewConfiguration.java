package org.mybatis.generator.config;

import org.mybatis.generator.codegen.mybatis3.freeMaker.html.layui.InnerListEditTemplate;

import java.util.*;
import java.util.stream.Collectors;

public class InnerListViewConfiguration extends PropertyHolder{

    private String listKey = "";

    /**
     * 设置表格尺寸，可选值有：lg、sm、xs
     */
    private String size = "mg";

    /**
     * 设置头部工具栏右侧图标,可选值有：filter,exports,print
     */
    private List<String> defaultToolbar = new ArrayList<>();

    private String height;

    private String width = "";

    private boolean totalRow = false;

    private String enablePage = "false";

    private String skin = "grid";

    private boolean even = true;

    private List<String> enableEditFields = new ArrayList<>();

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


    private String editExtendsForm;

    private List<String> queryColumns = new ArrayList<>();
    private List<HtmlElementDescriptor> htmlElements = new ArrayList<>();

    private HtmlGeneratorConfiguration htmlGeneratorConfiguration;

    private final Map<String, HtmlElementDescriptor> elementDescriptorMap = new HashMap<>();

    private final List<InnerListEditTemplate> innerListEditTemplate = new ArrayList<>();


    private List<String> toolbar = new ArrayList<>();

    private final List<ListColumnConfiguration> listColumnConfigurations = new ArrayList<>();
    private List<String> defaultDisplayFields = new ArrayList<>();

    private final Set<String> defaultHiddenFields = new HashSet<>();
    private final  Set<String> readonlyFields = new HashSet<>();

    private final Set<String> requiredColumns = new HashSet<>();

    private final List<HtmlButtonGeneratorConfiguration> htmlButtons = new ArrayList<>();


    private final List<QueryColumnConfiguration> queryColumnConfigurations = new ArrayList<>();

    /**
     * 构造器
     */
    public InnerListViewConfiguration() {
       super();
    }

    public String getListKey() {
        return listKey;
    }

    public void setListKey(String listKey) {
        this.listKey = listKey;
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

    public List<String> getDefaultDisplayFields() {
        return defaultDisplayFields;
    }

    public void setDefaultDisplayFields(List<String> defaultDisplayFields) {
        this.defaultDisplayFields = defaultDisplayFields;
    }

    public List<String> getDefaultToolbar() {
        return defaultToolbar;
    }

    public void setDefaultToolbar(List<String> defaultToolbar) {
        this.defaultToolbar = defaultToolbar;
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

    public boolean isTotalRow() {
        return totalRow;
    }

    public void setTotalRow(boolean totalRow) {
        this.totalRow = totalRow;
    }

    public String getEnablePage() {
        return enablePage;
    }

    public void setEnablePage(String enablePage) {
        this.enablePage = enablePage;
    }

    public String getSkin() {
        return skin;
    }

    public void setSkin(String skin) {
        this.skin = skin;
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

    public Set<String> getDefaultHiddenFields() {
        return defaultHiddenFields;
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
    public List<String> getToolbar() {
        return toolbar;
    }

    public void setToolbar(List<String> toolbar) {
        this.toolbar = toolbar;
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

    public List<String> getQueryColumns() {
        return queryColumns;
    }

    public void setQueryColumns(List<String> queryColumns) {
        this.queryColumns = queryColumns;
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
}
