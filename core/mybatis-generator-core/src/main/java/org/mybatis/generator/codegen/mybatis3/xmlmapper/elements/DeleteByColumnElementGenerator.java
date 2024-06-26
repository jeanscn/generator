package org.mybatis.generator.codegen.mybatis3.xmlmapper.elements;

import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;
import org.mybatis.generator.config.SelectByColumnGeneratorConfiguration;

public class DeleteByColumnElementGenerator extends AbstractXmlElementGenerator {

    private final SelectByColumnGeneratorConfiguration configuration;

    public DeleteByColumnElementGenerator(SelectByColumnGeneratorConfiguration configuration) {
        super();
        this.configuration = configuration;
    }

    @Override
    public void addElements(XmlElement parentElement) {
        XmlElement answer = new XmlElement("delete");
        answer.addAttribute(new Attribute("id", configuration.getDeleteMethodName()));
        if (configuration.getColumns().size() == 1) {
            answer.addAttribute(new Attribute("parameterType", configuration.getColumns().get(0).getFullyQualifiedJavaType().getFullyQualifiedName()));
        }
        context.getCommentGenerator().addComment(answer);
        answer.addElement(new TextElement("delete from " + introspectedTable.getFullyQualifiedTableNameAtRuntime()));
        //where
        XmlElement where = new XmlElement("where");
        configuration.getColumns().forEach(column -> {
            if (configuration.getParameterList()) {
                where.addElement(new TextElement("and " + MyBatis3FormattingUtilities.getEscapedColumnName(column) + " in "));
                XmlElement foreachListField = new XmlElement("foreach");
                foreachListField.addAttribute(new Attribute("collection", column.getJavaProperty() + "s"));
                foreachListField.addAttribute(new Attribute("item", "item"));
                foreachListField.addAttribute(new Attribute("index", "index"));
                foreachListField.addAttribute(new Attribute("separator", ","));
                foreachListField.addAttribute(new Attribute("open", "("));
                foreachListField.addAttribute(new Attribute("close", ")"));
                foreachListField.addElement(new TextElement("#{item}"));
                where.addElement(foreachListField);
            } else {
                where.addElement(new TextElement("and " + MyBatis3FormattingUtilities.getEscapedColumnName(column) + " = " +
                        MyBatis3FormattingUtilities.getParameterClause(column)));
            }
        });
        answer.addElement(where);
        parentElement.addElement(answer);
    }
}
