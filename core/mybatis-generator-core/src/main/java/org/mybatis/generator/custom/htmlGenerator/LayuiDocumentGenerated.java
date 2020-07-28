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
import org.mybatis.generator.api.dom.html.Document;
import org.mybatis.generator.api.dom.html.HtmlElement;

import java.util.List;

/**
 * @description:
 * @author: <a href="mailto:cjj@vip.sina.com">ChenJJ</a>
 * @created: 2020-07-20 06:22
 * @version: 3.0
 */
public class LayuiDocumentGenerated extends AbsHtmlDocumentGenerator {

    private IntrospectedTable introspectedTable;

    private Document document;

    private HtmlElement rootElement;

    private HtmlElement head;

    private HtmlElement body;

    private HtmlElement container;

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
        List<HtmlElement> elements = getElmentByClassName("innerContainer");
        this.container = elements.get(0);

        HtmlElement div = new HtmlElement("div");
        List<IntrospectedColumn> baseColumns = introspectedTable.getBaseColumns();
        StringBuilder sb = new StringBuilder();
        for (IntrospectedColumn baseColumn : baseColumns) {
            HtmlElement input = generateHtmlInput(baseColumn);
            div.addElement(input);
        }
        container.addElement(div);

        HtmlElement container = new HtmlElement("div");
        addClassNameToElement(container, "container-fluid");
        container.addElement(container);

        return true;
    }

    private HtmlElement generateLayuiHead(){
        HtmlElement head = generateHtmlHead();
        addStaticReplace(head, "subpages/webjarsPluginsRequired.html::layuiRequired");
        addStaticReplace(head, "subpages/webjarsPluginsRequired.html::layuiForm");
        /*增加本地资源
        * 包內样式
        * */
        String p = getIntrospectedTable().getMyBatis3HtmlMapperPackage();
        if (p.lastIndexOf(".")>0) {
            p = p.substring(0, p.lastIndexOf("."));
        }
        addStaticStyleSheet(head, genLocalCssFilePath(p,p));
        addStaticJavaScript(head, genLocalJsFilePath(getIntrospectedTable().getMyBatis3HtmlMapperPackage(),
                getEntityType().getShortName().toLowerCase()));
        return head;
    }

}
