package org.mybatis.generator.codegen.mybatis3.xmlmapper.elements;

import com.vgosoft.core.constant.enums.db.DefaultColumnNameEnum;
import org.apache.commons.lang3.ObjectUtils;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;
import org.mybatis.generator.config.SelectByTableGeneratorConfiguration;

import java.util.List;

public class InsertByTableElementGenerator extends
        AbstractXmlElementGenerator {

    public InsertByTableElementGenerator() {
        super();
    }

    @Override
    public void addElements(XmlElement parentElement) {
        final List<SelectByTableGeneratorConfiguration> configurations = introspectedTable.getTableConfiguration().getSelectByTableGeneratorConfiguration();
        if (configurations.stream().noneMatch(SelectByTableGeneratorConfiguration::isEnableUnion)) return;
        for (SelectByTableGeneratorConfiguration configuration : configurations) {
            if (ObjectUtils.anyNull(configuration.getTableName(), configuration.getPrimaryKeyColumn(), configuration.getOtherPrimaryKeyColumn())) {
                continue;
            }
            XmlElement answer = new XmlElement("insert");
            answer.addAttribute(new Attribute("id", configuration.getUnionMethodName()));
            answer.addAttribute(new Attribute("parameterType", FullyQualifiedJavaType.getStringInstance().getFullyQualifiedName()));
            context.getCommentGenerator().addComment(answer);
            answer.addElement(new TextElement("insert ignore into " + configuration.getTableName()));
            StringBuilder sb = new StringBuilder("(");
            sb.append(DefaultColumnNameEnum.ID.columnName()).append(",").append(configuration.getPrimaryKeyColumn()).append(",").append(configuration.getOtherPrimaryKeyColumn());
            sb.append(")");
            answer.addElement(new TextElement(sb.toString()));
            answer.addElement(new TextElement("values"));

            XmlElement foreachListField = new XmlElement("foreach");
            foreachListField.addAttribute(new Attribute("collection", configuration.getOtherColumn().getJavaProperty() + "s"));
            foreachListField.addAttribute(new Attribute("item", configuration.getOtherColumn().getJavaProperty()));
            foreachListField.addAttribute(new Attribute("index", "index"));
            foreachListField.addAttribute(new Attribute("separator", ","));
            foreachListField.addAttribute(new Attribute("open", ""));
            foreachListField.addAttribute(new Attribute("close", ""));
            sb.setLength(0);
            sb.append("(md5( concat(");
            sb.append(MyBatis3FormattingUtilities.getParameterClause(configuration.getThisColumn()));
            sb.append(",");
            sb.append(MyBatis3FormattingUtilities.getParameterClause(configuration.getOtherColumn()));
            sb.append(")),");
            sb.append(MyBatis3FormattingUtilities.getParameterClause(configuration.getThisColumn()));
            sb.append(",");
            sb.append(MyBatis3FormattingUtilities.getParameterClause(configuration.getOtherColumn()));
            sb.append(")");
            foreachListField.addElement(new TextElement(sb.toString()));
            answer.addElement(foreachListField);
            parentElement.addElement(answer);
        }
    }
}
