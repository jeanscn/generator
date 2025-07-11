package org.mybatis.generator.codegen.mybatis3.xmlmapper.elements;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.OutputUtilities;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.ListUtilities;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;

import java.util.Iterator;

public class UpdateByExampleWithoutBLOBsElementGenerator extends
        AbstractXmlElementGenerator {

    public UpdateByExampleWithoutBLOBsElementGenerator() {
        super();
    }

    @Override
    public void addElements(XmlElement parentElement) {
        XmlElement answer = new XmlElement("update"); //$NON-NLS-1$

        answer.addAttribute(new Attribute("id", //$NON-NLS-1$
                introspectedTable.getUpdateByExampleStatementId()));

        answer.addAttribute(new Attribute("parameterType", "map")); //$NON-NLS-1$ //$NON-NLS-2$

        context.getCommentGenerator().addComment(answer);

        StringBuilder sb = new StringBuilder();
        sb.append("update "); //$NON-NLS-1$
        /*b.append(introspectedTable
                .getAliasedFullyQualifiedTableNameAtRuntime());*/
        //addUpdateAliasedFullyQualifiedTableName(introspectedTable,sb);
        sb.append(introspectedTable.getFullyQualifiedTableNameAtRuntime());
        answer.addElement(new TextElement(sb.toString()));

        // set up for first column
        sb.setLength(0);
        sb.append("set "); //$NON-NLS-1$

        Iterator<IntrospectedColumn> iter =
                ListUtilities.removeGeneratedAlwaysColumns(introspectedTable.getNonBLOBColumns()).iterator();
        while (iter.hasNext()) {
            IntrospectedColumn introspectedColumn = iter.next();

            //sb.append(MyBatis3FormattingUtilities
            //        .getAliasedEscapedColumnName(introspectedColumn));
            sb.append(MyBatis3FormattingUtilities
                    .getEscapedColumnName(introspectedColumn));
            sb.append(" = "); //$NON-NLS-1$
            sb.append(MyBatis3FormattingUtilities.getParameterClause(
                    introspectedColumn, "record.")); //$NON-NLS-1$

            if (iter.hasNext()) {
                sb.append(',');
            }

            answer.addElement(new TextElement(sb.toString()));

            // set up for the next column
            if (iter.hasNext()) {
                sb.setLength(0);
                OutputUtilities.xmlIndent(sb, 1);
            }
        }
        //addUpdateAliasedFullyQualifiedTableNameFrom(introspectedTable,answer);
        answer.addElement(getUpdateByExampleIncludeElement());
        // 增加if判断，防止传入的参数为null或空
        XmlElement ifElement = new XmlElement("if"); //$NON-NLS-1$
        ifElement.addAttribute(new Attribute("test", "_parameter == null or _parameter.oredCriteria == null or _parameter.oredCriteria.size() == 0"));
        ifElement.addElement(new TextElement("-- 防止无条件删除，返回影响0行"));
        XmlElement where = new XmlElement("where");
        where.addElement(new TextElement("1 = 0"));
        ifElement.addElement(where);
        answer.addElement(ifElement);
        if (context.getPlugins()
                .sqlMapUpdateByExampleWithoutBLOBsElementGenerated(answer,
                        introspectedTable)) {
            parentElement.addElement(answer);
        }
    }
}
