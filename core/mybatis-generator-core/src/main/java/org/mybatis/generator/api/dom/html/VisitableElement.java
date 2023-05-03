package org.mybatis.generator.api.dom.html;

@FunctionalInterface
public interface VisitableElement {
    <R> R accept(ElementVisitor<R> visitor);
}
