package org.mybatis.generator.codegen.mybatis3.xmlmapper.elements;

import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.XmlElement;

public class CountBySqlElementGenerator extends AbstractXmlElementGenerator{

    public CountBySqlElementGenerator() {
        super();
    }

    @Override
    public void addElements(XmlElement parentElement) {
        XmlElement countBySqlBuilder = new XmlElement("select");
        countBySqlBuilder.addAttribute(new Attribute("id", "countBySql"));
        countBySqlBuilder.addAttribute(new Attribute("parameterType", "java.lang.String"));
        countBySqlBuilder.addAttribute(new Attribute("resultType", "java.lang.Long"));
        context.getCommentGenerator().addComment(countBySqlBuilder);
        countBySqlBuilder.addElement(getBaseBySqlElement());
        parentElement.addElement(countBySqlBuilder);
    }
}
