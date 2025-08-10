package org.mybatis.generator.codegen.mybatis3.xmlmapper.elements;

import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;

public class DeleteByPrimaryKeyElementGenerator extends AbstractXmlElementGenerator {

    private final boolean isSimple;

    public DeleteByPrimaryKeyElementGenerator(boolean isSimple) {
        super();
        this.isSimple = isSimple;
    }

    @Override
    public void addElements(XmlElement parentElement) {
        XmlElement answer = new XmlElement("delete");

        answer.addAttribute(new Attribute("id", introspectedTable.getDeleteByPrimaryKeyStatementId()));
        String parameterClass;
        if (!isSimple && introspectedTable.getRules().generatePrimaryKeyClass()) {
            parameterClass = introspectedTable.getPrimaryKeyType();
        } else {
            // PK fields are in the base class. If more than on PK
            // field, then they are coming in a map.
            if (introspectedTable.getPrimaryKeyColumns().size() > 1) {
                parameterClass = "map";
            } else {
                parameterClass =
                        introspectedTable.getPrimaryKeyColumns().get(0).getFullyQualifiedJavaType().toString();
            }
        }
        answer.addAttribute(new Attribute("parameterType", parameterClass));

        context.getCommentGenerator().addComment(answer);

        String sb = "delete from " + introspectedTable.getFullyQualifiedTableNameAtRuntime();
        answer.addElement(new TextElement(sb));

        buildPrimaryKeyWhereClause().forEach(answer::addElement);

        if (context.getPlugins().sqlMapDeleteByPrimaryKeyElementGenerated(answer,introspectedTable)) {
            parentElement.addElement(answer);
        }
    }
}
