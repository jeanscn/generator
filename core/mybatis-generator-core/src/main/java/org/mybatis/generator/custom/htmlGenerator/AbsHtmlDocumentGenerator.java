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
import org.mybatis.generator.api.dom.html.*;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.codegen.HtmlConstants;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.PropertyRegistry;
import org.mybatis.generator.config.TableConfiguration;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author: <a href="mailto:cjj@vip.sina.com">ChenJJ</a>
 *  2020-07-23 00:57
 * @version: 3.0
 */
public abstract class AbsHtmlDocumentGenerator implements HtmlDocumentGenerator {

    private Document document;
    private IntrospectedTable introspectedTable;
    private FullyQualifiedJavaType entityType;
    private TableConfiguration tableConfiguration;
    private Context context;
    protected final String btn_sumit_id = "btn_save";
    protected final String btn_close_id = "btn_close";
    protected final String input_subject_id = "subject";

    public AbsHtmlDocumentGenerator(Document document, IntrospectedTable introspectedTable) {
        this.document = document;
        this.introspectedTable = introspectedTable;
        this.entityType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        this.tableConfiguration = introspectedTable.getTableConfiguration();
        this.context = introspectedTable.getContext();
    }

    public int getPageColumnsConfig() {
        String pcStr = "2";
        Optional<String> propertyt = Optional.ofNullable(tableConfiguration.getProperty(PropertyRegistry.TABLE_HTML_PAGE_COLUMNS));
        if (!propertyt.isPresent()) {
            Optional<String> propertyc = Optional.ofNullable(context.getProperty(PropertyRegistry.TABLE_HTML_PAGE_COLUMNS));
            if (!propertyc.isPresent()) {
                pcStr = "2";
            } else {
                pcStr = propertyc.get();
            }
        } else {
            pcStr = propertyt.get();
        }
        int c = Integer.valueOf(pcStr);
        if (c > 12) {
            c = 12;
        } else if (c <= 0) {
            c = 2;
        }
        if (12 % c == 0) {
            return c;
        } else {
            return 2;
        }
    }

    public String getHtmlBarPositionConfig() {
        String bpStr = HtmlConstants.HTML_KEY_WORD_BOTTOM;
        Optional<String> propertyt = Optional.ofNullable(tableConfiguration.getProperty(PropertyRegistry.TABLE_HTML_TOOLBAR_POSITION));
        if (!propertyt.isPresent()) {
            Optional<String> propertyc = Optional.ofNullable(context.getProperty(PropertyRegistry.TABLE_HTML_TOOLBAR_POSITION));
            if (propertyc.isPresent()) {
                bpStr = propertyc.get();
            }
        } else {
            bpStr = propertyt.get();
        }
        return bpStr;
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
        HtmlElement out = addDivWithClassToParent(body, "outContainer");
        HtmlElement inner = addDivWithClassToParent(out, "innerContainer");
        HtmlElement content = addDivWithClassToParent(inner, "content");
        return body;
    }

    protected HtmlElement generateHtmlInput(IntrospectedColumn baseColumn, boolean isHidden) {
        StringBuilder sb = new StringBuilder();
        HtmlElement input = new HtmlElement("input");
        input.addAttribute(new Attribute("id", baseColumn.getJavaProperty()));
        input.addAttribute(new Attribute("name", baseColumn.getJavaProperty()));
        if (isHidden) {
            input.addAttribute(new Attribute("type", "hidden"));
        }else{
            input.addAttribute(new Attribute("type", "text"));
        }
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

    protected void addLocalStaticResource(HtmlElement head) {
        String p = getIntrospectedTable().getMyBatis3HtmlMapperPackage();
        if (p.lastIndexOf(".") > 0) {
            p = p.substring(0, p.lastIndexOf("."));
        }
        addStaticStyleSheet(head, GenerateUtils.getLocalCssFilePath(p, p));
        addStaticJavaScript(head, GenerateUtils.getLocalJsFilePath(getIntrospectedTable().getMyBatis3HtmlMapperPackage(),
                getEntityType().getShortName().toLowerCase()));
    }

    protected Document getDocument() {
        return document;
    }

    protected IntrospectedTable getIntrospectedTable() {
        return introspectedTable;
    }

    protected HtmlElement addDivWithClassToParent(HtmlElement parent, String className) {
        HtmlElement div = new HtmlElement("div");
        addClassNameToElement(div, className);
        parent.addElement(div);
        return div;
    }

    protected HtmlElement addDivWithClass(String className) {
        HtmlElement div = new HtmlElement("div");
        addClassNameToElement(div, className);
        return div;
    }

    protected void addClassNameToElement(HtmlElement element, String className) {
        boolean classExist = false;
        for (Attribute attribute : element.getAttributes()) {
            if ("class".equals(attribute.getName())) {
                String[] classNames = attribute.getValue().split(" ");
                List<String> listClassNames = new ArrayList<>(Arrays.asList(classNames));
                if (!listClassNames.contains(className)) {
                    listClassNames.add(className);
                    String v = listClassNames.stream().collect(Collectors.joining(" "));
                    attribute.setValue(v);
                }
                classExist = true;
            }
        }
        if (!classExist) {
            element.addAttribute(new Attribute("class", className));
        }
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

    protected List<HtmlElement> getElmentById(String id) {
        List<HtmlElement> answer = new ArrayList<>();
        for (VisitableElement element : document.getRootElement().getAllElements()) {
            HtmlElement htmlElement = (HtmlElement) element;
            if (htmlElement.getAttributes().size() > 0) {
                for (Attribute attribute : htmlElement.getAttributes()) {
                    if ("id".equalsIgnoreCase(attribute.getName()) && id.equals(attribute.getValue().toString())) {
                        answer.add((HtmlElement) element);
                    }
                }
            }
        }
        return answer;
    }

    /*按配置列数分割总列数*/
    protected Map<String, List<IntrospectedColumn>> getHtmlRows(List<IntrospectedColumn> baseColumns) {
        int pageColumnsConfig = getPageColumnsConfig();
        Map<String, List<IntrospectedColumn>> introspectedColumnRows = new HashMap<>();
        List<IntrospectedColumn> onRow = new ArrayList<>();
        List<IntrospectedColumn> alones = new ArrayList<>();
        for (IntrospectedColumn baseColumn : baseColumns) {
            if (baseColumn.getLength() > 255) {
                alones.add(baseColumn);
            } else {
                onRow.add(baseColumn);
            }
        }
        int columnsSize = onRow.size();
        int rows = (columnsSize + pageColumnsConfig - 1) / pageColumnsConfig;
        for (int i = 0; i < rows; i++) {
            int fromIndex = i * pageColumnsConfig;
            int toIndex = (i + 1) * pageColumnsConfig < columnsSize ? (i + 1) * pageColumnsConfig : columnsSize;
            introspectedColumnRows.put(onRow.get(i).getJavaProperty(), onRow.subList(fromIndex, toIndex));
        }
        if (alones.size() > 0) {
            for (IntrospectedColumn alone : alones) {
                List<IntrospectedColumn> a = new ArrayList<>();
                a.add(alone);
                introspectedColumnRows.put(alone.getJavaProperty(), new ArrayList<>(a));
            }
        }
        return introspectedColumnRows;
    }

    protected List<String> getColumnsJavaProperty() {
        List<IntrospectedColumn> columns = introspectedTable.getAllColumns();
        List<String> javaProperties = new ArrayList<>();
        for (IntrospectedColumn baseColumn : columns) {
            javaProperties.add(baseColumn.getJavaProperty());
        }
        return javaProperties;
    }

    protected boolean isContainProperty(List<IntrospectedColumn> columns, String propertyName){
        List<String> javaProperties = new ArrayList<>();
        for (IntrospectedColumn baseColumn : columns) {
            if (baseColumn.getJavaProperty().equals(propertyName)) {
                return true;
            }
        }
        return false;
    }

    protected List<IntrospectedColumn> getColumsExceptBlod(){
        return Stream.of(introspectedTable.getPrimaryKeyColumns().stream()
                ,introspectedTable.getBaseColumns().stream()).flatMap(Function.identity())
                .collect(Collectors.toList());
    }

    protected void addSubjectInput(HtmlElement parent){
        boolean include = false;
        for (IntrospectedColumn introspectedColumn : getColumsExceptBlod()) {
            if (input_subject_id.equals(introspectedColumn.getJavaProperty())) {
               include = true;
            }
        }
        if (!include) {
            HtmlElement input = new HtmlElement("input");
            input.addAttribute(new Attribute("id", input_subject_id));
            input.addAttribute(new Attribute("name", input_subject_id));
            input.addAttribute(new Attribute("type", "hidden"));
            input.addAttribute(new Attribute("value", introspectedTable.getRemarks()));
            parent.addElement(input);
        }
    }

    protected FullyQualifiedJavaType getEntityType() {
        return entityType;
    }

    protected void setEntityType(FullyQualifiedJavaType entityType) {
        this.entityType = entityType;
    }

    protected HtmlElement addButton(HtmlElement parent, String id, String text){
        HtmlElement btn = new HtmlElement("button");
        btn.addAttribute(new Attribute("type", "button"));
        btn.addAttribute(new Attribute("id", id));
        if (text!=null) {
            btn.addElement(new TextElement(text));
        }
        parent.addElement(btn);
        return btn;
    }

    protected HtmlElement generateToolBar(HtmlElement parent){
        String config = getHtmlBarPositionConfig();
        HtmlElement toolBar;
        String btnClass = null;
        if (HtmlConstants.HTML_KEY_WORD_TOP.equals(config)) {
            toolBar = addDivWithClassToParent(parent, "breadcrumb _top");
        }else if(HtmlConstants.HTML_KEY_WORD_BOTTOM.equals(config)){
            toolBar = addDivWithClassToParent(parent, "breadcrumb _footer");
        }else{
            toolBar = addDivWithClassToParent(parent, "breadcrumb _footer");
        }
        return toolBar;
    };

    protected HtmlElement addJavaScriptFragment(HtmlElement parent){
        HtmlElement js = new HtmlElement("script");
        js.addAttribute(new Attribute("language", "JavaScript"));
        js.addAttribute(new Attribute("type", "text/javascript"));
        parent.addElement(js);
        return js;
    }

}
