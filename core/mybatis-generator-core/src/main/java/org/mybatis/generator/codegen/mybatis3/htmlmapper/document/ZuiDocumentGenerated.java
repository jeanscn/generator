package org.mybatis.generator.codegen.mybatis3.htmlmapper.document;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.html.Attribute;
import org.mybatis.generator.api.dom.html.Document;
import org.mybatis.generator.api.dom.html.HtmlElement;
import org.mybatis.generator.api.dom.html.TextElement;
import org.mybatis.generator.codegen.GeneratorInitialParameters;
import org.mybatis.generator.codegen.mybatis3.htmlmapper.GenerateUtils;
import org.mybatis.generator.config.HtmlGeneratorConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author <a href="mailto:cjj@vip.sina.com">ChenJJ</a>
 *  2020-07-20 06:22
 * @version 3.0
 */
public class ZuiDocumentGenerated extends AbstractThymeleafHtmlDocumentGenerator {
    private final Document document;

    private final HtmlElement rootElement;

    private final HtmlElement head;

    private final Map<String,HtmlElement>  body;

    private HtmlElement content;

    public ZuiDocumentGenerated(GeneratorInitialParameters generatorInitialParameters, Document document, HtmlGeneratorConfiguration htmlGeneratorConfiguration) {
        super(generatorInitialParameters,document, htmlGeneratorConfiguration);
        this.document = document;
        this.rootElement = document.getRootElement();
        this.head = generateZuiHead();
        this.body = generateHtmlBody();
    }

    @Override
    public boolean htmlMapDocumentGenerated() {
        rootElement.addElement(head);
        rootElement.addElement(body.get("body"));
        document.setRootElement(rootElement);
        this.content = body.get("content");
        HtmlElement form = generateZuiForm(content);
        generateZuiToolBar(content);
        return true;
    }

    private HtmlElement generateZuiHead() {
        HtmlElement head = generateHtmlHead();
        addStaticReplace(head, "subpages/webjarsPluginsRequired2.html::zuiRequired");
        addStaticReplace(head, "subpages/webjarsPluginsRequired2.html::zuiForm");
        addLocalStaticResource(head);
        return head;
    }

    private HtmlElement generateZuiForm(HtmlElement parent) {
        HtmlElement form = addFormWithClassToParent(parent, "form-horizontal");
        List<IntrospectedColumn> columns = Stream.of(introspectedTable.getPrimaryKeyColumns().stream()
                , introspectedTable.getBaseColumns().stream()).flatMap(Function.identity())
                .collect(Collectors.toList());
        List<IntrospectedColumn> hiddenColumns = new ArrayList<>();
        List<IntrospectedColumn> displayColumns = new ArrayList<>();
        for (IntrospectedColumn baseColumn : columns) {
            if (GenerateUtils.isHiddenColumn(introspectedTable,baseColumn, htmlGeneratorConfiguration)) {
                hiddenColumns.add(baseColumn);
            } else {
                displayColumns.add(baseColumn);
            }
        }
        int pageColumnsConfig = getPageColumnsConfig();
        Map<Integer, List<IntrospectedColumn>> baseColumnsRows = getHtmlRows(displayColumns);
        String entityKey = GenerateUtils.getEntityKeyStr(introspectedTable);
        HtmlElement caption = addDivWithClassToParent(form,"form-title");
        caption.addElement(new TextElement(introspectedTable.getRemarks(true)));
        /*计算响应式宽度*/
        String colWidth = "col-sm-" + 12 / pageColumnsConfig;
        for (List<IntrospectedColumn> value : baseColumnsRows.values()) {
            /*行*/
            HtmlElement row = addDivWithClassToParent(form, "row");
            /*列*/
            for (IntrospectedColumn introspectedColumn : value) {
                HtmlElement td;
                if (value.size() == 1 && value.get(0).getLength() > 255) {
                    td = addDivWithClassToParent(row, "col-sm-12");
                } else {
                    td = addDivWithClassToParent(row, colWidth);
                }
                //输入框
                HtmlElement ictrl = addDivWithClassToParent(td, "input-control");
                addCssClassToElement(ictrl, "has-label-left");
                drawInput(introspectedColumn, entityKey, ictrl);
                drawLabel(introspectedColumn, ictrl);
            }
        }
        return form;
    }

    private void generateZuiToolBar(HtmlElement parent) {
        HtmlElement toolBar = generateToolBar(parent);
        HtmlElement btnClose = addZuiButton(toolBar, btn_close_id, "关闭", "icon-times");
        HtmlElement btnSubmit = addZuiButton(toolBar, btn_submit_id, "保存", "icon-check");
        String config = getHtmlBarPositionConfig();
        if (!HTML_KEY_WORD_TOP.equals(config)) {
            addCssClassToElement(btnClose, "footer-btn");
            addCssClassToElement(btnSubmit, "footer-btn");
        }
    }
    private HtmlElement addZuiButton(HtmlElement parent, String id, String text, String unicode) {
        HtmlElement btn = addHtmlButton(parent, id, null,"btn btn-sm");
        addZuiIconFont(btn, unicode);
        if (text != null) {
            btn.addElement(new TextElement(text));
        }
        return btn;
    }

    private void addZuiIconFont(HtmlElement parent, String unicode) {
        HtmlElement icon = new HtmlElement("i");
        addCssClassToElement(icon, "icon " + unicode);
        parent.addElement(icon);
    }

    private void drawLabel(IntrospectedColumn introspectedColumn, HtmlElement parent) {
        HtmlElement label = new HtmlElement("label");
        addCssClassToElement(label, "input-control-label-left");
        label.addAttribute(new Attribute("for", introspectedColumn.getJavaProperty()));
        label.addElement(new TextElement(introspectedColumn.getRemarks(true)));
        parent.addElement(label);
    }

    private void drawInput(IntrospectedColumn introspectedColumn, String entityKey, HtmlElement parent) {
        HtmlElement input = generateHtmlInput(introspectedColumn, false,false);
        input.addAttribute(new Attribute("th:value", thymeleafValue(introspectedColumn, entityKey)));
        addCssClassToElement(input, "form-control");
        parent.addElement(input);
        if (GenerateUtils.isWorkflowInstance(introspectedTable)) {
            HtmlElement div = addDivWithClassToParent(parent, "oas-form-item-read");
            div.addAttribute(new Attribute("th:text", thymeleafValue(introspectedColumn, entityKey)));
        }
    }

}
