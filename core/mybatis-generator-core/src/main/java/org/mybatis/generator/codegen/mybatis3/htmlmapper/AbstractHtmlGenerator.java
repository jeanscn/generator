package org.mybatis.generator.codegen.mybatis3.htmlmapper;

import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.ProgressCallback;
import org.mybatis.generator.api.dom.html.Attribute;
import org.mybatis.generator.api.dom.html.HtmlElement;
import org.mybatis.generator.api.dom.html.TextElement;
import org.mybatis.generator.codegen.AbstractGenerator;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.internal.util.VoGenService;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * html生成的基类
 *
 */
public class AbstractHtmlGenerator extends AbstractGenerator implements HtmlConstant{

    protected VoGenService voGenService;

    public AbstractHtmlGenerator(Context context, IntrospectedTable introspectedTable, List<String> warnings, ProgressCallback progressCallback) {
        super(context, introspectedTable, warnings, progressCallback);
        this.voGenService = new VoGenService(this.introspectedTable);
    }

    /**
     * 为元素添加style属性
     *
     * @param element  元素
     * @param cssStyle style样式
     */
    protected void addCssStyleToElement(HtmlElement element, String cssStyle) {
        if (element.getAttributes().stream().noneMatch(a -> a.getName().equals("style"))) {
            element.addAttribute(new Attribute("style", cssStyle));
        } else {
            Optional<Attribute> style = element.getAttributes().stream().filter(a -> a.getName().equals("style")).findFirst();
            style.ifPresent(attribute -> {
                if (!attribute.getValue().contains(cssStyle)) {
                    attribute.setValue(attribute.getValue() + ";" + cssStyle);
                }
            });
        }
    }

    /**
     * 为元素添加class属性
     *
     * @param element   元素
     * @param cssClasses class名称
     */
    protected void addCssClassToElement(HtmlElement element, String...cssClasses) {
        Set<String> classSet = Arrays.stream(cssClasses).filter(VStringUtil::stringHasValue).collect(Collectors.toSet());
        if (classSet.isEmpty()) {
            return;
        }
        if (element.getAttributes().stream().noneMatch(a -> a.getName().equals("class"))) {
            element.addAttribute(new Attribute("class", String.join(" ", cssClasses)));
        } else {
            Optional<Attribute> style = element.getAttributes().stream().filter(a -> a.getName().equals("class")).findFirst();
            style.ifPresent(attribute -> {
                for (String aClass : cssClasses) {
                    if (!attribute.getValue().contains(aClass)) {
                        attribute.setValue(attribute.getValue() + " " + aClass);
                    }
                }
            });
        }
    }

    /**
     * 在父元素中添加一个带有class属性的div元素
     * @param parent    父元素
     * @param cssClasses class名称
     * @return  div元素
     */
    protected HtmlElement addDivWithClassToParent(HtmlElement parent, String...cssClasses) {
        HtmlElement div = new HtmlElement("div");
        addCssClassToElement(div, cssClasses);
        parent.addElement(div);
        return div;
    }

    /**
     * 在父元素中添加一个带有class属性的i元素
     * @param parent    父元素
     * @param iconClasses class名称
     * @return  HtmlElement元素
     */
    protected HtmlElement addIconToParent(HtmlElement parent,String...iconClasses) {
        HtmlElement icon = new HtmlElement("i");
        addCssClassToElement(icon, iconClasses);
        parent.addElement(icon);
        return icon;
    }

    /**
     * 在父元素中添加一个带有class属性的tr元素
     */
    protected HtmlElement addTrWithClassToParent(HtmlElement parent, String...classNames) {
        HtmlElement tr = new HtmlElement("tr");
        addCssClassToElement(tr, classNames);
        parent.addElement(tr);
        return tr;
    }

    /**
     * 在tr元素中添加一个带有class属性的td元素
     */
    protected HtmlElement addTdWithClassToTr(HtmlElement tr, String className,int colspan) {
        HtmlElement td = new HtmlElement("td");
        if (!className.isEmpty()) {
            addCssClassToElement(td, className);
        }
        if(colspan>0) {
        	td.addAttribute(new Attribute("colspan", String.valueOf(colspan)));
        }
        tr.addElement(td);
        return td;
    }

    /**
     * 在td元素中添加或修改colspan元素
     */
    protected void addColspanToTd(HtmlElement td, int colspan) {
        if (colspan > 0) {
            td.getAttributes().removeIf(a -> a.getName().equals("colspan"));
            td.addAttribute(new Attribute("colspan", String.valueOf(colspan)));
        }
    }

    /**
     * 在td元素中添加或修改rowspan元素
     */
    protected void addRowspanToTd(HtmlElement td, int rowspan) {
        if (rowspan > 0) {
            td.getAttributes().removeIf(a -> a.getName().equals("rowspan"));
            td.addAttribute(new Attribute("rowspan", String.valueOf(rowspan)));
        }
    }

    /**
     * 在父元素中添加一个带有class属性的button元素
     */
    protected HtmlElement addHtmlButton(HtmlElement parent, String id, String text, String...classNames) {
        HtmlElement btn = new HtmlElement("button");
        btn.addAttribute(new Attribute("type", "button"));
        btn.addAttribute(new Attribute("id", id));
        if (text != null) {
            btn.addElement(new TextElement(text));
        }
        addCssClassToElement(btn, classNames);
        parent.addElement(btn);
        return btn;
    }

    /**
     * 在父元素中添加link元素
     */
    protected HtmlElement addStaticStyleSheet(HtmlElement htmlElement, String value) {
        HtmlElement link = new HtmlElement("link");
        link.addAttribute(new Attribute("rel", "stylesheet"));
        link.addAttribute(new Attribute("type", "text/css"));
        if (value != null) {
            link.addAttribute(new Attribute("href", value));
        }
        htmlElement.addElement(link);
        return link;
    }

    /**
     * 在父元素中添加script元素
     */
    protected HtmlElement addStaticJavaScript(HtmlElement htmlElement, String value) {
        HtmlElement script = new HtmlElement("script");
        script.addAttribute(new Attribute("charset", "utf-8"));
        if (value != null) {
            script.addAttribute(new Attribute("src", value));
        }
        script.addAttribute(new Attribute("type", "text/javascript"));
        htmlElement.addElement(script);
        return script;
    }

    /**
     * 添加或重写元素指定属性值
     * 如果属性存在则重写，不存在直接添加
     */
    protected void addOrReplaceElementAttribute(HtmlElement htmlElement, String attributeName, String attributeValue) {
        if (htmlElement.getAttributes().stream().noneMatch(a -> a.getName().equals(attributeName))) {
            htmlElement.addAttribute(new Attribute(attributeName, attributeValue));
        } else {
            Optional<Attribute> style = htmlElement.getAttributes().stream().filter(a -> a.getName().equals(attributeName)).findFirst();
            style.ifPresent(attribute -> attribute.setValue(attributeValue));
        }
    }

    /**
     * 删除元素的指定属性
     */
    protected void removeElementAttribute(HtmlElement htmlElement, String attributeName) {
        htmlElement.getAttributes().removeIf(a -> a.getName().equals(attributeName));
    }

    /**
     * 生成一个原生input元素
     */
    protected HtmlElement generateHtmlInput(String name, boolean isHidden, boolean isTextArea, boolean idAttribute,boolean nameAttribute) {
        HtmlElement input = new HtmlElement(isTextArea ? "textarea" : "input");
        addAttribute(name, isHidden, idAttribute, nameAttribute, input);
        return input;
    }

    /**
     * 生成一个原生select元素
     */
    protected HtmlElement generateHtmlSelect(String name, boolean isHidden, boolean idAttribute, boolean nameAttribute) {
        HtmlElement input = new HtmlElement("select");
        addAttribute(name, isHidden, idAttribute, nameAttribute, input);
        return input;
    }

    private static void addAttribute(String name, boolean isHidden, boolean idAttribute, boolean nameAttribute, HtmlElement input) {
        if (idAttribute) {
            input.addAttribute(new Attribute("id", name));
        }
        if (nameAttribute) {
            input.addAttribute(new Attribute("name", name));
        }
        input.addAttribute(new Attribute("type", isHidden ? "hidden" : "text"));
    }

}
