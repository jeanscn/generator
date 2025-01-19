package org.mybatis.generator.custom.annotations;

import com.vgosoft.core.annotation.HtmlButton;
import com.vgosoft.core.constant.enums.view.ViewDefaultToolBarsEnum;
import org.mybatis.generator.config.HtmlButtonGeneratorConfiguration;

import java.util.List;

/**
 * 按钮注解描述类
 *
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-08-23 22:27
 * @version 4.0
 */
public class HtmlButtonDesc  extends AbstractAnnotation{

    public static final String ANNOTATION_NAME = "@HtmlButton";

    private String value;
    private String id;
    private boolean text;
    private String title;
    private String label;
    private String icon;
    private String elIcon;
    private String classes;
    private String handler;
    private String type;
    private boolean isLink;
    private boolean  isRound;
    private boolean  isCircle;

    private boolean isPlain;
    private String css;
    private String showCondition;

    private boolean configurable;

    public static HtmlButtonDesc create(ViewDefaultToolBarsEnum viewDefaultToolBarsEnum) {
        return new HtmlButtonDesc(viewDefaultToolBarsEnum.id());
    }

    public static HtmlButtonDesc create(HtmlButtonGeneratorConfiguration htmlButtonGeneratorConfiguration) {
        HtmlButtonDesc htmlButtonDesc = new HtmlButtonDesc(htmlButtonGeneratorConfiguration.getId());
        htmlButtonDesc.setType(htmlButtonGeneratorConfiguration.getType());
        htmlButtonDesc.setText(htmlButtonGeneratorConfiguration.isText());
        htmlButtonDesc.setIcon(htmlButtonGeneratorConfiguration.getIcon());
        htmlButtonDesc.setElIcon(htmlButtonGeneratorConfiguration.getElIcon());
        htmlButtonDesc.setClasses(htmlButtonGeneratorConfiguration.getClasses());
        htmlButtonDesc.setHandler(htmlButtonGeneratorConfiguration.getHandler());
        htmlButtonDesc.isCircle = htmlButtonGeneratorConfiguration.isCircle();
        htmlButtonDesc.isLink = htmlButtonGeneratorConfiguration.isLink();
        htmlButtonDesc.isPlain = htmlButtonGeneratorConfiguration.isPlain();
        htmlButtonDesc.isRound = htmlButtonGeneratorConfiguration.isRound();
        htmlButtonDesc.setCss(htmlButtonGeneratorConfiguration.getCss());
        htmlButtonDesc.setShowCondition(htmlButtonGeneratorConfiguration.getShowCondition());
        htmlButtonDesc.setTitle(htmlButtonGeneratorConfiguration.getTitle());
        htmlButtonDesc.setLabel(htmlButtonGeneratorConfiguration.getLabel());
        htmlButtonDesc.setConfigurable(htmlButtonGeneratorConfiguration.isConfigurable());
        return htmlButtonDesc;
    }

    public HtmlButtonDesc(String id) {
        super();
        this.id = id;
        this.addImports(HtmlButton.class.getCanonicalName());
    }

    public HtmlButtonDesc(ViewDefaultToolBarsEnum viewDefaultToolBarsEnum) {
        super();
        this.id = viewDefaultToolBarsEnum.id();
        this.value = viewDefaultToolBarsEnum.id();
        this.text = viewDefaultToolBarsEnum.text();
        this.label = viewDefaultToolBarsEnum.codeName();
        this.title = viewDefaultToolBarsEnum.title();
        this.icon = viewDefaultToolBarsEnum.icon();
        this.elIcon = viewDefaultToolBarsEnum.elIcon();
        this.classes = viewDefaultToolBarsEnum.classes();
        this.handler = viewDefaultToolBarsEnum.handler();
        this.type = viewDefaultToolBarsEnum.type();
        this.css = viewDefaultToolBarsEnum.css();
        this.showCondition = viewDefaultToolBarsEnum.showCondition();
        this.isPlain = viewDefaultToolBarsEnum.plain();
        this.configurable = viewDefaultToolBarsEnum.configurable();
        this.addImports(HtmlButton.class.getCanonicalName());
    }

    @Override
    public String toAnnotation() {
        this.value = this.value==null?this.id:this.value;
        items.add("value = \""+this.value+"\"");
        if (this.text) {
            items.add("text = true");
        }
        if (this.title!=null) {
            items.add("title = \""+this.title+"\"");
        }
        if (this.icon!=null) {
            items.add("icon = \""+this.icon+"\"");
        }
        if (this.elIcon!=null) {
            items.add("elIcon = \""+this.elIcon+"\"");
        }
        if (this.classes!=null) {
            items.add("classes = \""+this.classes+"\"");
        }
        if (this.handler!=null) {
            items.add("handler = \""+this.handler+"\"");
        }
        if (this.isLink) {
            items.add("isLink = true");
        }
        if (this.isRound) {
            items.add("isRound = true");
        }
        if (this.isCircle) {
            items.add("isCircle = true");
        }
        if (this.isPlain) {
            items.add("isPlain = true");
        }
        if (this.type!=null && !this.type.equalsIgnoreCase("primary")) {
            items.add("type = \""+this.type+"\"");
        }
        if (this.label != null) {
            items.add("label = \""+this.label+"\"");
        }
        if (this.css!=null) {
            items.add("css = \""+this.css+"\"");
        }
        if (this.showCondition!=null && !this.showCondition.equalsIgnoreCase("true")) {
            items.add("showCondition = \""+this.showCondition+"\"");
        }
        if (!this.configurable) {
            items.add("configurable = false");
        }
        return ANNOTATION_NAME + "(" + String.join(", ", items.toArray(new String[0])) + ")";
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public void setClasses(String classes) {
        this.classes = classes;
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

    public void setType(String type) {
        this.type = type;
    }

    public void setCss(String css) {
        this.css = css;
    }

    public void setShowCondition(String showCondition) {
        this.showCondition = showCondition;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isText() {
        return text;
    }

    public void setText(boolean text) {
        this.text = text;
    }

    public String getValue() {
        return value;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getIcon() {
        return icon;
    }

    public String getClasses() {
        return classes;
    }

    public String getHandler() {
        return handler;
    }

    public String getType() {
        return type;
    }

    public String getCss() {
        return css;
    }

    public String getShowCondition() {
        return showCondition;
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
}
