package org.mybatis.generator.codegen.mybatis3.xmlmapper.elements;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;

import java.util.List;

/**
 * 生成按主键原子递增的XML语句。
 *
 * @author vgosoft-agent
 */
public class AtomicIncrementByPrimaryKeyElementGenerator extends AbstractXmlElementGenerator {

    @Override
    public void addElements(XmlElement parentElement) {
        XmlElement answer = new XmlElement("update");
        answer.addAttribute(new Attribute("id", introspectedTable.getTableConfiguration().getAtomicIncrementStatementId()));
        context.getCommentGenerator().addComment(answer);

        answer.addElement(new TextElement("update " + introspectedTable.getFullyQualifiedTableNameAtRuntime()));

        List<String> configuredColumns = introspectedTable.getTableConfiguration().getAtomicIncrementColumns();
        for (int i = 0; i < configuredColumns.size(); i++) {
            String columnName = configuredColumns.get(i);
            IntrospectedColumn introspectedColumn = introspectedTable.getColumn(columnName).orElseThrow(() ->
                    new IllegalStateException("原子递增字段不存在：" + columnName));
            String escapedColumnName = MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn);
            String prefix = i == 0 ? "set " : "  ";
            String suffix = i == configuredColumns.size() - 1 ? "" : ",";
            answer.addElement(new TextElement(prefix + escapedColumnName + " = " + escapedColumnName + " + 1" + suffix));
        }

        buildPrimaryKeyWhereClause().forEach(answer::addElement);

        if (context.getPlugins().sqlMapUpdateBatchElementGenerated(answer, introspectedTable)) {
            parentElement.addElement(answer);
        }
    }
}
