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
    private String text;
    private String title;
    private String icon;
    private String classes;
    private String handler;
    private String type;
    private boolean isLink;
    private boolean  isRound;
    private boolean  isCircle;

    private boolean isPlain;
    private String css;
    private String showCondition;

    public static HtmlButtonDesc create(ViewDefaultToolBarsEnum viewDefaultToolBarsEnum) {
        return new HtmlButtonDesc(viewDefaultToolBarsEnum.id());
    }

    public static HtmlButtonDesc create(HtmlButtonGeneratorConfiguration htmlButtonGeneratorConfiguration) {
        HtmlButtonDesc htmlButtonDesc = new HtmlButtonDesc(htmlButtonGeneratorConfiguration.getId());
        htmlButtonDesc.setType(htmlButtonGeneratorConfiguration.getType());
        htmlButtonDesc.setText(htmlButtonGeneratorConfiguration.getText());
        htmlButtonDesc.setIcon(htmlButtonGeneratorConfiguration.getIcon());
        htmlButtonDesc.setClasses(htmlButtonGeneratorConfiguration.getClasses());
        htmlButtonDesc.setHandler(htmlButtonGeneratorConfiguration.getHandler());
        htmlButtonDesc.isCircle = htmlButtonGeneratorConfiguration.isCircle();
        htmlButtonDesc.isLink = htmlButtonGeneratorConfiguration.isLink();
        htmlButtonDesc.isPlain = htmlButtonGeneratorConfiguration.isPlain();
        htmlButtonDesc.isRound = htmlButtonGeneratorConfiguration.isRound();
        htmlButtonDesc.setCss(htmlButtonGeneratorConfiguration.getCss());
        htmlButtonDesc.setShowCondition(htmlButtonGeneratorConfiguration.getShowCondition());
        htmlButtonDesc.setTitle(htmlButtonGeneratorConfiguration.getTitle());
        return htmlButtonDesc;
    }

    public HtmlButtonDesc(String id) {
        super();
        this.id = id;
        this.addImports(HtmlButton.class.getCanonicalName());
    }

    @Override
    public String toAnnotation() {
        this.value = this.value==null?this.id:this.value;
        items.add("value = \""+this.value+"\"");
        if (this.text!=null) {
            items.add("text = \""+this.text+"\"");
        }
        if (this.title!=null) {
            items.add("title = \""+this.title+"\"");
        }
        if (this.icon!=null) {
            items.add("icon = \""+this.icon+"\"");
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
        if (this.type!=null) {
            items.add("type = \""+this.type+"\"");
        }
        if (this.css!=null) {
            items.add("css = \""+this.css+"\"");
        }
        if (this.showCondition!=null && !this.showCondition.equalsIgnoreCase("true")) {
            items.add("showCondition = \""+this.showCondition+"\"");
        }
        return ANNOTATION_NAME + "(" + String.join(", ", items.toArray(new String[0])) + ")";
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setText(String text) {
        this.text = text;
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
}
