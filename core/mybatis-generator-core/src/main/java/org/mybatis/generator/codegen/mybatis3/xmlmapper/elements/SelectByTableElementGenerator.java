package org.mybatis.generator.codegen.mybatis3.xmlmapper.elements;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.custom.pojo.SelectByTableGeneratorConfiguration;
import org.mybatis.generator.internal.util.StringUtility;

import java.util.stream.Collectors;

public class SelectByTableElementGenerator extends
        AbstractXmlElementGenerator {

    public SelectByTableElementGenerator() {
        super();
    }

    @Override
    public void addElements(XmlElement parentElement) {
        if (introspectedTable.getTableConfiguration().getSelectByTableGeneratorConfiguration().size() == 0) {
            return;
        }
        for (SelectByTableGeneratorConfiguration selectByTableGeneratorConfiguration : introspectedTable.getTableConfiguration().getSelectByTableGeneratorConfiguration()) {
            XmlElement answer = new XmlElement("select");
            answer.addAttribute(new Attribute("id", selectByTableGeneratorConfiguration.getMethodName()));
            if (selectByTableGeneratorConfiguration.isReturnPrimaryKey()) {
                answer.addAttribute(new Attribute("resultType", FullyQualifiedJavaType.getStringInstance().getFullyQualifiedName()));
            }else{
                if(introspectedTable.getRules().generateResultMapWithBLOBs()){
                    answer.addAttribute(new Attribute("resultMap", introspectedTable.getResultMapWithBLOBsId()));
                }else if (introspectedTable.getRelationGeneratorConfigurations().size() > 0) {
                    answer.addAttribute(new Attribute("resultMap",introspectedTable.getRelationResultMapId()));
                }else{
                    answer.addAttribute(new Attribute("resultMap", introspectedTable.getBaseResultMapId()));
                }
            }
            answer.addAttribute(new Attribute("parameterType", FullyQualifiedJavaType.getStringInstance().getFullyQualifiedName()));
            context.getCommentGenerator().addComment(answer);
            answer.addElement(new TextElement("select "));
            if (selectByTableGeneratorConfiguration.isReturnPrimaryKey()) {
                String collect = introspectedTable.getPrimaryKeyColumns().stream().map(IntrospectedColumn::getActualColumnName).collect(Collectors.joining(","));
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
            sb.append(selectByTableGeneratorConfiguration.getTableName()).append(" RT on RT.");
            sb.append(selectByTableGeneratorConfiguration.getPrimaryKeyColumn());
            sb.append(" = ");
            IntrospectedColumn introspectedColumn = introspectedTable.getPrimaryKeyColumns().get(0);
            sb.append(introspectedColumn.getTableAlias()).append(".");
            sb.append(introspectedColumn.getActualColumnName());
            if (StringUtility.propertyValueValid(selectByTableGeneratorConfiguration.getAdditionCondition())) {
                sb.append(" and ").append(selectByTableGeneratorConfiguration.getAdditionCondition());
            }
            answer.addElement(new TextElement(sb.toString()));
            sb.setLength(0);
            sb.append("where ").append("RT.").append(selectByTableGeneratorConfiguration.getOtherPrimaryKeyColumn());
            sb.append(" = #{");
            sb.append(selectByTableGeneratorConfiguration.getParameterName());
            sb.append(",jdbcType=VARCHAR} ");
            answer.addElement(new TextElement(sb.toString()));
            if (StringUtility.propertyValueValid(selectByTableGeneratorConfiguration.getOrderByClause())) {
                answer.addElement(new TextElement("order by "+ selectByTableGeneratorConfiguration.getOrderByClause()));
            }
            parentElement.addElement(answer);
        }
    }
}
