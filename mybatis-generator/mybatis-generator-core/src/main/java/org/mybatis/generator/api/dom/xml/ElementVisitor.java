package org.mybatis.generator.api.dom.xml;

public interface ElementVisitor<R> {
    R visit(TextElement element);

    R visit(XmlElement element);
}
