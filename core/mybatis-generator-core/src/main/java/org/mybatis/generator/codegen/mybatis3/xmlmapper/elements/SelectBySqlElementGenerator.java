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
        StringBuilder sb = new StringBuilder();
        sb.append("from ");
        sb.append("(${sql}) ");
        String alias = introspectedTable.getFullyQualifiedTable().getAlias();
        if (stringHasValue(alias)) {
            sb.append(' ');
            sb.append(alias);
        }
        ifElement.addElement(new TextElement(sb.toString()));
        selectBySqlBuilder.addElement(ifElement);
        parentElement.addElement(selectBySqlBuilder);
    }
}
