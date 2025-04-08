package org.mybatis.generator.config;

import org.mybatis.generator.custom.UiFrameTypeEnum;

import java.util.List;

/**
 * button生成配置类
 *
 */
public class HtmlButtonGeneratorConfiguration extends TypedPropertyHolder{

    private String id;
    private String type = "primary";
    private boolean text;
    private String title;
    private String label;
    private String icon;
    private String elIcon;
    private String classes;
    private String handler;

    private boolean isLink = false;
    private boolean  isRound = false;
    private boolean  isCircle = false;
    private boolean isPlain = false;
    private String css;
    private String showCondition = "true";
    private String disabledCondition = "false";
    private boolean configurable = false;
    private String localeKey;

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

    public boolean isLink() {
        return isLink;
    }

    public void setLink(boolean link) {
        isLink = link;
    }

    public boolean isRound() {
        return isRound;
    }

    public void setRound(boolean round) {
        isRound = round;
    }

    public boolean isCircle() {
        return isCircle;
    }

    public void setCircle(boolean circle) {
        isCircle = circle;
    }

    public boolean isPlain() {
        return isPlain;
    }

    public void setPlain(boolean plain) {
        isPlain = plain;
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

    public boolean isText() {
        return text;
    }

    public void setText(boolean text) {
        this.text = text;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isConfigurable() {
        return configurable;
    }

    public void setConfigurable(boolean configurable) {
        this.configurable = configurable;
    }

    public String getElIcon() {
        return elIcon;
    }

    public void setElIcon(String elIcon) {
        this.elIcon = elIcon;
    }

    public String getDisabledCondition() {
        return disabledCondition;
    }

    public void setDisabledCondition(String disabledCondition) {
        this.disabledCondition = disabledCondition;
    }

    public String getLocaleKey() {
        return localeKey;
    }

    public void setLocaleKey(String localeKey) {
        this.localeKey = localeKey;
    }
}
