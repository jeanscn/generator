package org.mybatis.generator.codegen.mybatis3.xmlmapper.elements;

import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.XmlElement;

public class InsertBySqlElementGenerator extends AbstractXmlElementGenerator{

    public InsertBySqlElementGenerator() {
        super();
    }

    @Override
    public void addElements(XmlElement parentElement) {
        XmlElement insertBySqlBuilder = new XmlElement("insert");
        insertBySqlBuilder.addAttribute(new Attribute("id", "insertBySql"));
        insertBySqlBuilder.addAttribute(new Attribute("parameterType", "java.lang.String"));
        context.getCommentGenerator().addComment(insertBySqlBuilder);
        insertBySqlBuilder.addElement(getBaseBySqlElement());
        parentElement.addElement(insertBySqlBuilder);
    }
}
