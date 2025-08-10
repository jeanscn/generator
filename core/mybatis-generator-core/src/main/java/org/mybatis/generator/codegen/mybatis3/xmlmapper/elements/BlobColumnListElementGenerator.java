package org.mybatis.generator.codegen.mybatis3.xmlmapper.elements;

import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.XmlElement;

public class BlobColumnListElementGenerator extends AbstractXmlElementGenerator {

    public BlobColumnListElementGenerator() {
        super();
    }

    @Override
    public void addElements(XmlElement parentElement) {
        XmlElement answer = new XmlElement("sql");

        answer.addAttribute(new Attribute("id", introspectedTable.getBlobColumnListId()));

        context.getCommentGenerator().addComment(answer);

        buildSelectList(introspectedTable.getBLOBColumns()).forEach(answer::addElement);

        if (context.getPlugins().sqlMapBlobColumnListElementGenerated(answer, introspectedTable)) {
            parentElement.addElement(answer);
        }
    }
}
