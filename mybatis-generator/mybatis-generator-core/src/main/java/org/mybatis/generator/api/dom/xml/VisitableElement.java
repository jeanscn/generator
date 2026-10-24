package org.mybatis.generator.api.dom.xml;

/**
 * @author cen_c
 */
@FunctionalInterface
public interface VisitableElement {
    /**
     * Accept a visitor
     * @param visitor the visitor
     * @return the result
     * @param <R> the result type
     */
    <R> R accept(ElementVisitor<R> visitor);
}
