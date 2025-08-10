package org.mybatis.generator.codegen.mybatis3.xmlmapper.elements;

import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.config.PropertyRegistry;
import org.mybatis.generator.internal.util.StringUtility;

public class SimpleSelectAllElementGenerator extends AbstractXmlElementGenerator {

    public SimpleSelectAllElementGenerator() {
        super();
    }

    @Override
    public void addElements(XmlElement parentElement) {
        XmlElement answer = new XmlElement("select");

        answer.addAttribute(new Attribute(
                "id", introspectedTable.getSelectAllStatementId()));
        answer.addAttribute(new Attribute("resultMap",
                introspectedTable.getBaseResultMapId()));

        context.getCommentGenerator().addComment(answer);

        buildSelectList("select ", introspectedTable.getAllColumns()).forEach(answer::addElement);

        StringBuilder sb = new StringBuilder();
        sb.append("from ");
        sb.append(introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime());
        answer.addElement(new TextElement(sb.toString()));

        String orderByClause = introspectedTable.getTableConfigurationProperty(
                PropertyRegistry.TABLE_SELECT_ALL_ORDER_BY_CLAUSE);
        boolean hasOrderBy = StringUtility.stringHasValue(orderByClause);
        if (hasOrderBy) {
            sb.setLength(0);
            sb.append("order by ");
            sb.append(orderByClause);
            answer.addElement(new TextElement(sb.toString()));
        }

        if (context.getPlugins().sqlMapSelectAllElementGenerated(answer, introspectedTable)) {
            parentElement.addElement(answer);
        }
    }
}
