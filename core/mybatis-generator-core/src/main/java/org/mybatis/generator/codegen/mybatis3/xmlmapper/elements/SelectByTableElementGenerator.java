package org.mybatis.generator.codegen.mybatis3.xmlmapper.elements;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;
import org.mybatis.generator.config.SelectByTableGeneratorConfiguration;
import org.mybatis.generator.internal.util.StringUtility;

import java.util.stream.Collectors;

public class SelectByTableElementGenerator extends
        AbstractXmlElementGenerator {

    public SelectByTableElementGenerator() {
        super();
    }

    @Override
    public void addElements(XmlElement parentElement) {
        if (introspectedTable.getTableConfiguration().getSelectByTableGeneratorConfiguration().isEmpty()) {
            return;
        }
        for (SelectByTableGeneratorConfiguration configuration : introspectedTable.getTableConfiguration().getSelectByTableGeneratorConfiguration()) {
            XmlElement answer = new XmlElement("select");
            answer.addAttribute(new Attribute("id", configuration.getMethodName()));
            if (configuration.isReturnPrimaryKey()) {
                answer.addAttribute(new Attribute("resultType", FullyQualifiedJavaType.getStringInstance().getFullyQualifiedName()));
            }else{
                if(introspectedTable.getRules().generateResultMapWithBLOBs()){
                    answer.addAttribute(new Attribute("resultMap", introspectedTable.getResultMapWithBLOBsId()));
                }else if (!introspectedTable.getTableConfiguration().getRelationGeneratorConfigurations().isEmpty()) {
                    answer.addAttribute(new Attribute("resultMap",introspectedTable.getRelationResultMapId()));
                }else{
                    answer.addAttribute(new Attribute("resultMap", introspectedTable.getBaseResultMapId()));
                }
            }
            answer.addAttribute(new Attribute("parameterType", FullyQualifiedJavaType.getStringInstance().getFullyQualifiedName()));
            context.getCommentGenerator().addComment(answer);
            answer.addElement(new TextElement("select "));
            if (configuration.isReturnPrimaryKey()) {
                String collect = introspectedTable.getPrimaryKeyColumns().stream()
                        .map(MyBatis3FormattingUtilities::getAliasedEscapedColumnName)
                        .collect(Collectors.joining(","));
                answer.addElement(new TextElement(collect));
            }else{
                answer.addElement(getBaseColumnListElement());
            }
            //form
            StringBuilder sb = new StringBuilder();
            sb.append("from ");
            sb.append(introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime());
            answer.addElement(new TextElement(sb.toString()));
            sb.setLength(0);
            sb.append("left join ");
            sb.append(configuration.getTableName()).append(" RT on RT.");
            sb.append(configuration.getPrimaryKeyColumn());
            sb.append(" = ");
            IntrospectedColumn introspectedColumn = introspectedTable.getPrimaryKeyColumns().get(0);
            sb.append(introspectedColumn.getTableAlias()).append(".");
            sb.append(introspectedColumn.getActualColumnName());
            if (StringUtility.propertyValueValid(configuration.getAdditionCondition())) {
                sb.append(" and ").append(configuration.getAdditionCondition());
            }
            answer.addElement(new TextElement(sb.toString()));
            sb.setLength(0);
            sb.append("where ").append("RT.").append(configuration.getOtherPrimaryKeyColumn());
            if (configuration.getParameterType().equals("list")) {
                sb.append(" in ");
                answer.addElement(new TextElement(sb.toString()));
                XmlElement foreachListField = new XmlElement("foreach");
                foreachListField.addAttribute(new Attribute("collection", "list"));
                foreachListField.addAttribute(new Attribute("item", "item"));
                foreachListField.addAttribute(new Attribute("index", "index"));
                foreachListField.addAttribute(new Attribute("separator", ","));
                foreachListField.addAttribute(new Attribute("open", "("));
                foreachListField.addAttribute(new Attribute("close", ")"));
                answer.addElement(foreachListField);
                foreachListField.addElement(new TextElement("#{item}"));
            }else{
                sb.append(" = #{");
                sb.append(configuration.getParameterName());
                sb.append(",jdbcType=VARCHAR} ");
                answer.addElement(new TextElement(sb.toString()));
            }
            if (StringUtility.propertyValueValid(configuration.getOrderByClause())) {
                answer.addElement(new TextElement("order by "+ configuration.getOrderByClause()));
            }
            parentElement.addElement(answer);
        }
    }
}
