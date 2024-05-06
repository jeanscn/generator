package org.mybatis.generator.codegen.mybatis3.htmlmapper.document;

import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.html.Attribute;
import org.mybatis.generator.api.dom.html.Document;
import org.mybatis.generator.api.dom.html.HtmlElement;
import org.mybatis.generator.api.dom.html.TextElement;
import org.mybatis.generator.codegen.GeneratorInitialParameters;
import org.mybatis.generator.codegen.mybatis3.htmlmapper.GenerateUtils;
import org.mybatis.generator.codegen.mybatis3.htmlmapper.elements.layui.*;
import org.mybatis.generator.config.*;
import org.mybatis.generator.custom.ConstantsUtil;
import com.vgosoft.core.constant.enums.view.HtmlElementTagTypeEnum;
import org.mybatis.generator.internal.util.Mb3GenUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author <a href="mailto:cjj@vip.sina.com">ChenJJ</a>
 * 2020-07-20 06:22
 * @version 3.0
 */
public class LayuiViewDocumentGenerated extends AbstractThymeleafHtmlDocumentGenerator {
    private final Document document;

    private final HtmlElement rootElement;

    private final HtmlElement head;

    private final Map<String, HtmlElement> body;

    private final HtmlGeneratorConfiguration htmlGeneratorConfiguration;

    private final GeneratorInitialParameters generatorInitialParameters;

    public LayuiViewDocumentGenerated(GeneratorInitialParameters generatorInitialParameters, Document document, HtmlGeneratorConfiguration htmlGeneratorConfiguration) {
        super(generatorInitialParameters,document, htmlGeneratorConfiguration);
        this.htmlGeneratorConfiguration = htmlGeneratorConfiguration;
        this.document = document;
        this.rootElement = document.getRootElement();
        this.head = generateLayuiHead();
        this.body = generateHtmlBody();
        this.generatorInitialParameters = generatorInitialParameters;
    }

    @Override
    public boolean htmlMapDocumentGenerated() {
        rootElement.addElement(head);
        rootElement.addElement(body.get("body"));
        document.setRootElement(rootElement);
        HtmlElement content = body.get("content");
        HtmlElement form = generateForm(content);
        /*标题区域*/
        addSubjectInput(form);
        /*表单验证button*/
        addVerifyButton(form);
        /* 查看状态*/
        HtmlElement viewStatus = generateHtmlInput("viewStatus", true, false);
        viewStatus.addAttribute(new Attribute("th:value", "${viewStatus}?:1"));
        content.addElement(viewStatus);
        /* 是否工作流应用*/
        HtmlElement isWorkflow = generateHtmlInput("workflowEnabled", true, false);
        isWorkflow.addAttribute(new Attribute("th:value", "${" + GenerateUtils.getEntityKeyStr(introspectedTable) + "?.workflowEnabled}?:0"));
        content.addElement(isWorkflow);
        generateLayuiToolbar(content);

        String fileName = Arrays.stream(htmlGeneratorConfiguration.getViewPath().split("[/\\\\]"))
                .reduce((first, second) -> second)
                .orElse("");
        if (!GenerateUtils.isWorkflowInstance(introspectedTable)) {
            this.addStaticThymeleafJavaScript(body.get("body"), "/webjars/plugins/js/app-non-wf-form.min.js");
        } else {
            this.addStaticThymeleafJavaScript(body.get("body"), "/webjars/plugins/js/app-wf-form.min.js");
        }
        if (htmlGeneratorConfiguration.getHtmlFileAttachmentConfiguration() != null && !htmlGeneratorConfiguration.getHtmlFileAttachmentConfiguration().isEmpty() && htmlGeneratorConfiguration.getHtmlFileAttachmentConfiguration().get(0).isGenerate()) {
            this.addStaticThymeleafJavaScript(body.get("body"), "/webjars/plugins/js/file-attachment.min.js");
        }
        this.addStaticThymeleafJavaScript(body.get("body"), "/js/" + introspectedTable.getContext().getModuleKeyword() + "/" + fileName + ".min.js");
        //增加页面列表的编辑器模板页面片段
        if (introspectedTable.getRules().isAdditionInnerList(htmlGeneratorConfiguration)) {
            HtmlElementInnerListConfiguration listConfiguration = htmlGeneratorConfiguration.getHtmlElementInnerListConfiguration().get(0);
            HtmlElement div = new HtmlElement("div");
            String moduleKeyword = VStringUtil.stringHasValue(listConfiguration.getModuleKeyword()) ? listConfiguration.getModuleKeyword() : introspectedTable.getContext().getModuleKeyword();
            String format = VStringUtil.format("{0}/fragments/{1}::{2}", moduleKeyword,Mb3GenUtil.getInnerListFragmentFileName(listConfiguration,introspectedTable),ConstantsUtil.SUFFIX_INNER_LIST_FRAGMENTS);
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
        addStaticThymeleafJavaScript(head, "/webjars/plugins/js/mainform.min.js");
        addLocalStaticResource(head);
        //添加自定义样式
        addCustomCss(head, this.htmlGeneratorConfiguration);
        return head;
    }

    private HtmlElement generateForm(HtmlElement parent) {
        HtmlElement form = addFormWithClassToParent(parent, "layui-form");
        form.addAttribute(new Attribute("lay-filter", "mainForm"));
        List<IntrospectedColumn> hiddenColumns = new ArrayList<>();
        List<IntrospectedColumn> displayColumns = new ArrayList<>();
        for (IntrospectedColumn baseColumn : introspectedTable.getNonBLOBColumns()) {
            if (introspectedTable.getRules().isGenerateVoModel()) {
                if (isIgnore(baseColumn, introspectedTable.getTableConfiguration().getVoGeneratorConfiguration().getVoModelConfiguration())
                        && !baseColumn.isPrimaryKey()
                        && !baseColumn.getActualColumnName().equalsIgnoreCase("version_")) {
                    continue;
                }
            }
            if (GenerateUtils.isHiddenColumn(introspectedTable, baseColumn, htmlGeneratorConfiguration)) {
                hiddenColumns.add(baseColumn);
            } else {
                displayColumns.add(baseColumn);
            }
        }
        //可变对象包装变量，计算附件的前置列
        AtomicReference<String> beforeElement = new AtomicReference<>();
        if (!htmlGeneratorConfiguration.getHtmlFileAttachmentConfiguration().isEmpty() && htmlGeneratorConfiguration.getHtmlFileAttachmentConfiguration().get(0).isGenerate()) {
            HtmlFileAttachmentConfiguration fileAttachmentConfiguration = htmlGeneratorConfiguration.getHtmlFileAttachmentConfiguration().get(0);
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
        //计算内置列表的位置
        if (introspectedTable.getRules().isAdditionInnerList(htmlGeneratorConfiguration)) {
            HtmlElementInnerListConfiguration listConfiguration = htmlGeneratorConfiguration.getHtmlElementInnerListConfiguration().get(0);
            if (listConfiguration.getAfterColumn() == null) {
                listConfiguration.setAfterColumn(displayColumns.get(displayColumns.size() - 1).getActualColumnName());
            } else if (displayColumns.stream().noneMatch(col -> col.getActualColumnName().equals(listConfiguration.getAfterColumn()))) {
                listConfiguration.setAfterColumn(displayColumns.get(displayColumns.size() - 1).getActualColumnName());
            }
        }

        int pageColumnsConfig = getPageColumnsConfig(6);
        Map<Integer, List<IntrospectedColumn>> baseColumnsRows = getHtmlRows(displayColumns,6);
        String entityKey = GenerateUtils.getEntityKeyStr(introspectedTable);
        List<IntrospectedColumn> rtfColumn = new ArrayList<>();
        if (pageColumnsConfig > 1) {
            /*添加表格*/
            HtmlElement table = new HtmlElement("table");
            table.addAttribute(new Attribute("class", "layui-table table-layout-fixed"));
            for (List<IntrospectedColumn> rowIntrospectedColumns : baseColumnsRows.values()) {
                /*行*/
                HtmlElement tr = addTrWithClassToParent(table, "");
                /*列*/
                int colNum = 0;
                for (IntrospectedColumn introspectedColumn : rowIntrospectedColumns) {
                    colNum++;

                    HtmlElement td = addDtWithClassToTr(tr, "",0);
                    //label
                    drawLabel(introspectedColumn, td);
                    //input
                    HtmlElement block = addDivWithClassToParent(td, "layui-input-block","class-"+VStringUtil.toHyphenCase(introspectedColumn.getActualColumnName()));
                    //如果是单独一列，且长度大于255，则占满一行
                    if (rowIntrospectedColumns.size() == 1
                            && (rowIntrospectedColumns.get(0).getLength() > 255
                            || this.htmlGeneratorConfiguration.getLayoutDescriptor().getExclusiveColumns().contains(rowIntrospectedColumns.get(0).getActualColumnName()))) {
                        addColspanToTd(td, pageColumnsConfig);
                        colNum = pageColumnsConfig;
                    }
                    if (introspectedColumn.isLongVarchar()) {
                        rtfColumn.add(introspectedColumn);
                        drawRtfContentDiv(entityKey, introspectedColumn, block);
                    } else {
                        generateHtmlInputComponent(introspectedColumn, block, td);
                    }
                }
                /*如果列数小于指定列数，则后面补充空单元格*/
                if (colNum < pageColumnsConfig && rowIntrospectedColumns.get(0).getLength() <= 255) {
                    for (int i = pageColumnsConfig - colNum; i > 0; i--) {
                        HtmlElement td = new HtmlElement("td");
                        tr.addElement(td);
                    }
                }
                //添加附件
                if (beforeElement.get() != null && rowIntrospectedColumns.stream().map(IntrospectedColumn::getActualColumnName).anyMatch(col -> beforeElement.get().equals(col))) {
                    String label = htmlGeneratorConfiguration.getHtmlFileAttachmentConfiguration().get(0).getLabel();
                    String basePath = htmlGeneratorConfiguration.getHtmlFileAttachmentConfiguration().get(0).getRestBasePath();
                    HtmlElement atr = new HtmlElement("tr");
                    HtmlElement td = new HtmlElement("td");
                    td.addAttribute(new Attribute("colspan", String.valueOf(pageColumnsConfig)));
                    HtmlElement div = new HtmlElement("div");
                    div.addAttribute(new Attribute("th:replace", "subpages/webjarsPluginsRequired2.html::commonAttachmentFragment('" + label + "','file','"+basePath+"')"));
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
                //是否需要插入页面列表
                if (introspectedTable.getRules().isAdditionInnerList(htmlGeneratorConfiguration)) {
                    HtmlElementInnerListConfiguration listConfiguration = htmlGeneratorConfiguration.getHtmlElementInnerListConfiguration().get(0);
                    if (rowIntrospectedColumns.stream().map(IntrospectedColumn::getActualColumnName).anyMatch(col -> listConfiguration.getAfterColumn().equals(col))) {
                        addInnerList(table, htmlGeneratorConfiguration,pageColumnsConfig);
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
                    HtmlElement inputInline = addDivWithClassToParent(formItem, "layui-input-block","class-"+VStringUtil.toHyphenCase(introspectedColumn.getActualColumnName()));
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
                    //是否需要插入页面列表
                    if (introspectedTable.getRules().isAdditionInnerList(htmlGeneratorConfiguration)) {
                        HtmlElementInnerListConfiguration listConfiguration = htmlGeneratorConfiguration.getHtmlElementInnerListConfiguration().get(0);
                        if (introspectedColumn.getActualColumnName().equals(listConfiguration.getAfterColumn())) {
                            addInnerList(form, htmlGeneratorConfiguration,pageColumnsConfig);
                        }
                    }
                }
            }
        }

        //添加checkbox的隐藏input，避免提交时没有值
        for (HtmlElementDescriptor htmlElementDescriptor : htmlGeneratorConfiguration.getElementDescriptors()) {
            if (htmlElementDescriptor.getTagType().equals(HtmlElementTagTypeEnum.CHECKBOX.codeName())) {
                HtmlElement input = new HtmlElement("input");
                input.addAttribute(new Attribute("name",
                        htmlElementDescriptor.getColumn().getJavaProperty() +
                                (htmlElementDescriptor.getTagType().equals(HtmlElementTagTypeEnum.CHECKBOX.codeName()) ? "[]" : "")
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

        if (!hiddenColumns.isEmpty()) {
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
        HtmlElement div = new HtmlElement("div");
        if (GenerateUtils.isWorkflowInstance(introspectedTable)) {
            div.addAttribute(new Attribute("th:replace", "vgowf/jsp/workflowsubjsp.html::workflowsubjsp"));
        }else{
            div.addAttribute(new Attribute("th:replace", "vgoweb/fragments/vgocoresub.html::vgocoresubjsp"));
        }
        form.addElement(div);
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

    private void addInnerList(HtmlElement content, HtmlGeneratorConfiguration htmlGeneratorConfiguration,int pageColumnsConfig) {
        HtmlElementInnerListConfiguration htmlElementInnerList = htmlGeneratorConfiguration.getHtmlElementInnerListConfiguration().get(0);
        HtmlElement atr = new HtmlElement("tr");
        content.addElement(atr);
        HtmlElement td = new HtmlElement("td");
        td.addAttribute(new Attribute("colspan", String.valueOf(pageColumnsConfig)));
        atr.addElement(td);
        HtmlElement div = new HtmlElement("div");
        div.addAttribute(new Attribute("class", "inner-list-container"));
        td.addElement(div);
        HtmlElement table = new HtmlElement("table");
        table.addAttribute(new Attribute("lay-filter", htmlElementInnerList.getTagId()));
        table.addAttribute(new Attribute("id", htmlElementInnerList.getTagId()));
        div.addElement(table);
    }

    //生成页面dropdownlist、switch、radio、checkbox、date及其它元素
    private void generateHtmlInputComponent(IntrospectedColumn introspectedColumn, HtmlElement parent, HtmlElement td) {
        HtmlElementDescriptor htmlElementDescriptor = htmlGeneratorConfiguration.getElementDescriptors().stream()
                .filter(t -> t.getName().equals(introspectedColumn.getActualColumnName())).findFirst().orElse(null);
        if (htmlElementDescriptor!=null) { //如果配置了当前字段的元素描述，则使用配置的元素描述
            switch (htmlElementDescriptor.getTagType().toLowerCase()) {
                case "dropdownlist":
                    DropdownListThymeleafHtmlGenerator dropdownListHtmlGenerator = new DropdownListThymeleafHtmlGenerator(generatorInitialParameters,introspectedColumn,htmlGeneratorConfiguration);
                    dropdownListHtmlGenerator.setHtmlElementDescriptor(htmlElementDescriptor);
                    dropdownListHtmlGenerator.addHtmlElement(parent);
                    break;
                case "switch":
                    //增加美化的switch
                    SwitchThymeleafHtmlGenerator switchHtmlGenerator = new SwitchThymeleafHtmlGenerator(generatorInitialParameters,introspectedColumn,htmlGeneratorConfiguration);
                    switchHtmlGenerator.setHtmlElementDescriptor(htmlElementDescriptor);
                    switchHtmlGenerator.addHtmlElement(parent);
                    break;
                case "radio":
                    RadioThymeleafHtmlGenerator radioHtmlGenerator = new RadioThymeleafHtmlGenerator(generatorInitialParameters,introspectedColumn,htmlGeneratorConfiguration);
                    radioHtmlGenerator.setHtmlElementDescriptor(htmlElementDescriptor);
                    radioHtmlGenerator.addHtmlElement(parent);
                    break;
                case "checkbox":
                    CheckBoxThymeleafHtmlGenerator checkBoxHtmlGenerator = new CheckBoxThymeleafHtmlGenerator(generatorInitialParameters,introspectedColumn,htmlGeneratorConfiguration);
                    checkBoxHtmlGenerator.setHtmlElementDescriptor(htmlElementDescriptor);
                    checkBoxHtmlGenerator.addHtmlElement(parent);
                    break;
                case "date":
                    DateThymeleafHtmlElementGenerator dateHtmlElementGenerator = new DateThymeleafHtmlElementGenerator(generatorInitialParameters,introspectedColumn,htmlGeneratorConfiguration);
                    dateHtmlElementGenerator.setHtmlElementDescriptor(htmlElementDescriptor);
                    dateHtmlElementGenerator.addHtmlElement(parent);
                    break;
                case "select":
                    SelectElementGeneratorThymeleaf selectElementGenerator = new SelectElementGeneratorThymeleaf(generatorInitialParameters,introspectedColumn,htmlGeneratorConfiguration);
                    selectElementGenerator.setHtmlElementDescriptor(htmlElementDescriptor);
                    selectElementGenerator.addHtmlElement(parent);
                    break;
                default:
                    if (GenerateUtils.isDateType(introspectedColumn)) {
                        DateThymeleafHtmlElementGenerator date = new DateThymeleafHtmlElementGenerator(generatorInitialParameters,introspectedColumn,htmlGeneratorConfiguration);
                        date.setHtmlElementDescriptor(htmlElementDescriptor);
                        date.addHtmlElement(parent);
                    } else {
                        InputThymeleafHtmlElementGenerator inputHtmlElementGenerator = new InputThymeleafHtmlElementGenerator(generatorInitialParameters,introspectedColumn,htmlGeneratorConfiguration);
                        inputHtmlElementGenerator.setHtmlElementDescriptor(htmlElementDescriptor);
                        inputHtmlElementGenerator.addHtmlElement(parent);
                    }

            }
        } else {
            if (GenerateUtils.isDateType(introspectedColumn)) {
                DateThymeleafHtmlElementGenerator date = new DateThymeleafHtmlElementGenerator(generatorInitialParameters,introspectedColumn,htmlGeneratorConfiguration);
                date.addHtmlElement(parent);
            } else {
                InputThymeleafHtmlElementGenerator inputHtmlElementGenerator = new InputThymeleafHtmlElementGenerator(generatorInitialParameters,introspectedColumn,htmlGeneratorConfiguration);
                inputHtmlElementGenerator.addHtmlElement(parent);
            }
        }
    }

    private void drawLabel(IntrospectedColumn introspectedColumn, HtmlElement parent) {
        HtmlElementDescriptor htmlElementDescriptor = getHtmlElementDescriptor(introspectedColumn);
        HtmlElement label = new HtmlElement("label");
        addCssClassToElement(label, "layui-form-label");
        if (htmlElementDescriptor == null) {
            label.addAttribute(new Attribute("for", introspectedColumn.getJavaProperty()));
        } else if(!htmlElementDescriptor.getTagType().equals(HtmlElementTagTypeEnum.RADIO.codeName())){
            label.addAttribute(new Attribute("for", introspectedColumn.getJavaProperty()));
        }
        OverridePropertyValueGeneratorConfiguration overrideConfig = voGenService.getOverridePropertyValueConfiguration(introspectedColumn);
        if (overrideConfig != null && overrideConfig.getRemark() != null) {
            label.addElement(new TextElement(overrideConfig.getRemark()));
        } else {
            label.addElement(new TextElement(introspectedColumn.getRemarks(true)));
        }
        addCssClassToElement(label, "class-"+introspectedColumn.getActualColumnName());
        //追加样式css
        if (htmlElementDescriptor != null && htmlElementDescriptor.getLabelCss() != null) {
            addCssStyleToElement(label, htmlElementDescriptor.getLabelCss());
        }
        parent.addElement(label);
    }

    private void generateLayuiToolbar(HtmlElement parent) {
        HtmlElement toolBar = generateToolBar(parent);
        String config = getHtmlBarPositionConfig();
        if (!HTML_KEY_WORD_TOP.equals(config)) {
            HtmlElement btnClose = addLayButton(toolBar, btn_close_id, "关闭", "&#x1006;");
            addCssClassToElement(btnClose, "footer-btn","layui-btn-primary");
            if (htmlGeneratorConfiguration.getLayoutDescriptor().getLoadingFrameType().equals("inner")) {
                HtmlElement btnReset = addLayButton(toolBar, btn_reset_id, "重置", "&#xe9aa;");
                addCssClassToElement(btnReset, "footer-btn","btn-primary");
            }
        }
    }
}
