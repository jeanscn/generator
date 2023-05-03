package org.mybatis.generator.api.dom.html.render;

import org.mybatis.generator.api.dom.html.Attribute;

public class AttributeRenderer {

    public String render(Attribute attribute) {
        StringBuilder sb = new StringBuilder();
        sb.append(attribute.getName());
        if (attribute.getValue() != null) {
            sb.append("=\"").append(attribute.getValue()).append("\"");
        }
        return sb.toString();
    }
}
