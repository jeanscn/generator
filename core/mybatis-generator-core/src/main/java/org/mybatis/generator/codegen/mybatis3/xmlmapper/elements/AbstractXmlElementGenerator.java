package org.mybatis.generator.codegen.mybatis3.xmlmapper.elements;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.OutputUtilities;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.AbstractGenerator;
import org.mybatis.generator.codegen.mybatis3.ListUtilities;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;
import org.mybatis.generator.config.GeneratedKey;
import org.mybatis.generator.internal.util.StringUtility;

public abstract class AbstractXmlElementGenerator extends AbstractGenerator {
    public abstract void addElements(XmlElement parentElement);

    protected AbstractXmlElementGenerator() {
        super();
    }

    /**
     * This method should return an XmlElement for the select key used to
     * automatically generate keys.
     *
     * @param introspectedColumn the column related to the select key statement
     * @param generatedKey       the generated key for the current table
     * @return the selectKey element
     */
    protected XmlElement getSelectKey(IntrospectedColumn introspectedColumn,
                                      GeneratedKey generatedKey) {
        String identityColumnType = introspectedColumn.getFullyQualifiedJavaType().getFullyQualifiedName();

        XmlElement answer = new XmlElement("selectKey"); //$NON-NLS-1$
        answer.addAttribute(new Attribute("resultType", identityColumnType)); //$NON-NLS-1$
        answer.addAttribute(new Attribute(
                "keyProperty", introspectedColumn.getJavaProperty())); //$NON-NLS-1$
        answer.addAttribute(new Attribute("order", //$NON-NLS-1$
                generatedKey.getMyBatis3Order()));

        answer.addElement(new TextElement(generatedKey
                .getRuntimeSqlStatement()));

        return answer;
    }

    protected XmlElement getBaseColumnListElement() {
        XmlElement answer = new XmlElement("include"); //$NON-NLS-1$
        answer.addAttribute(new Attribute("refid", introspectedTable.getBaseColumnListId())); //$NON-NLS-1$
        return answer;
    }

    protected XmlElement getBlobColumnListElement() {
        XmlElement answer = new XmlElement("include"); //$NON-NLS-1$
        answer.addAttribute(new Attribute("refid", introspectedTable.getBlobColumnListId())); //$NON-NLS-1$
        return answer;
    }

    protected XmlElement getExampleIncludeElement() {
        XmlElement ifElement = new XmlElement("if"); //$NON-NLS-1$
        ifElement.addAttribute(new Attribute("test", "_parameter != null and _parameter.oredCriteria != null and _parameter.oredCriteria.size() > 0")); //$NON-NLS-1$ //$NON-NLS-2$

        XmlElement includeElement = new XmlElement("include"); //$NON-NLS-1$
        includeElement.addAttribute(new Attribute("refid", introspectedTable.getExampleWhereClauseId())); //$NON-NLS-1$
        ifElement.addElement(includeElement);

        return ifElement;
    }

    protected XmlElement getUpdateByExampleIncludeElement() {
        XmlElement ifElement = new XmlElement("if"); //$NON-NLS-1$
        ifElement.addAttribute(new Attribute("test", "_parameter != null and _parameter.oredCriteria != null and _parameter.oredCriteria.size() > 0")); //$NON-NLS-1$ //$NON-NLS-2$

        XmlElement includeElement = new XmlElement("include"); //$NON-NLS-1$
        includeElement.addAttribute(new Attribute("refid", //$NON-NLS-1$
                introspectedTable.getMyBatis3UpdateByExampleWhereClauseId()));
        ifElement.addElement(includeElement);
        return ifElement;
    }

    protected List<TextElement> buildSelectList(List<IntrospectedColumn> columns) {
        return buildSelectList("", columns); //$NON-NLS-1$
    }

    protected List<TextElement> buildSelectList(String initial, List<IntrospectedColumn> columns) {
        List<TextElement> answer = new ArrayList<>();
        StringBuilder sb = new StringBuilder(initial);
        Iterator<IntrospectedColumn> iter = columns.iterator();
        while (iter.hasNext()) {
            sb.append(MyBatis3FormattingUtilities.getSelectListPhrase(iter.next()));

            if (iter.hasNext()) {
                sb.append(", "); //$NON-NLS-1$
            }

            if (sb.length() > 80) {
                answer.add(new TextElement(sb.toString()));
                sb.setLength(0);
            }
        }

        if (sb.length() > 0) {
            answer.add(new TextElement(sb.toString()));
        }

        return answer;
    }

    protected List<TextElement> buildPrimaryKeyWhereClause() {
        List<TextElement> answer = new ArrayList<>();
        boolean first = true;
        for (IntrospectedColumn introspectedColumn : introspectedTable.getPrimaryKeyColumns()) {
            String line;
            if (first) {
                line = "where "; //$NON-NLS-1$
                first = false;
            } else {
                line = "  and "; //$NON-NLS-1$
            }

            line += MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn);
            line += " = "; //$NON-NLS-1$
            line += MyBatis3FormattingUtilities.getParameterClause(introspectedColumn);
            answer.add(new TextElement(line));
        }

        return answer;
    }

    protected List<TextElement> buildKeysWhereClause(List<IntrospectedColumn> columns) {
        List<TextElement> answer = new ArrayList<>();
        boolean first = true;
        for (IntrospectedColumn introspectedColumn : columns) {
            String line;
            if (first) {
                line = "where "; //$NON-NLS-1$
                first = false;
            } else {
                line = "  and "; //$NON-NLS-1$
            }

            line += MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn);
            line += " = "; //$NON-NLS-1$
            line += MyBatis3FormattingUtilities.getParameterClause(introspectedColumn);
            answer.add(new TextElement(line));
        }

        return answer;
    }

    protected XmlElement buildInitialInsert(String statementId, FullyQualifiedJavaType parameterType) {
        XmlElement answer = new XmlElement("insert"); //$NON-NLS-1$

        answer.addAttribute(new Attribute("id", statementId)); //$NON-NLS-1$

        answer.addAttribute(new Attribute("parameterType", parameterType.getFullyQualifiedName())); //$NON-NLS-1$

        context.getCommentGenerator().addComment(answer);

        introspectedTable.getGeneratedKey().ifPresent(gk ->
                introspectedTable.getColumn(gk.getColumn()).ifPresent(introspectedColumn -> {
                    // if the column is null, then it's a configuration error. The
                    // warning has already been reported
                    if (gk.isJdbcStandard()) {
                        answer.addAttribute(new Attribute("useGeneratedKeys", "true")); //$NON-NLS-1$ //$NON-NLS-2$
                        answer.addAttribute(
                                new Attribute("keyProperty", introspectedColumn.getJavaProperty())); //$NON-NLS-1$
                        answer.addAttribute(
                                new Attribute("keyColumn", introspectedColumn.getActualColumnName())); //$NON-NLS-1$
                    } else {
                        answer.addElement(getSelectKey(introspectedColumn, gk));
                    }
                })
        );

        return answer;
    }

    protected enum ResultElementType {
        ID("id"), //$NON-NLS-1$
        RESULT("result"); //$NON-NLS-1$

        private final String value;

        ResultElementType(String value) {
            this.value = value;
        }
    }

    protected List<XmlElement> buildResultMapItems(ResultElementType elementType, List<IntrospectedColumn> columns) {
        List<XmlElement> answer = new ArrayList<>();
        for (IntrospectedColumn introspectedColumn : columns) {
            XmlElement resultElement = new XmlElement(elementType.value);

            resultElement.addAttribute(buildColumnAttribute(introspectedColumn));
            resultElement.addAttribute(new Attribute("property", introspectedColumn.getJavaProperty())); //$NON-NLS-1$
            resultElement.addAttribute(new Attribute("jdbcType", introspectedColumn.getJdbcTypeName())); //$NON-NLS-1$

            if (stringHasValue(introspectedColumn.getTypeHandler())) {
                resultElement.addAttribute(
                        new Attribute("typeHandler", introspectedColumn.getTypeHandler())); //$NON-NLS-1$
            }

            answer.add(resultElement);
        }

        return answer;
    }

    protected XmlElement buildConstructorElement(boolean includeBlobColumns) {
        XmlElement constructor = new XmlElement("constructor"); //$NON-NLS-1$

        for (IntrospectedColumn introspectedColumn : introspectedTable.getPrimaryKeyColumns()) {
            XmlElement resultElement = new XmlElement("idArg"); //$NON-NLS-1$

            resultElement.addAttribute(buildColumnAttribute(introspectedColumn));
            resultElement.addAttribute(new Attribute("jdbcType", //$NON-NLS-1$
                    introspectedColumn.getJdbcTypeName()));
            resultElement.addAttribute(new Attribute("javaType", //$NON-NLS-1$
                    introspectedColumn.getFullyQualifiedJavaType().getFullyQualifiedName()));

            if (stringHasValue(introspectedColumn.getTypeHandler())) {
                resultElement.addAttribute(
                        new Attribute("typeHandler", introspectedColumn.getTypeHandler())); //$NON-NLS-1$
            }

            constructor.addElement(resultElement);
        }

        List<IntrospectedColumn> columns;
        if (includeBlobColumns) {
            columns = introspectedTable.getNonPrimaryKeyColumns();
        } else {
            columns = introspectedTable.getBaseColumns();
        }
        for (IntrospectedColumn introspectedColumn : columns) {
            XmlElement resultElement = new XmlElement("arg"); //$NON-NLS-1$

            resultElement.addAttribute(buildColumnAttribute(introspectedColumn));
            resultElement.addAttribute(new Attribute("jdbcType", //$NON-NLS-1$
                    introspectedColumn.getJdbcTypeName()));

            if (introspectedColumn.getFullyQualifiedJavaType().isPrimitive()) {
                // need to use the MyBatis type alias for a primitive byte
                String s = '_'
                        + introspectedColumn.getFullyQualifiedJavaType().getShortName();
                resultElement.addAttribute(new Attribute("javaType", s)); //$NON-NLS-1$
            } else if ("byte[]".equals(introspectedColumn.getFullyQualifiedJavaType() //$NON-NLS-1$
                    .getFullyQualifiedName())) {
                // need to use the MyBatis type alias for a primitive byte arry
                resultElement.addAttribute(new Attribute("javaType", //$NON-NLS-1$
                        "_byte[]")); //$NON-NLS-1$
            } else {
                resultElement.addAttribute(new Attribute("javaType", //$NON-NLS-1$
                        introspectedColumn.getFullyQualifiedJavaType().getFullyQualifiedName()));
            }

            if (stringHasValue(introspectedColumn.getTypeHandler())) {
                resultElement.addAttribute(new Attribute(
                        "typeHandler", introspectedColumn.getTypeHandler())); //$NON-NLS-1$
            }

            constructor.addElement(resultElement);
        }

        return constructor;
    }

    protected Attribute buildColumnAttribute(IntrospectedColumn introspectedColumn) {
        return new Attribute("column", //$NON-NLS-1$
                MyBatis3FormattingUtilities.getRenamedColumnNameForResultMap(introspectedColumn));
    }

    protected XmlElement buildUpdateByExampleElement(String statementId, List<IntrospectedColumn> columns) {
        XmlElement answer = new XmlElement("update"); //$NON-NLS-1$

        answer.addAttribute(new Attribute("id", statementId)); //$NON-NLS-1$

        answer.addAttribute(new Attribute("parameterType", "map")); //$NON-NLS-1$ //$NON-NLS-2$
        context.getCommentGenerator().addComment(answer);

        StringBuilder sb = new StringBuilder();
        sb.append("update "); //$NON-NLS-1$
        sb.append(introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime());
        answer.addElement(new TextElement(sb.toString()));

        // set up for first column
        sb.setLength(0);
        sb.append("set "); //$NON-NLS-1$

        Iterator<IntrospectedColumn> iter = ListUtilities.removeGeneratedAlwaysColumns(columns).iterator();
        while (iter.hasNext()) {
            IntrospectedColumn introspectedColumn = iter.next();

            sb.append(MyBatis3FormattingUtilities.getAliasedEscapedColumnName(introspectedColumn));
            sb.append(" = "); //$NON-NLS-1$
            sb.append(MyBatis3FormattingUtilities.getParameterClause(introspectedColumn, "row.")); //$NON-NLS-1$

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

        answer.addElement(getUpdateByExampleIncludeElement());
        return answer;
    }

    protected XmlElement buildUpdateByPrimaryKeyElement(String statementId, String parameterType,
                                                        List<IntrospectedColumn> columns) {
        XmlElement answer = new XmlElement("update"); //$NON-NLS-1$

        answer.addAttribute(new Attribute("id", statementId)); //$NON-NLS-1$
        answer.addAttribute(new Attribute("parameterType", parameterType)); //$NON-NLS-1$

        context.getCommentGenerator().addComment(answer);

        StringBuilder sb = new StringBuilder();
        sb.append("update "); //$NON-NLS-1$
        sb.append(introspectedTable.getFullyQualifiedTableNameAtRuntime());
        answer.addElement(new TextElement(sb.toString()));

        // set up for first column
        sb.setLength(0);
        sb.append("set "); //$NON-NLS-1$

        Iterator<IntrospectedColumn> iter = ListUtilities.removeGeneratedAlwaysColumns(columns).iterator();
        while (iter.hasNext()) {
            IntrospectedColumn introspectedColumn = iter.next();

            sb.append(MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn));
            sb.append(" = "); //$NON-NLS-1$
            sb.append(MyBatis3FormattingUtilities.getParameterClause(introspectedColumn));

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

        buildPrimaryKeyWhereClause().forEach(answer::addElement);

        return answer;
    }

    protected void addUpdateAliasedFullyQualifiedTableName(IntrospectedTable introspectedTable, StringBuilder sb) {
        if (StringUtility.stringHasValue(introspectedTable.getFullyQualifiedTable().getAlias())) {
            sb.append(introspectedTable.getFullyQualifiedTable().getAlias());
        } else {
            sb.append(introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime());
        }
    }

    protected void addUpdateAliasedFullyQualifiedTableNameFrom(IntrospectedTable introspectedTable, XmlElement parent) {
        if (StringUtility.stringHasValue(introspectedTable.getFullyQualifiedTable().getAlias())) {
            String from = "from " + introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime();
            parent.addElement(new TextElement(from));
        }
    }

    protected XmlElement getBaseBySqlElement() {
        XmlElement answer = new XmlElement("include"); //$NON-NLS-1$
        answer.addAttribute(new Attribute("refid", "Base_By_Sql"));
        return answer;
    }

    protected TextElement buildOrderByDefault() {
        StringBuilder sb = new StringBuilder();
        introspectedTable.getColumn("sort_").ifPresent(introspectedColumn -> {
            if (sb.length() > 0) sb.append(", ");
            sb.append("sort_");
        });
        introspectedTable.getColumn("created_").ifPresent(introspectedColumn -> {
            if (sb.length() > 0) sb.append(", ");
            sb.append("created_ ");
        });
        if (sb.length() > 0) {
            return new TextElement("ORDER BY " + sb);
        } else {
            return null;
        }
    }

    protected void addResultMap(XmlElement answer) {
        if (introspectedTable.getRules().generateResultMapWithBLOBs()) {
            answer.addAttribute(new Attribute("resultMap", introspectedTable.getResultMapWithBLOBsId()));
        } else {
            if (introspectedTable.getRules().generateRelationWithSubSelected()) {
                answer.addAttribute(new Attribute("resultMap", introspectedTable.getRelationResultMapId()));
            } else {
                answer.addAttribute(new Attribute("resultMap", introspectedTable.getBaseResultMapId()));
            }
        }
    }

    protected XmlElement createBracketTrim() {
        XmlElement trim = new XmlElement("trim");
        trim.addAttribute(new Attribute("prefix", "("));
        trim.addAttribute(new Attribute("suffix", ")"));
        trim.addAttribute(new Attribute("prefixOverrides", "and | or"));
        return trim;
    }

    /**
     * 获取多值foreach的when条件内容
     *
     * @param introspectedColumn 字段
     * @param listProperty       参数key
     */
    protected XmlElement getWhenMultiKeyInElement(IntrospectedColumn introspectedColumn, String listProperty) {
        XmlElement when = new XmlElement("when");
        when.addAttribute(new Attribute("test", listProperty + " != null and " + listProperty + ".size() > 0"));
        when.addElement(new TextElement("and " + MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn) + " in"));
        XmlElement foreach = getForeachXmlElement(listProperty);
        when.addElement(foreach);
        return when;
    }

    protected static XmlElement getForeachXmlElement(String listProperty) {
        XmlElement foreach = new XmlElement("foreach");
        foreach.addAttribute(new Attribute("collection", listProperty));
        foreach.addAttribute(new Attribute("item", "item"));
        foreach.addAttribute(new Attribute("open", "("));
        foreach.addAttribute(new Attribute("separator", ","));
        foreach.addAttribute(new Attribute("close", ")"));
        foreach.addElement(new TextElement("#{item}"));
        return foreach;
    }

    /**
     * 获取参数值
     *
     * @param introspectedColumn 字段
     * @param vKey               参数key
     * @return String 参数值 假设vKey为key，类型为String，则返回类似：#{key,jdbcType=VARCHAR,typeHandler=com.vgosoft.mybatis.typeHandler.StringTypeHandler}
     */
    protected String getParameterClause(IntrospectedColumn introspectedColumn, String vKey) {
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
