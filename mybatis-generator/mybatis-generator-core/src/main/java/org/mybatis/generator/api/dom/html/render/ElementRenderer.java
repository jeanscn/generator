package org.mybatis.generator.api.dom.html.render;

import org.mybatis.generator.api.dom.html.ElementVisitor;
import org.mybatis.generator.api.dom.html.TextElement;
import org.mybatis.generator.api.dom.html.VisitableElement;
import org.mybatis.generator.api.dom.html.HtmlElement;
import org.mybatis.generator.internal.util.CustomCollectors;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * @author cen_c
 */
public class ElementRenderer implements ElementVisitor<Stream<String>> {

    /**
     * 用于渲染属性
     */
    private final AttributeRenderer attributeRenderer = new AttributeRenderer();

    @Override
    public Stream<String> visit(TextElement element) {
        return Stream.of(element.getContent());
    }

    @Override
    public Stream<String> visit(HtmlElement element) {
        if (element.hasChildren()) {
            return renderWithChildren(element);
        } else {
            return renderWithoutChildren(element);
        }
    }

    private Stream<String> renderWithoutChildren(HtmlElement element) {
        StringBuilder sb = new StringBuilder();
        sb.append("<").append(element.getName());
        sb.append(renderAttributes(element));
        if (isAllowSelfColse(element.getName().toLowerCase())) {
            sb.append("/>");
        }else{
            sb.append("></" + element.getName() + ">");
        }
        return Stream.of(sb.toString());
    }

    public Stream<String> renderWithChildren(HtmlElement element) {
        return Stream.of(renderOpen(element), renderChildren(element), renderClose(element))
                .flatMap(s -> s);
    }

    private String renderAttributes(HtmlElement element) {
        return element.getAttributes().stream()
                .sorted((a1, a2) -> a1.getName().compareTo(a2.getName()))
                .map(attributeRenderer::render)
                .collect(CustomCollectors.joining(" ", " ", ""));
    }

    private Stream<String> renderOpen(HtmlElement element) {
        return Stream.of("<"
                + element.getName()
                + renderAttributes(element)
                + ">");
    }

    private Stream<String> renderChildren(HtmlElement element) {
        return element.getElements().stream()
                .flatMap(this::renderChild)
                .map(this::indent);
    }

    private Stream<String> renderChild(VisitableElement child) {
        return child.accept(this);
    }

    private String indent(String s) {
        return "  " + s;
    }

    private Stream<String> renderClose(HtmlElement element) {
        return Stream.of("</"
                + element.getName()
                + ">");
    }
    private boolean isAllowSelfColse(String elementName){
        return  Arrays.asList(new String[]{"br","hr","area","base","img",
                "input","link","meta","basefont","param","col","frame"
                ,"embed","keygen","source","command","track","wbr"}).contains(elementName);
    }

}
