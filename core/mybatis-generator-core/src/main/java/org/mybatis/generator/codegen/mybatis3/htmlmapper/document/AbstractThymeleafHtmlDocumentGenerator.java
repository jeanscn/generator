package org.mybatis.generator.codegen.mybatis3.htmlmapper.document;

import com.vgosoft.core.constant.enums.core.EntityAbstractParentEnum;
import com.vgosoft.tool.core.VArrayUtil;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.html.Attribute;
import org.mybatis.generator.api.dom.html.Document;
import org.mybatis.generator.api.dom.html.HtmlElement;
import org.mybatis.generator.api.dom.html.TextElement;
import org.mybatis.generator.codegen.GeneratorInitialParameters;
import org.mybatis.generator.codegen.mybatis3.htmlmapper.AbstractThymeleafHtmlGenerator;
import org.mybatis.generator.codegen.mybatis3.htmlmapper.GenerateUtils;
import org.mybatis.generator.codegen.mybatis3.htmlmapper.HtmlConstant;
import org.mybatis.generator.config.*;

import java.util.*;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

/**
 * @author <a href="mailto:cjj@vip.sina.com">ChenJJ</a>
 * 2020-07-23 00:57
 * @version 3.0
 */
public abstract class AbstractThymeleafHtmlDocumentGenerator extends AbstractThymeleafHtmlGenerator implements HtmlDocumentGenerator, HtmlConstant {
    private final Document document;
    protected final String btn_submit_id = "btn_save";
    protected final String btn_close_id = "btn_close";
    protected final String btn_reset_id = "btn_reset";
    protected final String input_subject_id = "subject";

    protected final HtmlGeneratorConfiguration htmlGeneratorConfiguration;

    public AbstractThymeleafHtmlDocumentGenerator(GeneratorInitialParameters generatorInitialParameters, Document document, HtmlGeneratorConfiguration htmlGeneratorConfiguration) {
        super(generatorInitialParameters.getContext(), generatorInitialParameters.getIntrospectedTable(), generatorInitialParameters.getWarnings(), generatorInitialParameters.getProgressCallback(),htmlGeneratorConfiguration);
        this.document = document;
        this.htmlGeneratorConfiguration = htmlGeneratorConfiguration;
    }
    public int getPageColumnsConfig() {
        int c = htmlGeneratorConfiguration.getLayoutDescriptor().getPageColumnsNum();
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
        return htmlGeneratorConfiguration.getLayoutDescriptor().getBarPosition();
    }

    public abstract boolean htmlMapDocumentGenerated();

    protected HtmlElement generateHtmlHead() {
        HtmlElement head = new HtmlElement("head");
        addStaticReplace(head, "subpages/webjarsPluginsRequired2.html::baseRequired('" + introspectedTable.getRemarks(true) + "')");
        addStaticReplace(head, "subpages/webjarsPluginsRequired2.html::jQueryRequired");
        return head;
    }

    protected Map<String, HtmlElement> generateHtmlBody() {
        Map<String, HtmlElement> answer = new HashMap<>();
        HtmlElement body = new HtmlElement("body");
        HtmlElement out = addDivWithClassToParent(body, "container");
        answer.put("body", body);
        answer.put("out", out);
        HtmlLayoutDescriptor layoutDescriptor = htmlGeneratorConfiguration.getLayoutDescriptor();
        switch (layoutDescriptor.getLoadingFrameType()) {
            case "pop":
                addCssClassToElement(out, "popContainer");
                body.addAttribute(new Attribute("style", "background-color: #FFFFFF;"));
                break;
            case "inner":
                addCssClassToElement(out, "innerContainer");
                body.addAttribute(new Attribute("style", "background-color: #FFFFFF;"));
                break;
            default:
                addCssClassToElement(out, "outContainer");
        }
        HtmlElement inner = addDivWithClassToParent(out, "icontainer");
        HtmlElement content = addDivWithClassToParent(inner, "content");
        HtmlElement contentHeader = addDivWithClassToParent(content, "content-header");
        HtmlElement headerText = new HtmlElement("span");
        headerText.addElement(new TextElement(introspectedTable.getRemarks(true)));
        contentHeader.addElement(headerText);
        answer.put("content", content);
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
        if (baseColumn.isJava8TimeColumn()) {
            sb.append("!=null?#temporals.format(").append(entityName).append(".");
        }else if(baseColumn.isJDBCTimeColumn() || baseColumn.isJDBCTimeStampColumn() || baseColumn.isJDBCDateColumn()){
            sb.append("!=null?#dates.format(").append(entityName).append(".");
        } else {
            if ("version".equals(baseColumn.getJavaProperty())) {
                sb.append("}?:1");
            } else {
                sb.append("}?:_");
            }
            return sb.toString();
        }
        if ("DATE".equalsIgnoreCase(baseColumn.getJdbcTypeName())) {
            sb.append(baseColumn.getJavaProperty()).append(",'yyyy-MM-dd'):''}");
        } else if ("TIME".equalsIgnoreCase(baseColumn.getJdbcTypeName())) {
            sb.append(baseColumn.getJavaProperty()).append(",'HH:mm:ss'):''}");
        } else if ("TIMESTAMP".equalsIgnoreCase(baseColumn.getJdbcTypeName()) || "DATETIME".equalsIgnoreCase(baseColumn.getJdbcTypeName())) {
            sb.append(baseColumn.getJavaProperty()).append(",'yyyy-MM-dd HH:mm:ss'):''}");
        }
        return sb.toString();
    }

    protected void addLocalStaticResource(HtmlElement head) {
        String p = introspectedTable.getTableConfiguration().getHtmlBasePath();
        addStaticThymeleafStyleSheet(head, GenerateUtils.getLocalCssFilePath(p, p));
        //addStaticJavaScript(head, GenerateUtils.getLocalJsFilePath(p, p));
    }

    protected HtmlElement addFormWithClassToParent(HtmlElement parent, String className) {
        HtmlElement from = new HtmlElement("form");
        addCssClassToElement(from, className);
        parent.addElement(from);
        return from;
    }

    /*按配置列数分割总列数*/
    protected Map<Integer, List<IntrospectedColumn>> getHtmlRows(List<IntrospectedColumn> baseColumns) {
        int pageColumnsConfig = getPageColumnsConfig();
        Map<Integer, List<IntrospectedColumn>> introspectedColumnRows = new HashMap<>();
        int rowIndex = 1, colIndex = 1;
        for (IntrospectedColumn baseColumn : baseColumns) {
            if (baseColumn.getLength() > 255 || this.htmlGeneratorConfiguration.getLayoutDescriptor().getExclusiveColumns().contains(baseColumn.getActualColumnName())) {
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

    protected void addSubjectInput(HtmlElement parent) {
        if (introspectedTable.getNonBLOBColumns().stream()
                .noneMatch(c -> input_subject_id.equals(c.getJavaProperty()))) {
            HtmlElement input = new HtmlElement("input");
            input.addAttribute(new Attribute("id", input_subject_id));
            input.addAttribute(new Attribute("name", input_subject_id));
            input.addAttribute(new Attribute("type", "hidden"));
            input.addAttribute(new Attribute("value", introspectedTable.getRemarks(true)));
            parent.addElement(input);
        }
    }

    protected HtmlElement generateToolBar(HtmlElement parent) {
        String config = getHtmlBarPositionConfig();
        if (HTML_KEY_WORD_TOP.equals(config)) {
            return addDivWithClassToParent(parent, "breadcrumb _top");
        } else if (HTML_KEY_WORD_BOTTOM.equals(config)) {
            return addDivWithClassToParent(parent, "breadcrumb _footer");
        } else {
            return addDivWithClassToParent(parent, "breadcrumb _footer");
        }
    }

    protected boolean isIgnore(IntrospectedColumn introspectedColumn, VOModelGeneratorConfiguration configuration) {
        List<String> allFields = new ArrayList<>(EntityAbstractParentEnum.ABSTRACT_PERSISTENCE_LOCK_ENTITY.fields());
        allFields.add("tenantId");
        String property = configuration.getProperty(PropertyRegistry.ELEMENT_IGNORE_COLUMNS);
        boolean ret = false;
        if (stringHasValue(property)) {
            ret = VArrayUtil.contains(property.split(","), introspectedColumn.getActualColumnName());
        }
        return ret || allFields.contains(introspectedColumn.getJavaProperty());
    }


    protected HtmlElementDescriptor getHtmlElementDescriptor(IntrospectedColumn introspectedColumn) {
        return this.htmlGeneratorConfiguration.getElementDescriptors().stream()
                .filter(d->d.getColumn().getActualColumnName().equals(introspectedColumn.getActualColumnName()))
                .findFirst().orElse(null);
    }
}
