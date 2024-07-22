package org.mybatis.generator.config;

import org.mybatis.generator.custom.ConstantsUtil;

import java.util.ArrayList;
import java.util.List;

public class HtmlHrefElementConfiguration {
    private String href;

    private String target = "_blank";

    private String type = "slideLeft";

    private String text;

    private String icon;

    private String title;

    private String method;

    private String keySelector = "#id";

    private String hideExpression;


    public HtmlHrefElementConfiguration() {

    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getKeySelector() {
        return keySelector;
    }

    public void setKeySelector(String keySelector) {
        this.keySelector = keySelector;
    }

    public String getHideExpression() {
        return hideExpression;
    }

    public void setHideExpression(String hideExpression) {
        this.hideExpression = hideExpression;
    }
}
