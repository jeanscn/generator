package org.mybatis.generator.api.dom.xml;

import lombok.Getter;

/**
 * @author cen_c
 */
@Getter
public class TextElement implements VisitableElement {

    private final String content;

    public TextElement(String content) {
        this.content = content;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
