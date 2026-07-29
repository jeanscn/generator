package org.mybatis.generator.codegen.mybatis3.xmlmapper.elements;

import com.vgosoft.core.db.query.DatabaseType;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.ListUtilities;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;
import org.mybatis.generator.config.PropertyRegistry;

import java.util.List;

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
            List<IntrospectedColumn> insertColumns = ListUtilities.removeIdentityAndGeneratedAlwaysColumns(introspectedTable.getAllColumns());
            List<IntrospectedColumn> updateColumns = ListUtilities.removeGeneratedAlwaysColumns(introspectedTable.getNonPrimaryKeyColumns());
            List<IntrospectedColumn> primaryKeyColumns = introspectedTable.getPrimaryKeyColumns();
            switch (dbType) {
                case MYSQL, MARIADB:
                    addInsertHeader(answer);
                    XmlElement mysqlInsertTrimElement = buildColumnsTrimElement();
                    answer.addElement(mysqlInsertTrimElement);
                    XmlElement mysqlValuesTrimElement = buildValuesTrimElement();
                    answer.addElement(mysqlValuesTrimElement);
                    addSelectiveInsertColumnsAndValues(mysqlInsertTrimElement, mysqlValuesTrimElement, insertColumns, false);
                    answer.addElement(new TextElement("ON duplicate KEY UPDATE"));
                    XmlElement mysqlUpdateTrimElement = buildUpdateTrimElement("", "");
                    answer.addElement(mysqlUpdateTrimElement);
                    addSelectiveUpdateAssignments(mysqlUpdateTrimElement, updateColumns, null, null);
                    break;
                case POSTGRE_SQL, KINGBASE_ES, HIGHGO, SQLITE:
                    addInsertHeader(answer);
                    XmlElement pgInsertTrimElement = buildColumnsTrimElement();
                    answer.addElement(pgInsertTrimElement);
                    XmlElement pgValuesTrimElement = buildValuesTrimElement();
                    answer.addElement(pgValuesTrimElement);
                    addSelectiveInsertColumnsAndValues(pgInsertTrimElement, pgValuesTrimElement, insertColumns, false);
                    if (!primaryKeyColumns.isEmpty()) {
                        answer.addElement(new TextElement("on conflict " + buildConflictKeys(primaryKeyColumns) + " do update"));
                        XmlElement pgUpdateTrimElement = buildUpdateTrimElement("set ", "");
                        answer.addElement(pgUpdateTrimElement);
                        addSelectiveUpdateAssignments(pgUpdateTrimElement, updateColumns, null, null);
                    }
                    break;
                case ORACLE, DM, XU_GU, CACHEDB, OTHER:
                    addMergeByDual(answer, primaryKeyColumns, insertColumns, updateColumns);
                    break;
                case DB2, SQL_SERVER, SQL_SERVER2005, H2, HSQL:
                    addMergeByValues(answer, primaryKeyColumns, insertColumns, updateColumns);
                    break;
                case PHOENIX:
                    answer.addElement(new TextElement("upsert into " + introspectedTable.getFullyQualifiedTableNameAtRuntime()));
                    XmlElement phoenixInsertTrimElement = buildColumnsTrimElement();
                    answer.addElement(phoenixInsertTrimElement);
                    XmlElement phoenixValuesTrimElement = buildValuesTrimElement();
                    answer.addElement(phoenixValuesTrimElement);
                    addSelectiveInsertColumnsAndValues(phoenixInsertTrimElement, phoenixValuesTrimElement, insertColumns, false);
                    break;
                default:
                    break;
            }
            if (context.getPlugins().sqlMapInsertOrUpdateSelectiveElementGenerated(answer, introspectedTable)) {
                parentElement.addElement(answer);
            }
        });
    }

    private void addInsertHeader(XmlElement answer) {
        answer.addElement(new TextElement("insert into " + introspectedTable.getFullyQualifiedTableNameAtRuntime()));
    }

    private XmlElement buildColumnsTrimElement() {
        XmlElement insertTrimElement = new XmlElement("trim");
        insertTrimElement.addAttribute(new Attribute("prefix", "("));
        insertTrimElement.addAttribute(new Attribute("suffix", ")"));
        insertTrimElement.addAttribute(new Attribute("suffixOverrides", ","));
        return insertTrimElement;
    }

    private XmlElement buildValuesTrimElement() {
        XmlElement valuesTrimElement = new XmlElement("trim");
        valuesTrimElement.addAttribute(new Attribute("prefix", "values ("));
        valuesTrimElement.addAttribute(new Attribute("suffix", ")"));
        valuesTrimElement.addAttribute(new Attribute("suffixOverrides", ","));
        return valuesTrimElement;
    }

    private XmlElement buildUpdateTrimElement(String prefix, String suffix) {
        XmlElement updateTrimElement = new XmlElement("trim");
        updateTrimElement.addAttribute(new Attribute("prefix", prefix));
        updateTrimElement.addAttribute(new Attribute("suffix", suffix));
        updateTrimElement.addAttribute(new Attribute("suffixOverrides", ","));
        return updateTrimElement;
    }

    private void addSelectiveInsertColumnsAndValues(XmlElement insertTrimElement, XmlElement valuesTrimElement,
                                                    List<IntrospectedColumn> columns, boolean sourceAliasValues) {
        StringBuilder sb = new StringBuilder();
        for (IntrospectedColumn introspectedColumn : columns) {
            if (introspectedColumn.isSequenceColumn() || introspectedColumn.getFullyQualifiedJavaType().isPrimitive()) {
                insertTrimElement.addElement(new TextElement(MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn) + ","));
                valuesTrimElement.addElement(new TextElement(getInsertValueClause(introspectedColumn, sourceAliasValues) + ","));
                continue;
            }
            sb.setLength(0);
            sb.append(introspectedColumn.getJavaProperty());
            sb.append(" != null");

            XmlElement insertNotNullElement = new XmlElement("if");
            insertNotNullElement.addAttribute(new Attribute("test", sb.toString()));
            insertNotNullElement.addElement(new TextElement(MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn) + ","));
            insertTrimElement.addElement(insertNotNullElement);

            XmlElement valuesNotNullElement = new XmlElement("if");
            valuesNotNullElement.addAttribute(new Attribute("test", sb.toString()));
            valuesNotNullElement.addElement(new TextElement(getInsertValueClause(introspectedColumn, sourceAliasValues) + ","));
            valuesTrimElement.addElement(valuesNotNullElement);
        }
    }

    private String getInsertValueClause(IntrospectedColumn introspectedColumn, boolean sourceAliasValues) {
        if (sourceAliasValues) {
            return "s." + introspectedColumn.getJavaProperty();
        }
        return MyBatis3FormattingUtilities.getParameterClause(introspectedColumn);
    }

    private void addSelectiveUpdateAssignments(XmlElement updateTrimElement, List<IntrospectedColumn> updateColumns,
                                               String targetAlias, String sourceAlias) {
        for (IntrospectedColumn introspectedColumn : updateColumns) {
            XmlElement updateNotNullElement = new XmlElement("if");
            updateNotNullElement.addAttribute(new Attribute("test", introspectedColumn.getJavaProperty() + " != null"));
            StringBuilder sb = new StringBuilder();
            if (targetAlias != null) {
                sb.append(targetAlias).append('.');
            }
            sb.append(MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn));
            sb.append(" = ");
            if (sourceAlias != null) {
                sb.append(sourceAlias).append('.').append(introspectedColumn.getJavaProperty());
            } else {
                sb.append(MyBatis3FormattingUtilities.getParameterClause(introspectedColumn));
            }
            sb.append(',');
            updateNotNullElement.addElement(new TextElement(sb.toString()));
            updateTrimElement.addElement(updateNotNullElement);
        }
    }

    private String buildConflictKeys(List<IntrospectedColumn> primaryKeyColumns) {
        StringBuilder sb = new StringBuilder("(");
        for (IntrospectedColumn primaryKeyColumn : primaryKeyColumns) {
            sb.append(MyBatis3FormattingUtilities.getEscapedColumnName(primaryKeyColumn)).append(", ");
        }
        sb.setLength(sb.length() - 2);
        sb.append(')');
        return sb.toString();
    }

    private void addMergeByDual(XmlElement answer, List<IntrospectedColumn> primaryKeyColumns, List<IntrospectedColumn> insertColumns,
                                List<IntrospectedColumn> updateColumns) {
        if (primaryKeyColumns.isEmpty()) {
            addInsertHeader(answer);
            XmlElement insertTrimElement = buildColumnsTrimElement();
            answer.addElement(insertTrimElement);
            XmlElement valuesTrimElement = buildValuesTrimElement();
            answer.addElement(valuesTrimElement);
            addSelectiveInsertColumnsAndValues(insertTrimElement, valuesTrimElement, insertColumns, false);
            return;
        }
        answer.addElement(new TextElement("merge into " + introspectedTable.getFullyQualifiedTableNameAtRuntime() + " t"));
        XmlElement sourceTrim = buildUpdateTrimElement("using (select ", " from dual) s");
        answer.addElement(sourceTrim);
        for (IntrospectedColumn introspectedColumn : insertColumns) {
            sourceTrim.addElement(new TextElement(MyBatis3FormattingUtilities.getParameterClause(introspectedColumn) + " as " + introspectedColumn.getJavaProperty() + ","));
        }
        answer.addElement(new TextElement("on (" + buildMergeMatchCondition(primaryKeyColumns) + ")"));
        answer.addElement(new TextElement("when matched then"));
        XmlElement updateTrimElement = buildUpdateTrimElement("update set ", "");
        answer.addElement(updateTrimElement);
        addSelectiveUpdateAssignments(updateTrimElement, updateColumns, "t", "s");
        answer.addElement(new TextElement("when not matched then insert"));
        XmlElement insertTrimElement = buildColumnsTrimElement();
        answer.addElement(insertTrimElement);
        XmlElement valuesTrimElement = buildValuesTrimElement();
        answer.addElement(valuesTrimElement);
        addSelectiveInsertColumnsAndValues(insertTrimElement, valuesTrimElement, insertColumns, true);
    }

    private void addMergeByValues(XmlElement answer, List<IntrospectedColumn> primaryKeyColumns, List<IntrospectedColumn> insertColumns,
                                  List<IntrospectedColumn> updateColumns) {
        if (primaryKeyColumns.isEmpty()) {
            addInsertHeader(answer);
            XmlElement insertTrimElement = buildColumnsTrimElement();
            answer.addElement(insertTrimElement);
            XmlElement valuesTrimElement = buildValuesTrimElement();
            answer.addElement(valuesTrimElement);
            addSelectiveInsertColumnsAndValues(insertTrimElement, valuesTrimElement, insertColumns, false);
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("merge into ");
        sb.append(introspectedTable.getFullyQualifiedTableNameAtRuntime());
        sb.append(" t using (values (");
        for (IntrospectedColumn introspectedColumn : insertColumns) {
            sb.append(MyBatis3FormattingUtilities.getParameterClause(introspectedColumn));
            sb.append(", ");
        }
        sb.setLength(sb.length() - 2);
        sb.append(")) s(");
        for (IntrospectedColumn introspectedColumn : insertColumns) {
            sb.append(introspectedColumn.getJavaProperty());
            sb.append(", ");
        }
        sb.setLength(sb.length() - 2);
        sb.append(") on (");
        sb.append(buildMergeMatchCondition(primaryKeyColumns));
        sb.append(")");
        answer.addElement(new TextElement(sb.toString()));
        answer.addElement(new TextElement("when matched then"));
        XmlElement updateTrimElement = buildUpdateTrimElement("update set ", "");
        answer.addElement(updateTrimElement);
        addSelectiveUpdateAssignments(updateTrimElement, updateColumns, "t", "s");
        answer.addElement(new TextElement("when not matched then insert"));
        XmlElement insertTrimElement = buildColumnsTrimElement();
        answer.addElement(insertTrimElement);
        XmlElement valuesTrimElement = buildValuesTrimElement();
        answer.addElement(valuesTrimElement);
        addSelectiveInsertColumnsAndValues(insertTrimElement, valuesTrimElement, insertColumns, true);
    }

    private String buildMergeMatchCondition(List<IntrospectedColumn> primaryKeyColumns) {
        StringBuilder sb = new StringBuilder();
        for (IntrospectedColumn introspectedColumn : primaryKeyColumns) {
            sb.append("t.");
            sb.append(MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn));
            sb.append(" = s.");
            sb.append(introspectedColumn.getJavaProperty());
            sb.append(" and ");
        }
        sb.setLength(sb.length() - 5);
        return sb.toString();
    }
}
