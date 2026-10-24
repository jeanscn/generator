package org.mybatis.generator.codegen.mybatis3.xmlmapper.elements;

import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.XmlElement;

public class ListBySqlElementGenerator extends AbstractXmlElementGenerator{

    public ListBySqlElementGenerator() {
        super();
    }

    @Override
    public void addElements(XmlElement parentElement) {
        XmlElement listBySqlBuilder = new XmlElement("select");
        listBySqlBuilder.addAttribute(new Attribute("id", "listBySql"));
        listBySqlBuilder.addAttribute(new Attribute("parameterType", "java.lang.String"));
        listBySqlBuilder.addAttribute(new Attribute("resultType", "java.lang.String"));
        context.getCommentGenerator().addComment(listBySqlBuilder);
        listBySqlBuilder.addElement(getBaseBySqlElement());
        parentElement.addElement(listBySqlBuilder);
    }
}
