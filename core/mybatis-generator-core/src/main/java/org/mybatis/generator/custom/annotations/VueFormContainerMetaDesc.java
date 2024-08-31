package org.mybatis.generator.custom.annotations;

import com.vgosoft.core.annotation.VueFormContainerMeta;
import org.mybatis.generator.config.HtmlGroupContainerConfiguration;

import static com.vgosoft.tool.core.VStringUtil.format;
import static com.vgosoft.tool.core.VStringUtil.stringHasValue;

public class VueFormContainerMetaDesc extends AbstractAnnotation {

    public static final String ANNOTATION_NAME = "@VueFormContainerMeta";

    private String value;

    private String name;

    private String parentElementKey;

    private String type;
    private String title;

    private int span = 24;

    private int columnNum = 1;

    private String afterColumn;

    private String includeElements;

    private boolean noBorder = false;

    private String hideExpression;

    private String className;

    public VueFormContainerMetaDesc() {
        super();
        this.addImports(VueFormContainerMeta.class.getCanonicalName());
    }

    public VueFormContainerMetaDesc(HtmlGroupContainerConfiguration htmlGroupContainerConfiguration) {
        super();
        this.addImports(VueFormContainerMeta.class.getCanonicalName());
        this.name = htmlGroupContainerConfiguration.getName();
        this.value = htmlGroupContainerConfiguration.getElementKey();
        this.type = htmlGroupContainerConfiguration.getType();
        this.title = htmlGroupContainerConfiguration.getTitle();
        this.span = htmlGroupContainerConfiguration.getSpan();
        this.columnNum = htmlGroupContainerConfiguration.getColumnNum();
        this.afterColumn = htmlGroupContainerConfiguration.getAfterColumn();
        this.includeElements = String.join(",", htmlGroupContainerConfiguration.getIncludeElements());
        this.noBorder = htmlGroupContainerConfiguration.isNoBorder();
        this.hideExpression = htmlGroupContainerConfiguration.getHideExpression();
        this.className = htmlGroupContainerConfiguration.getClassName();
    }

    @Override
    public String toAnnotation() {
        if (stringHasValue(value)) {
            items.add(format("value = \"{0}\"", value));
        }
        if (stringHasValue(name)) {
            items.add(format("name = \"{0}\"", name));
        }
        if (stringHasValue(type)) {
            items.add(format("type = \"{0}\"", type));
        }
        if (stringHasValue(parentElementKey)) {
            items.add(format("parentElementKey = \"{0}\"", parentElementKey));
        }
        if (stringHasValue(title)) {
            items.add(format("title = \"{0}\"", title));
        }
        if (span > 0 && span != 24) {
            items.add(format("span = {0}", span));
        }
        if (columnNum > 0 && columnNum != 1) {
            items.add(format("columnNum = {0}", columnNum));
        }
        if (stringHasValue(afterColumn)) {
            items.add(format("afterColumn = \"{0}\"", afterColumn));
        }
        if (stringHasValue(includeElements)) {
            items.add(format("includeElements = \"{0}\"", includeElements));
        }
        if (noBorder) {
            items.add("noBorder = true");
        }
        if (stringHasValue(hideExpression)) {
            items.add(format("hideExpression = \"{0}\"", hideExpression));
        }
        if (stringHasValue(className)) {
            items.add(format("className = \"{0}\"", className));
        }
        return ANNOTATION_NAME + "(" + String.join(", ", items.toArray(new String[0])) + ")";
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
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

    public int getSpan() {
        return span;
    }

    public void setSpan(int span) {
        this.span = span;
    }

    public int getColumnNum() {
        return columnNum;
    }

    public void setColumnNum(int columnNum) {
        this.columnNum = columnNum;
    }

    public String getAfterColumn() {
        return afterColumn;
    }

    public void setAfterColumn(String afterColumn) {
        this.afterColumn = afterColumn;
    }

    public String getIncludeElements() {
        return includeElements;
    }

    public void setIncludeElements(String includeElements) {
        this.includeElements = includeElements;
    }


    public String getParentElementKey() {
        return parentElementKey;
    }

    public void setParentElementKey(String parentElementKey) {
        this.parentElementKey = parentElementKey;
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
