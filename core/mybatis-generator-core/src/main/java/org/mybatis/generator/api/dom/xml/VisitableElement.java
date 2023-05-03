package org.mybatis.generator.api.dom.xml;

@FunctionalInterface
public interface VisitableElement {
    <R> R accept(ElementVisitor<R> visitor);
}
