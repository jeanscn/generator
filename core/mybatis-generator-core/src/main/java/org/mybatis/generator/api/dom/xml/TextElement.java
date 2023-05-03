package org.mybatis.generator.api.dom.xml;

public class TextElement implements VisitableElement {

    private final String content;

    public TextElement(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
