package org.mybatis.generator.codegen.mybatis3.xmlmapper.elements;

import org.apache.commons.lang3.ObjectUtils;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;
import org.mybatis.generator.config.SelectByTableGeneratorConfiguration;

import java.util.List;

public class DeleteByTableElementGenerator extends
        AbstractXmlElementGenerator {

    public DeleteByTableElementGenerator() {
        super();
    }

    @Override
    public void addElements(XmlElement parentElement) {
        final List<SelectByTableGeneratorConfiguration> configurations = introspectedTable.getTableConfiguration().getSelectByTableGeneratorConfiguration();
        if (configurations.stream().noneMatch(SelectByTableGeneratorConfiguration::isEnableSplit)) return;
        for (SelectByTableGeneratorConfiguration configuration : configurations) {
            if (ObjectUtils.anyNull(configuration.getTableName(), configuration.getPrimaryKeyColumn(), configuration.getOtherPrimaryKeyColumn())) {
                continue;
            }
            XmlElement answer = new XmlElement("delete");
            answer.addAttribute(new Attribute("id", configuration.getSplitMethodName()));
            answer.addAttribute(new Attribute("parameterType", FullyQualifiedJavaType.getStringInstance().getFullyQualifiedName()));
            context.getCommentGenerator().addComment(answer);
            answer.addElement(new TextElement("delete from "+configuration.getTableName()));
            //where
            StringBuilder sb = new StringBuilder();
            XmlElement where = new XmlElement("where");

            sb.append(configuration.getPrimaryKeyColumn()).append(" = ");
            sb.append(MyBatis3FormattingUtilities.getParameterClause(configuration.getThisColumn()));
            where.addElement(new TextElement(sb.toString()));
            where.addElement(new TextElement(" and  "+configuration.getOtherColumn().getActualColumnName()+" in"));
            XmlElement foreachListField = new XmlElement("foreach");
            foreachListField.addAttribute(new Attribute("collection", configuration.getOtherColumn().getJavaProperty()+"s"));
            foreachListField.addAttribute(new Attribute("item", configuration.getOtherColumn().getJavaProperty()));
            foreachListField.addAttribute(new Attribute("index", "index"));
            foreachListField.addAttribute(new Attribute("separator", ","));
            foreachListField.addAttribute(new Attribute("open", "("));
            foreachListField.addAttribute(new Attribute("close", ")"));
            foreachListField.addElement(new TextElement(MyBatis3FormattingUtilities.getParameterClause(configuration.getOtherColumn())));
            where.addElement(foreachListField);
            answer.addElement(where);
            parentElement.addElement(answer);
        }
    }
}
