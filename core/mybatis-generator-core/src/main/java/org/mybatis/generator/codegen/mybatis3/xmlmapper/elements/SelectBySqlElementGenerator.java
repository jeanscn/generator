package org.mybatis.generator.codegen.mybatis3.xmlmapper.elements;

import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

public class SelectBySqlElementGenerator extends AbstractXmlElementGenerator{
    public SelectBySqlElementGenerator() {
        super();
    }

    @Override
    public void addElements(XmlElement parentElement) {
        XmlElement selectBySqlBuilder = new XmlElement("select");
        selectBySqlBuilder.addAttribute(new Attribute("id", "selectBySql"));
        selectBySqlBuilder.addAttribute(new Attribute("parameterType", "java.lang.String"));
        selectBySqlBuilder.addAttribute(new Attribute("resultMap", "BaseResultMap"));
        context.getCommentGenerator().addComment(selectBySqlBuilder);
        XmlElement ifElement = new XmlElement("if"); //$NON-NLS-1$
        ifElement.addAttribute(new Attribute("test", "_parameter != null")); //$NON-NLS-1$ //$NON-NLS-2$
        ifElement.addElement(new TextElement("select")); //$NON-NLS-1$
        ifElement.addElement(getBaseColumnListElement());

        XmlElement ifNotNull = new XmlElement("if");
        ifNotNull.addAttribute(new Attribute("test", "sql != null and  sql !=''"));
        ifNotNull.addElement(new TextElement("from (${sql})"+getAlias()));
        ifElement.addElement(ifNotNull);
        XmlElement ifNull = new XmlElement("if");
        ifNull.addAttribute(new Attribute("test", "sql == null or sql ==''"));
        ifNull.addElement(new TextElement("from "+introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime()));
        ifElement.addElement(ifNull);
        selectBySqlBuilder.addElement(ifElement);
        parentElement.addElement(selectBySqlBuilder);
    }

    private String getAlias() {
        String alias = introspectedTable.getFullyQualifiedTable().getAlias();
        if (stringHasValue(alias)) {
            return " as "+alias;
        }
        return "";
    }
}
