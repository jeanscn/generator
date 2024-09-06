package org.mybatis.generator.codegen.mybatis3.htmlmapper.document;

import com.vgosoft.core.constant.enums.core.EntityAbstractParentEnum;
import com.vgosoft.tool.core.VArrayUtil;
import com.vgosoft.tool.core.VStringUtil;
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
import org.mybatis.generator.custom.ConstantsUtil;
import org.mybatis.generator.custom.HtmlDocumentTypeEnum;

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

    protected final String btn_print_id = "btn_print";
    protected final String input_subject_id = "subject";

    protected final HtmlGeneratorConfiguration htmlGeneratorConfiguration;

    public AbstractThymeleafHtmlDocumentGenerator(GeneratorInitialParameters generatorInitialParameters, Document document, HtmlGeneratorConfiguration htmlGeneratorConfiguration) {
        super(generatorInitialParameters.getContext(), generatorInitialParameters.getIntrospectedTable(), generatorInitialParameters.getWarnings(), generatorInitialParameters.getProgressCallback(),htmlGeneratorConfiguration);
        this.document = document;
        this.htmlGeneratorConfiguration = htmlGeneratorConfiguration;
    }
    public int getPageColumnsConfig(int maxColumns) {
        int c = htmlGeneratorConfiguration.getLayoutDescriptor().getPageColumnsNum();
        if (c > maxColumns) {
            c = maxColumns;
        } else if (c <= 0) {
            c = 1;
        }
        if (maxColumns % c == 0) {
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
        HtmlElement title = new HtmlElement("title");
        title.addElement(new TextElement(getHtmlTitle()));
        head.addElement(title);
        addStaticReplace(head, "subpages/webjarsPluginsRequired2.html::baseRequired('" + introspectedTable.getRemarks(true) + "')");
        addStaticReplace(head, "subpages/webjarsPluginsRequired2.html::jQueryRequired");
        return head;
    }

    protected Map<String, HtmlElement> generateHtmlBody() {
        Map<String, HtmlElement> answer = new HashMap<>();
        HtmlElement body = new HtmlElement("body");
        answer.put("body", body);
        HtmlElement out = addDivWithClassToParent(body, "container");
        out.addAttribute(new Attribute("id", "container"));
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
        HtmlElement content = null;
        String docTitle = getDocTitle();
        if (htmlGeneratorConfiguration.getType().equals(HtmlDocumentTypeEnum.EDITABLE)) {
            HtmlElement inner = addDivWithClassToParent(out, "icontainer");
            content = addDivWithClassToParent(inner, "content");
            if (VStringUtil.stringHasValue(docTitle)) {
                HtmlElement contentHeader = addDivWithClassToParent(content, "content-header");
                HtmlElement headerText = new HtmlElement("span");
                headerText.addElement(new TextElement(docTitle));
                contentHeader.addElement(headerText);
            }
        }else if(htmlGeneratorConfiguration.getType().equals(HtmlDocumentTypeEnum.PRINT)) {
            content = addDivWithIdToParent(out, "printArea");
            HtmlElement div = addDivWithClassToParent(content, "print-title");
            if (VStringUtil.stringHasValue(docTitle)) {
                addDivWithClassToParent(div, "title").addElement(new TextElement(docTitle));
            }
            HtmlElement div1 = new HtmlElement("div");
            div1.addAttribute(new Attribute("id", "qrcode"));
            div.addElement(div1);
        }else if(htmlGeneratorConfiguration.getType().equals(HtmlDocumentTypeEnum.VIEWONLY)) {
            content = addDivWithIdToParent(out, "viewArea");
            if (VStringUtil.stringHasValue(docTitle)) {
                HtmlElement h2 = new HtmlElement("H2");
                h2.addElement(new TextElement(docTitle));
                content.addElement(h2);
            }
        }
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
    protected Map<Integer, List<IntrospectedColumn>> getHtmlRows(List<IntrospectedColumn> baseColumns,int maxColumns) {
        int pageColumnsConfig = getPageColumnsConfig(maxColumns);
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
            HtmlElement subject = generateHtmlInput(input_subject_id, true, false, true, true);
            String title = getDocTitle();
            if (VStringUtil.stringHasValue(title)) {
                subject.addAttribute(new Attribute("th:value", title));
            }
            parent.addElement(subject);
        }
    }

    protected HtmlElement generateToolBar(HtmlElement parent) {
        String config = getHtmlBarPositionConfig();
        HtmlElement div;
        if (HTML_KEY_WORD_TOP.equals(config)) {
            div = addDivWithClassToParent(parent, "breadcrumb","_top");
        } else if (HTML_KEY_WORD_BOTTOM.equals(config)) {
            div = addDivWithClassToParent(parent, "breadcrumb","_footer");
        } else {
            div = addDivWithClassToParent(parent, "breadcrumb","_footer");
        }
        div.addAttribute(new Attribute("id", "breadcrumb_footer"));
        return div;
    }

    protected boolean isIgnore(IntrospectedColumn introspectedColumn, VOModelGeneratorConfiguration configuration) {
        List<String> allFields = new ArrayList<>(EntityAbstractParentEnum.ABSTRACT_PERSISTENCE_LOCK_ENTITY.fields());
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

    protected HtmlElement addLayButton(HtmlElement parent, String id, String text, String unicode) {
        HtmlElement btn = addHtmlButton(parent, id, text, "layui-btn layui-btn-sm");
        addLayIconFont(btn, unicode);
        return btn;
    }

    protected void addLayIconFont(HtmlElement parent, String unicode) {
        if (VStringUtil.stringHasValue(unicode)) {
            HtmlElement icon = addIconToParent(parent, "layui-icon");
            icon.addElement(new TextElement(unicode));
        }
    }

    protected void drawRtfContentDiv(String entityKey, IntrospectedColumn introspectedColumn, HtmlElement inputInline) {
        HtmlElement htmlElement = addDivWithClassToParent(inputInline, "rtf-content");
        htmlElement.addAttribute(new Attribute("for", introspectedColumn.getJavaProperty()));
        htmlElement.addAttribute(new Attribute("th:utext", thymeleafValue(introspectedColumn, entityKey)));
        if (this.isReadonly(introspectedColumn)) {
            addCssClassToElement(htmlElement, "readonly");
        }
        //追加样式css
        HtmlElementDescriptor htmlElementDescriptor = getHtmlElementDescriptor(introspectedColumn);
        if (htmlElementDescriptor != null && htmlElementDescriptor.getElementCss() != null) {
            addCssStyleToElement(htmlElement, htmlElementDescriptor.getElementCss());
        }
    }

    protected void addCustomCss(HtmlElement head, HtmlGeneratorConfiguration htmlGeneratorConfiguration) {
        HtmlLayoutDescriptor layoutDescriptor = htmlGeneratorConfiguration.getLayoutDescriptor();
        if (layoutDescriptor.getBorderWidth() != 1 || !layoutDescriptor.getBorderColor().equals(ConstantsUtil.HTML_BORDER_COLOR_DEFAULT)) {
            HtmlElement style = new HtmlElement("style");
            style.addAttribute(new Attribute("type", "text/css"));
            String styleStr = ".layui-table td,.layui-table th,.layui-table-col-set,.layui-table-fixed-r,.layui-table-grid-down,.layui-table-header,.layui-table-page,.layui-table-tips-main,.layui-table-tool,.layui-table-total,.layui-table-view,.layui-table[lay-skin=line],.layui-table[lay-skin=row] {\n";
            styleStr += "            border-width: " + (layoutDescriptor.getBorderWidth() == 0 ? 0 : layoutDescriptor.getBorderWidth() + "px") + ";\n";
            styleStr += "            border-color: " + layoutDescriptor.getBorderColor() + ";\n";
            styleStr += "  i          border-style: solid;\n";
            styleStr += "       }";
            style.addElement(new TextElement(styleStr));
            head.addElement(style);
        }
    }

    protected String getHtmlTitle() {
        if (htmlGeneratorConfiguration==null || htmlGeneratorConfiguration.getTitle() == null || "none".equalsIgnoreCase(htmlGeneratorConfiguration.getTitle())) {
            return introspectedTable.getRemarks(true);
        }else{
            return htmlGeneratorConfiguration.getTitle();
        }
    }

    protected String getDocTitle() {
        if (htmlGeneratorConfiguration==null || htmlGeneratorConfiguration.getTitle() == null) {
            return introspectedTable.getRemarks(true);
        }else if("none".equalsIgnoreCase(htmlGeneratorConfiguration.getTitle())){
            return "";
        }else{
            return htmlGeneratorConfiguration.getTitle();
        }
    }
}
