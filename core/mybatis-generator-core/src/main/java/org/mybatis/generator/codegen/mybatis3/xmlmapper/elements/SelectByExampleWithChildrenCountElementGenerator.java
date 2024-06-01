package org.mybatis.generator.codegen.mybatis3.xmlmapper.elements;

import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;

public class SelectByExampleWithChildrenCountElementGenerator extends
        AbstractXmlElementGenerator {

    public SelectByExampleWithChildrenCountElementGenerator() {
        super();
    }

    @Override
    public void addElements(XmlElement parentElement) {
        String fqjt = introspectedTable.getExampleType();
        String aliasPrefix = VStringUtil.stringHasValue(introspectedTable.getTableConfiguration().getAlias())?introspectedTable.getTableConfiguration().getAlias()+"_":"";
        XmlElement answer = new XmlElement("select");
        answer.addAttribute(new Attribute("id",introspectedTable.getSelectByExampleWithChildrenCountStatementId()));
        answer.addAttribute(new Attribute("resultMap", "ResultMapChildrenCount"));
        answer.addAttribute(new Attribute("parameterType", fqjt));
        context.getCommentGenerator().addComment(answer);

        answer.addElement(new TextElement("select")); //$NON-NLS-1$
        XmlElement ifElement = new XmlElement("if"); //$NON-NLS-1$
        ifElement.addAttribute(new Attribute("test", "distinct")); //$NON-NLS-1$ //$NON-NLS-2$
        ifElement.addElement(new TextElement("distinct")); //$NON-NLS-1$
        answer.addElement(ifElement);
        answer.addElement(getBaseColumnListElement());
        answer.addElement(new TextElement(",")); //$NON-NLS-1$
        StringBuilder sb = new StringBuilder();
        sb.append("(SELECT count(0) FROM ");
        sb.append(introspectedTable.getTableConfiguration().getTableName());
        sb.append(" t WHERE ");
        sb.append("t.parent_id = ");
        sb.append(aliasPrefix).append(introspectedTable.getPrimaryKeyColumns().get(0).getActualColumnName());
        sb.append(") as ").append(aliasPrefix).append("children_count");
        answer.addElement(new TextElement(sb.toString()));

        sb.setLength(0);
        sb.append("from ");
        sb.append(introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime());
        answer.addElement(new TextElement(sb.toString()));
        answer.addElement(getExampleIncludeElement());

        ifElement = new XmlElement("if"); //$NON-NLS-1$
        ifElement.addAttribute(new Attribute("test", "orderByClause != null")); //$NON-NLS-1$ //$NON-NLS-2$
        ifElement.addElement(new TextElement("order by ${orderByClause}")); //$NON-NLS-1$
        answer.addElement(ifElement);

        parentElement.addElement(answer);
    }
}
