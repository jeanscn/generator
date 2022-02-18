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
import org.mybatis.generator.config.PropertyScope;
import org.mybatis.generator.config.TableConfiguration;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author <a href="mailto:cjj@vip.sina.com">ChenJJ</a>
 *  2020-07-23 00:57
 * @version 3.0
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
        String pcStr = introspectedTable.getConfigPropertyValue(PropertyRegistry.TABLE_HTML_PAGE_COLUMNS, PropertyScope.any,"2");
        int c = Integer.valueOf(pcStr);
        if (c > 12) {
            c = 12;
        } else if (c <= 0) {
            c = 1;
        }
        if (12 % c == 0) {
            return c;
        } else {
            return 1;
        }
    }

    public String getHtmlBarPositionConfig() {
        return introspectedTable.getConfigPropertyValue(PropertyRegistry.TABLE_HTML_TOOLBAR_POSITION, PropertyScope.any,"bottom");
    }

    public abstract boolean htmlMapDocumentGenerated();

    protected HtmlElement generateHtmlHead() {
        HtmlElement head = new HtmlElement("head");
        HtmlElement div = new HtmlElement("div");
        addStaticReplace(head, "subpages/webjarsPluginsRequired2.html::baseRequired('" + introspectedTable.getRemarks() + "')");
        addStaticReplace(head, "subpages/webjarsPluginsRequired2.html::jQueryRequired");
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
        HtmlElement out = addDivWithClassToParent(body, "container");
        addClassNameToElement(out, "outContainer");
        HtmlElement inner = addDivWithClassToParent(out, "icontainer");
        HtmlElement content = addDivWithClassToParent(inner, "content");
        return body;
    }

    protected HtmlElement generateHtmlInput(IntrospectedColumn baseColumn, boolean isHidden,boolean isTextArea) {
        return generateHtmlInput(baseColumn.getJavaProperty(), isHidden, isTextArea);
    }

    protected HtmlElement generateHtmlInput(String name, boolean isHidden,boolean isTextArea) {
        String type = isTextArea?"textarea":"input";
        HtmlElement input = new HtmlElement(type);
        input.addAttribute(new Attribute("id", name));
        input.addAttribute(new Attribute("name", name));
        if (isHidden) {
            input.addAttribute(new Attribute("type", "hidden"));
        }else{
            input.addAttribute(new Attribute("type", "text"));
        }
        return input;
    }

    protected String thymeleafValue(IntrospectedColumn baseColumn,String entityName){
        StringBuilder sb = new StringBuilder();
        sb.append("${"+entityName+"?.").append(baseColumn.getJavaProperty());
        if ("DATE".equals(baseColumn.getJdbcTypeName().toUpperCase())) {
            sb.append("!=null?#dates.format("+entityName+".");
            sb.append(baseColumn.getJavaProperty()).append(",'yyyy-MM-dd'):''}");
        } else if ("TIME".equals(baseColumn.getJdbcTypeName().toUpperCase())) {
            sb.append("!=null?#dates.format("+entityName+".");
            sb.append(baseColumn.getJavaProperty()).append(",'HH:mm:ss'):''}");
        } else if ("TIMESTAMP".equals(baseColumn.getJdbcTypeName().toUpperCase())) {
            sb.append("!=null?#dates.format("+entityName+".");
            sb.append(baseColumn.getJavaProperty()).append(",'yyyy-MM-dd HH:mm:ss'):''}");
        } else {
            if ("version".equals(baseColumn.getJavaProperty())) {
                sb.append("}?:1");
            }else{
                sb.append("}?:_");
            }
        }
        return sb.toString();
    }

    protected void addLocalStaticResource(HtmlElement head) {
        String p = getIntrospectedTable().getMyBatis3HtmlMapperPackage();
        if (p.lastIndexOf(".") > 0) {
            p = p.substring(0, p.lastIndexOf("."));
        }
        addStaticStyleSheet(head, GenerateUtils.getLocalCssFilePath(p, p));
        addStaticJavaScript(head, GenerateUtils.getLocalJsFilePath(p,p));
    }

    //TODO 计划第一次生成js文件，并初始化
    protected boolean isEntityTypeJsFileExsit(){
        String filePath = GenerateUtils.getLocalJsFilePath(getIntrospectedTable().getMyBatis3HtmlMapperPackage(),getEntityType().getShortName().toLowerCase());

        return false;
    }

    protected Document getDocument() {
        return document;
    }

    protected IntrospectedTable getIntrospectedTable() {
        return introspectedTable;
    }

    protected HtmlElement addDivWithClassToParent(HtmlElement parent, String className) {
        HtmlElement div = new HtmlElement("div");
        if (!className.isEmpty()) {
            addClassNameToElement(div, className);
        }
        parent.addElement(div);
        return div;
    }

    protected HtmlElement addFormWithClassToParent(HtmlElement parent, String className) {
        HtmlElement from = new HtmlElement("form");
        addClassNameToElement(from, className);
        parent.addElement(from);
        return from;
    }

    /*protected HtmlElement addDivWithClass(String className) {
        HtmlElement div = new HtmlElement("div");
        addClassNameToElement(div, className);
        return div;
    }*/

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

   /* protected List<HtmlElement> getElmentById(String id) {
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
    }*/
/*
    *//*按配置列数分割总列数*//*
    protected Map<String, List<IntrospectedColumn>> getHtmlRows2(List<IntrospectedColumn> baseColumns) {
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
    }*/

    /*按配置列数分割总列数*/
    protected Map<Integer, List<IntrospectedColumn>> getHtmlRows(List<IntrospectedColumn> baseColumns) {
        int pageColumnsConfig = getPageColumnsConfig();
        Map<Integer, List<IntrospectedColumn>> introspectedColumnRows = new HashMap<>();
        int rowIndex = 1,colIndex=1;
        for (IntrospectedColumn baseColumn : baseColumns) {
            if (baseColumn.getLength() > 255) {
                if (introspectedColumnRows.get(rowIndex) != null) {
                    rowIndex = rowIndex+1;
                }
                List<IntrospectedColumn> tmp = new ArrayList<>();
                tmp.add(baseColumn);
                introspectedColumnRows.put(rowIndex, tmp);
                rowIndex = rowIndex+1;
            }else{
                if (introspectedColumnRows.get(rowIndex) != null && introspectedColumnRows.get(rowIndex).size()<pageColumnsConfig) {
                   introspectedColumnRows.get(rowIndex).add(baseColumn);
                    if (introspectedColumnRows.get(rowIndex).size()==pageColumnsConfig) {
                        rowIndex = rowIndex+1;
                    }
                }else{
                    List<IntrospectedColumn> tmp = new ArrayList<>();
                    tmp.add(baseColumn);
                    introspectedColumnRows.put(rowIndex, tmp);
                    if (tmp.size()==pageColumnsConfig) {
                        rowIndex = rowIndex+1;
                    }
                }
            }
        }
        return introspectedColumnRows;
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
