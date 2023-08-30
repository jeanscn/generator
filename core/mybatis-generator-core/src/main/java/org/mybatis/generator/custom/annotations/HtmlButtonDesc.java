package org.mybatis.generator.custom.annotations;

import com.vgosoft.core.annotation.DictData;
import com.vgosoft.core.annotation.HtmlButton;
import com.vgosoft.core.constant.enums.view.ViewDefaultToolBarsEnum;
import org.mybatis.generator.config.HtmlButtonGeneratorConfiguration;

import java.util.List;
import java.util.stream.Collectors;

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
    private String icon;
    private String classes;
    private String handler;
    private String type;
    private List<String> handlerParams;
    private List<String>  handlerParamsType;
    private List<String>  handlerParamsValue;
    private String css;

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
        htmlButtonDesc.setHandlerParams(htmlButtonGeneratorConfiguration.getHandlerParams());
        htmlButtonDesc.setHandlerParamsType(htmlButtonGeneratorConfiguration.getHandlerParamsType());
        htmlButtonDesc.setHandlerParamsValue(htmlButtonGeneratorConfiguration.getHandlerParamsValue());
        htmlButtonDesc.setCss(htmlButtonGeneratorConfiguration.getCss());
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
        if (this.icon!=null) {
            items.add("icon = \""+this.icon+"\"");
        }
        if (this.classes!=null) {
            items.add("classes = \""+this.classes+"\"");
        }
        if (this.handler!=null) {
            items.add("handler = \""+this.handler+"\"");
        }
        if (this.handlerParams!=null) {
            items.add("handlerParams = \""+ String.join(",", this.handlerParams) +"\"");
        }
        if (this.handlerParamsType!=null) {
            items.add("handlerParamsType = \""+String.join(",", this.handlerParamsType)+"\"");
        }
        if (this.handlerParamsValue!=null) {
            items.add("handlerParamsValue = \""+String.join(",", this.handlerParamsValue)+"\"");
        }
        if (this.type!=null) {
            items.add("type = \""+this.type+"\"");
        }
        if (this.css!=null) {
            items.add("css = \""+this.css+"\"");
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

    public void setHandlerParams(List<String> handlerParams) {
        this.handlerParams = handlerParams;
    }

    public void setHandlerParamsType(List<String> handlerParamsType) {
        this.handlerParamsType = handlerParamsType;
    }

    public void setHandlerParamsValue(List<String> handlerParamsValue) {
        this.handlerParamsValue = handlerParamsValue;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setCss(String css) {
        this.css = css;
    }
}
