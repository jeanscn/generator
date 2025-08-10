package org.mybatis.generator.api.dom.xml.render;

import org.mybatis.generator.api.dom.xml.Attribute;

/**
 * @author cen_c
 */
public class AttributeRenderer {

    public String render(Attribute attribute) {
        return attribute.getName()
                + "=\""
                + attribute.getValue()
                + "\"";
    }
}
