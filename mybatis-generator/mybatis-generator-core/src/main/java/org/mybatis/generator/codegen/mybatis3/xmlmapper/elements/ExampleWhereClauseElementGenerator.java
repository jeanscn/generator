package org.mybatis.generator.codegen.mybatis3.xmlmapper.elements;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;

/**
 * @author cen_c
 */
public class ExampleWhereClauseElementGenerator extends AbstractXmlElementGenerator {

    private final boolean isForUpdateByExample;

    public ExampleWhereClauseElementGenerator(boolean isForUpdateByExample) {
        super();
        this.isForUpdateByExample = isForUpdateByExample;
    }

    @Override
    public void addElements(XmlElement parentElement) {
        XmlElement answer = new XmlElement("sql");

        if (isForUpdateByExample) {
            answer.addAttribute(new Attribute(
                    "id", introspectedTable.getMyBatis3UpdateByExampleWhereClauseId()));
        } else {
            answer.addAttribute(new Attribute(
                    "id", introspectedTable.getExampleWhereClauseId()));
        }

        context.getCommentGenerator().addComment(answer);

        XmlElement whereElement = new XmlElement("where");
        answer.addElement(whereElement);

        XmlElement outerForEachElement = new XmlElement("foreach");
        if (isForUpdateByExample) {
            outerForEachElement.addAttribute(new Attribute(
                    "collection", "example.oredCriteria"));
        } else {
            outerForEachElement.addAttribute(new Attribute(
                    "collection", "oredCriteria"));
        }
        outerForEachElement.addAttribute(new Attribute("item", "criteria"));
        outerForEachElement.addAttribute(new Attribute("separator", "or"));
        whereElement.addElement(outerForEachElement);

        XmlElement ifElement = new XmlElement("if");
        ifElement.addAttribute(new Attribute("test", "criteria.valid"));
        outerForEachElement.addElement(ifElement);

        XmlElement trimElement = new XmlElement("trim");
        trimElement.addAttribute(new Attribute("prefix", "("));
        trimElement.addAttribute(new Attribute("suffix", ")"));
        trimElement.addAttribute(new Attribute("prefixOverrides", "and"));

        ifElement.addElement(trimElement);

        boolean optionConditions = introspectedTable.getTableConfiguration().hasOptionQueryConditions();
        if (optionConditions) {
            trimElement.addElement(getOptionAwareCriteriaForEach());
        } else {
            trimElement.addElement(getMiddleForEachElement(null));
        }

        for (IntrospectedColumn introspectedColumn : introspectedTable.getNonBlobColumns()) {
            if (stringHasValue(introspectedColumn.getTypeHandler())) {
                trimElement.addElement(getMiddleForEachElement(introspectedColumn));
            }
        }

        if (context.getPlugins().sqlMapExampleWhereClauseElementGenerated(answer, introspectedTable)) {
            parentElement.addElement(answer);
        }

        if (optionConditions && !isForUpdateByExample) {
            // 原子条件列表的共享渲染片段只登记一次；UpdateByExample 变体与外层组渲染按 refid 复用
            parentElement.addElement(buildCriterionChooseFragment());
        }
    }

    /**
     * 共享 {@code <sql>} 片段标识；顶层与嵌套 OR 组复用同一份原子条件 choose。
     */
    private String criterionChooseFragmentId() {
        return introspectedTable.getExampleWhereClauseId() + "_Criterion_Choose";
    }

    /**
     * 选项型条件的无类型谓词列表：分组谓词渲染为 {@code and ((内层 AND) or (内层 AND))}，
     * 原子谓词经共享片段渲染；嵌套列表只允许原子谓词，不产生二级分组。
     */
    private XmlElement getOptionAwareCriteriaForEach() {
        XmlElement middleForEachElement = new XmlElement("foreach");
        middleForEachElement.addAttribute(new Attribute("collection", "criteria.criteria"));
        middleForEachElement.addAttribute(new Attribute("item", "criterion"));

        XmlElement chooseElement = new XmlElement("choose");
        middleForEachElement.addElement(chooseElement);

        XmlElement groupWhen = new XmlElement("when");
        groupWhen.addAttribute(new Attribute("test", "criterion.groupValue"));
        groupWhen.addElement(new TextElement("and"));
        XmlElement orForEach = new XmlElement("foreach");
        orForEach.addAttribute(new Attribute("collection", "criterion.orGroups"));
        orForEach.addAttribute(new Attribute("item", "orGroup"));
        orForEach.addAttribute(new Attribute("open", "("));
        orForEach.addAttribute(new Attribute("close", ")"));
        orForEach.addAttribute(new Attribute("separator", "or "));
        XmlElement innerTrim = new XmlElement("trim");
        innerTrim.addAttribute(new Attribute("prefix", "("));
        innerTrim.addAttribute(new Attribute("suffix", ")"));
        innerTrim.addAttribute(new Attribute("prefixOverrides", "and"));
        XmlElement innerForEach = new XmlElement("foreach");
        innerForEach.addAttribute(new Attribute("collection", "orGroup"));
        innerForEach.addAttribute(new Attribute("item", "criterion"));
        innerForEach.addElement(buildCriterionChooseInclude());
        innerTrim.addElement(innerForEach);
        orForEach.addElement(innerTrim);
        groupWhen.addElement(orForEach);
        chooseElement.addElement(groupWhen);

        XmlElement otherwise = new XmlElement("otherwise");
        otherwise.addElement(buildCriterionChooseInclude());
        chooseElement.addElement(otherwise);
        return middleForEachElement;
    }

    private XmlElement buildCriterionChooseInclude() {
        XmlElement include = new XmlElement("include");
        include.addAttribute(new Attribute("refid", criterionChooseFragmentId()));
        return include;
    }

    private XmlElement buildCriterionChooseFragment() {
        XmlElement fragment = new XmlElement("sql");
        fragment.addAttribute(new Attribute("id", criterionChooseFragmentId()));
        context.getCommentGenerator().addComment(fragment);
        fragment.addElement(buildScalarCriterionChoose());
        return fragment;
    }

    /**
     * 无类型谓词列表的共享 choose：供共享 {@code <sql>} 片段与嵌套 OR 组引用。
     */
    private XmlElement buildScalarCriterionChoose() {
        return getCriterionChoose(false, null);
    }

    private XmlElement getMiddleForEachElement(IntrospectedColumn introspectedColumn) {
        StringBuilder sb = new StringBuilder();
        String criteriaAttribute;
        boolean typeHandled;
        String typeHandlerString;
        if (introspectedColumn == null) {
            criteriaAttribute = "criteria.criteria";
            typeHandled = false;
            typeHandlerString = null;
        } else {
            sb.setLength(0);
            sb.append("criteria.");
            sb.append(introspectedColumn.getJavaProperty());
            sb.append("Criteria");
            criteriaAttribute = sb.toString();

            typeHandled = true;

            sb.setLength(0);
            sb.append(",typeHandler=");
            sb.append(introspectedColumn.getTypeHandler());
            typeHandlerString = sb.toString();
        }

        XmlElement middleForEachElement = new XmlElement("foreach");
        middleForEachElement.addAttribute(new Attribute(
                "collection", criteriaAttribute));
        middleForEachElement.addAttribute(new Attribute("item", "criterion"));

        middleForEachElement.addElement(getCriterionChoose(typeHandled, typeHandlerString));

        return middleForEachElement;
    }

    private XmlElement getCriterionChoose(boolean typeHandled, String typeHandlerString) {
        StringBuilder sb = new StringBuilder();
        XmlElement chooseElement = new XmlElement("choose");

        XmlElement when = new XmlElement("when");
        when.addAttribute(new Attribute("test", "criterion.noValue"));
        when.addElement(new TextElement("and ${criterion.condition}"));
        chooseElement.addElement(when);

        if (!typeHandled && introspectedTable.getTableConfiguration().hasQueryConditions()) {
            // 组合条件 LIKE 分支：方言由执行语句所属 Configuration 的 _databaseId 选择，
            // 未支持方言经 otherwise 中的守卫在 SQL 构建时显式失败
            chooseElement.addElement(getLikeWhenElement("criterion.literalLikeValue",
                    "and ${criterion.condition} like #{criterion.value} escape '!'",
                    "and ${criterion.condition} like #{criterion.secondValue} escape '!'",
                    "LITERAL_LIKE"));
            chooseElement.addElement(getLikeWhenElement("criterion.tokenLikeValue",
                    null, null, "TOKEN_LIKE"));
        }

        when = new XmlElement("when");
        when.addAttribute(new Attribute("test", "criterion.singleValue"));
        sb.setLength(0);
        sb.append("and ${criterion.condition} #{criterion.value");
        if (typeHandled) {
            sb.append(typeHandlerString);
        }
        sb.append('}');
        when.addElement(new TextElement(sb.toString()));
        chooseElement.addElement(when);

        when = new XmlElement("when");
        when.addAttribute(new Attribute("test", "criterion.betweenValue"));
        sb.setLength(0);
        sb.append("and ${criterion.condition} #{criterion.value");
        if (typeHandled) {
            sb.append(typeHandlerString);
        }
        sb.append("} and #{criterion.secondValue");
        if (typeHandled) {
            sb.append(typeHandlerString);
        }
        sb.append('}');
        when.addElement(new TextElement(sb.toString()));
        chooseElement.addElement(when);

        when = new XmlElement("when");
        when.addAttribute(new Attribute("test", "criterion.listValue"));
        when.addElement(new TextElement("and ${criterion.condition}"));
        XmlElement innerForEach = new XmlElement("foreach");
        innerForEach.addAttribute(new Attribute("collection", "criterion.value"));
        innerForEach.addAttribute(new Attribute("item", "listItem"));
        innerForEach.addAttribute(new Attribute("open", "("));
        innerForEach.addAttribute(new Attribute("close", ")"));
        innerForEach.addAttribute(new Attribute("separator", ","));
        sb.setLength(0);
        sb.append("#{listItem");
        if (typeHandled) {
            sb.append(typeHandlerString);
        }
        sb.append('}');
        innerForEach.addElement(new TextElement(sb.toString()));
        when.addElement(innerForEach);
        chooseElement.addElement(when);

        return chooseElement;
    }

    /**
     * 组合条件 LIKE 类分支：按 _databaseId 选择谓词，固定输出 escape '!'，否则调用方言守卫。
     * literalSql 为空时表示 Token 模式，按方言分组输出定界拼接表达式。
     */
    private XmlElement getLikeWhenElement(String criterionFlag, String literalSql, String literalSqlServerSql, String kind) {
        XmlElement when = new XmlElement("when");
        when.addAttribute(new Attribute("test", criterionFlag));
        XmlElement choose = new XmlElement("choose");
        when.addElement(choose);
        boolean tokenLike = "TOKEN_LIKE".equals(kind);
        if (tokenLike) {
            choose.addElement(dialectWhen("_databaseId == 'mysql' or _databaseId == 'mariadb'",
                    "and concat(',', ${criterion.condition}, ',') like #{criterion.value} escape '!'"));
            choose.addElement(dialectWhen(
                    "_databaseId == 'postgresql' or _databaseId == 'h2' or _databaseId == 'oracle' or _databaseId == 'db2' or _databaseId == 'dm'",
                    "and (',' || ${criterion.condition} || ',') like #{criterion.value} escape '!'"));
        } else {
            choose.addElement(dialectWhen(
                    "_databaseId == 'mysql' or _databaseId == 'mariadb' or _databaseId == 'postgresql' or _databaseId == 'h2' or _databaseId == 'oracle' or _databaseId == 'db2' or _databaseId == 'dm'",
                    literalSql));
        }
        choose.addElement(dialectWhen("_databaseId == 'sqlserver' or _databaseId == 'sqlserver2005'",
                tokenLike ? "and (',' + ${criterion.condition} + ',') like #{criterion.secondValue} escape '!'"
                        : literalSqlServerSql));
        XmlElement otherwise = new XmlElement("otherwise");
        XmlElement bind = new XmlElement("bind");
        bind.addAttribute(new Attribute("name", "unsupportedDialect"));
        bind.addAttribute(new Attribute("value",
                "@com.vgosoft.mybatis.query.QueryDialectGuard@unsupported(_databaseId, '" + kind + "')"));
        otherwise.addElement(bind);
        choose.addElement(otherwise);
        return when;
    }

    private XmlElement dialectWhen(String test, String sql) {
        XmlElement when = new XmlElement("when");
        when.addAttribute(new Attribute("test", test));
        when.addElement(new TextElement(sql));
        return when;
    }
}
