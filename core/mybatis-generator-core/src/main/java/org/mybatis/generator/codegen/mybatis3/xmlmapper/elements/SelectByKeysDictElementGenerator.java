package org.mybatis.generator.codegen.mybatis3.xmlmapper.elements;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;
import org.mybatis.generator.codegen.mybatis3.service.ServiceMethods;
import org.mybatis.generator.config.VOCacheGeneratorConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SelectByKeysDictElementGenerator extends AbstractXmlElementGenerator {

    public SelectByKeysDictElementGenerator() {
        super();
    }

    @Override
    public void addElements(XmlElement parentElement) {
        XmlElement answer = new XmlElement("select");
        answer.addAttribute(new Attribute("id", introspectedTable.getSelectByKeysDictStatementId()));
        answer.addAttribute(new Attribute("resultMap",introspectedTable.getBaseResultMapId()));

        answer.addAttribute(new Attribute("parameterType", "map")); //$NON-NLS-1$
        context.getCommentGenerator().addComment(answer);

        answer.addElement(new TextElement("select "));
        answer.addElement(getBaseColumnListElement());
        StringBuilder sb = new StringBuilder();
        sb.append("from ");
        sb.append(introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime());
        answer.addElement(new TextElement(sb.toString()));
        VOCacheGeneratorConfiguration config = introspectedTable.getTableConfiguration().getVoCacheGeneratorConfiguration();
        List<IntrospectedColumn> keysColumns = (new ServiceMethods(context,introspectedTable)).getSelectDictParameterColumns(config, introspectedTable);
        XmlElement where = new XmlElement("where");
        answer.addElement(where);
        for (IntrospectedColumn keysColumn : keysColumns) {
            XmlElement anIf = new XmlElement("if");
           sb.setLength(0);
           sb.append(keysColumn.getJavaProperty()).append(" != null");
            if (keysColumn.isStringColumn()) {
                sb.append(" and ").append(keysColumn.getJavaProperty()).append(" != ''");
            }
            anIf.addAttribute(new Attribute("test", sb.toString()));
            String line = " and ";
            line += MyBatis3FormattingUtilities.getEscapedColumnName(keysColumn);
            line += " = "; //$NON-NLS-1$
            line += MyBatis3FormattingUtilities.getParameterClause(keysColumn);
            anIf.addElement(new TextElement(line));
            where.addElement(anIf);
        }
        TextElement defaultOrderBy = buildOrderByDefault();
        if (defaultOrderBy != null) {
            answer.addElement(defaultOrderBy);
        }
        if (context.getPlugins().sqlMapSelectByKeysDictElementGenerated(answer, introspectedTable)) {
            parentElement.addElement(answer);
        }
    }
}
