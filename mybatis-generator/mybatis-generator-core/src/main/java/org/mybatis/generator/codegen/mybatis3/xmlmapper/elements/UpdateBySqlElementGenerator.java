package org.mybatis.generator.codegen.mybatis3.xmlmapper.elements;

import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.XmlElement;

public class UpdateBySqlElementGenerator extends AbstractXmlElementGenerator{

    public UpdateBySqlElementGenerator() {
        super();
    }

    @Override
    public void addElements(XmlElement parentElement) {
        XmlElement updateBySqlBuilder = new XmlElement("update");
        updateBySqlBuilder.addAttribute(new Attribute("id", "updateBySql"));
        updateBySqlBuilder.addAttribute(new Attribute("parameterType", "java.lang.String"));
        context.getCommentGenerator().addComment(updateBySqlBuilder);
        updateBySqlBuilder.addElement(getBaseBySqlElement());
        parentElement.addElement(updateBySqlBuilder);
    }
}
