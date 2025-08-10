package org.mybatis.generator.codegen.mybatis3.xmlmapper.elements;

import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;

/**
 * @author cen_c
 */
public class CountByExampleElementGenerator extends AbstractXmlElementGenerator {

    public CountByExampleElementGenerator() {
        super();
    }

    @Override
    public void addElements(XmlElement parentElement) {
        XmlElement answer = new XmlElement("select");

        answer.addAttribute(new Attribute(
                "id", introspectedTable.getCountByExampleStatementId()));
        answer.addAttribute(new Attribute("parameterType", introspectedTable.getExampleType()));
        answer.addAttribute(new Attribute("resultType", "java.lang.Long"));  

        context.getCommentGenerator().addComment(answer);

        String s = "select count(*) from "
                + introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime();
        answer.addElement(new TextElement(s));
        answer.addElement(getExampleIncludeElement());

        if (context.getPlugins().sqlMapCountByExampleElementGenerated(answer, introspectedTable)) {
            parentElement.addElement(answer);
        }
    }
}
