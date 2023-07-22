package org.mybatis.generator.codegen.mybatis3.freeMaker.js.layui;

import org.mybatis.generator.custom.HtmlElementTagTypeEnum;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-05-26 14:46
 * @version 4.0
 */
public class CallBackMethod {

    private String methodName;

    private String requestKey;

    private String thisKey;

    private String otherKey;

    // 0:更新关系 1:普通select回调方法
    private int type = 1;

    private String tagName;

    private String columnName;

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getRequestKey() {
        return requestKey;
    }

    public void setRequestKey(String requestKey) {
        this.requestKey = requestKey;
    }

    public String getThisKey() {
        return thisKey;
    }

    public void setThisKey(String thisKey) {
        this.thisKey = thisKey;
    }

    public String getOtherKey() {
        return otherKey;
    }

    public void setOtherKey(String otherKey) {
        this.otherKey = otherKey;
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
}
