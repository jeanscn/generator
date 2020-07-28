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
import org.mybatis.generator.api.dom.html.VisitableElement;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.config.PropertyRegistry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: <a href="mailto:cjj@vip.sina.com">ChenJJ</a>
 * @created: 2020-07-23 00:57
 * @version: 3.0
 */
public abstract class AbsHtmlDocumentGenerator implements HtmlDocumentGenerator {

    private Document document;
    private IntrospectedTable introspectedTable;
    private FullyQualifiedJavaType entityType;
    private int pageColumns;

    public AbsHtmlDocumentGenerator(Document document, IntrospectedTable introspectedTable) {
        this.document = document;
        this.introspectedTable = introspectedTable;
        this.entityType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        String uiFrame = Optional.ofNullable(introspectedTable.getContext().getProperty(PropertyRegistry.TABLE_HTML_UI_FRAME))
                .orElse("layui");
    }

    public abstract boolean htmlMapDocumentGenerated();

    protected HtmlElement generateHtmlHead() {
        HtmlElement head = new HtmlElement("head");
        HtmlElement div = new HtmlElement("div");
        addStaticReplace(head, "subpages/webjarsPluginsRequired.html::baseRequired('" + introspectedTable.getRemarks() + "')");
        addStaticReplace(head, "subpages/webjarsPluginsRequired.html::jQueryRequired");
        return head;
    }

    protected void addStaticStyleSheet(HtmlElement htmlElement, String value) {
        HtmlElement div = new HtmlElement("link");
        div.addAttribute(new Attribute("rel", "stylesheet"));
        div.addAttribute(new Attribute("type", "text/css"));
        if (value != null) {
            div.addAttribute(new Attribute("th:href", "@{" + value + "}"));
        }
        htmlElement.addElement(div);
    }

    protected void addStaticJavaScript(HtmlElement htmlElement, String value) {
        HtmlElement div = new HtmlElement("script");
        div.addAttribute(new Attribute("charset", "utf-8"));
        if (value != null) {
            div.addAttribute(new Attribute("th:src", "@{" + value + "}"));
        }
        div.addAttribute(new Attribute("type", "text/javascript"));
        htmlElement.addElement(div);
    }

    protected void addStaticReplace(HtmlElement htmlElement, String value) {
        HtmlElement div = new HtmlElement("div");
        div.addAttribute(new Attribute("th:replace", value));
        htmlElement.addElement(div);
    }

    protected HtmlElement generateHtmlBody() {
        HtmlElement body = new HtmlElement("body");
        HtmlElement out = new HtmlElement("div");
        out.addAttribute(new Attribute("class", "outContainer"));
        HtmlElement inner = new HtmlElement("div");
        inner.addAttribute(new Attribute("class", "innerContainer"));
        out.addElement(inner);
        body.addElement(out);
        return body;
    }

    protected HtmlElement generateHtmlInput(IntrospectedColumn baseColumn) {
        StringBuilder sb = new StringBuilder();
        HtmlElement input = new HtmlElement("input");
        input.addAttribute(new Attribute("id", baseColumn.getJavaProperty()));
        input.addAttribute(new Attribute("name", baseColumn.getJavaProperty()));
        input.addAttribute(new Attribute("type", "text"));
        sb.setLength(0);
        sb.append("${entity?.").append(baseColumn.getJavaProperty());
        if ("DATE".equals(baseColumn.getJdbcTypeName().toUpperCase())) {
            sb.append("!=null?#dates.format(entity.");
            sb.append(baseColumn.getJavaProperty()).append(",'yyyy-MM-dd'):''}");
        } else if ("TIME".equals(baseColumn.getJdbcTypeName().toUpperCase())) {
            sb.append("!=null?#dates.format(entity.");
            sb.append(baseColumn.getJavaProperty()).append(",'HH:mm:ss'):''}");
        } else if ("TIMESTAMP".equals(baseColumn.getJdbcTypeName().toUpperCase())) {
            sb.append("!=null?#dates.format(entity.");
            sb.append(baseColumn.getJavaProperty()).append(",'yyyy-MM-dd HH:mm:ss'):''}");
        } else {
            sb.append("}?:_");
        }
        input.addAttribute(new Attribute("th:value", sb.toString()));
        return input;
    }

    protected String genLocalCssFilePath(String path, String filename) {
        return genLocalFilePath(path, filename, "css");
    }

    protected String genLocalJsFilePath(String path, String filename) {
        return genLocalFilePath(path, filename, "js");
    }

    private String genLocalFilePath(String path, String filename, String type) {
        StringBuilder sb = new StringBuilder();
        sb.append("/" + type + "/");
        if (path != null && path.length() > 0) {
            if (path.indexOf(".") > -1) {
                path = path.replace(".", "/");
            }
            sb.append(path).append("/");
        }
        if (filename != null && filename.length() > 0) {
            sb.append(filename).append(".").append(type);
        }
        return sb.toString();
    }

    protected Document getDocument() {
        return document;
    }

    protected IntrospectedTable getIntrospectedTable() {
        return introspectedTable;
    }

    protected void addClassNameToElement(HtmlElement element, String className) {
        boolean isExsit = false;
        for (Attribute attribute : element.getAttributes()) {
            if ("class".equals(attribute.getName())) {
                String[] classNames = attribute.getValue().split(" ");
                List<String> listClassNames = Arrays.asList(classNames);
                if (!listClassNames.contains(className)) {
                    listClassNames.add(className);
                    String v = listClassNames.stream().collect(Collectors.joining(" "));
                    attribute.setValue(v);
                }
                isExsit = true;
            }
        }
        element.addAttribute(new Attribute("class", className));
    }

    protected List<HtmlElement> getElmentByClassName(String className) {
        List<HtmlElement> answer = new ArrayList<>();
        for (VisitableElement element : document.getRootElement().getAllElements()) {
            HtmlElement htmlElement = (HtmlElement) element;
            if (htmlElement.getAttributes().size() > 0) {
                for (Attribute attribute : htmlElement.getAttributes()) {
                    if ("class".equals(attribute.getName())) {
                        if (attribute.getValue() != null) {
                            if (Arrays.asList(attribute.getValue().split(" ")).contains(className)) {
                                answer.add((HtmlElement) element);
                            }
                        }
                    }
                }
            }
        }
        return answer;
    }

    public FullyQualifiedJavaType getEntityType() {
        return entityType;
    }

    public void setEntityType(FullyQualifiedJavaType entityType) {
        this.entityType = entityType;
    }
}
