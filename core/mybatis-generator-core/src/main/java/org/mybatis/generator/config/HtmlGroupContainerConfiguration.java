package org.mybatis.generator.config;

import java.util.ArrayList;
import java.util.List;

public class HtmlGroupContainerConfiguration   extends TypedPropertyHolder{
    private String elementKey;

    private String name;
    private String type;
    private String title;

    private Integer span = 24;

    private Integer columnNum = 1;

    private String afterColumn;

    private List<String> includeElements = new ArrayList<>();

    private boolean noBorder = false;

    private String hideExpression;

    private String className;

    private List<HtmlGroupContainerConfiguration> groupContainerConfigurations = new ArrayList<>();
    public String getElementKey() {
        return elementKey;
    }

    public void setElementKey(String elementKey) {
        this.elementKey = elementKey;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getSpan() {
        return span;
    }

    public void setSpan(Integer span) {
        this.span = span;
    }

    public Integer getColumnNum() {
        return columnNum;
    }

    public void setColumnNum(Integer columnNum) {
        this.columnNum = columnNum;
    }

    public String getAfterColumn() {
        return afterColumn;
    }

    public void setAfterColumn(String afterColumn) {
        this.afterColumn = afterColumn;
    }

    public List<String> getIncludeElements() {
        return includeElements;
    }

    public void setIncludeElements(List<String> includeElements) {
        this.includeElements = includeElements;
    }

    public List<HtmlGroupContainerConfiguration> getGroupContainerConfigurations() {
        return groupContainerConfigurations;
    }

    public void setGroupContainerConfigurations(List<HtmlGroupContainerConfiguration> groupContainerConfigurations) {
        this.groupContainerConfigurations = groupContainerConfigurations;
    }

    public boolean isNoBorder() {
        return noBorder;
    }

    public void setNoBorder(boolean noBorder) {
        this.noBorder = noBorder;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHideExpression() {
        return hideExpression;
    }

    public void setHideExpression(String hideExpression) {
        this.hideExpression = hideExpression;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }
}
