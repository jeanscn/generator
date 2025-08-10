package org.mybatis.generator.codegen.mybatis3.xmlmapper.elements;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;

public class SimpleSelectByPrimaryKeyElementGenerator extends AbstractXmlElementGenerator {

    public SimpleSelectByPrimaryKeyElementGenerator() {
        super();
    }

    @Override
    public void addElements(XmlElement parentElement) {
        XmlElement answer = new XmlElement("select");

        answer.addAttribute(new Attribute(
                "id", introspectedTable.getSelectByPrimaryKeyStatementId()));
        answer.addAttribute(new Attribute("resultMap",
                introspectedTable.getBaseResultMapId()));

        String parameterType;
        // PK fields are in the base class. If more than on PK
        // field, then they are coming in a map.
        if (introspectedTable.getPrimaryKeyColumns().size() > 1) {
            parameterType = "map";
        } else {
            parameterType = introspectedTable.getPrimaryKeyColumns().get(0)
                    .getFullyQualifiedJavaType().toString();
        }

        answer.addAttribute(new Attribute("parameterType", parameterType));

        context.getCommentGenerator().addComment(answer);

        StringBuilder sb = new StringBuilder();
        sb.append("select ");

        if (stringHasValue(introspectedTable.getSelectByPrimaryKeyQueryId())) {
            sb.append('\'');
            sb.append(introspectedTable.getSelectByPrimaryKeyQueryId());
            sb.append("' as QUERYID,");
        }

        buildSelectList(sb.toString(), introspectedTable.getAllColumns()).forEach(answer::addElement);

        sb.setLength(0);
        sb.append("from ");
        sb.append(introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime());
        answer.addElement(new TextElement(sb.toString()));

        buildPrimaryKeyWhereClause().forEach(answer::addElement);

        if (context.getPlugins().sqlMapSelectByPrimaryKeyElementGenerated(answer, introspectedTable)) {
            parentElement.addElement(answer);
        }
    }
}
