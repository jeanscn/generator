package org.mybatis.generator.codegen.mybatis3.htmlmapper.document;

import com.vgosoft.core.constant.enums.view.HtmlElementTagTypeEnum;
import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.html.Attribute;
import org.mybatis.generator.api.dom.html.Document;
import org.mybatis.generator.api.dom.html.HtmlElement;
import org.mybatis.generator.api.dom.html.TextElement;
import org.mybatis.generator.codegen.GeneratorInitialParameters;
import org.mybatis.generator.codegen.mybatis3.htmlmapper.GenerateUtils;
import org.mybatis.generator.config.*;
import org.mybatis.generator.custom.ThymeleafValueScopeEnum;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author <a href="mailto:cjj@vip.sina.com">ChenJJ</a>
 * 2020-07-20 06:22
 * @version 3.0
 */
public class LayuiPrintDocumentGenerated extends AbstractThymeleafHtmlDocumentGenerator {
    private final Document document;

    private final HtmlElement rootElement;

    private final HtmlElement head;

    private final Map<String, HtmlElement> body;

    private final HtmlGeneratorConfiguration htmlGeneratorConfiguration;

    public LayuiPrintDocumentGenerated(GeneratorInitialParameters generatorInitialParameters, Document document, HtmlGeneratorConfiguration htmlGeneratorConfiguration) {
        super(generatorInitialParameters, document, htmlGeneratorConfiguration);
        this.htmlGeneratorConfiguration = htmlGeneratorConfiguration;
        this.document = document;
        this.rootElement = document.getRootElement();
        this.head = generatePrintHead();
        this.body = generateHtmlBody();
    }

    @Override
    public boolean htmlMapDocumentGenerated() {
        rootElement.addElement(head);
        rootElement.addElement(body.get("body"));
        document.setRootElement(rootElement);
        HtmlElement content = body.get("content");
        HtmlElement scriptElement = new HtmlElement("script");

        //打印的表格
        generateTable(content, scriptElement);
        //打印时间及打印人
        HtmlElement printer = addDivWithClassToParent(content, "print-foot");
        HtmlElement p = new HtmlElement("p");
        printer.addElement(p);
        HtmlElement l1 = new HtmlElement("span");
        l1.addElement(new TextElement("打印时间："));
        p.addElement(l1);
        HtmlElement l2 = new HtmlElement("span");
        l2.addAttribute(new Attribute("th:text", "${#dates.format(#dates.createNow(), 'yyyy-MM-dd HH:mm:ss')}"));
        p.addElement(l2);
        HtmlElement l3 = new HtmlElement("span");
        l3.addElement(new TextElement("&nbsp;打印人："));
        p.addElement(l3);
        HtmlElement l4 = new HtmlElement("span");
        l4.addAttribute(new Attribute("th:text", "${currentUser?.name}?:_"));
        p.addElement(l4);
        //隐藏input
        HtmlElement workflowEnabled = generateHtmlInput("workflowEnabled", true, false, true, false);
        content.addElement(workflowEnabled);
        if (GenerateUtils.isWorkflowInstance(introspectedTable)) {
            HtmlElement fileCategory = generateHtmlInput("fileCategory", true, false, true, false);
            fileCategory.addAttribute(new Attribute("th:value", "${" + GenerateUtils.getEntityKeyStr(introspectedTable) + "?.fileCategory}?:_"));
            content.addElement(fileCategory);
            HtmlElement regDocNumber = generateHtmlInput("regDocNumber", true, false, true, false);
            regDocNumber.addAttribute(new Attribute("th:value", "${" + GenerateUtils.getEntityKeyStr(introspectedTable) + "?.regDocNumber}?:_"));
            content.addElement(regDocNumber);
            HtmlElement deptName = generateHtmlInput("deptName", true, false, true, false);
            deptName.addAttribute(new Attribute("th:value", "${currentDept?.name}?:_"));
            content.addElement(deptName);
            HtmlElement userName = generateHtmlInput("wfState", true, false, true, false);
            userName.addAttribute(new Attribute("th:value", "${" + GenerateUtils.getEntityKeyStr(introspectedTable) + ".wfState == 2 ? '文件审批通过': '文件没有正常完成审批'}"));
            content.addElement(userName);
            workflowEnabled.addAttribute(new Attribute("value", "1"));
        } else {
            workflowEnabled.addAttribute(new Attribute("value", "0"));
        }
        HtmlElement subject = generateHtmlInput(input_subject_id, true, false, true, true);
        String title = getDocTitle();
        if (VStringUtil.stringHasValue(title)) {
            subject.addAttribute(new Attribute("th:value", title));
        }
        content.addElement(subject);
        generatePrintToolbar(content);

        //增加页面列表的编辑器模板页面片段
        if (introspectedTable.getRules().isAdditionInnerList(htmlGeneratorConfiguration)) {
            HtmlElementInnerListConfiguration listConfiguration = htmlGeneratorConfiguration.getHtmlElementInnerListConfiguration().get(0);
            HtmlElement div = new HtmlElement("div");
            body.get("body").addElement(div);
            String moduleKeyword = VStringUtil.stringHasValue(listConfiguration.getModuleKeyword()) ? listConfiguration.getModuleKeyword() : introspectedTable.getContext().getModuleKeyword();

        }
        /* 添加公共属性 */
        HtmlElement coreSub = new HtmlElement("div");
        coreSub.addAttribute(new Attribute("th:replace", "vgoweb/fragments/vgocoresub.html::vgocoresub"));
        body.get("body").addElement(coreSub);
        /*是否需要添加script元素*/
        if (!scriptElement.getElements().isEmpty()) {
            body.get("body").addElement(scriptElement);
        }
        return true;
    }

    private HtmlElement generatePrintHead() {
        HtmlElement head = new HtmlElement("head");
        HtmlElement meta = new HtmlElement("meta");
        meta.addAttribute(new Attribute("charset", "UTF-8"));
        head.addElement(meta);
        HtmlElement t = new HtmlElement("title");
        t.addElement(new TextElement(getHtmlTitle()));
        head.addElement(t);
        addStaticReplace(head, "subpages/webjarsPluginsRequired2.html::jQueryRequired");
        addStaticReplace(head, "subpages/webjarsPluginsRequired2.html::layuiRequired");
        addStaticThymeleafStyleSheet(head, "/webjars/plugins/printjs/css/print.min.css");
        addStaticThymeleafJavaScript(head, "/webjars/plugins/printjs/js/print.min.js");
        if (GenerateUtils.isWorkflowInstance(introspectedTable)) {
            addStaticThymeleafJavaScript(head, "/webjars/plugins/jquery/jquery.qrcode.min.js");
            addStaticThymeleafJavaScript(head, "/webjars/plugins/jquery/jquery.watermark.min.js");
        }
        addStaticThymeleafStyleSheet(head, "/webjars/plugins/css/printjs.css");
        addStaticThymeleafJavaScript(head, "/webjars/plugins/js/printjs.js");
        return head;
    }

    private void generateTable(HtmlElement parent, HtmlElement scriptElement) {
        HtmlElement table = new HtmlElement("table");
        parent.addElement(table);
        table.addAttribute(new Attribute("id", "main-table"));
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
        List<HtmlFileAttachmentConfiguration> attachmentConfiguration = htmlGeneratorConfiguration.getHtmlFileAttachmentConfiguration();
        if (!attachmentConfiguration.isEmpty() && attachmentConfiguration.get(0).isGenerate()) {
            HtmlFileAttachmentConfiguration fileAttachmentConfiguration = attachmentConfiguration.get(0);
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
        Map<Integer, List<IntrospectedColumn>> baseColumnsRows = getHtmlRows(displayColumns, 6);
        String entityKey = GenerateUtils.getEntityKeyStr(introspectedTable);
        List<IntrospectedColumn> rtfColumn = new ArrayList<>();
        for (List<IntrospectedColumn> rowIntrospectedColumns : baseColumnsRows.values()) {
            /*行*/
            HtmlElement tr = addTrWithClassToParent(table, "");
            /*列*/
            int colNum = 0;
            for (IntrospectedColumn introspectedColumn : rowIntrospectedColumns) {
                colNum++;
                HtmlElement tdl = addDtWithClassToTr(tr, "", 0);
                //label
                drawLabel(introspectedColumn, tdl);
                //显示内容
                HtmlElement tdv = addDtWithClassToTr(tr, "", 0);
                //如果是单独一列，且长度大于255，则占满一行
                if (rowIntrospectedColumns.size() == 1
                        && (rowIntrospectedColumns.get(0).getLength() > 255
                        || this.htmlGeneratorConfiguration.getLayoutDescriptor().getExclusiveColumns().contains(rowIntrospectedColumns.get(0).getActualColumnName()))) {
                    addColspanToTd(tdv, pageColumnsConfig * 2 - 1);
                    colNum = pageColumnsConfig;
                }
                if (introspectedColumn.isLongVarchar()) {
                    rtfColumn.add(introspectedColumn);
                    drawRtfContentDiv(entityKey, introspectedColumn, tdv);
                } else {
                    tdv.addAttribute(new Attribute("th:text", getThymeleafValueFieldName(introspectedColumn)));
                }
            }
            /*如果列数小于指定列数，则后面补充空单元格*/
            if (colNum < pageColumnsConfig && rowIntrospectedColumns.get(0).getLength() <= 255) {
                for (int i = (pageColumnsConfig - colNum) * 2; i > 0; i--) {
                    HtmlElement td = new HtmlElement("td");
                    tr.addElement(td);
                }
            }
        }
        //添加附件
        if (!attachmentConfiguration.isEmpty() && attachmentConfiguration.get(0).isGenerate()) {
            String label = attachmentConfiguration.get(0).getLabel();
            HtmlElement atr = new HtmlElement("tr");
            table.addElement(atr);
            HtmlElement tdl = addDtWithClassToTr(atr, "label", 0);
            tdl.addElement(new TextElement(label));
            HtmlElement tdv = addDtWithClassToTr(atr, "", pageColumnsConfig * 2 - 1);
            tdv.addAttribute(new Attribute("id", "attachments"));
            HtmlElement block = new HtmlElement("th:block");
            block.addAttribute(new Attribute("th:each", "item: ${attachments}"));
            tdv.addElement(block);
            HtmlElement p = new HtmlElement("p");
            block.addElement(p);
            p.addAttribute(new Attribute("th:text", "${item?.name}?:_"));
        }
        //添加意见
        htmlGeneratorConfiguration.getHtmlApprovalCommentConfigurations().forEach(configuration -> {
            if (configuration.isGenerate()) {
                addApprovalCommentTag(pageColumnsConfig, configuration, table);
            }
        });

        //是否需要插入页面列表
        if (introspectedTable.getRules().isAdditionInnerList(htmlGeneratorConfiguration)) {
            HtmlElementInnerListConfiguration listConfiguration = htmlGeneratorConfiguration.getHtmlElementInnerListConfiguration().get(0);
            HtmlLayoutDescriptor layoutDescriptor = htmlGeneratorConfiguration.getLayoutDescriptor();
            if (listConfiguration.getPrintMode().equals("form")) {
                // 添加内置列表，打印模式为form时
                String labelWidth = layoutDescriptor.getLabelWidth();
                int pageColumnsNum = listConfiguration.getPrintFormColumnsNum();  // 用来计算colspan的值
                String placeId = "detail-table";
                String tplId = "detailTableTpl";
                //渲染的占位元素
                HtmlElement detailTable = new HtmlElement("table");
                detailTable.addAttribute(new Attribute("id", placeId));
                parent.addElement(detailTable);
                // 子表layui-html模板
                HtmlElement script = new HtmlElement("script");
                script.addAttribute(new Attribute("type", "text/html"));
                script.addAttribute(new Attribute("id", tplId));
                parent.addElement(script);
                HtmlElement innerListBody = new HtmlElement("tbody");
                script.addElement(innerListBody);
                innerListBody.addElement(new TextElement("{{# layui.each(d, function(index, item){ }}"));
                //要显示的列（属性）
                List<String> printFields = listConfiguration.getPrintFields();
                if (!printFields.isEmpty()) {
                    // 处理第一个字段
                    String firstField = printFields.get(0);
                    HtmlElement trFirst = new HtmlElement("tr");
                    HtmlElement tdFirstLabel = new HtmlElement("td");
                    tdFirstLabel.addAttribute(new Attribute("class", "label"));
                    tdFirstLabel.addAttribute(new Attribute("style", "width: " + labelWidth + ";word-wrap: break-word;"));
                    tdFirstLabel.addAttribute(new Attribute("th:text", "${ innerListHeaderMap['"+firstField+"'] }"));
                    trFirst.addElement(tdFirstLabel);
                    HtmlElement tdFirstValue = new HtmlElement("td");
                    if (pageColumnsNum>1) {
                        tdFirstValue.addAttribute(new Attribute("colspan", String.valueOf(pageColumnsNum * 2 -1)));
                    }
                    trFirst.addElement(tdFirstValue);
                    tdFirstValue.addElement(new TextElement("{{= item." + firstField + " }}"));
                    innerListBody.addElement(trFirst);
                    // 处理剩余字段
                    HtmlElement tr = new HtmlElement("tr");
                    innerListBody.addElement(tr);
                    int colCount = 0;
                    HtmlElement lastTd = null;
                    for (int i = 1; i < printFields.size(); i++) {
                        String fieldName = printFields.get(i);
                        // 创建label td
                        HtmlElement tdLabel = new HtmlElement("td");
                        tdLabel.addAttribute(new Attribute("class", "label"));
                        tdLabel.addAttribute(new Attribute("style", "width: " + labelWidth + ";word-wrap: break-word;"));
                        tdLabel.addAttribute(new Attribute("th:text", "${ innerListHeaderMap['"+fieldName+"'] }"));
                        tr.addElement(tdLabel);
                        // 创建value td
                        HtmlElement tdValue = new HtmlElement("td");
                        tdValue.addAttribute(new Attribute("class", "detail-value"));
                        tdValue.addAttribute(new Attribute("style", "word-wrap: break-word;"));
                        tdValue.addElement(new TextElement("{{= item." + fieldName + " }}"));
                        tr.addElement(tdValue);
                        lastTd = tdValue;
                        colCount += 2;
                        // 如果当前行已满，创建新行
                        if (colCount >= pageColumnsNum * 2 && i < printFields.size() - 1) {
                            tr = new HtmlElement("tr");
                            innerListBody.addElement(tr);
                            colCount = 0;
                        }
                    }
                    // 如果最后一行未满，合并剩余列
                    if (colCount > 0 && colCount < pageColumnsNum * 2) {
                        lastTd.addAttribute(new Attribute("colspan", String.valueOf(pageColumnsNum * 2 - colCount + 1)));
                    }
                } else {
                    HtmlElement tr = new HtmlElement("tr");
                    HtmlElement td = new HtmlElement("td");
                    td.addElement(new TextElement("没有配置要打印的字段"));
                    tr.addElement(td);
                    innerListBody.addElement(tr);
                }
                innerListBody.addElement(new TextElement("{{# }); }}"));


                scriptElement.addAttribute(new Attribute("th:inline", "javascript"));
                scriptElement.addElement(new TextElement("    layui.use(['laytpl'], function () {"));
                scriptElement.addElement(new TextElement("        let innerListHeaders = /*[[${innerListHeaders}]]*/[];"));
                if (listConfiguration.getRelationKey() != null) {
                    scriptElement.addElement(new TextElement("        let relationField = /*[[${" + GenerateUtils.getEntityKeyStr(introspectedTable) + "?."+ listConfiguration.getRelationKey() +"}]]*/'';"));
                }else{
                    scriptElement.addElement(new TextElement("        let relationField = /*[[${" + GenerateUtils.getEntityKeyStr(introspectedTable) + "?.id}]]*/'';"));
                }
                scriptElement.addElement(new TextElement("        let dataUrl = \""+ listConfiguration.getDataUrl() +"?pageSize=0&" + listConfiguration.getRelationField() + "=\" + relationField;"));
                scriptElement.addElement(new TextElement("        const laytpl = layui.laytpl;"));
                scriptElement.addElement(new TextElement("        let getTpl = document.getElementById('"+tplId+"').innerHTML;"));
                scriptElement.addElement(new TextElement("        const elemView = document.getElementById('"+placeId+"');"));
                scriptElement.addElement(new TextElement("        $.requestJsonSuccessCallback($.addRootPath(dataUrl), function (resp) {"));
                scriptElement.addElement(new TextElement("            let data = resp.data;"));
                scriptElement.addElement(new TextElement("            laytpl(getTpl).render(data, function (html) {"));
                scriptElement.addElement(new TextElement("                elemView.innerHTML = html;"));
                scriptElement.addElement(new TextElement("            });"));
                scriptElement.addElement(new TextElement("        }, {type: \"GET\"});"));
                scriptElement.addElement(new TextElement("    });"));

            } else {
                // 添加内置列表，打印模式为table时
                HtmlElement tableList = new HtmlElement("table");
                tableList.addAttribute(new Attribute("id", "detail-table"));
                tableList.addAttribute(new Attribute("th:if", "${not #lists.isEmpty(innerListHeaders)}"));
                parent.addElement(tableList);
                HtmlElement thead = new HtmlElement("thead");
                tableList.addElement(thead);
                HtmlElement tr = new HtmlElement("tr");
                thead.addElement(tr);
                HtmlElement th = new HtmlElement("td");
                th.addAttribute(new Attribute("th:each", "header:${innerListHeaders}"));
                th.addAttribute(new Attribute("th:text", "${header.title}"));
                th.addAttribute(new Attribute("class", "item_label"));
                tr.addElement(th);
                HtmlElement tbody = new HtmlElement("tbody");
                tbody.addAttribute(new Attribute("id", "innerListBody"));
                tableList.addElement(tbody);

                scriptElement.addAttribute(new Attribute("th:inline", "javascript"));
                scriptElement.addElement(new TextElement("    $(function () {"));
                scriptElement.addElement(new TextElement("        let innerListHeaders = /*[[${innerListHeaders}]]*/[];"));
                if (listConfiguration.getRelationKey() != null) {
                    scriptElement.addElement(new TextElement("        let relationField = /*[[${" + GenerateUtils.getEntityKeyStr(introspectedTable) + "?."+ listConfiguration.getRelationKey() +"}]]*/'';"));
                }else{
                    scriptElement.addElement(new TextElement("        let relationField = /*[[${" + GenerateUtils.getEntityKeyStr(introspectedTable) + "?.id}]]*/'';"));
                }
                scriptElement.addElement(new TextElement("        let dataUrl = \"" + listConfiguration.getDataUrl() + "?pageSize=0&" + listConfiguration.getRelationField() + "=\"+relationField;"));
                scriptElement.addElement(new TextElement("        let innerBody = $(\"#innerListBody\");"));
                scriptElement.addElement(new TextElement("        loadDetail(innerListHeaders, dataUrl, innerBody, layui);"));
                scriptElement.addElement(new TextElement("    });"));
            }
        }
    }

    private void addApprovalCommentTag(int pageColumnsConfig, HtmlApprovalCommentConfiguration configuration, HtmlElement table) {
        HtmlElement atr = new HtmlElement("tr");
        table.addElement(atr);
        HtmlElement tdl = addDtWithClassToTr(atr, "label", 0);
        tdl.addElement(new TextElement(configuration.getLabel()));
        HtmlElement tdv = addDtWithClassToTr(atr, "", pageColumnsConfig * 2 - 1);

        HtmlElement tableTrack = new HtmlElement("table");
        addCssClassToElement(tableTrack, "layui-table", "inner-traceInfo");
        tdv.addElement(tableTrack);
        HtmlElement tr1 = new HtmlElement("tr");
        tr1.addAttribute(new Attribute("th:each", "item,itemStat:${comments}"));
        tableTrack.addElement(tr1);
        HtmlElement td1 = new HtmlElement("td");
        tr1.addElement(td1);
        HtmlElement commentDiv = new HtmlElement("div");
        addCssClassToElement(commentDiv, "traceInfo-comment");
        commentDiv.addAttribute(new Attribute("th:utext", "${item.busComment}"));
        td1.addElement(commentDiv);
        HtmlElement signatureDiv = addDivWithClassToParent(td1, "traceInfo-signature");
        HtmlElement span1 = new HtmlElement("span");
        addCssClassToElement(span1, "traceInfo-username");
        span1.addAttribute(new Attribute("th:utext", "${item.userName}"));
        signatureDiv.addElement(span1);
        HtmlElement span2 = new HtmlElement("span");
        addCssClassToElement(span2, "traceInfo-username");
        span2.addAttribute(new Attribute("th:utext", "${item.deptName}"));
        signatureDiv.addElement(span2);
        HtmlElement span3 = new HtmlElement("span");
        addCssClassToElement(span3, "traceInfo-time");
        span3.addAttribute(new Attribute("th:utext", "${#temporals.format(item.signDateTime, 'yyyy-MM-dd HH:mm:ss')}"));
        signatureDiv.addElement(span3);
    }

    private void addInnerList(HtmlElement content, int pageColumnsConfig) {
        HtmlElement atr = new HtmlElement("tr");
        content.addElement(atr);
        HtmlElement tdl = addDtWithClassToTr(atr, "label", 0);
        tdl.addElement(new TextElement("审批信息"));
        HtmlElement tdv = addDtWithClassToTr(atr, "", pageColumnsConfig * 2 - 1);
        HtmlElement tableTrack = new HtmlElement("table");
        addCssClassToElement(tableTrack, "layui-table", "inner-traceInfo");
        tdv.addElement(tableTrack);
        HtmlElement tr1 = new HtmlElement("tr");
        tr1.addAttribute(new Attribute("th:each", "item,itemStat:${traceInfo}"));
        tableTrack.addElement(tr1);
        HtmlElement td1 = new HtmlElement("td");
        tr1.addElement(td1);
        HtmlElement span1 = new HtmlElement("span");
        span1.addAttribute(new Attribute("th:utext", "${item.comment}"));
        addCssClassToElement(span1, "traceInfo-comment");
        td1.addElement(span1);
        HtmlElement span2 = new HtmlElement("span");
        span2.addAttribute(new Attribute("th:text", "${item.username}"));
        addCssClassToElement(span2, "traceInfo-username");
        td1.addElement(span2);
        HtmlElement span3 = new HtmlElement("span");
        span3.addAttribute(new Attribute("th:text", "${item.commentTime==null?(item.endTime==null?item.startTimeText:item.endTimeText):item.commentTimeText}"));
        addCssClassToElement(span3, "traceInfo-time");
        td1.addElement(span3);
    }

    private void addTraceInfo(HtmlElement content, HtmlGeneratorConfiguration htmlGeneratorConfiguration, int pageColumnsConfig) {
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
    private String getThymeleafValueFieldName(IntrospectedColumn introspectedColumn) {
        HtmlElementDescriptor htmlElementDescriptor = htmlGeneratorConfiguration.getElementDescriptors().stream()
                .filter(t -> t.getName().equals(introspectedColumn.getActualColumnName())).findFirst().orElse(null);
        if (introspectedColumn.isJDBCDateColumn() || introspectedColumn.isJavaLocalDateColumn() || introspectedColumn.isJavaLocalDateTimeColumn() || (htmlElementDescriptor != null && htmlElementDescriptor.getTagType().equals(HtmlElementTagTypeEnum.DATE.codeName()))) {
            return getDateFieldValueFormatPattern(introspectedColumn, ThymeleafValueScopeEnum.READONLY);
        } else {
            return thymeleafValue(introspectedColumn, ThymeleafValueScopeEnum.READONLY, htmlElementDescriptor);
        }
    }

    private void drawLabel(IntrospectedColumn introspectedColumn, HtmlElement td) {
        HtmlElementDescriptor htmlElementDescriptor = getHtmlElementDescriptor(introspectedColumn);
        addCssClassToElement(td, "label");
        OverridePropertyValueGeneratorConfiguration overrideConfig = voGenService.getOverridePropertyValueConfiguration(introspectedColumn);
        if (overrideConfig != null && overrideConfig.getRemark() != null) {
            td.addElement(new TextElement(overrideConfig.getRemark()));
        } else {
            td.addElement(new TextElement(introspectedColumn.getRemarks(true)));
        }
        //追加样式css
        if (htmlElementDescriptor != null && htmlElementDescriptor.getLabelCss() != null) {
            addCssStyleToElement(td, htmlElementDescriptor.getLabelCss());
        }
    }

    private void generatePrintToolbar(HtmlElement parent) {
        HtmlElement toolBar = generateToolBar(parent);
        String config = getHtmlBarPositionConfig();
        if (!HTML_KEY_WORD_TOP.equals(config)) {
            addHtmlButton(toolBar, "btn_close", "关闭", "button", "footer-btn");
            addHtmlButton(toolBar, "btn_print", "打印", "button", "footer-btn");
        }
    }
}
