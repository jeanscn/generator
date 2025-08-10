package org.mybatis.generator.codegen.mybatis3.xmlmapper.elements;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.ListUtilities;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;

public class UpdateByExampleSelectiveElementGenerator extends
        AbstractXmlElementGenerator {

    public UpdateByExampleSelectiveElementGenerator() {
        super();
    }

    @Override
    public void addElements(XmlElement parentElement) {
        XmlElement answer = new XmlElement("update");

        answer.addAttribute(new Attribute(
                "id", introspectedTable.getUpdateByExampleSelectiveStatementId()));

        answer.addAttribute(new Attribute("parameterType", "map"));  //$NON-NLS-2$

        context.getCommentGenerator().addComment(answer);

        StringBuilder sb = new StringBuilder();
        sb.append("update ");
        //addUpdateAliasedFullyQualifiedTableName(introspectedTable,sb);
        sb.append(introspectedTable.getFullyQualifiedTableNameAtRuntime());
        answer.addElement(new TextElement(sb.toString()));

        XmlElement dynamicElement = new XmlElement("set");
        answer.addElement(dynamicElement);

        for (IntrospectedColumn introspectedColumn :
                ListUtilities.removeGeneratedAlwaysColumns(introspectedTable.getAllColumns())) {
            sb.setLength(0);
            sb.append(introspectedColumn.getJavaProperty("row."));
            sb.append(" != null");
            XmlElement isNotNullElement = new XmlElement("if");
            isNotNullElement.addAttribute(new Attribute("test", sb.toString()));
            dynamicElement.addElement(isNotNullElement);

            sb.setLength(0);
            //sb.append(MyBatis3FormattingUtilities
             //       .getAliasedEscapedColumnName(introspectedColumn));
            sb.append(MyBatis3FormattingUtilities.getAliasedEscapedColumnName(introspectedColumn));
            sb.append(" = ");
            sb.append(MyBatis3FormattingUtilities.getParameterClause(introspectedColumn, "row."));
            sb.append(',');

            isNotNullElement.addElement(new TextElement(sb.toString()));
        }
        //addUpdateAliasedFullyQualifiedTableNameFrom(introspectedTable,answer);
        answer.addElement(getUpdateByExampleIncludeElement());
        // 增加if判断，防止传入的参数为null或空
        XmlElement ifElement = new XmlElement("if");
        ifElement.addAttribute(new Attribute("test", "_parameter == null or _parameter.oredCriteria == null or _parameter.oredCriteria.size() == 0"));
        ifElement.addElement(new TextElement("-- 防止无条件删除，返回影响0行"));
        XmlElement where = new XmlElement("where");
        where.addElement(new TextElement("1 = 0"));
        ifElement.addElement(where);
        answer.addElement(ifElement);
        if (context.getPlugins().sqlMapUpdateByExampleSelectiveElementGenerated(answer, introspectedTable)) {
            parentElement.addElement(answer);
        }
    }
}
