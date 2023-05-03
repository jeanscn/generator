package org.mybatis.generator.codegen.mybatis3.xmlmapper.elements;

import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;
import org.mybatis.generator.config.SelectBySqlMethodGeneratorConfiguration;

public class SelectBySqlMethodElementGenerator extends AbstractXmlElementGenerator {

    private final SelectBySqlMethodGeneratorConfiguration configuration;

    public SelectBySqlMethodElementGenerator(SelectBySqlMethodGeneratorConfiguration configuration) {
        super();
        this.configuration = configuration;
    }

    @Override
    public void addElements(XmlElement parentElement) {
        XmlElement answer = new XmlElement("select");
        answer.addAttribute(new Attribute("id", configuration.getMethodName()));
        if (introspectedTable.getRules().generateResultMapWithBLOBs()) {
            answer.addAttribute(new Attribute("resultMap", introspectedTable.getResultMapWithBLOBsId()));
        } else {
            if (introspectedTable.getTableConfiguration().getRelationGeneratorConfigurations().size() > 0) {
                answer.addAttribute(new Attribute("resultMap", introspectedTable.getRelationResultMapId()));
            } else {
                answer.addAttribute(new Attribute("resultMap", introspectedTable.getBaseResultMapId()));
            }
        }
        answer.addAttribute(new Attribute("parameterType", configuration.getParentIdColumn().getFullyQualifiedJavaType().getFullyQualifiedName()));
        context.getCommentGenerator().addComment(answer);
        answer.addElement(new TextElement("select "));
        answer.addElement(getBaseColumnListElement());
        if (introspectedTable.hasBLOBColumns()) {
            answer.addElement(new TextElement(","));
            answer.addElement(getBlobColumnListElement());
        }
        //form
        StringBuilder sb = new StringBuilder();
        sb.append("from ");
        sb.append(introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime());
        answer.addElement(new TextElement(sb.toString()));
        //条件
        sb.setLength(0);
        sb.append("where ");
        sb.append("FIND_IN_SET(");
        sb.append(MyBatis3FormattingUtilities.getAliasedEscapedColumnName(configuration.getPrimaryKeyColumn()));
        sb.append(",");
        sb.append(configuration.getSqlMethod());
        sb.append("(");
        sb.append(MyBatis3FormattingUtilities.getParameterClause(configuration.getParentIdColumn()));
        sb.append("))");
        answer.addElement(new TextElement(sb.toString()));
        parentElement.addElement(answer);
    }
}
