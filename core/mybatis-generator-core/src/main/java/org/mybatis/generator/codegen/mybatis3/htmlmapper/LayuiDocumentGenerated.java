package org.mybatis.generator.codegen.mybatis3.htmlmapper;

import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.html.Attribute;
import org.mybatis.generator.api.dom.html.Document;
import org.mybatis.generator.api.dom.html.HtmlElement;
import org.mybatis.generator.api.dom.html.TextElement;
import org.mybatis.generator.codegen.GeneratorInitialParameters;
import org.mybatis.generator.codegen.mybatis3.htmlmapper.elements.layui.*;
import org.mybatis.generator.config.*;
import org.mybatis.generator.custom.ConstantsUtil;
import org.mybatis.generator.custom.HtmlElementTagTypeEnum;
import org.mybatis.generator.internal.util.Mb3GenUtil;
import org.mybatis.generator.internal.util.VoGenService;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
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
        //是否需要插入页面列表
        if (introspectedTable.getRules().isAdditionInnerList(htmlGeneratorConfiguration)) {
            addInnerList(content, htmlGeneratorConfiguration);
        }
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
        generateLayuiToolbar(content);

        String fileName = Arrays.stream(htmlGeneratorConfiguration.getViewPath().split("[/\\\\]"))
                .reduce((first, second) -> second)
                .orElse("");
        if (!GenerateUtils.isWorkflowInstance(introspectedTable)) {
            addStaticJavaScript(body.get("body"), "/webjars/plugins/js/app-non-wf-form.min.js");
        } else {
            addStaticJavaScript(body.get("body"), "/webjars/plugins/js/app-wf-form.min.js");
        }
        if (htmlGeneratorConfiguration.getHtmlFileAttachmentConfiguration() != null && htmlGeneratorConfiguration.getHtmlFileAttachmentConfiguration().isGenerate()) {
            addStaticJavaScript(body.get("body"), "/webjars/plugins/js/file-attachment.min.js");
        }
        addStaticJavaScript(body.get("body"), "/js/" + introspectedTable.getContext().getModuleKeyword() + "/" + fileName + ".min.js");
        //增加页面列表的编辑器模板页面片段
        if (introspectedTable.getRules().isAdditionInnerList(htmlGeneratorConfiguration)) {
            HtmlElementInnerListConfiguration listConfiguration = htmlGeneratorConfiguration.getHtmlElementInnerListConfiguration();
            HtmlElement div = new HtmlElement("div");
            String appKey = VStringUtil.stringHasValue(listConfiguration.getAppKeyword()) ? listConfiguration.getAppKeyword() : introspectedTable.getContext().getModuleKeyword();
            String format = VStringUtil.format("{0}/fragments/{1}_{2}.html::{2}", appKey, listConfiguration.getSourceViewPath(), ConstantsUtil.SUFFIX_INNER_LIST_FRAGMENTS);
            div.addAttribute(new Attribute("th:replace", format));
            body.get("body").addElement(div);
            //根据数据源添加
            if (VStringUtil.stringHasValue(listConfiguration.getDataField())) {
                HtmlElement script = new HtmlElement("script");
                script.addAttribute(new Attribute("th:inline", "javascript"));
                String format1 = VStringUtil.format("var {0} = JSON.stringify(/*[[$'{'{1}.{0}'}']]*/ null);", listConfiguration.getDataField(), GenerateUtils.getEntityKeyStr(introspectedTable));
                script.addElement(new TextElement(format1));
                body.get("body").addElement(script);
            }
        }
        return true;
    }

    private void addInnerList(HtmlElement content, HtmlGeneratorConfiguration htmlGeneratorConfiguration) {
        HtmlElementInnerListConfiguration htmlElementInnerList = htmlGeneratorConfiguration.getHtmlElementInnerListConfiguration();
        HtmlElement div = new HtmlElement("div");
        div.addAttribute(new Attribute("class", "inner-list-container"));
        HtmlElement table = new HtmlElement("table");
        table.addAttribute(new Attribute("lay-filter", htmlElementInnerList.getTagId()));
        table.addAttribute(new Attribute("id", htmlElementInnerList.getTagId()));
        div.addElement(table);
        content.addElement(div);
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
        if (introspectedTable.getRules().isAdditionInnerList(htmlGeneratorConfiguration)) {
            addStaticReplace(head, "subpages/webjarsPluginsRequired2.html::layTable");
        }
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
            styleStr += "  i          border-style: solid;\n";
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
            if (introspectedTable.getRules().isGenerateVoModel()) {
                if (isIgnore(baseColumn
                        , introspectedTable.getTableConfiguration().getVoGeneratorConfiguration().getVoModelConfiguration())
                        && !baseColumn.isPrimaryKey()
                        && !baseColumn.getActualColumnName().equalsIgnoreCase("version_")) {
                    continue;
                }
            }
            if (GenerateUtils.isHiddenColumn(introspectedTable, baseColumn, htmlGeneratorConfiguration)) {
                hiddenColumns.add(baseColumn);
            } else {
                displayColumns.add(baseColumn);
                //指定一些列的默认生成的样式
                for (HtmlElementDescriptor htmlElementDescriptor : htmlGeneratorConfiguration.getElementDescriptors()) {
                    if (htmlElementDescriptor.getName().equals(baseColumn.getActualColumnName())) {
                        waitRenderMap.put(baseColumn.getActualColumnName(), baseColumn);
                    }
                }
            }
        }
        //可变对象包装变量，计算附件的前置列
        AtomicReference<String> beforeElement = new AtomicReference<>();
        if (htmlGeneratorConfiguration.getHtmlFileAttachmentConfiguration() != null && htmlGeneratorConfiguration.getHtmlFileAttachmentConfiguration().isGenerate()) {
            HtmlFileAttachmentConfiguration fileAttachmentConfiguration = htmlGeneratorConfiguration.getHtmlFileAttachmentConfiguration();
            if (fileAttachmentConfiguration.getAfterColumn() != null) {
                beforeElement.set(fileAttachmentConfiguration.getAfterColumn());
                if (displayColumns.stream().map(IntrospectedColumn::getActualColumnName).noneMatch(col -> beforeElement.get().equals(col))) {
                    beforeElement.set(displayColumns.get(displayColumns.size() - 1).getActualColumnName());
                }
            }
            if (beforeElement.get() == null) {
                beforeElement.set(displayColumns.get(displayColumns.size() - 1).getActualColumnName());
            }
        }
        //计算审批意见的前置列
        htmlGeneratorConfiguration.getHtmlApprovalCommentConfigurations().forEach(approvalCommentConfiguration -> {
            if (approvalCommentConfiguration.getAfterColumn() == null) {
                approvalCommentConfiguration.setAfterColumn(displayColumns.get(displayColumns.size() - 1).getActualColumnName());
            } else if (displayColumns.stream().noneMatch(col -> col.getActualColumnName().equals(approvalCommentConfiguration.getAfterColumn()))) {
                approvalCommentConfiguration.setAfterColumn(displayColumns.get(displayColumns.size() - 1).getActualColumnName());
            }
        });

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
                    addClassNameToElement(block,"class-"+introspectedColumn.getActualColumnName());
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
                //添加附件
                if (beforeElement.get() != null && rowIntrospectedColumns.stream().map(IntrospectedColumn::getActualColumnName).anyMatch(col -> beforeElement.get().equals(col))) {
                    String label = htmlGeneratorConfiguration.getHtmlFileAttachmentConfiguration().getLabel();
                    HtmlElement atr = new HtmlElement("tr");
                    HtmlElement td = new HtmlElement("td");
                    td.addAttribute(new Attribute("colspan", String.valueOf(pageColumnsConfig)));
                    HtmlElement div = new HtmlElement("div");
                    div.addAttribute(new Attribute("th:replace", "subpages/webjarsPluginsRequired2.html::fileAttachmentFragment('" + label + "')"));
                    table.addElement(atr);
                    atr.addElement(td);
                    td.addElement(div);
                }
                //添加意见
                htmlGeneratorConfiguration.getHtmlApprovalCommentConfigurations().stream()
                        .filter(HtmlApprovalCommentConfiguration::isGenerate)
                        .forEach(approvalCommentConfiguration -> {
                            if (rowIntrospectedColumns.stream().map(IntrospectedColumn::getActualColumnName).anyMatch(col -> approvalCommentConfiguration.getAfterColumn().equals(col))) {
                                addApprovalCommentTag(pageColumnsConfig, approvalCommentConfiguration, table);
                            }
                        });
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
                    addClassNameToElement(inputInline,"class-"+introspectedColumn.getActualColumnName());
                    if (introspectedColumn.isLongVarchar()) {
                        rtfColumn.add(introspectedColumn);
                        drawRtfContentDiv(entityKey, introspectedColumn, inputInline);
                    } else {
                        generateHtmlInputComponent(introspectedColumn, inputInline, formItem);
                    }
                    //添加附件元素
                    if (beforeElement.get() != null && introspectedColumn.getActualColumnName().equals(beforeElement.get())) {
                        HtmlElement aFormItem = addDivWithClassToParent(form, "layui-form-item");
                        HtmlElement div = new HtmlElement("div");
                        div.addAttribute(new Attribute("th:replace", "subpages/webjarsPluginsRequired2.html::fileAttachmentFragment"));
                        aFormItem.addElement(div);
                    }
                    //添加意见元素
                    htmlGeneratorConfiguration.getHtmlApprovalCommentConfigurations().stream()
                            .filter(HtmlApprovalCommentConfiguration::isGenerate)
                            .forEach(approvalCommentConfiguration -> {
                                if (introspectedColumn.getActualColumnName().equals(approvalCommentConfiguration.getAfterColumn())) {
                                    addApprovalCommentTag(pageColumnsConfig, approvalCommentConfiguration, form);
                                }
                            });
                }
            }
        }

        //添加checkbox的隐藏input，避免提交时没有值
        for (HtmlElementDescriptor htmlElementDescriptor : htmlGeneratorConfiguration.getElementDescriptors()) {
            if (htmlElementDescriptor.getTagType().equals(HtmlElementTagTypeEnum.CHECKBOX.getCode())) {
                HtmlElement input = new HtmlElement("input");
                input.addAttribute(new Attribute("name",
                        htmlElementDescriptor.getColumn().getJavaProperty() +
                                (htmlElementDescriptor.getTagType().equals(HtmlElementTagTypeEnum.CHECKBOX.getCode()) ? "[]" : "")
                ));
                input.addAttribute(new Attribute("type", "hidden"));
                input.addAttribute(new Attribute("value", ""));
                form.addElement(input);
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

    private static void addApprovalCommentTag(int pageColumnsConfig, HtmlApprovalCommentConfiguration configuration, HtmlElement table) {
        HtmlElement atr = new HtmlElement("tr");
        table.addElement(atr);
        HtmlElement td = new HtmlElement("td");
        td.addAttribute(new Attribute("colspan", String.valueOf(pageColumnsConfig)));
        atr.addElement(td);
        HtmlElement label = new HtmlElement("label");
        label.addAttribute(new Attribute("class", "layui-form-label"));
        label.addElement(new TextElement(configuration.getLabel()));
        td.addElement(label);
        HtmlElement div = new HtmlElement("div");
        div.addAttribute(new Attribute("data-location", configuration.getLocationTag()));
        div.addAttribute(new Attribute("class", "data-value layui-input-block comments-container"));
        td.addElement(div);
    }


    //生成页面dropdownlist、switch、radio、checkbox、date及其它元素
    private void generateHtmlInputComponent(IntrospectedColumn introspectedColumn, HtmlElement parent, HtmlElement td) {
        HtmlElementDescriptor htmlElementDescriptor = htmlGeneratorConfiguration.getElementDescriptors().stream()
                .filter(t -> t.getName().equals(introspectedColumn.getActualColumnName())).findFirst().orElse(null);
        if (htmlElementDescriptor!=null) { //如果配置了当前字段的元素描述，则使用配置的元素描述
            switch (htmlElementDescriptor.getTagType().toLowerCase()) {
                case "dropdownlist":
                    DropdownListHtmlGenerator dropdownListHtmlGenerator = new DropdownListHtmlGenerator(generatorInitialParameters,introspectedColumn);
                    dropdownListHtmlGenerator.setHtmlGeneratorConfiguration(htmlGeneratorConfiguration);
                    dropdownListHtmlGenerator.setHtmlElementDescriptor(htmlElementDescriptor);
                    dropdownListHtmlGenerator.addHtmlElement(parent);
                    HtmlElement dpRead = addDivWithClassToParent(td, "oas-form-item-read");
                    if (getOtherValueFormatPattern(htmlElementDescriptor) != null) {
                        dpRead.addAttribute(new Attribute("th:text", getOtherValueFormatPattern(htmlElementDescriptor)));
                    }
                    addEnumClassNamAttribute(htmlElementDescriptor, dpRead);
                    addBeanNameApplyProperty(htmlElementDescriptor, dpRead);
                    addDictCodeAttribute(htmlElementDescriptor, dpRead);
                    break;
                case "switch":
                    //增加美化的switch
                    SwitchHtmlGenerator switchHtmlGenerator = new SwitchHtmlGenerator(generatorInitialParameters,introspectedColumn);
                    switchHtmlGenerator.setHtmlGeneratorConfiguration(htmlGeneratorConfiguration);
                    switchHtmlGenerator.setHtmlElementDescriptor(htmlElementDescriptor);
                    switchHtmlGenerator.addHtmlElement(parent);
                    HtmlElement sRead = addDivWithClassToParent(td, "oas-form-item-read");
                    if (getOtherValueFormatPattern(htmlElementDescriptor) != null) {
                        sRead.addAttribute(new Attribute("th:text", getOtherValueFormatPattern(htmlElementDescriptor)));
                    }
                    addBeanNameApplyProperty(htmlElementDescriptor, sRead);
                    addEnumClassNamAttribute(htmlElementDescriptor, sRead);
                    addDictCodeAttribute(htmlElementDescriptor, sRead);
                    break;
                case "radio":
                    RadioHtmlGenerator radioHtmlGenerator = new RadioHtmlGenerator(generatorInitialParameters,introspectedColumn);
                    radioHtmlGenerator.setHtmlGeneratorConfiguration(htmlGeneratorConfiguration);
                    radioHtmlGenerator.setHtmlElementDescriptor(htmlElementDescriptor);
                    radioHtmlGenerator.addHtmlElement(parent);
                    HtmlElement rRead = addDivWithClassToParent(td, "oas-form-item-read");
                    if (getOtherValueFormatPattern(htmlElementDescriptor) != null) {
                        rRead.addAttribute(new Attribute("th:text", getOtherValueFormatPattern(htmlElementDescriptor)));
                    }
                    addBeanNameApplyProperty(htmlElementDescriptor, rRead);
                    addEnumClassNamAttribute(htmlElementDescriptor, rRead);
                    addDictCodeAttribute(htmlElementDescriptor, rRead);
                    break;
                case "checkbox":
                    CheckBoxHtmlGenerator checkBoxHtmlGenerator = new CheckBoxHtmlGenerator(generatorInitialParameters,introspectedColumn);
                    checkBoxHtmlGenerator.setHtmlGeneratorConfiguration(htmlGeneratorConfiguration);
                    checkBoxHtmlGenerator.setHtmlElementDescriptor(htmlElementDescriptor);
                    checkBoxHtmlGenerator.addHtmlElement(parent);
                    HtmlElement cRead = addDivWithClassToParent(td, "oas-form-item-read");
                    if (getOtherValueFormatPattern(htmlElementDescriptor) != null) {
                        cRead.addAttribute(new Attribute("th:text", getOtherValueFormatPattern(htmlElementDescriptor)));
                    }
                    addBeanNameApplyProperty(htmlElementDescriptor, cRead);
                    addEnumClassNamAttribute(htmlElementDescriptor, cRead);
                    addDictCodeAttribute(htmlElementDescriptor, cRead);
                    break;
                case "date":
                    DateHtmlElementGenerator dateHtmlElementGenerator = new DateHtmlElementGenerator(generatorInitialParameters,introspectedColumn);
                    dateHtmlElementGenerator.setHtmlGeneratorConfiguration(htmlGeneratorConfiguration);
                    dateHtmlElementGenerator.setHtmlElementDescriptor(htmlElementDescriptor);
                    dateHtmlElementGenerator.addHtmlElement(parent);
                    break;
                case "select":
                    SelectElementGenerator selectElementGenerator = new SelectElementGenerator(generatorInitialParameters,introspectedColumn);
                    selectElementGenerator.setHtmlGeneratorConfiguration(htmlGeneratorConfiguration);
                    selectElementGenerator.setHtmlElementDescriptor(htmlElementDescriptor);
                    selectElementGenerator.addHtmlElement(parent);
                    break;
                default:
                    if (GenerateUtils.isDateType(introspectedColumn)) {
                        DateHtmlElementGenerator date = new DateHtmlElementGenerator(generatorInitialParameters,introspectedColumn);
                        date.setHtmlGeneratorConfiguration(htmlGeneratorConfiguration);
                        date.setHtmlElementDescriptor(htmlElementDescriptor);
                        date.addHtmlElement(parent);
                    } else {
                        InputHtmlElementGenerator inputHtmlElementGenerator = new InputHtmlElementGenerator(generatorInitialParameters,introspectedColumn);
                        inputHtmlElementGenerator.setHtmlGeneratorConfiguration(htmlGeneratorConfiguration);
                        inputHtmlElementGenerator.setHtmlElementDescriptor(htmlElementDescriptor);
                        inputHtmlElementGenerator.addHtmlElement(parent);
                    }

            }
        } else {
            if (GenerateUtils.isDateType(introspectedColumn)) {
                DateHtmlElementGenerator date = new DateHtmlElementGenerator(generatorInitialParameters,introspectedColumn);
                date.setHtmlGeneratorConfiguration(htmlGeneratorConfiguration);
                date.addHtmlElement(parent);
            } else {
                InputHtmlElementGenerator inputHtmlElementGenerator = new InputHtmlElementGenerator(generatorInitialParameters,introspectedColumn);
                inputHtmlElementGenerator.setHtmlGeneratorConfiguration(htmlGeneratorConfiguration);
                inputHtmlElementGenerator.addHtmlElement(parent);
            }
        }
    }

    private void addDictCodeAttribute(HtmlElementDescriptor htmlElementDescriptor, HtmlElement htmlElement) {
        if (VStringUtil.stringHasValue(htmlElementDescriptor.getDictCode())) {
            htmlElement.addAttribute(new Attribute(HTML_ATTRIBUTE_DICT_CODE, htmlElementDescriptor.getDictCode()));
        }
    }

    private void addBeanNameApplyProperty(HtmlElementDescriptor htmlElementDescriptor, HtmlElement element) {
        if (htmlElementDescriptor.getBeanName() != null) {
            element.addAttribute(new Attribute(HTML_ATTRIBUTE_BEAN_NAME, htmlElementDescriptor.getBeanName()));
        }
        if (htmlElementDescriptor.getApplyProperty() != null) {
            element.addAttribute(new Attribute(HTML_ATTRIBUTE_APPLY_PROPERTY, htmlElementDescriptor.getApplyProperty()));
        }
    }

    private void addEnumClassNamAttribute(HtmlElementDescriptor htmlElementDescriptor, HtmlElement element) {
        if (htmlElementDescriptor.getEnumClassName() != null) {
            element.addAttribute(new Attribute(HTML_ATTRIBUTE_ENUM_CLASS_NAME, htmlElementDescriptor.getEnumClassName()));
        }
    }

    private void drawRtfContentDiv(String entityKey, IntrospectedColumn introspectedColumn, HtmlElement inputInline) {
        HtmlElement htmlElement = addDivWithClassToParent(inputInline, "rtf-content");
        htmlElement.addAttribute(new Attribute("for", introspectedColumn.getJavaProperty()));
        htmlElement.addAttribute(new Attribute("th:utext", thymeleafValue(introspectedColumn, entityKey)));
        //追加样式css
        HtmlElementDescriptor htmlElementDescriptor = getHtmlElementDescriptor(introspectedColumn);
        if (htmlElementDescriptor != null && htmlElementDescriptor.getElementCss() != null) {
            voGenService.addCssStyleToElement(htmlElement, htmlElementDescriptor.getElementCss());
        }
    }

    private void drawLabel(IntrospectedColumn introspectedColumn, HtmlElement parent) {
        HtmlElementDescriptor htmlElementDescriptor = getHtmlElementDescriptor(introspectedColumn);
        HtmlElement label = new HtmlElement("label");
        addClassNameToElement(label, "layui-form-label");
        if (!Mb3GenUtil.isInDefaultFields(introspectedTable,introspectedColumn.getJavaProperty())) {
            if (htmlElementDescriptor!=null && !htmlElementDescriptor.getTagType().equals(HtmlElementTagTypeEnum.RADIO.getCode())) {
                label.addAttribute(new Attribute("for", introspectedColumn.getJavaProperty()));
            }else{
                label.addAttribute(new Attribute("for", introspectedColumn.getJavaProperty()));
            }
        }
        OverridePropertyValueGeneratorConfiguration overrideConfig = voGenService.getOverridePropertyValueConfiguration(introspectedColumn);
        if (overrideConfig != null && overrideConfig.getRemark() != null) {
            label.addElement(new TextElement(overrideConfig.getRemark()));
        } else {
            label.addElement(new TextElement(introspectedColumn.getRemarks(true)));
        }
        addClassNameToElement(label, "class-"+introspectedColumn.getActualColumnName());
        //追加样式css
        if (htmlElementDescriptor != null && htmlElementDescriptor.getLabelCss() != null) {
            voGenService.addCssStyleToElement(label, htmlElementDescriptor.getLabelCss());
        }
        parent.addElement(label);
    }

    private HtmlElement generateLayuiToolbar(HtmlElement parent) {
        HtmlElement toolBar = generateToolBar(parent);
        String config = getHtmlBarPositionConfig();
        if (!HTML_KEY_WORD_TOP.equals(config)) {
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
