package org.mybatis.generator.codegen.mybatis3.xmlmapper.elements;

import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.XmlElement;

public class SelectMapBySqlElementGenerator extends AbstractXmlElementGenerator{

    public SelectMapBySqlElementGenerator() {
        super();
    }

    @Override
    public void addElements(XmlElement parentElement) {
        XmlElement selectMapBySqlBuilder = new XmlElement("select");
        selectMapBySqlBuilder.addAttribute(new Attribute("id", "selectMapBySql"));
        selectMapBySqlBuilder.addAttribute(new Attribute("parameterType", "java.lang.String"));
        selectMapBySqlBuilder.addAttribute(new Attribute("resultType", "java.util.Map"));
        context.getCommentGenerator().addComment(selectMapBySqlBuilder);
        selectMapBySqlBuilder.addElement(getBaseBySqlElement());
        parentElement.addElement(selectMapBySqlBuilder);
    }
}
