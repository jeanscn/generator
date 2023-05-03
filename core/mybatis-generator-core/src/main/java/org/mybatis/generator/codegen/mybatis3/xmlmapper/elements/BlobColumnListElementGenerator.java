package org.mybatis.generator.codegen.mybatis3.xmlmapper.elements;

import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.XmlElement;

public class BlobColumnListElementGenerator extends AbstractXmlElementGenerator {

    public BlobColumnListElementGenerator() {
        super();
    }

    @Override
    public void addElements(XmlElement parentElement) {
        XmlElement answer = new XmlElement("sql"); //$NON-NLS-1$

        answer.addAttribute(new Attribute("id", introspectedTable.getBlobColumnListId())); //$NON-NLS-1$

        context.getCommentGenerator().addComment(answer);

        buildSelectList(introspectedTable.getBLOBColumns()).forEach(answer::addElement);

        if (context.getPlugins().sqlMapBlobColumnListElementGenerated(answer, introspectedTable)) {
            parentElement.addElement(answer);
        }
    }
}
