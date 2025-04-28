package org.mybatis.generator.codegen.mybatis3.xmlmapper.elements;

import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;

public class SelectByKeysWithAllParentGenerator extends AbstractXmlElementGenerator {

    public SelectByKeysWithAllParentGenerator() {
        super();
    }

    @Override
    public void addElements(XmlElement parentElement) {
        XmlElement answer = new XmlElement("select");
        answer.addAttribute(new Attribute("id", introspectedTable.getSelectByKeysWithAllParent()));
        answer.addAttribute(new Attribute("resultMap", introspectedTable.getBaseResultMapId()));

        answer.addAttribute(new Attribute("parameterType", "java.util.List")); //$NON-NLS-1$
        context.getCommentGenerator().addComment(answer);

        answer.addElement(new TextElement("<!-- 使用递归CTE查询指定ID的数据及其所有父级  递归深度限制为10，防止性能问题  -->"));
        answer.addElement(new TextElement("WITH RECURSIVE cte AS ("));
        answer.addElement(new TextElement("<!-- 基本查询：选择指定主键的记录 -->"));
        answer.addElement(new TextElement("SELECT o.*, 1 AS depth"));
        answer.addElement(new TextElement("FROM "+introspectedTable.getTableConfiguration().getTableName()+" o"));
        answer.addElement(new TextElement("WHERE o.id_ IN"));
        answer.addElement( getForeachXmlElement("list"));
        answer.addElement(new TextElement("UNION ALL"));
        answer.addElement(new TextElement("<!-- 递归查询：查找所有父级 -->"));
        answer.addElement(new TextElement("SELECT parent.*, child.depth + 1"));
        answer.addElement(new TextElement("FROM "+introspectedTable.getTableConfiguration().getTableName()+" parent"));
        answer.addElement(new TextElement("INNER JOIN cte child ON child.parent_id = parent.id_"));
        answer.addElement(new TextElement("WHERE child.depth &lt; 10 <!-- 限制递归深度为10层，避免性能问题 -->"));
        answer.addElement(new TextElement(")"));
        answer.addElement(new TextElement("SELECT DISTINCT"));
        answer.addElement(getBaseColumnListElement());
        answer.addElement(new TextElement(",depth"));
        answer.addElement(new TextElement("FROM cte "+introspectedTable.getTableConfiguration().getAlias()));
        answer.addElement(new TextElement("ORDER BY depth desc"));
        introspectedTable.getColumn("sort_").ifPresent(introspectedColumn -> {
            answer.addElement(new TextElement(", sort_"));
        });
        introspectedTable.getColumn("created_").ifPresent(introspectedColumn -> {
            answer.addElement(new TextElement(", created_"));
        });
        parentElement.addElement(answer);
    }
}
