package org.mybatis.generator.api.dom.html;

public interface ElementVisitor<R> {
    R visit(TextElement element);

    R visit(HtmlElement element);
}
