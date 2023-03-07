package org.mybatis.generator.custom.htmlGenerator;

import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.html.Attribute;
import org.mybatis.generator.api.dom.html.Document;
import org.mybatis.generator.api.dom.html.HtmlElement;
import org.mybatis.generator.api.dom.html.TextElement;
import org.mybatis.generator.codegen.HtmlConstants;
import org.mybatis.generator.config.HtmlGeneratorConfiguration;
import org.mybatis.generator.custom.pojo.HtmlElementDescriptor;
import org.mybatis.generator.internal.util.StringUtility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author <a href="mailto:cjj@vip.sina.com">ChenJJ</a>
 * 2020-07-20 06:22
 * @version 3.0
 */
public class LayuiDocumentGenerated extends AbsHtmlDocumentGenerator {

    private final IntrospectedTable introspectedTable;

    private final Document document;

    private final HtmlElement rootElement;

    private final HtmlElement head;

    private final Map<String, HtmlElement> body;

    public LayuiDocumentGenerated(Document document, IntrospectedTable introspectedTable, HtmlGeneratorConfiguration htmlGeneratorConfiguration) {
        super(document, introspectedTable, htmlGeneratorConfiguration);
        this.introspectedTable = introspectedTable;
        this.document = document;
        this.rootElement = document.getRootElement();
        this.head = generateLayuiHead();
        this.body = generateHtmlBody();
    }

    @Override
    public boolean htmlMapDocumentGenerated() {
        rootElement.addElement(head);
        rootElement.addElement(body.get("body"));
        document.setRootElement(rootElement);
        //List<HtmlElement> elements = getElementByClassName("content");
        HtmlElement content = body.get("content");
        HtmlElement form = generateForm(content);
        /*标题区域*/
        addSubjectInput(form);
        /*表单验证button*/
        addVerifyButton(form);
        /* 查看状态*/
        // if (!GenerateUtils.isWorkflowInstance(introspectedTable)) {
        HtmlElement viewStatus = generateHtmlInput("viewStatus", true, false);
        viewStatus.addAttribute(new Attribute("th:value", "${viewStatus}?:1"));
        content.addElement(viewStatus);
        // }
        generateLayuiToolBar(content);
        addLayJavaScriptFragment(body.get("body"));
        return true;
    }

    private void addVerifyButton(HtmlElement parent) {
        String form_verify_id = "btn_form_verify";
        HtmlElement btn = new HtmlElement("button");
        btn.addAttribute(new Attribute("type", "submit"));
        btn.addAttribute(new Attribute("id", form_verify_id));
        btn.addAttribute(new Attribute("lay-submit", ""));
        btn.addAttribute(new Attribute("lay-filter", form_verify_id));
        btn.addAttribute(new Attribute("style", "display: none;"));
        parent.addElement(btn);
    }

    private HtmlElement generateLayuiHead() {
        HtmlElement head = generateHtmlHead();
        addStaticReplace(head, "subpages/webjarsPluginsRequired2.html::layuiRequired");
        addStaticReplace(head, "subpages/webjarsPluginsRequired2.html::layuiForm");
        long count = introspectedTable.getBaseColumns().stream().filter(GenerateUtils::isLongVarchar).count();
        if (count > 0) {
            addStaticReplace(head, "subpages/webjarsPluginsRequired2.html::neditorRequired");
        }
        addStaticReplace(head, "subpages/webjarsPluginsRequired2.html::contextMemuOnly");
        addStaticJavaScript(head, "/webjars/plugins/js/mainform.min.js");
        addLocalStaticResource(head);
        return head;
    }

    private HtmlElement generateForm(HtmlElement parent) {
        HtmlElement form = addFormWithClassToParent(parent, "layui-form");
        List<IntrospectedColumn> columns = Stream.of(introspectedTable.getPrimaryKeyColumns().stream()
                        , introspectedTable.getBaseColumns().stream()).flatMap(Function.identity())
                .collect(Collectors.toList());
        List<IntrospectedColumn> hiddenColumns = new ArrayList<>();
        List<IntrospectedColumn> displayColumns = new ArrayList<>();
        Map<String, IntrospectedColumn> waitRenderMap = new HashMap<>();
        for (IntrospectedColumn baseColumn : columns) {
            if (introspectedTable.getRules().isGenerateVO()) {
                if (isIgnore(baseColumn
                        , introspectedTable.getTableConfiguration().getVoGeneratorConfiguration().getVoModelConfiguration())
                        && !baseColumn.isIdentity()
                        && !baseColumn.getActualColumnName().equalsIgnoreCase("version_")) {
                    continue;
                }
            }
            if (GenerateUtils.isHiddenColumn(baseColumn, htmlGeneratorConfiguration)) {
                hiddenColumns.add(baseColumn);
            } else {
                displayColumns.add(baseColumn);
                //指定一些列的默认生成的样式
                for (HtmlElementDescriptor htmlElementDescriptor : htmlGeneratorConfiguration.getElementDescriptors()) {
                    if (htmlElementDescriptor.getName().equals(baseColumn.getActualColumnName())) {
                        htmlElementDescriptor.setColumn(baseColumn);
                        waitRenderMap.put(baseColumn.getActualColumnName().toUpperCase(), baseColumn);
                    }
                }
            }
        }

        int pageColumnsConfig = getPageColumnsConfig();
        Map<Integer, List<IntrospectedColumn>> baseColumnsRows = getHtmlRows(displayColumns);
        String entityKey = GenerateUtils.getEntityKeyStr(introspectedTable);
        List<IntrospectedColumn> rtfColumn = new ArrayList<>();
        if (pageColumnsConfig > 1) {
            /*添加表格*/
            HtmlElement table = new HtmlElement("table");
            table.addAttribute(new Attribute("class", "layui-table"));
            /*HtmlElement caption = new HtmlElement("caption");
            caption.addElement(new TextElement(StringUtility.remarkLeft(introspectedTable.getRemarks())));
            table.addElement(caption);*/
            for (List<IntrospectedColumn> rowIntrospectedColumns : baseColumnsRows.values()) {
                /*行*/
                HtmlElement tr = new HtmlElement("tr");
                table.addElement(tr);
                /*列*/
                for (IntrospectedColumn introspectedColumn : rowIntrospectedColumns) {
                    HtmlElement td = new HtmlElement("td");
                    //label
                    drawLabel(introspectedColumn, td);
                    //input
                    HtmlElement block = addDivWithClassToParent(td, "layui-input-block");
                    if (rowIntrospectedColumns.size() == 1 && rowIntrospectedColumns.get(0).getLength() > 255) {
                        td.addAttribute(new Attribute("colspan", String.valueOf(pageColumnsConfig)));
                    }
                    if (GenerateUtils.isLongVarchar(introspectedColumn)) {
                        rtfColumn.add(introspectedColumn);
                        drawRtfContentDiv(entityKey, introspectedColumn, block);
                    } else {
                        generateHtmlInputComponent(introspectedColumn, entityKey, block, td);
                    }
                    tr.addElement(td);
                }
                /*如果列数小于指定列数，则后面补充空单元格*/
                if (rowIntrospectedColumns.size() < pageColumnsConfig && rowIntrospectedColumns.get(0).getLength() <= 255) {
                    for (int i = pageColumnsConfig - rowIntrospectedColumns.size(); i > 0; i--) {
                        HtmlElement td = new HtmlElement("td");
                        tr.addElement(td);
                    }
                }
            }
            form.addElement(table);
        } else {
//            HtmlElement h2 = new HtmlElement("H2");
//            h2.addElement(new TextElement(StringUtility.remarkLeft(introspectedTable.getRemarks())));
//            form.addElement(h2);
            for (List<IntrospectedColumn> introspectedColumns : baseColumnsRows.values()) {
                /*行*/
                HtmlElement formItem = addDivWithClassToParent(form, "layui-form-item");
                /*列*/
                for (IntrospectedColumn introspectedColumn : introspectedColumns) {
                    //HtmlElement inline = addDivWithClassToParent(formItem, "layui-inline");
                    //label
                    drawLabel(introspectedColumn, formItem);
                    //input
                    HtmlElement inputInline = addDivWithClassToParent(formItem, "layui-input-block");
                    if (GenerateUtils.isLongVarchar(introspectedColumn)) {
                        rtfColumn.add(introspectedColumn);
                        drawRtfContentDiv(entityKey, introspectedColumn, inputInline);
                    } else {
                        generateHtmlInputComponent(introspectedColumn, entityKey, inputInline, formItem);
                    }
                }
            }
        }

        if (hiddenColumns.size() > 0) {
            for (IntrospectedColumn hiddenColumn : hiddenColumns) {
                HtmlElement input = generateHtmlInput(hiddenColumn, true, false);
                input.addAttribute(new Attribute("th:value", thymeleafValue(hiddenColumn, entityKey)));
                form.addElement(input);
            }
        }
        //添加固定隐藏input
        HtmlElement restBasePath = generateHtmlInput("restBasePath", true, false);
        StringBuilder sb = new StringBuilder();
        sb.append("${").append(entityKey).append("?.restBasePath}");
        restBasePath.addAttribute(new Attribute("th:value", sb.toString()));
        form.addElement(restBasePath);
        sb.setLength(0);
        for (IntrospectedColumn introspectedColumn : rtfColumn) {
            HtmlElement htmlElement = generateHtmlInput(introspectedColumn, true, false);
            htmlElement.addAttribute(new Attribute("th:value", thymeleafValue(introspectedColumn, entityKey)));
            form.addElement(htmlElement);
        }
        HtmlElement persistenceBeanName = generateHtmlInput("persistenceBeanName", true, false);
        sb.append("${").append(entityKey).append("?.persistenceBeanName}");
        persistenceBeanName.addAttribute(new Attribute("th:value", sb.toString()));
        form.addElement(persistenceBeanName);
        /*添加工作流内容*/
        if (GenerateUtils.isWorkflowInstance(introspectedTable)) {
            HtmlElement div = new HtmlElement("div");
            div.addAttribute(new Attribute("th:replace", "vgowf/jsp/workflowsubjsp.html::workflowsubjsp"));
            form.addElement(div);
        }
        return form;
    }

    //生成页面dropdownlist、switch、radio、checkbox、date及其它元素
    private void generateHtmlInputComponent(IntrospectedColumn introspectedColumn, String entityKey, HtmlElement parent, HtmlElement td) {
        List<HtmlElementDescriptor> collect = htmlGeneratorConfiguration.getElementDescriptors().stream()
                .filter(t -> t.getName().equals(introspectedColumn.getActualColumnName()))
                .collect(Collectors.toList());
        if (collect.size() > 0) {
            HtmlElementDescriptor htmlElementDescriptor = collect.get(0);
            StringBuilder sb = new StringBuilder();
            switch (htmlElementDescriptor.getTagType().toLowerCase()) {
                case "dropdownlist":
                    HtmlElement element = new HtmlElement("select");
                    element.addAttribute(new Attribute("id", introspectedColumn.getJavaProperty()));
                    element.addAttribute(new Attribute("name", introspectedColumn.getJavaProperty()));
                    element.addAttribute(new Attribute("lay-filter", introspectedColumn.getJavaProperty()));
                    element.addAttribute(new Attribute("th:data-value", thymeleafValue(introspectedColumn, entityKey)));
                    if (StringUtility.stringHasValue(htmlElementDescriptor.getDataUrl())) {
                        element.addAttribute(new Attribute("data-url", htmlElementDescriptor.getDataUrl()));
                    } else {
                        element.addAttribute(new Attribute("data-url", "/system/dict/options/" + introspectedColumn.getJavaProperty()));
                    }
                    HtmlElement option = new HtmlElement("option");
                    option.addAttribute(new Attribute("value", ""));
                    option.addElement(new TextElement("请选择"));
                    element.addElement(option);
                    parent.addElement(element);
                    //读写状态区
                    addClassNameToElement(parent, "oas-form-item-edit");
                    HtmlElement dpRead = addDivWithClassToParent(td, "oas-form-item-read");
                    dpRead.addAttribute(new Attribute("th:text", thymeleafValue(introspectedColumn, entityKey)));
                    //非空验证
                    addElementRequired(introspectedColumn.getActualColumnName(), element);
                    break;
                case "switch":
                    element = new HtmlElement("input");
                    element.addAttribute(new Attribute("id", introspectedColumn.getJavaProperty()));
                    element.addAttribute(new Attribute("name", introspectedColumn.getJavaProperty()));
                    element.addAttribute(new Attribute("type", "checkbox"));
                    element.addAttribute(new Attribute("lay-skin", "switch"));
                    if (htmlElementDescriptor.getDataFormat() != null) {
                        switch (htmlElementDescriptor.getDataFormat()) {
                            case "有无":
                                element.addAttribute(new Attribute("lay-text", "有|无"));
                                break;
                            case "是否":
                                element.addAttribute(new Attribute("lay-text", "是|否"));
                                break;
                            case "性别":
                                element.addAttribute(new Attribute("lay-text", "男|女"));
                                break;
                            default:
                                element.addAttribute(new Attribute("lay-text", "启用|停用"));
                        }
                    }
                    element.addAttribute(new Attribute("value", "1"));
                    element.addAttribute(new Attribute("lay-filter", introspectedColumn.getJavaProperty()));
                    if (htmlElementDescriptor.getDataUrl() != null) {
                        element.addAttribute(new Attribute("data-url", htmlElementDescriptor.getDataUrl()));
                    }
                    sb.setLength(0);
                    sb.append("${").append(entityKey).append("?.");
                    sb.append(introspectedColumn.getJavaProperty()).append("} ne 0");
                    element.addAttribute(new Attribute("th:checked", sb.toString()));
                    parent.addElement(element);
                    //读写状态区
                    addClassNameToElement(parent, "oas-form-item-edit");
                    HtmlElement sRead = addDivWithClassToParent(td, "oas-form-item-read");
                    sb.setLength(0);
                    sb.append("${").append(entityKey).append("?.").append(introspectedColumn.getJavaProperty());
                    if (htmlElementDescriptor.getDataFormat() != null) {
                        switch (htmlElementDescriptor.getDataFormat()) {
                            case "有无":
                                sb.append("} eq 1 ? '有':'无'");
                                break;
                            case "是否":
                                sb.append("} eq 1 ? '是':'否'");
                                break;
                            case "性别":
                                sb.append("} eq 1 ? '男':'女'");
                                break;
                            default:
                                sb.append("} eq 1 ? '启用':'停用'");
                        }
                    }

                    sRead.addAttribute(new Attribute("th:text", sb.toString()));
                    //非空验证
                    addElementRequired(introspectedColumn.getActualColumnName(), element);
                    break;
                case "radio":
                    if (htmlElementDescriptor.getDataFormat() != null) {
                        switch (htmlElementDescriptor.getDataFormat()) {
                            case "sex":
                            case "性别":
                                parent.addElement(drawRadio(introspectedColumn.getJavaProperty(), "男", "男", entityKey));
                                parent.addElement(drawRadio(introspectedColumn.getJavaProperty(), "女", "女", entityKey));
                                break;
                            case "level":
                            case "级别":
                                parent.addElement(drawRadio(introspectedColumn.getJavaProperty(), "1", "1级", entityKey));
                                parent.addElement(drawRadio(introspectedColumn.getJavaProperty(), "2", "2级", entityKey));
                                parent.addElement(drawRadio(introspectedColumn.getJavaProperty(), "3", "3级", entityKey));
                                break;
                            case "true":
                            case "是":
                            case "是否":
                                parent.addElement(drawRadio(introspectedColumn.getJavaProperty(), "是", "是", entityKey));
                                parent.addElement(drawRadio(introspectedColumn.getJavaProperty(), "否", "否", entityKey));
                                break;
                            case "有":
                            case "有无":
                                parent.addElement(drawRadio(introspectedColumn.getJavaProperty(), "有", "有", entityKey));
                                parent.addElement(drawRadio(introspectedColumn.getJavaProperty(), "无", "无", entityKey));
                                break;
                            case "急":
                            case "缓急":
                                parent.addElement(drawRadio(introspectedColumn.getJavaProperty(), "70", "紧急", entityKey));
                                parent.addElement(drawRadio(introspectedColumn.getJavaProperty(), "50", "正常", entityKey));
                                break;
                        }
                    } else {
                        for (int i = 0; i < 3; i++) {
                            HtmlElement element1 = new HtmlElement("input");
                            element1.addAttribute(new Attribute("name", introspectedColumn.getJavaProperty()));
                            element1.addAttribute(new Attribute("type", "radio"));
                            element1.addAttribute(new Attribute("value", Integer.toString(i + 1)));
                            element1.addAttribute(new Attribute("title", "选项"));
                            element1.addAttribute(new Attribute("lay-filter", introspectedColumn.getJavaProperty()));
                            sb.setLength(0);
                            sb.append("${").append(entityKey).append(".");
                            sb.append(introspectedColumn.getJavaProperty()).append("} eq ").append(i + 1);
                            element1.addAttribute(new Attribute("th:checked", sb.toString()));
                            parent.addElement(element1);
                        }
                        if (htmlElementDescriptor.getDataUrl() != null) {
                            parent.addAttribute(new Attribute("data-url", htmlElementDescriptor.getDataUrl()));
                        }
                    }
                    addClassNameToElement(parent, "oas-form-item-edit");
                    HtmlElement rRead = addDivWithClassToParent(td, "oas-form-item-read");
                    if (htmlElementDescriptor.getDataFormat().equals("急")) {
                        String format = VStringUtil.format("$'{'{0}.{1} ne null?({0}.{1} <= 50?''正常'':''紧急''):''正常''}'", entityKey, introspectedColumn.getJavaProperty());
                        rRead.addAttribute(new Attribute("th:text", format));
                    } else {
                        rRead.addAttribute(new Attribute("th:text", thymeleafValue(introspectedColumn, entityKey)));
                    }
                    break;
                case "checkbox":
                    for (int i = 0; i < 2; i++) {
                        HtmlElement element1 = new HtmlElement("input");
                        element1.addAttribute(new Attribute("type", "checkbox"));
                        element1.addAttribute(new Attribute("name", introspectedColumn.getJavaProperty() + "[" + i + "]"));
                        element1.addAttribute(new Attribute("title", "选项"));
                        element1.addAttribute(new Attribute("lay-filter", introspectedColumn.getJavaProperty()));
                        element1.addAttribute(new Attribute("value", Integer.toString(i + 1)));
                        sb.setLength(0);
                        sb.append("${").append(entityKey).append(".");
                        sb.append(introspectedColumn.getJavaProperty());
                        sb.append("} eq ").append(i + 1);
                        element1.addAttribute(new Attribute("th:checked", sb.toString()));
                        parent.addElement(element1);
                    }
                    if (htmlElementDescriptor.getDataUrl() != null) {
                        parent.addAttribute(new Attribute("data-url", htmlElementDescriptor.getDataUrl()));
                    }
                    addClassNameToElement(parent, "oas-form-item-edit");
                    HtmlElement cRead = addDivWithClassToParent(td, "oas-form-item-read");
                    cRead.addAttribute(new Attribute("th:text", thymeleafValue(introspectedColumn, entityKey)));
                    break;
                case "date":
                    HtmlElement input = generateHtmlInput(introspectedColumn, false, false);
                    String dateType = htmlElementDescriptor.getDataFormat() != null ? htmlElementDescriptor.getDataFormat() : htmlElementDescriptor.getDataUrl();
                    if (!StringUtility.stringHasValue(dateType)) {
                        if (introspectedColumn.isJDBCDateColumn()) dateType = "date";
                        if (introspectedColumn.isJDBCTimeColumn() || introspectedColumn.isJDBCTimeStampColumn())
                            dateType = "datetime";
                    }
                    input.addAttribute(new Attribute("lay-date", dateType));
                    input.addAttribute(new Attribute("readonly", "readonly"));
                    input.addAttribute(new Attribute("th:value", thymeleafValue(introspectedColumn, entityKey)));
                    addClassNameToElement(input, "layui-input");
                    input.addAttribute(new Attribute("lay-filter", introspectedColumn.getJavaProperty()));
                    addElementRequired(introspectedColumn.getActualColumnName(), input);
                    parent.addElement(input);
                    addClassNameToElement(input, "oas-form-item-edit");
                    HtmlElement dateRead = addDivWithClassToParent(parent, "oas-form-item-read");
                    dateRead.addAttribute(new Attribute("th:text", thymeleafValue(introspectedColumn, entityKey)));
                    break;
                default:
                    drawInput(introspectedColumn, entityKey, parent);
            }
        } else {
            drawInput(introspectedColumn, entityKey, parent);
        }
    }

    private HtmlElement drawRadio(String propertyName, String value, String text, String entityKey) {
        HtmlElement element = new HtmlElement("input");
        element.addAttribute(new Attribute("name", propertyName));
        element.addAttribute(new Attribute("type", "radio"));
        element.addAttribute(new Attribute("value", value));
        element.addAttribute(new Attribute("title", text));
        element.addAttribute(new Attribute("lay-filter", propertyName));
        StringBuilder sb = new StringBuilder();
        sb.setLength(0);
        sb.append("${").append(entityKey).append("?.");
        sb.append(propertyName).append("} eq ");
        sb.append("'").append(value).append("'");
        element.addAttribute(new Attribute("th:checked", sb.toString()));
        return element;
    }


    private void drawInput(IntrospectedColumn introspectedColumn, String entityKey, HtmlElement parent) {
        boolean isTextArea = introspectedColumn.getLength() > 500;
        HtmlElement input = generateHtmlInput(introspectedColumn, false, isTextArea);
        addElementRequired(introspectedColumn.getActualColumnName(), input);

        if (isTextArea) {
            addClassNameToElement(input, "layui-textarea");
            input.addAttribute(new Attribute("th:utext", thymeleafValue(introspectedColumn, entityKey)));
        } else {
            addClassNameToElement(input, "layui-input");
            input.addAttribute(new Attribute("th:value", thymeleafValue(introspectedColumn, entityKey)));
        }
        input.addAttribute(new Attribute("autocomplete", "off"));
        input.addAttribute(new Attribute("lay-filter", introspectedColumn.getJavaProperty()));
        if (introspectedColumn.isJDBCDateColumn()) {
            input.addAttribute(new Attribute("readonly", "readonly"));
            input.addAttribute(new Attribute("lay-date", "date"));
        } else if (introspectedColumn.isJDBCTimeColumn() || introspectedColumn.isJDBCTimeStampColumn()) {
            input.addAttribute(new Attribute("readonly", "readonly"));
            input.addAttribute(new Attribute("lay-date", "datetime"));
        }
        parent.addElement(input);
        //if (GenerateUtils.isWorkflowInstance(introspectedTable)) {
        addClassNameToElement(input, "oas-form-item-edit");
        HtmlElement div = addDivWithClassToParent(parent, "oas-form-item-read");
        div.addAttribute(new Attribute("th:text", thymeleafValue(introspectedColumn, entityKey)));
        //}
    }

    private void addElementRequired(String columnName, HtmlElement element) {
        List<String> htmlElementInputRequired = htmlGeneratorConfiguration.getElementRequired();
        if (htmlElementInputRequired.contains(columnName)) {
            element.addAttribute(new Attribute("lay-verify", "required"));
        }
    }

    private void drawRtfContentDiv(String entityKey, IntrospectedColumn introspectedColumn, HtmlElement inputInline) {
        HtmlElement htmlElement = addDivWithClassToParent(inputInline, "rtf-content");
        htmlElement.addAttribute(new Attribute("for", introspectedColumn.getJavaProperty()));
        htmlElement.addAttribute(new Attribute("th:utext", thymeleafValue(introspectedColumn, entityKey)));
    }

    private void drawLabel(IntrospectedColumn introspectedColumn, HtmlElement parent) {
        HtmlElement label = new HtmlElement("label");
        addClassNameToElement(label, "layui-form-label");
        label.addElement(new TextElement(introspectedColumn.getRemarks(true)));
        parent.addElement(label);
    }

    private HtmlElement generateLayuiToolBar(HtmlElement parent) {
        HtmlElement toolBar = generateToolBar(parent);
        String config = getHtmlBarPositionConfig();
        if (!HtmlConstants.HTML_KEY_WORD_TOP.equals(config)) {
            HtmlElement btnClose = addLayButton(toolBar, btn_close_id, "关闭", "&#x1006;");
            addClassNameToElement(btnClose, "footer-btn");
            if (htmlGeneratorConfiguration.getLoadingFrameType().equals("inner")) {
                HtmlElement btnReset = addLayButton(toolBar, btn_reset_id, "重置", "&#xe9aa;");
                addClassNameToElement(btnReset, "footer-btn");
            }
            if (!GenerateUtils.isWorkflowInstance(introspectedTable)) {
                HtmlElement btnSubmit = addLayButton(toolBar, btn_submit_id, "保存", "&#xe605;");
                addClassNameToElement(btnSubmit, "footer-btn");
            }
        }
        return toolBar;
    }

    private HtmlElement addLayButton(HtmlElement parent, String id, String text, String unicode) {
        HtmlElement btn = addButton(parent, id, null);
        addClassNameToElement(btn, "layui-btn layui-btn-sm btn-primary");
        addLayIconFont(btn, unicode);
        if (text != null) {
            btn.addElement(new TextElement(text));
        }
        return btn;
    }

    private HtmlElement addLayIconFont(HtmlElement parent, String unicode) {
        HtmlElement icon;
        if (unicode != null) {
            icon = new HtmlElement("i");
            addClassNameToElement(icon, "layui-icon");
            icon.addElement(new TextElement(unicode));
            parent.addElement(icon);
            return icon;
        } else {
            return null;
        }
    }

    private HtmlElement addLayJavaScriptFragment(HtmlElement parent) {
        boolean innerWindow = htmlGeneratorConfiguration.getLoadingFrameType().equals("inner");
        final boolean workflow = GenerateUtils.isWorkflowInstance(introspectedTable);

        StringBuilder sb = new StringBuilder();
        HtmlElement javascript = addJavaScriptFragment(parent);
        javascript.addElement(new TextElement("$(function(){"));
        if (!workflow) {
            if (innerWindow) {
                javascript.addElement(new TextElement(insertTab(1) + "$('#btn_close').hide();"));
            } else {
                javascript.addElement(new TextElement(insertTab(1) + "$('#btn_close').click(function () {"));
                javascript.addElement(new TextElement(insertTab(2) + "if (parent.datatable && parent.datatable.ajax)  parent.datatable.ajax.reload();"));
                javascript.addElement(new TextElement(insertTab(2) + "$.refreshPortlet(1);"));
                javascript.addElement(new TextElement(insertTab(2) + "if (parent.layer) parent.layer.close(parent.layer.getFrameIndex(window.name));"));
                javascript.addElement(new TextElement(insertTab(1) + "})"));
            }
        }
        javascript.addElement(new TextElement(insertTab(1) + "let saveDoc = function(data) {"));
        if (workflow) {
            javascript.addElement(new TextElement(insertTab(2) + "updateSubject(data)"));
            javascript.addElement(new TextElement(insertTab(2) + "let upData = {};"));
            javascript.addElement(new TextElement(insertTab(2) + "upData.field = data;"));
            javascript.addElement(new TextElement(insertTab(2) + "$.wfSaveDoc(upData, {});"));
        } else {
            sb.setLength(0);
            sb.append(insertTab(2)).append("let url = \"/");
            sb.append(introspectedTable.getControllerSimplePackage());
            sb.append("/").append(introspectedTable.getControllerBeanName()).append("\";");
            javascript.addElement(new TextElement(sb.toString()));
            sb.setLength(0);
            sb.append(insertTab(2)).append("$.requestJsonSuccessCallback(url, function (resp) {");
            javascript.addElement(new TextElement(sb.toString()));
            sb.setLength(0);
            sb.append(insertTab(3)).append("if (resp.attributes.id) $('#id').val(resp.attributes.id);");
            javascript.addElement(new TextElement(sb.toString()));
            sb.setLength(0);
            sb.append(insertTab(3)).append("if (resp.attributes.version && $('#version').length > 0) $('#version').val(resp.attributes.version);");
            javascript.addElement(new TextElement(sb.toString()));
            sb.setLength(0);
            sb.append(insertTab(3)).append("$.showFrameAlertBox(\"操作成功！\", \"info\", {});");
            javascript.addElement(new TextElement(sb.toString()));
            if (!innerWindow) {
                sb.setLength(0);
                sb.append(insertTab(3)).append("$('#btn_close').trigger('click');");
                javascript.addElement(new TextElement(sb.toString()));
            }
            sb.setLength(0);
            sb.append(insertTab(2)).append(" }, {");
            javascript.addElement(new TextElement(sb.toString()));
            sb.setLength(0);
            sb.append(insertTab(3)).append("data: JSON.stringify(data),");
            javascript.addElement(new TextElement(sb.toString()));
            sb.setLength(0);
            sb.append(insertTab(3)).append("type: !$.isEmpty(data.id) ? 'PUT' : 'POST'");
            javascript.addElement(new TextElement(sb.toString()));
            sb.setLength(0);
            sb.append(insertTab(2)).append("})");
            javascript.addElement(new TextElement(sb.toString()));
        }
        javascript.addElement(new TextElement(insertTab(1) + "}"));
        javascript.addElement(new TextElement(insertTab(1) + "let updateSubject = function(data){"));
        javascript.addElement(new TextElement(insertTab(2) + "data.subject = \"【\" + data.fileCategory + \"】\" + data.name + \"的处理单（\" + data.applyDate + \"）\";"));
        javascript.addElement(new TextElement(insertTab(1) + "}"));
        javascript.addElement(new TextElement(insertTab(1) + "window.saveDoc = saveDoc;"));
        javascript.addElement(new TextElement(insertTab(1) + "window.updateSubject = updateSubject;"));
        javascript.addElement(new TextElement("})"));
        return javascript;
    }

    private String insertTab(int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append("    ");
        }
        return sb.toString();
    }
}
