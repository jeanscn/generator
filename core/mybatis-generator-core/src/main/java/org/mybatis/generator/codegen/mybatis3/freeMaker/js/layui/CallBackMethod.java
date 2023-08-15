package org.mybatis.generator.codegen.mybatis3.freeMaker.js.layui;

import org.mybatis.generator.custom.HtmlElementTagTypeEnum;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-05-26 14:46
 * @version 4.0
 */
public class CallBackMethod {

    private String methodName;

    // 1:列表回调方法（一个参数data） 2:普通select回调方法（两个参数o，n）
    private int type = 1;

    private String tagName;

    private String columnName;

    private String params;

    private List<String> methodBodyLines = new ArrayList<>();

    private List<String> remarks = new ArrayList<>();


    public CallBackMethod(String methodName) {
        this.methodName = methodName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public List<String> getMethodBodyLines() {
        return methodBodyLines;
    }

    public void setMethodBodyLines(List<String> methodBodyLines) {
        this.methodBodyLines = methodBodyLines;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public List<String> getRemarks() {
        return remarks;
    }

    public void setRemarks(List<String> remarks) {
        this.remarks = remarks;
    }
}
