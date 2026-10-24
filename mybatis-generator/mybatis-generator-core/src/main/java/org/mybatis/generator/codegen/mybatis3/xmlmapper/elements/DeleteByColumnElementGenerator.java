package org.mybatis.generator.codegen.mybatis3.xmlmapper.elements;

import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;
import org.mybatis.generator.config.SelectByColumnGeneratorConfiguration;
import org.mybatis.generator.codegen.mybatis3.LogicalDeleteGenerationValidator;
import org.mybatis.generator.api.AbstractIntrospectedTable;
import org.mybatis.generator.api.IntrospectedColumn;

public class DeleteByColumnElementGenerator extends AbstractXmlElementGenerator {

    private final SelectByColumnGeneratorConfiguration configuration;

    public DeleteByColumnElementGenerator(SelectByColumnGeneratorConfiguration configuration) {
        super();
        this.configuration = configuration;
    }

    @Override
    public void addElements(XmlElement parentElement) {
        if (configuration.isLogicalDelete()) {
            parentElement.addElement(logicalStatement(introspectedTable, configuration));
            return;
        }
        XmlElement answer = new XmlElement("delete");
        answer.addAttribute(new Attribute("id", configuration.getDeleteMethodName()));
        if (configuration.getColumns().size() == 1) {
            answer.addAttribute(new Attribute("parameterType", configuration.getColumns().get(0).getFullyQualifiedJavaType().getFullyQualifiedName()));
        }
        context.getCommentGenerator().addComment(answer);
        answer.addElement(new TextElement("delete from " + introspectedTable.getFullyQualifiedTableNameAtRuntime()));
        //where
        XmlElement where = new XmlElement("where");
        configuration.getColumns().forEach(column -> {
            if (configuration.getParameterList()) {
                where.addElement(new TextElement("and " + MyBatis3FormattingUtilities.getEscapedColumnName(column) + " in "));
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
                where.addElement(new TextElement("and " + MyBatis3FormattingUtilities.getEscapedColumnName(column) + " = " +
                        MyBatis3FormattingUtilities.getParameterClause(column)));
            }
        });
        answer.addElement(where);
        parentElement.addElement(answer);
    }

    /**
     * 生成普通表的逻辑删除；条件均为配置期列名，不接受客户端 SQL。
     *
     * @param table 已核验的表
     * @param config 删除配置
     * @return 原子更新标志及可选版本的语句
     */
    public static XmlElement logicalStatement(AbstractIntrospectedTable table,
                                              SelectByColumnGeneratorConfiguration config) {
        XmlElement answer = new XmlElement("update");
        answer.addAttribute(new Attribute("id", config.getDeleteMethodName()));
        table.getContext().getCommentGenerator().addComment(answer);
        String flag = MyBatis3FormattingUtilities.getEscapedColumnName(LogicalDeleteGenerationValidator.flag(table));
        StringBuilder sql = new StringBuilder("update ").append(table.getFullyQualifiedTableNameAtRuntime())
                .append(" set ").append(flag).append(" = 1");
        table.getColumn("version_").ifPresent(v -> {
            String name = MyBatis3FormattingUtilities.getEscapedColumnName(v);
            sql.append(", ").append(name).append(" = ").append(name).append(" + 1");
        });
        answer.addElement(new TextElement(sql.toString()));
        XmlElement where = new XmlElement("where");
        where.addElement(new TextElement(flag + " = 0"));
        for (var column : config.getColumns()) {
            String name = MyBatis3FormattingUtilities.getEscapedColumnName(column);
            if (config.getParameterList()) {
                where.addElement(new TextElement("and " + name + " in"));
                XmlElement loop = new XmlElement("foreach");
                loop.addAttribute(new Attribute("collection", column.getJavaProperty() + "s"));
                loop.addAttribute(new Attribute("item", "item"));
                loop.addAttribute(new Attribute("open", "("));
                loop.addAttribute(new Attribute("close", ")"));
                loop.addAttribute(new Attribute("separator", ","));
                loop.addElement(new TextElement(typedParameter(column, "item")));
                where.addElement(loop);
            } else {
                where.addElement(new TextElement("and " + name + " = " + typedParameter(column, column.getJavaProperty())));
            }
        }
        table.getColumn("version_").ifPresent(v -> {
            String name = MyBatis3FormattingUtilities.getEscapedColumnName(v);
            String max = "BIGINT".equals(v.getJdbcTypeName()) ? "9223372036854775807" : "2147483647";
            where.addElement(new TextElement("and " + name + " &gt; 0 and " + name + " &lt; " + max));
        });
        answer.addElement(where);
        return answer;
    }

    private static String typedParameter(IntrospectedColumn column, String property) {
        return "#{" + property + ",jdbcType=" + column.getJdbcTypeName() + ",javaType="
                + column.getFullyQualifiedJavaType().getFullyQualifiedName() + "}";
    }
}
