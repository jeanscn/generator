package org.mybatis.generator.config;

import org.mybatis.generator.codegen.mybatis3.freeMaker.html.layui.InnerListEditTemplate;

import java.util.*;
import java.util.stream.Collectors;

public class InnerListViewConfiguration extends PropertyHolder{
    private String size = "mg";

    /**
     * 设置头部工具栏右侧图标,可选值有：filter,exports,print
     */
    private List<String> defaultToolbar = new ArrayList<>();

    private String height;

    private Integer width;

    private boolean totalRow = false;

    private String enablePage = "false";

    private String skin = "grid";

    private boolean even = true;

    private List<String> enableEditFields = new ArrayList<>();

    private String indexColumn;

    private List<String> actionColumn = new ArrayList<>();

    private Set<String> defaultDisplayFields = new HashSet<>();

    private Set<String> defaultHiddenFields = new HashSet<>();

    private String editExtendsForm;

    private List<HtmlElementDescriptor> htmlElements = new ArrayList<>();

    private HtmlGeneratorConfiguration htmlGeneratorConfiguration;

    private Map<String, HtmlElementDescriptor> elementDescriptorMap = new HashMap<>();

    private List<InnerListEditTemplate> innerListEditTemplate = new ArrayList<>();

    public InnerListViewConfiguration() {
        defaultToolbar.add("filter");
        defaultToolbar.add("exports");
        defaultToolbar.add("print");
        defaultToolbar.add("columns");
        defaultToolbar.add("fullscreen");
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

    public Set<String> getDefaultDisplayFields() {
        return defaultDisplayFields;
    }

    public void setDefaultDisplayFields(Set<String> defaultDisplayFields) {
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

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
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

    public void setDefaultHiddenFields(Set<String> defaultHiddenFields) {
        this.defaultHiddenFields = defaultHiddenFields;
    }

    public Map<String, HtmlElementDescriptor> getElementDescriptorMap() {
        if (elementDescriptorMap.size()>0) {
            return elementDescriptorMap;
        }
        Map<String, HtmlElementDescriptor> map = htmlElements.stream().collect(Collectors.toMap(h -> h.getColumn().getJavaProperty(), h -> h, (h1, h2) -> h1));
        elementDescriptorMap.putAll(map);
        return elementDescriptorMap;
    }

    public List<InnerListEditTemplate> getInnerListEditTemplate() {
        return innerListEditTemplate;
    }

    public void setInnerListEditTemplate(List<InnerListEditTemplate> innerListEditTemplate) {
        this.innerListEditTemplate = innerListEditTemplate;
    }
}
