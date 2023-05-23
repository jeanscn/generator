package org.mybatis.generator.codegen.mybatis3.xmlmapper.elements;

import io.swagger.models.Xml;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;
import org.mybatis.generator.codegen.mybatis3.service.ServiceMethods;
import org.mybatis.generator.config.VOCacheGeneratorConfiguration;
import org.mybatis.generator.internal.util.StringUtility;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

public class SelectByKeysDictElementGenerator extends AbstractXmlElementGenerator {

    public SelectByKeysDictElementGenerator() {
        super();
    }

    @Override
    public void addElements(XmlElement parentElement) {
        XmlElement answer = new XmlElement("select");
        answer.addAttribute(new Attribute("id", introspectedTable.getSelectByKeysDictStatementId()));
        answer.addAttribute(new Attribute("resultMap", introspectedTable.getBaseResultMapId()));

        answer.addAttribute(new Attribute("parameterType", "com.vgosoft.core.pojo.parameter.SelDictByKeysParam")); //$NON-NLS-1$
        context.getCommentGenerator().addComment(answer);

        answer.addElement(new TextElement("select "));
        answer.addElement(getBaseColumnListElement());
        String sb = "from " +
                introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime();
        answer.addElement(new TextElement(sb));

        VOCacheGeneratorConfiguration config = introspectedTable.getTableConfiguration().getVoCacheGeneratorConfiguration();
        //List<IntrospectedColumn> keysColumns = (new ServiceMethods(context, introspectedTable)).getSelectDictParameterColumns(config, introspectedTable);
        XmlElement where = new XmlElement("where");
        answer.addElement(where);
        XmlElement trim = createBracketTrim();
        where.addElement(trim);
        //choose条件
        XmlElement choose = new XmlElement("choose");
        trim.addElement(choose);
        if (stringHasValue(config.getKeyColumn()) && introspectedTable.getColumn(config.getKeyColumn()).isPresent()) {
            introspectedTable.getColumn(config.getKeyColumn()).ifPresent(introspectedColumn -> {
                choose.addElement(getKeysWhenSingleElement(introspectedColumn, "keys"));
                choose.addElement(getKeysWhenMultiElement(introspectedColumn, "keys"));
            });
        }else{
            introspectedTable.getPrimaryKeyColumns().forEach(introspectedColumn -> {
                choose.addElement(getKeysWhenSingleElement(introspectedColumn, "keys"));
                choose.addElement(getKeysWhenMultiElement(introspectedColumn, "keys"));
            });
        }
        introspectedTable.getColumn(config.getTypeColumn()).ifPresent(introspectedColumn -> {
            choose.addElement(getKeysWhenSingleElement(introspectedColumn,"types"));
            choose.addElement(getKeysWhenMultiElement(introspectedColumn,"types"));
        });

        //如果没有主键列，增加一个or条件
        /*if (StringUtility.stringHasValue(config.getKeyColumn())) {
            if (introspectedTable.getPrimaryKeyColumns().stream().noneMatch(introspectedColumn -> config.getKeyColumn().equals(introspectedColumn.getActualColumnName()))) {
                for (IntrospectedColumn primaryKeyColumn : introspectedTable.getPrimaryKeyColumns()) {
                    if (!config.getKeyColumn().equals(primaryKeyColumn.getActualColumnName())) {
                        introspectedTable.getColumn(config.getKeyColumn()).ifPresent(introspectedColumn -> {
                            choose.addElement(getKeysWhenElement(introspectedColumn,"keys","or"));
                        });
                    }
                }
            }
        }*/
        //排除的列
       /* XmlElement choose1 = new XmlElement("choose");
        trim.addElement(choose1);
        introspectedTable.getPrimaryKeyColumns().forEach(introspectedColumn -> {
            choose1.addElement(getKeysWhenElement(introspectedColumn,"excludeIds","and",false));
        });*/

        //增加默认排序
        TextElement defaultOrderBy = buildOrderByDefault();
        if (defaultOrderBy != null) {
            answer.addElement(defaultOrderBy);
        }
        if (context.getPlugins().sqlMapSelectByKeysDictElementGenerated(answer, introspectedTable)) {
            parentElement.addElement(answer);
        }
    }

    private XmlElement getKeysWhenSingleElement(IntrospectedColumn column,String listProperty) {
        //单一key的情况
        XmlElement when = new XmlElement("when");
        when.addAttribute(new Attribute("test", listProperty+".size() == 1"));
        String line = " and ";
        line += MyBatis3FormattingUtilities.getEscapedColumnName(column);
        line += " = ";
        line += getParameterClause(column,listProperty+"[0]");
        when.addElement(new TextElement(line));
        return when;
    }

    private XmlElement getKeysWhenMultiElement(IntrospectedColumn column,String listProperty) {
        XmlElement when = new XmlElement("when");
        when.addAttribute(new Attribute("test", listProperty+".size() > 1"));
        String line = " and ";
        line += MyBatis3FormattingUtilities.getEscapedColumnName(column);
        line += " in ";
        when.addElement(new TextElement(line));
        XmlElement foreach = new XmlElement("foreach");
        when.addElement(foreach);
        foreach.addAttribute(new Attribute("collection", listProperty));
        foreach.addAttribute(new Attribute("item", "item"));
        foreach.addAttribute(new Attribute("open", "("));
        foreach.addAttribute(new Attribute("separator", ","));
        foreach.addAttribute(new Attribute("close", ")"));
        foreach.addElement(new TextElement(getParameterClause(column,"item")));
        return when;
    }

    private String getParameterClause(IntrospectedColumn introspectedColumn,String vKey) {
        StringBuilder sb = new StringBuilder();
        sb.append("#{");
        sb.append(vKey);
        sb.append(",jdbcType=");
        sb.append(introspectedColumn.getJdbcTypeName());
        if (stringHasValue(introspectedColumn.getTypeHandler())) {
            sb.append(",typeHandler=");
            sb.append(introspectedColumn.getTypeHandler());
        }
        sb.append('}');
        return sb.toString();
    }
}
