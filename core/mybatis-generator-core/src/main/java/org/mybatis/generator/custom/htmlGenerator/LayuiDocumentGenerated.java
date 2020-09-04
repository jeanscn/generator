/**
 *    Copyright 2006-2020 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
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
public class LayuiDocumentGenerated extends AbsHtmlDocumentGenerator {

    private IntrospectedTable introspectedTable;

    private Document document;

    private HtmlElement rootElement;

    private HtmlElement head;

    private HtmlElement body;

    private HtmlElement content;

    private final String input_subject_id = "subject";
    private final String form_verify_id = "btn_form_verify";

    public LayuiDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
        super(document,introspectedTable);
        this.introspectedTable = introspectedTable;
        this.document = document;
        this.rootElement = document.getRootElement();
        this.head =  generateLayuiHead();
        this.body = generateHtmlBody();
    }

    @Override
    public boolean htmlMapDocumentGenerated(){
        rootElement.addElement(head);
        rootElement.addElement(body);
        document.setRootElement(rootElement);
        List<HtmlElement> elements = getElmentByClassName("content");
        this.content = elements.get(0);
        HtmlElement form = generateForm(content);
        /*标题区域*/
        addSubjectInput(content);
        /*表单验证button*/
        addVerifyButton(content);
        generateLayuiToolBar(content);
        addLayJavaScriptFrament(body);
        return true;
    }

    private void addVerifyButton(HtmlElement parent) {
        HtmlElement btn = new HtmlElement("button");
        btn.addAttribute(new Attribute("type", "submit"));
        btn.addAttribute(new Attribute("id", form_verify_id));
        btn.addAttribute(new Attribute("lay-submit", ""));
        btn.addAttribute(new Attribute("lay-filter", form_verify_id));
        btn.addAttribute(new Attribute("style", "display: none;"));
        parent.addElement(btn);
    }

    private HtmlElement generateLayuiHead(){
        HtmlElement head = generateHtmlHead();
        addStaticReplace(head, "subpages/webjarsPluginsRequired.html::layuiRequired");
        addStaticReplace(head, "subpages/webjarsPluginsRequired.html::pureFormV2");
        addStaticJavaScript(head, "/webjars/plugins/js/mainform.min.js");
        addLocalStaticResource(head);
        return head;
    }

    private HtmlElement generateForm(HtmlElement parent){
        HtmlElement form = addDivWithClassToParent(parent,"layui-form");
        List<IntrospectedColumn> columns = Stream.of(introspectedTable.getPrimaryKeyColumns().stream()
                ,introspectedTable.getBaseColumns().stream()).flatMap(Function.identity())
                .collect(Collectors.toList());
        List<IntrospectedColumn> hiddenColumns = new ArrayList<>();
        List<IntrospectedColumn> displayColumns = new ArrayList<>();
        for (IntrospectedColumn baseColumn : columns) {
            if (GenerateUtils.isHiddenColumn(baseColumn)) {
                hiddenColumns.add(baseColumn);
            }else{
                displayColumns.add(baseColumn);
            }
        }
        Map<String,List<IntrospectedColumn>> baseColumnsRows = getHtmlRows(displayColumns);
        for (IntrospectedColumn baseColumn : displayColumns) {
            if (baseColumnsRows.get(baseColumn.getJavaProperty()) != null) {
                /*行*/
                HtmlElement formItem = addDivWithClassToParent(form,"layui-form-item");
                /*列*/
                for (IntrospectedColumn introspectedColumn : baseColumnsRows.get(baseColumn.getJavaProperty())) {
                    HtmlElement inline = addDivWithClassToParent(formItem,"layui-inline");
                    //label
                    HtmlElement label = new HtmlElement("label");
                    addClassNameToElement(label, "layui-form-label");
                    label.addElement(new TextElement(introspectedColumn.getRemarks()));
                    inline.addElement(label);
                    //input
                    HtmlElement block = addDivWithClassToParent(inline, "layui-input-block");
                    HtmlElement input = generateHtmlInput(introspectedColumn,false);
                    addClassNameToElement(input, "layui-input");
                    block.addElement(input);
                }
            }
        }
        if (hiddenColumns.size()>0) {
            for (IntrospectedColumn hiddenColumn : hiddenColumns) {
                HtmlElement input = generateHtmlInput(hiddenColumn,true);
                form.addElement(input);
            }
        }
        return form;
    }

    private HtmlElement generateLayuiToolBar(HtmlElement parent){
        HtmlElement toolBar = generateToolBar(parent);
        HtmlElement btnClose = addLayButton(toolBar, btn_close_id,"关闭","&#x1006;");
        HtmlElement btnSubmit = addLayButton(toolBar, btn_sumit_id,"保存","&#xe605;");
        String config = getHtmlBarPositionConfig();
        if (!HtmlConstants.HTML_KEY_WORD_TOP.equals(config)) {
            addClassNameToElement(btnClose, "footer-btn");
            addClassNameToElement(btnSubmit, "footer-btn");
        }
        return toolBar;
    };

    private HtmlElement addLayButton(HtmlElement parent, String id, String text, String unicode){
        HtmlElement btn = addButton(parent, id, null);
        addClassNameToElement(btn, "layui-btn layui-btn-sm");
        addLayIconFont(btn, unicode);
        if (text!=null) {
            btn.addElement(new TextElement(text));
        }
        return btn;
    }

    private HtmlElement addLayIconFont(HtmlElement parent, String unicode){
        HtmlElement icon;
        if (unicode!=null) {
            icon = new HtmlElement("i");
            addClassNameToElement(icon, "layui-icon");
            icon.addElement(new TextElement(unicode));
            parent.addElement(icon);
            return icon;
        }else{
            return null;
        }
    }

    private HtmlElement addLayJavaScriptFrament(HtmlElement parent){
        HtmlElement javascript = addJavaScriptFragment(parent);
        StringBuilder sb = new StringBuilder();
        sb.append("layui.use([");
        sb.append("'form'");
        sb.append("], function () {");
        javascript.addElement(new TextElement(sb.toString()));
        //layui js代码段
        sb.setLength(0);
        sb.append("let form = layui.form;");
        javascript.addElement(new TextElement(sb.toString()));
        javascript.addElement(new TextElement(""));

        javascript.addElement(new TextElement("var formVerifyStatus;"));
        sb.setLength(0);
        sb.append("form.on('submit(");
        sb.append(form_verify_id);
        sb.append(")', function (data) {");
        javascript.addElement(new TextElement(sb.toString()));
        javascript.addElement(new TextElement("formVerifyStatus = true;"));
        javascript.addElement(new TextElement("return false;"));
        javascript.addElement(new TextElement("});"));

        sb.setLength(0);
        sb.append("$('#");
        sb.append(btn_sumit_id);
        sb.append("').click(function () {");
        javascript.addElement(new TextElement(sb.toString()));
        javascript.addElement(new TextElement("formVerifyStatus = false;"));
        sb.setLength(0);
        sb.append("$('#");
        sb.append(form_verify_id);
        sb.append("').click();");
        javascript.addElement(new TextElement(sb.toString()));
        javascript.addElement(new TextElement("if (formVerifyStatus) {"));
        javascript.addElement(new TextElement("let data = $('.layui-form').serializeFormVJSON();"));
        sb.setLength(0);
        sb.append("let url = $.getRootPath() + '/");
        sb.append(introspectedTable.getControllerSimplePackage());
        sb.append("/");
        sb.append(introspectedTable.getControllerBeanName());
        sb.append("';");
        javascript.addElement(new TextElement(sb.toString()));
        javascript.addElement(new TextElement("let rtype = $('#id').val().length>0?'PUT':'POST';"));
        javascript.addElement(new TextElement("$.requestJsonSuccessCallback(url, function (resp) {"));
        javascript.addElement(new TextElement("$.refreshParentDatatables();"));
        javascript.addElement(new TextElement("$('#"+btn_close_id+"').click();"));
        javascript.addElement(new TextElement("}, {"));
        javascript.addElement(new TextElement("data: JSON.stringify(data),"));
        javascript.addElement(new TextElement("type: rtype"));
        javascript.addElement(new TextElement("})"));

        javascript.addElement(new TextElement("}"));
        javascript.addElement(new TextElement("});"));
        javascript.addElement(new TextElement(""));
        //关闭
        sb.setLength(0);
        sb.append("$('#");
        sb.append(btn_close_id);
        sb.append("').closeself();");
        javascript.addElement(new TextElement(sb.toString()));

        sb.setLength(0);
        sb.append("});");
        javascript.addElement(new TextElement(sb.toString()));
        return javascript;
    }
}
