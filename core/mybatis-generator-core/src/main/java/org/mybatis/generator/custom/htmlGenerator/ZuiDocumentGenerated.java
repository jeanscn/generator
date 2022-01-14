/**
 * Copyright 2006-2020 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mybatis.generator.custom.htmlGenerator;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.html.Attribute;
import org.mybatis.generator.api.dom.html.Document;
import org.mybatis.generator.api.dom.html.HtmlElement;
import org.mybatis.generator.api.dom.html.TextElement;
import org.mybatis.generator.codegen.HtmlConstants;

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
public class ZuiDocumentGenerated extends AbsHtmlDocumentGenerator {

    private IntrospectedTable introspectedTable;

    private Document document;

    private HtmlElement rootElement;

    private HtmlElement head;

    private HtmlElement body;

    private HtmlElement content;

    public ZuiDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
        super(document, introspectedTable);
        this.introspectedTable = introspectedTable;
        this.document = document;
        this.rootElement = document.getRootElement();
        this.head = generateZuiHead();
        this.body = generateHtmlBody();
    }

    @Override
    public boolean htmlMapDocumentGenerated() {
        rootElement.addElement(head);
        rootElement.addElement(body);
        document.setRootElement(rootElement);
        List<HtmlElement> elements = getElmentByClassName("content");
        this.content = elements.get(0);
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
            if (GenerateUtils.isHiddenColumn(baseColumn)) {
                hiddenColumns.add(baseColumn);
            } else {
                displayColumns.add(baseColumn);
            }
        }
        int pageColumnsConfig = getPageColumnsConfig();
        Map<Integer, List<IntrospectedColumn>> baseColumnsRows = getHtmlRows(displayColumns);
        String entityKey = GenerateUtils.getEntityKeyStr(introspectedTable);
        HtmlElement caption = addDivWithClassToParent(form,"form-title");
        caption.addElement(new TextElement(introspectedTable.getRemarks()));
        /*计算响应式宽度*/
        String colWidth = "col-sm-" + String.valueOf(12 / pageColumnsConfig);
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
                addClassNameToElement(ictrl, "has-label-left");
                drawInput(introspectedColumn, entityKey, ictrl);
                drawLabel(introspectedColumn, ictrl);
            }
        }
        return form;
    }

    private HtmlElement generateZuiToolBar(HtmlElement parent) {
        HtmlElement toolBar = generateToolBar(parent);
        HtmlElement btnClose = addZuiButton(toolBar, btn_close_id, "关闭", "icon-times");
        HtmlElement btnSubmit = addZuiButton(toolBar, btn_sumit_id, "保存", "icon-check");
        String config = getHtmlBarPositionConfig();
        if (!HtmlConstants.HTML_KEY_WORD_TOP.equals(config)) {
            addClassNameToElement(btnClose, "footer-btn");
            addClassNameToElement(btnSubmit, "footer-btn");
        }
        return toolBar;
    }
    private HtmlElement addZuiButton(HtmlElement parent, String id, String text, String unicode) {
        HtmlElement btn = addButton(parent, id, null);
        addClassNameToElement(btn, "btn btn-sm");
        addZuiIconFont(btn, unicode);
        if (text != null) {
            btn.addElement(new TextElement(text));
        }
        return btn;
    }

    private HtmlElement addZuiIconFont(HtmlElement parent, String unicode) {
        HtmlElement icon = new HtmlElement("i");
        addClassNameToElement(icon, "icon " + unicode);
        parent.addElement(icon);
        return icon;
    }

    private void drawLabel(IntrospectedColumn introspectedColumn, HtmlElement parent) {
        HtmlElement label = new HtmlElement("label");
        addClassNameToElement(label, "input-control-label-left");
        label.addAttribute(new Attribute("for", introspectedColumn.getJavaProperty()));
        label.addElement(new TextElement(introspectedColumn.getRemarks()));
        parent.addElement(label);
    }

    private void drawInput(IntrospectedColumn introspectedColumn, String entityKey, HtmlElement parent) {
        HtmlElement input = generateHtmlInput(introspectedColumn, false);
        input.addAttribute(new Attribute("th:value", thymeleafValue(introspectedColumn, entityKey)));
        addClassNameToElement(input, "form-control");
        parent.addElement(input);
        if (GenerateUtils.isWorkflowInstance(introspectedTable)) {
            addClassNameToElement(input, "oas-form-item-edit");
            HtmlElement div = addDivWithClassToParent(parent, "oas-form-item-read");
            div.addAttribute(new Attribute("th:text", thymeleafValue(introspectedColumn, entityKey)));
        }
    }

}
