package org.mybatis.generator.codegen.mybatis3.xmlmapper.elements;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.ListUtilities;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;

public class InsertOrUpdateSelectiveElementGenerator extends AbstractXmlElementGenerator {

    public InsertOrUpdateSelectiveElementGenerator() {
        super();
    }

    @Override
    public void addElements(XmlElement parentElement) {
        XmlElement answer = buildInitialInsert(introspectedTable.getInsertOrUpdateStatementId(),
                introspectedTable.getRules().calculateAllFieldsClass());

        StringBuilder sb = new StringBuilder();

        sb.append("insert into "); //$NON-NLS-1$
        sb.append(introspectedTable.getFullyQualifiedTableNameAtRuntime());
        answer.addElement(new TextElement(sb.toString()));

        XmlElement insertTrimElement = new XmlElement("trim"); //$NON-NLS-1$
        insertTrimElement.addAttribute(new Attribute("prefix", "(")); //$NON-NLS-1$ //$NON-NLS-2$
        insertTrimElement.addAttribute(new Attribute("suffix", ")")); //$NON-NLS-1$ //$NON-NLS-2$
        insertTrimElement.addAttribute(new Attribute("suffixOverrides", ",")); //$NON-NLS-1$ //$NON-NLS-2$
        answer.addElement(insertTrimElement);

        XmlElement valuesTrimElement = new XmlElement("trim"); //$NON-NLS-1$
        valuesTrimElement.addAttribute(new Attribute("prefix", "values (")); //$NON-NLS-1$ //$NON-NLS-2$
        valuesTrimElement.addAttribute(new Attribute("suffix", ")")); //$NON-NLS-1$ //$NON-NLS-2$
        valuesTrimElement.addAttribute(new Attribute("suffixOverrides", ",")); //$NON-NLS-1$ //$NON-NLS-2$
        answer.addElement(valuesTrimElement);

        answer.addElement(new TextElement("ON duplicate KEY UPDATE"));
        XmlElement updateTrimElement = new XmlElement("trim"); //$NON-NLS-1$
        updateTrimElement.addAttribute(new Attribute("prefix", "")); //$NON-NLS-1$ //$NON-NLS-2$
        updateTrimElement.addAttribute(new Attribute("suffix", "")); //$NON-NLS-1$ //$NON-NLS-2$
        updateTrimElement.addAttribute(new Attribute("suffixOverrides", ",")); //$NON-NLS-1$ //$NON-NLS-2$
        answer.addElement(updateTrimElement);

        for (IntrospectedColumn introspectedColumn :
                ListUtilities.removeIdentityAndGeneratedAlwaysColumns(introspectedTable.getAllColumns())) {

            if (introspectedColumn.isSequenceColumn() || introspectedColumn.getFullyQualifiedJavaType().isPrimitive()) {
                // if it is a sequence column, it is not optional
                // This is required for MyBatis3 because MyBatis3 parses
                // and calculates the SQL before executing the selectKey

                // if it is primitive, we cannot do a null check
                sb.setLength(0);
                sb.append(MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn));
                sb.append(',');
                insertTrimElement.addElement(new TextElement(sb.toString()));

                sb.setLength(0);
                sb.append(MyBatis3FormattingUtilities.getParameterClause(introspectedColumn));
                sb.append(',');
                valuesTrimElement.addElement(new TextElement(sb.toString()));

                continue;
            }

            sb.setLength(0);
            sb.append(introspectedColumn.getJavaProperty());
            sb.append(" != null"); //$NON-NLS-1$
            XmlElement insertNotNullElement = new XmlElement("if"); //$NON-NLS-1$
            insertNotNullElement.addAttribute(new Attribute(
                    "test", sb.toString())); //$NON-NLS-1$

            sb.setLength(0);
            sb.append(MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn));
            sb.append(',');
            insertNotNullElement.addElement(new TextElement(sb.toString()));
            insertTrimElement.addElement(insertNotNullElement);

            sb.setLength(0);
            sb.append(introspectedColumn.getJavaProperty());
            sb.append(" != null"); //$NON-NLS-1$
            XmlElement valuesNotNullElement = new XmlElement("if"); //$NON-NLS-1$
            valuesNotNullElement.addAttribute(new Attribute("test", sb.toString())); //$NON-NLS-1$

            sb.setLength(0);
            sb.append(MyBatis3FormattingUtilities.getParameterClause(introspectedColumn));
            sb.append(',');
            valuesNotNullElement.addElement(new TextElement(sb.toString()));
            valuesTrimElement.addElement(valuesNotNullElement);
        }

        for (IntrospectedColumn introspectedColumn :
                ListUtilities.removeGeneratedAlwaysColumns(introspectedTable.getNonPrimaryKeyColumns())) {
            sb.setLength(0);
            sb.append(introspectedColumn.getJavaProperty());
            sb.append(" != null"); //$NON-NLS-1$
            XmlElement updateNotNullElement = new XmlElement("if"); //$NON-NLS-1$
            updateNotNullElement.addAttribute(new Attribute("test", sb.toString())); //$NON-NLS-1$
            updateTrimElement.addElement(updateNotNullElement);

            sb.setLength(0);
            sb.append(MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn));
            sb.append(" = "); //$NON-NLS-1$
            sb.append(MyBatis3FormattingUtilities.getParameterClause(introspectedColumn));
            sb.append(',');
            updateNotNullElement.addElement(new TextElement(sb.toString()));
        }

        if (context.getPlugins().sqlMapInsertOrUpdateSelectiveElementGenerated(answer, introspectedTable)) {
            parentElement.addElement(answer);
        }
    }
}
