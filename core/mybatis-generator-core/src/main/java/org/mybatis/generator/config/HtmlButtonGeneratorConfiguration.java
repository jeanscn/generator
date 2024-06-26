package org.mybatis.generator.config;

import org.mybatis.generator.custom.UiFrameTypeEnum;

import java.util.List;

/**
 * button生成配置类
 *
 */
public class HtmlButtonGeneratorConfiguration extends TypedPropertyHolder{

    private String id;
    private String type = "button";
    private String text;
    private String title;
    private String icon;
    private String classes;
    private String handler;
    private List<String> handlerParams;
    private List<String> handlerParamsType;
    private List<String> handlerParamsValue;
    private String css;

    private String showCondition = "true";

    public HtmlButtonGeneratorConfiguration() {
        super();
    }

    public HtmlButtonGeneratorConfiguration(String id) {
       super();
       this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getClasses() {
        return classes;
    }

    public void setClasses(String classes) {
        this.classes = classes;
    }

    public String getHandler() {
        return handler;
    }

    public void setHandler(String handler) {
        this.handler = handler;
    }

    public List<String> getHandlerParams() {
        return handlerParams;
    }

    public void setHandlerParams(List<String> handlerParams) {
        this.handlerParams = handlerParams;
    }

    public List<String> getHandlerParamsType() {
        return handlerParamsType;
    }

    public void setHandlerParamsType(List<String> handlerParamsType) {
        this.handlerParamsType = handlerParamsType;
    }

    public List<String> getHandlerParamsValue() {
        return handlerParamsValue;
    }

    public void setHandlerParamsValue(List<String> handlerParamsValue) {
        this.handlerParamsValue = handlerParamsValue;
    }

    public String getCss() {
        return css;
    }

    public void setCss(String css) {
        this.css = css;
    }

    public String getShowCondition() {
        return showCondition;
    }

    public void setShowCondition(String showCondition) {
        this.showCondition = showCondition;
    }
}
