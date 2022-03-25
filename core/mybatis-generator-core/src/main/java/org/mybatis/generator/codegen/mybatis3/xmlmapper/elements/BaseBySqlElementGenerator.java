package org.mybatis.generator.codegen.mybatis3.xmlmapper.elements;

import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;

public class BaseBySqlElementGenerator extends AbstractXmlElementGenerator{

    public BaseBySqlElementGenerator() {
        super();
    }

    @Override
    public void addElements(XmlElement parentElement) {
        XmlElement sqlSqlBuilder = new XmlElement("sql");
        sqlSqlBuilder.addAttribute(new Attribute("id", "Base_By_Sql"));
        context.getCommentGenerator().addComment(sqlSqlBuilder);
        XmlElement ifElement = new XmlElement("if");
        ifElement.addAttribute(new Attribute("test", "_parameter != null"));
        ifElement.addElement(new TextElement("${sql}"));
        sqlSqlBuilder.addElement(ifElement);
        parentElement.addElement(sqlSqlBuilder);
    }
}
