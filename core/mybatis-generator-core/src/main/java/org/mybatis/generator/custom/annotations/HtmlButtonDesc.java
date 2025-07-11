package org.mybatis.generator.custom.annotations;

import com.vgosoft.core.annotation.HtmlButton;
import com.vgosoft.core.constant.enums.view.ViewDefaultToolBarsEnum;
import com.vgosoft.tool.core.VStringUtil;
import lombok.Getter;
import lombok.Setter;
import org.mybatis.generator.config.HtmlButtonGeneratorConfiguration;

/**
 * 按钮注解描述类
 *
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-08-23 22:27
 * @version 4.0
 */
@Getter
@Setter
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

    private String disabledCondition;

    private boolean configurable;

    private String localeKey;

    private String componentType;

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
        htmlButtonDesc.setDisabledCondition(htmlButtonGeneratorConfiguration.getDisabledCondition());
        htmlButtonDesc.setTitle(htmlButtonGeneratorConfiguration.getTitle());
        htmlButtonDesc.setLabel(htmlButtonGeneratorConfiguration.getLabel());
        htmlButtonDesc.setConfigurable(htmlButtonGeneratorConfiguration.isConfigurable());
        htmlButtonDesc.setLocaleKey(htmlButtonGeneratorConfiguration.getLocaleKey());
        htmlButtonDesc.setCircle(htmlButtonGeneratorConfiguration.isCircle());
        htmlButtonDesc.setRound(htmlButtonGeneratorConfiguration.isRound());
        htmlButtonDesc.setLink(htmlButtonGeneratorConfiguration.isLink());
        htmlButtonDesc.setComponentType(htmlButtonGeneratorConfiguration.getComponentType());
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
        this.disabledCondition = viewDefaultToolBarsEnum.disabledCondition();
        this.isPlain = viewDefaultToolBarsEnum.plain();
        this.configurable = viewDefaultToolBarsEnum.configurable();
        this.localeKey = viewDefaultToolBarsEnum.localeKey();
        this.isLink = viewDefaultToolBarsEnum.isLink();
        this.isRound = viewDefaultToolBarsEnum.isRound();
        this.isCircle = viewDefaultToolBarsEnum.isCircle();
        this.componentType = viewDefaultToolBarsEnum.componentType();
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
        if (VStringUtil.stringHasValue(this.css)) {
            items.add("css = \""+this.css+"\"");
        }
        if (this.showCondition!=null && !this.showCondition.equalsIgnoreCase("true")) {
            items.add("showCondition = \""+this.showCondition+"\"");
        }
        if (this.disabledCondition!=null && !this.disabledCondition.equalsIgnoreCase("false")) {
            items.add("disabledCondition = \""+this.disabledCondition+"\"");
        }
        if (this.configurable) {
            items.add("configurable = true");
        }
        if (VStringUtil.stringHasValue(this.localeKey)) {
            items.add("localeKey = \""+this.localeKey+"\"");
        }
        if (VStringUtil.stringHasValue(this.componentType) && !this.componentType.equalsIgnoreCase("button")) {
            items.add("componentType = \""+this.componentType+"\"");
        }
        return ANNOTATION_NAME + "(" + String.join(", ", items.toArray(new String[0])) + ")";
    }
}
