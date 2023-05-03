package org.mybatis.generator.api.dom.xml.render;

import org.mybatis.generator.api.dom.xml.Attribute;

public class AttributeRenderer {

    public String render(Attribute attribute) {
        return attribute.getName()
                + "=\"" //$NON-NLS-1$
                + attribute.getValue()
                + "\""; //$NON-NLS-1$
    }
}
