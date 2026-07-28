package org.mybatis.generator.codegen.mybatis3.xmlmapper.elements;

import com.vgosoft.core.db.query.DatabaseType;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.ListUtilities;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;
import org.mybatis.generator.config.PropertyRegistry;

/**
 * @author cen_c
 */
public class InsertOrUpdateSelectiveElementGenerator extends AbstractXmlElementGenerator {

    public InsertOrUpdateSelectiveElementGenerator() {
        super();
    }

    @Override
    public void addElements(XmlElement parentElement) {
        introspectedTable.getTableConfiguration().getAdditionalDbTypes().forEach(dbType -> {
            XmlElement answer = buildInitialInsert(introspectedTable.getInsertOrUpdateStatementId(), introspectedTable.getRules().calculateAllFieldsClass(), dbType);
            switch (dbType) {
                case MYSQL, MARIADB:
                    StringBuilder sb = new StringBuilder();
                    sb.append("insert into ");
                    sb.append(introspectedTable.getFullyQualifiedTableNameAtRuntime());
                    answer.addElement(new TextElement(sb.toString()));
                    XmlElement insertTrimElement = new XmlElement("trim");
                    insertTrimElement.addAttribute(new Attribute("prefix", "("));
                    insertTrimElement.addAttribute(new Attribute("suffix", ")"));
                    insertTrimElement.addAttribute(new Attribute("suffixOverrides", ","));
                    answer.addElement(insertTrimElement);
                    XmlElement valuesTrimElement = new XmlElement("trim");
                    valuesTrimElement.addAttribute(new Attribute("prefix", "values ("));
                    valuesTrimElement.addAttribute(new Attribute("suffix", ")"));
                    valuesTrimElement.addAttribute(new Attribute("suffixOverrides", ","));
                    answer.addElement(valuesTrimElement);
                    answer.addElement(new TextElement("ON duplicate KEY UPDATE"));
                    XmlElement updateTrimElement = new XmlElement("trim");
                    updateTrimElement.addAttribute(new Attribute("prefix", ""));
                    updateTrimElement.addAttribute(new Attribute("suffix", ""));
                    updateTrimElement.addAttribute(new Attribute("suffixOverrides", ","));
                    answer.addElement(updateTrimElement);
                    for (IntrospectedColumn introspectedColumn :
                            ListUtilities.removeIdentityAndGeneratedAlwaysColumns(introspectedTable.getAllColumns())) {
                        if (introspectedColumn.isSequenceColumn() || introspectedColumn.getFullyQualifiedJavaType().isPrimitive()) {
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
                        sb.append(" != null");
                        XmlElement insertNotNullElement = new XmlElement("if");
                        insertNotNullElement.addAttribute(new Attribute("test", sb.toString()));
                        sb.setLength(0);
                        sb.append(MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn));
                        sb.append(',');
                        insertNotNullElement.addElement(new TextElement(sb.toString()));
                        insertTrimElement.addElement(insertNotNullElement);
                        sb.setLength(0);
                        sb.append(introspectedColumn.getJavaProperty());
                        sb.append(" != null");
                        XmlElement valuesNotNullElement = new XmlElement("if");
                        valuesNotNullElement.addAttribute(new Attribute("test", sb.toString()));
                        sb.setLength(0);
                        sb.append(MyBatis3FormattingUtilities.getParameterClause(introspectedColumn));
                        sb.append(',');
                        valuesNotNullElement.addElement(new TextElement(sb.toString()));
                        valuesTrimElement.addElement(valuesNotNullElement);
                    }
                    for (IntrospectedColumn introspectedColumn : ListUtilities.removeGeneratedAlwaysColumns(introspectedTable.getNonPrimaryKeyColumns())) {
                        sb.setLength(0);
                        sb.append(introspectedColumn.getJavaProperty());
                        sb.append(" != null");
                        XmlElement updateNotNullElement = new XmlElement("if");
                        updateNotNullElement.addAttribute(new Attribute("test", sb.toString()));
                        updateTrimElement.addElement(updateNotNullElement);
                        sb.setLength(0);
                        sb.append(MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn));
                        sb.append(" = ");
                        sb.append(MyBatis3FormattingUtilities.getParameterClause(introspectedColumn));
                        sb.append(',');
                        updateNotNullElement.addElement(new TextElement(sb.toString()));
                    }
                    break;
                case POSTGRE_SQL, KINGBASE_ES, HIGHGO, SQLITE:

                    break;
                case ORACLE, DB2, SQL_SERVER, DM:
                    sb = new StringBuilder();
                    sb.append("merge into ");
                    sb.append(introspectedTable.getFullyQualifiedTableNameAtRuntime());
                    sb.append(" t using dual on (");
                    for (IntrospectedColumn introspectedColumn : introspectedTable.getPrimaryKeyColumns()) {
                        sb.append(MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn));
                        sb.append(" = ");
                        sb.append(MyBatis3FormattingUtilities.getParameterClause(introspectedColumn));
                        sb.append(" and ");
                    }
                    sb.setLength(sb.length() - 5);
                    sb.append(")");
                    answer.addElement(new TextElement(sb.toString()));
                    XmlElement whenNotMatchedElement = new XmlElement("when");
                    whenNotMatchedElement.addAttribute(new Attribute("condition", "not matched"));
                    answer.addElement(whenNotMatchedElement);
                    XmlElement insertTrimElement1 = new XmlElement("trim");
                    insertTrimElement1.addAttribute(new Attribute("prefix", "("));
                    insertTrimElement1.addAttribute(new Attribute("suffix", ")"));
                    insertTrimElement1.addAttribute(new Attribute("suffixOverrides", ","));
                    whenNotMatchedElement.addElement(insertTrimElement1);
                    break;
                default:
                    break;
            }
            if (context.getPlugins().sqlMapInsertOrUpdateSelectiveElementGenerated(answer, introspectedTable)) {
                parentElement.addElement(answer);
            }
        });
    }
}
