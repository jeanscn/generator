package org.mybatis.generator.custom.htmlGenerator;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.html.*;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.codegen.HtmlConstants;
import org.mybatis.generator.config.HtmlMapGeneratorConfiguration;
import org.mybatis.generator.internal.util.StringUtility;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author <a href="mailto:cjj@vip.sina.com">ChenJJ</a>
 * 2020-07-23 00:57
 * @version 3.0
 */
public abstract class AbsHtmlDocumentGenerator implements HtmlDocumentGenerator {

    private final Document document;
    private final IntrospectedTable introspectedTable;
    private FullyQualifiedJavaType entityType;
    protected final String btn_submit_id = "btn_save";
    protected final String btn_close_id = "btn_close";
    protected final String btn_reset_id = "btn_reset";
    protected final String input_subject_id = "subject";
    protected final HtmlMapGeneratorConfiguration htmlMapGeneratorConfiguration;

    public AbsHtmlDocumentGenerator(Document document, IntrospectedTable introspectedTable, HtmlMapGeneratorConfiguration htmlMapGeneratorConfiguration) {
        this.document = document;
        this.introspectedTable = introspectedTable;
        this.entityType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        this.htmlMapGeneratorConfiguration = htmlMapGeneratorConfiguration;
    }

    public int getPageColumnsConfig() {
        int c = htmlMapGeneratorConfiguration.getPageColumnsNum();
        if (c > 12) {
            c = 12;
        } else if (c <= 0) {
            c = 1;
        }
        if (12 % c == 0) {
            return c;
        } else {
            return 1;
        }
    }

    public String getHtmlBarPositionConfig() {
        return htmlMapGeneratorConfiguration.getBarPosition();
    }

    public abstract boolean htmlMapDocumentGenerated();

    protected HtmlElement generateHtmlHead() {
        HtmlElement head = new HtmlElement("head");
        addStaticReplace(head, "subpages/webjarsPluginsRequired2.html::baseRequired('" + introspectedTable.getRemarks() + "')");
        addStaticReplace(head, "subpages/webjarsPluginsRequired2.html::jQueryRequired");
        return head;
    }

    protected void addStaticStyleSheet(HtmlElement htmlElement, String value) {
        HtmlElement div = new HtmlElement("link");
        div.addAttribute(new Attribute("rel", "stylesheet"));
        div.addAttribute(new Attribute("type", "text/css"));
        if (value != null) {
            div.addAttribute(new Attribute("th:href", "@{" + value + "}"));
        }
        htmlElement.addElement(div);
    }

    protected void addStaticJavaScript(HtmlElement htmlElement, String value) {
        HtmlElement div = new HtmlElement("script");
        div.addAttribute(new Attribute("charset", "utf-8"));
        if (value != null) {
            div.addAttribute(new Attribute("th:src", "@{" + value + "}"));
        }
        div.addAttribute(new Attribute("type", "text/javascript"));
        htmlElement.addElement(div);
    }

    protected void addStaticReplace(HtmlElement htmlElement, String value) {
        HtmlElement div = new HtmlElement("div");
        div.addAttribute(new Attribute("th:replace", value));
        htmlElement.addElement(div);
    }

    protected Map<String,HtmlElement> generateHtmlBody() {
        Map<String,HtmlElement> answer = new HashMap<>();
        HtmlElement body = new HtmlElement("body");
        HtmlElement out = addDivWithClassToParent(body, "container");
        answer.put("body", body);
        answer.put("out", out);
        switch (htmlMapGeneratorConfiguration.getLoadingFrameType()) {
            case "pop":
                addClassNameToElement(out, "popContainer");
                body.addAttribute(new Attribute("style", "background-color: #FFFFFF;"));
                break;
            case "inner":
                addClassNameToElement(out, "innerContainer");
                body.addAttribute(new Attribute("style", "background-color: #FFFFFF;"));
                break;
            default:
                addClassNameToElement(out, "outContainer");
        }
        HtmlElement inner = addDivWithClassToParent(out, "icontainer");
        HtmlElement content = addDivWithClassToParent(inner, "content");
        HtmlElement contentHeader = addDivWithClassToParent(content, "content-header");
        HtmlElement headerText = new HtmlElement("span");
        headerText.addElement(new TextElement(StringUtility.remarkLeft(introspectedTable.getRemarks())));
        contentHeader.addElement(headerText);
        answer.put("content",content);
        return answer;
    }

    protected HtmlElement generateHtmlInput(IntrospectedColumn baseColumn, boolean isHidden, boolean isTextArea) {
        return generateHtmlInput(baseColumn.getJavaProperty(), isHidden, isTextArea);
    }

    protected HtmlElement generateHtmlInput(String name, boolean isHidden, boolean isTextArea) {
        String type = isTextArea ? "textarea" : "input";
        HtmlElement input = new HtmlElement(type);
        input.addAttribute(new Attribute("id", name));
        input.addAttribute(new Attribute("name", name));
        if (isHidden) {
            input.addAttribute(new Attribute("type", "hidden"));
        } else {
            input.addAttribute(new Attribute("type", "text"));
        }
        return input;
    }

    protected String thymeleafValue(IntrospectedColumn baseColumn, String entityName) {
        StringBuilder sb = new StringBuilder();
        sb.append("${").append(entityName).append("?.").append(baseColumn.getJavaProperty());
        if ("DATE".equalsIgnoreCase(baseColumn.getJdbcTypeName())) {
            sb.append("!=null?#dates.format(").append(entityName).append(".");
            sb.append(baseColumn.getJavaProperty()).append(",'yyyy-MM-dd'):''}");
        } else if ("TIME".equalsIgnoreCase(baseColumn.getJdbcTypeName())) {
            sb.append("!=null?#dates.format(").append(entityName).append(".");
            sb.append(baseColumn.getJavaProperty()).append(",'HH:mm:ss'):''}");
        } else if ("TIMESTAMP".equalsIgnoreCase(baseColumn.getJdbcTypeName())) {
            sb.append("!=null?#dates.format(").append(entityName).append(".");
            sb.append(baseColumn.getJavaProperty()).append(",'yyyy-MM-dd HH:mm:ss'):''}");
        } else {
            if ("version".equals(baseColumn.getJavaProperty())) {
                sb.append("}?:1");
            } else {
                sb.append("}?:_");
            }
        }
        return sb.toString();
    }

    protected void addLocalStaticResource(HtmlElement head) {
        String p = htmlMapGeneratorConfiguration.getTargetPackage();
        if (p.lastIndexOf(".") > 0) {
            p = p.substring(0, p.lastIndexOf("."));
        }
        addStaticStyleSheet(head, GenerateUtils.getLocalCssFilePath(p, p));
        addStaticJavaScript(head, GenerateUtils.getLocalJsFilePath(p, p));
    }

    //TODO 计划第一次生成js文件，并初始化
    protected boolean isEntityTypeJsFileExist() {
        String filePath = GenerateUtils.getLocalJsFilePath(htmlMapGeneratorConfiguration.getTargetProject(), getEntityType().getShortName().toLowerCase());

        return false;
    }

    protected Document getDocument() {
        return document;
    }

    protected IntrospectedTable getIntrospectedTable() {
        return introspectedTable;
    }

    protected HtmlElement addDivWithClassToParent(HtmlElement parent, String className) {
        HtmlElement div = new HtmlElement("div");
        if (!className.isEmpty()) {
            addClassNameToElement(div, className);
        }
        parent.addElement(div);
        return div;
    }

    protected HtmlElement addFormWithClassToParent(HtmlElement parent, String className) {
        HtmlElement from = new HtmlElement("form");
        addClassNameToElement(from, className);
        parent.addElement(from);
        return from;
    }

    protected void addClassNameToElement(HtmlElement element, String className) {
        boolean classExist = false;
        for (Attribute attribute : element.getAttributes()) {
            if ("class".equals(attribute.getName())) {
                String[] classNames = attribute.getValue().split(" ");
                List<String> listClassNames = new ArrayList<>(Arrays.asList(classNames));
                if (!listClassNames.contains(className)) {
                    listClassNames.add(className);
                    String v = String.join(" ", listClassNames);
                    attribute.setValue(v);
                }
                classExist = true;
            }
        }
        if (!classExist) {
            element.addAttribute(new Attribute("class", className));
        }
    }

    protected List<HtmlElement> getElementByClassName(String className) {
        List<HtmlElement> answer = new ArrayList<>();
        for (VisitableElement element : document.getRootElement().getAllElements()) {
            if (element instanceof HtmlElement) {
                HtmlElement htmlElement = (HtmlElement) element;
                if (htmlElement.getAttributes().size() > 0) {
                    for (Attribute attribute : htmlElement.getAttributes()) {
                        if ("class".equals(attribute.getName())) {
                            if (attribute.getValue() != null) {
                                if (Arrays.asList(attribute.getValue().split(" ")).contains(className)) {
                                    answer.add((HtmlElement) element);
                                }
                            }
                        }
                    }
                }
            }
        }
        return answer;
    }

    /*按配置列数分割总列数*/
    protected Map<Integer, List<IntrospectedColumn>> getHtmlRows(List<IntrospectedColumn> baseColumns) {
        int pageColumnsConfig = getPageColumnsConfig();
        Map<Integer, List<IntrospectedColumn>> introspectedColumnRows = new HashMap<>();
        int rowIndex = 1, colIndex = 1;
        for (IntrospectedColumn baseColumn : baseColumns) {
            if (baseColumn.getLength() > 255) {
                if (introspectedColumnRows.get(rowIndex) != null) {
                    rowIndex = rowIndex + 1;
                }
                List<IntrospectedColumn> tmp = new ArrayList<>();
                tmp.add(baseColumn);
                introspectedColumnRows.put(rowIndex, tmp);
                rowIndex = rowIndex + 1;
            } else {
                if (introspectedColumnRows.get(rowIndex) != null && introspectedColumnRows.get(rowIndex).size() < pageColumnsConfig) {
                    introspectedColumnRows.get(rowIndex).add(baseColumn);
                    if (introspectedColumnRows.get(rowIndex).size() == pageColumnsConfig) {
                        rowIndex = rowIndex + 1;
                    }
                } else {
                    List<IntrospectedColumn> tmp = new ArrayList<>();
                    tmp.add(baseColumn);
                    introspectedColumnRows.put(rowIndex, tmp);
                    if (tmp.size() == pageColumnsConfig) {
                        rowIndex = rowIndex + 1;
                    }
                }
            }
        }
        return introspectedColumnRows;
    }

    protected List<IntrospectedColumn> getColumnsExceptBlock() {
        return Stream.of(introspectedTable.getPrimaryKeyColumns().stream()
                        , introspectedTable.getBaseColumns().stream()).flatMap(Function.identity())
                .collect(Collectors.toList());
    }

    protected void addSubjectInput(HtmlElement parent) {
        boolean include = false;
        for (IntrospectedColumn introspectedColumn : getColumnsExceptBlock()) {
            if (input_subject_id.equals(introspectedColumn.getJavaProperty())) {
                include = true;
            }
        }
        if (!include) {
            HtmlElement input = new HtmlElement("input");
            input.addAttribute(new Attribute("id", input_subject_id));
            input.addAttribute(new Attribute("name", input_subject_id));
            input.addAttribute(new Attribute("type", "hidden"));
            input.addAttribute(new Attribute("value", introspectedTable.getRemarks()));
            parent.addElement(input);
        }
    }

    protected FullyQualifiedJavaType getEntityType() {
        return entityType;
    }

    protected void setEntityType(FullyQualifiedJavaType entityType) {
        this.entityType = entityType;
    }

    protected HtmlElement addButton(HtmlElement parent, String id, String text) {
        HtmlElement btn = new HtmlElement("button");
        btn.addAttribute(new Attribute("type", "button"));
        btn.addAttribute(new Attribute("id", id));
        if (text != null) {
            btn.addElement(new TextElement(text));
        }
        parent.addElement(btn);
        return btn;
    }

    protected HtmlElement generateToolBar(HtmlElement parent) {
        String config = getHtmlBarPositionConfig();
        if (HtmlConstants.HTML_KEY_WORD_TOP.equals(config)) {
            return addDivWithClassToParent(parent, "breadcrumb _top");
        } else if (HtmlConstants.HTML_KEY_WORD_BOTTOM.equals(config)) {
            return addDivWithClassToParent(parent, "breadcrumb _footer");
        } else {
            return addDivWithClassToParent(parent, "breadcrumb _footer");
        }
    }

    protected HtmlElement addJavaScriptFragment(HtmlElement parent) {
        HtmlElement js = new HtmlElement("script");
        js.addAttribute(new Attribute("type", "text/javascript"));
        parent.addElement(js);
        return js;
    }
}