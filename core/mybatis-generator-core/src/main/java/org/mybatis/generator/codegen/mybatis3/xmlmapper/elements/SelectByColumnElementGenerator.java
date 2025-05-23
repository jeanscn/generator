package org.mybatis.generator.codegen.mybatis3.xmlmapper.elements;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;
import org.mybatis.generator.internal.util.StringUtility;

import java.util.stream.Collectors;

public class SelectByColumnElementGenerator extends
        AbstractXmlElementGenerator {

    public SelectByColumnElementGenerator() {
        super();
    }

    @Override
    public void addElements(XmlElement parentElement) {
        if (introspectedTable.getTableConfiguration().getSelectByColumnGeneratorConfigurations().isEmpty()) {
            return;
        }
        introspectedTable.getTableConfiguration().getSelectByColumnGeneratorConfigurations().stream().filter(c -> c.getColumns().size() > 0)
                .forEach(c -> {
                    XmlElement answer = new XmlElement("select");
                    answer.addAttribute(new Attribute("id", c.getMethodName()));
                    if (c.isReturnPrimaryKey()) {
                        answer.addAttribute(new Attribute("resultType", FullyQualifiedJavaType.getStringInstance().getFullyQualifiedName()));
                    } else {
                        if (introspectedTable.getRules().generateResultMapWithBLOBs()) {
                            answer.addAttribute(new Attribute("resultMap", introspectedTable.getResultMapWithBLOBsId()));
                        } else if (!introspectedTable.getTableConfiguration().getRelationGeneratorConfigurations().isEmpty()) {
                            answer.addAttribute(new Attribute("resultMap", introspectedTable.getRelationResultMapId()));
                        } else {
                            answer.addAttribute(new Attribute("resultMap", introspectedTable.getBaseResultMapId()));
                        }
                    }
                    if (c.getColumns().size() == 1) {
                        answer.addAttribute(new Attribute("parameterType", c.getColumns().get(0).getFullyQualifiedJavaType().getFullyQualifiedName()));
                    }
                    context.getCommentGenerator().addComment(answer);

                    answer.addElement(new TextElement("select "));
                    if (c.isReturnPrimaryKey()) {
                        String collect = introspectedTable.getPrimaryKeyColumns().stream().map(IntrospectedColumn::getActualColumnName).collect(Collectors.joining(","));
                        answer.addElement(new TextElement(collect));
                    } else {
                        answer.addElement(getBaseColumnListElement());
                        if (introspectedTable.hasBLOBColumns()) {
                            answer.addElement(new TextElement(","));
                            answer.addElement(getBlobColumnListElement());
                        }
                    }
                    //form
                    answer.addElement(new TextElement("from " + introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime()));

                    //where
                    XmlElement where = new XmlElement("where");
                    c.getColumns().forEach(column -> {
                        if (c.getParameterList()) {
                            where.addElement(new TextElement("and " + MyBatis3FormattingUtilities.getAliasedEscapedColumnName(column) + " in "));
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
                            where.addElement(new TextElement("and " + MyBatis3FormattingUtilities.getAliasedEscapedColumnName(column) + " = " +
                                    MyBatis3FormattingUtilities.getParameterClause(column)));
                        }
                    });
                    answer.addElement(where);

                    //order by
                    if (StringUtility.propertyValueValid(c.getOrderByClause())) {
                        answer.addElement(new TextElement("order by " + c.getOrderByClause()));
                    } else {
                        TextElement defaultOrderBy = buildOrderByDefault();
                        if (defaultOrderBy != null) {
                            answer.addElement(defaultOrderBy);
                        }
                    }
                    parentElement.addElement(answer);
                });
    }
}
