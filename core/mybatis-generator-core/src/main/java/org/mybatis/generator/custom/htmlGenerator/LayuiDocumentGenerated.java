package org.mybatis.generator.custom.htmlGenerator;

import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.html.Attribute;
import org.mybatis.generator.api.dom.html.Document;
import org.mybatis.generator.api.dom.html.HtmlElement;
import org.mybatis.generator.api.dom.html.TextElement;
import org.mybatis.generator.codegen.GeneratorInitialParameters;
import org.mybatis.generator.codegen.HtmlConstants;
import org.mybatis.generator.codegen.mybatis3.htmlmapper.elements.layui.*;
import org.mybatis.generator.config.HtmlGeneratorConfiguration;
import org.mybatis.generator.config.HtmlElementDescriptor;
import org.mybatis.generator.config.HtmlLayoutDescriptor;
import org.mybatis.generator.custom.ConstantsUtil;

import java.util.*;
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

    private final HtmlGeneratorConfiguration htmlGeneratorConfiguration;

    private final GeneratorInitialParameters generatorInitialParameters;

    public LayuiDocumentGenerated(Document document, IntrospectedTable introspectedTable, HtmlGeneratorConfiguration htmlGeneratorConfiguration) {
        super(document, introspectedTable, htmlGeneratorConfiguration);
        this.htmlGeneratorConfiguration = htmlGeneratorConfiguration;
        this.introspectedTable = introspectedTable;
        this.document = document;
        this.rootElement = document.getRootElement();
        this.head = generateLayuiHead();
        this.body = generateHtmlBody();
        this.generatorInitialParameters = new GeneratorInitialParameters(introspectedTable.getContext(), introspectedTable, null, null);
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
        // if (!GenerateUtils.isWorkflowInstance(introspectedTable)) {
        /* 查看状态*/
        HtmlElement viewStatus = generateHtmlInput("viewStatus", true, false);
        viewStatus.addAttribute(new Attribute("th:value", "${viewStatus}?:1"));
        content.addElement(viewStatus);
        /* 是否工作流应用*/
        HtmlElement isWorkflow = generateHtmlInput("workflowEnabled", true, false);
        isWorkflow.addAttribute(new Attribute("th:value", "${" + GenerateUtils.getEntityKeyStr(introspectedTable) + "?.workflowEnabled}?:0"));
        content.addElement(isWorkflow);
        // }
        generateLayuiToolBar(content);

        String fileName = Arrays.stream(htmlGeneratorConfiguration.getViewPath().split("[/\\\\]"))
                .reduce((first, second) -> second)
                .orElse("");
        if (!GenerateUtils.isWorkflowInstance(introspectedTable)) {
            addStaticJavaScript(body.get("body"), "/webjars/plugins/js/app-non-wf-form.js");
        } else {
            addStaticJavaScript(body.get("body"), "/webjars/plugins/js/app-wf-form.js");
        }
        addStaticJavaScript(body.get("body"), "/js/" + introspectedTable.getContext().getModuleKeyword() + "/" + fileName + ".js");
        //addLayJavaScriptFragment(body.get("body"));
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
        long count = introspectedTable.getBaseColumns().stream().filter(IntrospectedColumn::isLongVarchar).count();
        if (count > 0) {
            addStaticReplace(head, "subpages/webjarsPluginsRequired2.html::neditorRequired");
        }
        addStaticReplace(head, "subpages/webjarsPluginsRequired2.html::contextMemuOnly");
        addStaticJavaScript(head, "/webjars/plugins/js/mainform.min.js");
        addLocalStaticResource(head);
        //添加自定义样式
        addCustomCss(head, this.htmlGeneratorConfiguration);
        return head;
    }

    private void addCustomCss(HtmlElement head, HtmlGeneratorConfiguration htmlGeneratorConfiguration) {
        HtmlLayoutDescriptor layoutDescriptor = htmlGeneratorConfiguration.getLayoutDescriptor();
        if (layoutDescriptor.getBorderWidth() != 1 || !layoutDescriptor.getBorderColor().equals(ConstantsUtil.HTML_BORDER_COLOR_DEFAULT)) {
            HtmlElement style = new HtmlElement("style");
            style.addAttribute(new Attribute("type", "text/css"));
            String styleStr = ".layui-table td,.layui-table th,.layui-table-col-set,.layui-table-fixed-r,.layui-table-grid-down,.layui-table-header,.layui-table-page,.layui-table-tips-main,.layui-table-tool,.layui-table-total,.layui-table-view,.layui-table[lay-skin=line],.layui-table[lay-skin=row] {\n";
            styleStr += "            border-width: " + (layoutDescriptor.getBorderWidth() == 0 ? 0 : layoutDescriptor.getBorderWidth() + "px") + ";\n";
            styleStr += "            border-color: " + layoutDescriptor.getBorderColor() + ";\n";
            styleStr += "            border-style: solid;\n";
            styleStr += "       }";
            style.addElement(new TextElement(styleStr));
            head.addElement(style);
        }
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
                        waitRenderMap.put(baseColumn.getActualColumnName(), baseColumn);
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
                    if (rowIntrospectedColumns.size() == 1
                            && (rowIntrospectedColumns.get(0).getLength() > 255
                            || this.htmlGeneratorConfiguration.getLayoutDescriptor().getExclusiveColumns().contains(rowIntrospectedColumns.get(0).getActualColumnName()))) {
                        td.addAttribute(new Attribute("colspan", String.valueOf(pageColumnsConfig)));
                    }
                    if (introspectedColumn.isLongVarchar()) {
                        rtfColumn.add(introspectedColumn);
                        drawRtfContentDiv(entityKey, introspectedColumn, block);
                    } else {
                        generateHtmlInputComponent(introspectedColumn, block, td);
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
            for (List<IntrospectedColumn> introspectedColumns : baseColumnsRows.values()) {
                /*行*/
                HtmlElement formItem = addDivWithClassToParent(form, "layui-form-item");
                /*列*/
                for (IntrospectedColumn introspectedColumn : introspectedColumns) {
                    //label
                    drawLabel(introspectedColumn, formItem);
                    //input
                    HtmlElement inputInline = addDivWithClassToParent(formItem, "layui-input-block");
                    if (introspectedColumn.isLongVarchar()) {
                        rtfColumn.add(introspectedColumn);
                        drawRtfContentDiv(entityKey, introspectedColumn, inputInline);
                    } else {
                        generateHtmlInputComponent(introspectedColumn, inputInline, formItem);
                    }
                }
            }
        }

        //添加主键字段
        for (IntrospectedColumn primaryKeyColumn : introspectedTable.getPrimaryKeyColumns()) {
            if (hiddenColumns.stream().anyMatch(c -> c.getActualColumnName().equals(primaryKeyColumn.getActualColumnName()))) {
                continue;
            }
            HtmlElement input = generateHtmlInput(primaryKeyColumn, true, false);
            input.addAttribute(new Attribute("th:value", thymeleafValue(primaryKeyColumn, entityKey)));
            form.addElement(input);
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
    private void generateHtmlInputComponent(IntrospectedColumn introspectedColumn, HtmlElement parent, HtmlElement td) {
        List<HtmlElementDescriptor> collect = htmlGeneratorConfiguration.getElementDescriptors().stream()
                .filter(t -> t.getName().equals(introspectedColumn.getActualColumnName()))
                .collect(Collectors.toList());
        if (collect.size() > 0) {
            HtmlElementDescriptor htmlElementDescriptor = collect.get(0);
            //计算使用方言
            String thisDialect;
            Attribute beanName = null, applyProperty = null;
            if (VStringUtil.stringHasValue(htmlElementDescriptor.getDataSource())) {
                switch (htmlElementDescriptor.getDataSource()) {
                    case "department":
                        thisDialect = "vgo:deptName";
                        break;
                    case "user":
                        thisDialect = "vgo:userName";
                        break;
                    default:
                        thisDialect = "vgo:" + htmlElementDescriptor.getDataSource();
                        if (htmlElementDescriptor.getBeanName() != null) {
                            beanName = new Attribute("beanName", htmlElementDescriptor.getBeanName());
                        }
                        if (htmlElementDescriptor.getApplyProperty() != null) {
                            applyProperty = new Attribute("applyProperty", htmlElementDescriptor.getApplyProperty());
                        }
                        break;
                }
            } else {
                thisDialect = "th:text";
            }
            switch (htmlElementDescriptor.getTagType().toLowerCase()) {
                case "dropdownlist":
                    DropdownListHtmlGenerator dropdownListHtmlGenerator = new DropdownListHtmlGenerator(generatorInitialParameters);
                    dropdownListHtmlGenerator.setHtmlGeneratorConfiguration(htmlGeneratorConfiguration);
                    dropdownListHtmlGenerator.setHtmlElementDescriptor(htmlElementDescriptor);
                    dropdownListHtmlGenerator.addHtmlElement(introspectedColumn, parent);
                    HtmlElement dpRead = addDivWithClassToParent(td, "oas-form-item-read");
                    dpRead.addAttribute(new Attribute(thisDialect, dropdownListHtmlGenerator.getFieldValueFormatPattern(introspectedColumn)));
                    if (beanName != null) {
                        dpRead.addAttribute(beanName);
                    }
                    if (applyProperty != null) {
                        dpRead.addAttribute(applyProperty);
                    }
                    break;
                case "switch":
                    //增加美化的switch
                    SwitchHtmlGenerator switchHtmlGenerator = new SwitchHtmlGenerator(generatorInitialParameters);
                    switchHtmlGenerator.setHtmlGeneratorConfiguration(htmlGeneratorConfiguration);
                    switchHtmlGenerator.setHtmlElementDescriptor(htmlElementDescriptor);
                    switchHtmlGenerator.addHtmlElement(introspectedColumn, parent);
                    HtmlElement sRead = addDivWithClassToParent(td, "oas-form-item-read");
                    sRead.addAttribute(new Attribute(thisDialect, switchHtmlGenerator.getFieldValueFormatPattern(introspectedColumn)));
                    if (beanName != null) {
                        sRead.addAttribute(beanName);
                    }
                    if (applyProperty != null) {
                        sRead.addAttribute(applyProperty);
                    }
                    break;
                case "radio":
                    RadioHtmlGenerator radioHtmlGenerator = new RadioHtmlGenerator(generatorInitialParameters);
                    radioHtmlGenerator.setHtmlGeneratorConfiguration(htmlGeneratorConfiguration);
                    radioHtmlGenerator.setHtmlElementDescriptor(htmlElementDescriptor);
                    radioHtmlGenerator.addHtmlElement(introspectedColumn, parent);
                    HtmlElement rRead = addDivWithClassToParent(td, "oas-form-item-read");
                    rRead.addAttribute(new Attribute(thisDialect, radioHtmlGenerator.getFieldValueFormatPattern(introspectedColumn)));
                    if (beanName != null) {
                        rRead.addAttribute(beanName);
                    }
                    if (applyProperty != null) {
                        rRead.addAttribute(applyProperty);
                    }
                    break;
                case "checkbox":
                    CheckBoxHtmlGenerator checkBoxHtmlGenerator = new CheckBoxHtmlGenerator(generatorInitialParameters);
                    checkBoxHtmlGenerator.setHtmlGeneratorConfiguration(htmlGeneratorConfiguration);
                    checkBoxHtmlGenerator.setHtmlElementDescriptor(htmlElementDescriptor);
                    checkBoxHtmlGenerator.addHtmlElement(introspectedColumn, parent);
                    HtmlElement cRead = addDivWithClassToParent(td, "oas-form-item-read");
                    cRead.addAttribute(new Attribute(thisDialect, checkBoxHtmlGenerator.getFieldValueFormatPattern(introspectedColumn)));
                    if (beanName != null) {
                        cRead.addAttribute(beanName);
                    }
                    if (applyProperty != null) {
                        cRead.addAttribute(applyProperty);
                    }
                    break;
                case "date":
                    DateHtmlElementGenerator dateHtmlElementGenerator = new DateHtmlElementGenerator(generatorInitialParameters);
                    dateHtmlElementGenerator.setHtmlElementDescriptor(htmlElementDescriptor);
                    dateHtmlElementGenerator.addHtmlElement(introspectedColumn, parent);
                    break;
                case "select":
                    SelectElementGenerator selectElementGenerator = new SelectElementGenerator(generatorInitialParameters);
                    selectElementGenerator.setHtmlElementDescriptor(htmlElementDescriptor);
                    selectElementGenerator.addHtmlElement(introspectedColumn, parent);
                    break;
                default:
                    if (GenerateUtils.isDateType(introspectedColumn)) {
                        DateHtmlElementGenerator date = new DateHtmlElementGenerator(generatorInitialParameters);
                        date.setHtmlElementDescriptor(htmlElementDescriptor);
                        date.addHtmlElement(introspectedColumn, parent);
                    } else {
                        InputHtmlElementGenerator inputHtmlElementGenerator = new InputHtmlElementGenerator(generatorInitialParameters);
                        inputHtmlElementGenerator.setHtmlElementDescriptor(htmlElementDescriptor);
                        inputHtmlElementGenerator.addHtmlElement(introspectedColumn, parent);
                    }

            }
        } else {
            if (GenerateUtils.isDateType(introspectedColumn)) {
                DateHtmlElementGenerator date = new DateHtmlElementGenerator(generatorInitialParameters);
                date.addHtmlElement(introspectedColumn, parent);
            } else {
                InputHtmlElementGenerator inputHtmlElementGenerator = new InputHtmlElementGenerator(generatorInitialParameters);
                inputHtmlElementGenerator.addHtmlElement(introspectedColumn, parent);
            }
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
            if (htmlGeneratorConfiguration.getLayoutDescriptor().getLoadingFrameType().equals("inner")) {
                HtmlElement btnReset = addLayButton(toolBar, btn_reset_id, "重置", "&#xe9aa;");
                addClassNameToElement(btnReset, "footer-btn");
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
}
