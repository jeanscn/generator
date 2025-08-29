package org.mybatis.generator.codegen.mybatis3.xmlmapper.elements;

import com.vgosoft.core.constant.SymbolConstants;
import com.vgosoft.core.constant.enums.data.StatTimeDimEnum;
import com.vgosoft.core.constant.enums.data.StatTypeEnum;
import com.vgosoft.core.constant.enums.db.DefaultColumnNameEnum;
import com.vgosoft.core.constant.enums.db.JoinTableTypeEnum;
import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.config.JavaServiceImplGeneratorConfiguration;
import org.mybatis.generator.config.JoinTableClauseGeneratorConfiguration;
import org.mybatis.generator.config.StatisticsCustomGeneratorConfiguration;
import org.mybatis.generator.config.StatisticsDateDimGeneratorConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author cen_c
 */
public class StatisticsCustomTotalGenerator extends AbstractXmlElementGenerator {

    public StatisticsCustomTotalGenerator() {
        super();
    }

    @Override
    public void addElements(XmlElement parentElement) {
        if (!introspectedTable.getRules().isGenerateCustomStatisticsElement()) {
            return;
        }
        JavaServiceImplGeneratorConfiguration serviceImplGeneratorConfiguration = introspectedTable.getTableConfiguration().getJavaServiceImplGeneratorConfiguration();
        String alias = introspectedTable.getTableConfiguration().getAlias();
        String aliasDot = alias + SymbolConstants.DOT;
        for (StatisticsCustomGeneratorConfiguration statisticsConfiguration : serviceImplGeneratorConfiguration.getStatisticsCustomConfigurations()) {

            List<JoinTableClauseGeneratorConfiguration> joinTableClauseGeneratorConfigurations = statisticsConfiguration.getJoinTableClauseGeneratorConfigurations();

            XmlElement selectElement = new XmlElement("select");
            selectElement.addAttribute(new Attribute("id", statisticsConfiguration.getMethodName()));
            selectElement.addAttribute(new Attribute("resultType", "java.util.Map"));
            context.getCommentGenerator().addComment(selectElement);

            selectElement.addElement(new TextElement("SELECT"));
            // 添加时间维度字段
            if (isContainDateDimension(statisticsConfiguration)) {
                addDateDimSelectFields(selectElement);
            }
            // 添加所有关联表的字段
            List<String> aliasedJoinColumn = new ArrayList<>();
            for (JoinTableClauseGeneratorConfiguration joinConfiguration : joinTableClauseGeneratorConfigurations) {
                if (!joinConfiguration.getClauseColumns().isEmpty()) {
                    String joinAlias = VStringUtil.stringHasValue(joinConfiguration.getTableAlias()) ? joinConfiguration.getTableAlias() : joinConfiguration.getJoinTable();
                    String joinAliasDot = joinAlias + SymbolConstants.DOT;
                    for (String column : joinConfiguration.getClauseColumns()) {
                        selectElement.addElement(new TextElement(VStringUtil.format("{0}{1} AS {2},", joinAliasDot, column, VStringUtil.toCamelCase(joinAlias + column))));
                        aliasedJoinColumn.add(joinAliasDot + column);
                    }
                }
            }
            // 添加自定义的聚合表达式
            for (String expr : statisticsConfiguration.getCustomAggregationExpr()) {
                if (VStringUtil.stringHasValue(expr)) {
                    selectElement.addElement(new TextElement(VStringUtil.replaceXml(expr)));
                }
            }
            // 添加统计列
            for (String statColumn : statisticsConfiguration.getStatColumns()) {
                if (VStringUtil.stringHasValue(statColumn)) {
                    selectElement.addElement(new TextElement(VStringUtil.format("COALESCE ( SUM( {0}{1} ), 0 ) AS {2},", aliasDot, statColumn, VStringUtil.toCamelCase(alias + statColumn))));
                }
            }
            // 统一添加一个统计的条目数，同时去除最后一个逗号
            selectElement.addElement(new TextElement(VStringUtil.format("COALESCE ( COUNT( {0} ), 0 ) AS itemCount", aliasDot + introspectedTable.getPrimaryKeyColumns().get(0).getActualColumnName())));

            selectElement.addElement(new TextElement(VStringUtil.format("FROM {0}", introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime())));
            // 添加关联表的join语句
            String dateDimColumn = VStringUtil.stringHasValue(statisticsConfiguration.getDateDimColumn()) ? statisticsConfiguration.getDateDimColumn() : DefaultColumnNameEnum.CREATE_TIME.columnName();
            for (int i = 0; i < joinTableClauseGeneratorConfigurations.size(); i++) {
                JoinTableClauseGeneratorConfiguration joinConfiguration = joinTableClauseGeneratorConfigurations.get(i);
                String joinAlias = VStringUtil.stringHasValue(joinConfiguration.getTableAlias()) ? joinConfiguration.getTableAlias() : joinConfiguration.getJoinTable();
                JoinTableTypeEnum joinType = JoinTableTypeEnum.ofCode(joinConfiguration.getJoinType());
                selectElement.addElement(new TextElement(VStringUtil.format("{0} {1} {2} ON {3}", joinType.keyWord(), joinConfiguration.getJoinTable(), joinAlias, VStringUtil.replaceXml(joinConfiguration.getOnSearchConditionExpr()))));
                if (isContainDateDimension(statisticsConfiguration)) {
                    selectElement.addElement(new TextElement(VStringUtil.format("AND DATE({0}) = dt.date_value", aliasDot + dateDimColumn)));
                }
                if (i == 0) {
                    addDateFilterCondition(selectElement, aliasDot + dateDimColumn);
                }
            }
            // 添加时间维度的join语句
            if (isContainDateDimension(statisticsConfiguration)){
                selectElement.addElement(new TextElement(VStringUtil.format("LEFT JOIN sys_dim_date dt ON DATE({0}) = dt.date_value", aliasDot + dateDimColumn)));
            }
            // 添加where条件
            if (VStringUtil.stringHasValue(statisticsConfiguration.getDefaultFilterExpr())) {
                XmlElement whereClause = new XmlElement("where");
                whereClause.addElement(new TextElement(VStringUtil.format("AND {0}", VStringUtil.replaceXml(statisticsConfiguration.getDefaultFilterExpr()))));
                selectElement.addElement(whereClause);
            }

            // 添加group by语句
            selectElement.addElement(new TextElement("GROUP BY"));
            if (isContainDateDimension(statisticsConfiguration)) {
                addDateDimGroupByFields(selectElement);
                if (!aliasedJoinColumn.isEmpty()) {
                    selectElement.addElement(new TextElement(",")) ;
                    selectElement.addElement(new TextElement(String.join(",", aliasedJoinColumn)));
                }
            } else {
                if (!aliasedJoinColumn.isEmpty()) {
                    selectElement.addElement(new TextElement(String.join(",", aliasedJoinColumn)));
                } else {
                    selectElement.addElement(new TextElement(aliasDot + introspectedTable.getPrimaryKeyColumns().get(0).getActualColumnName()));
                }
            }
            // 添加ORDER BY语句
            if (!statisticsConfiguration.getOrderByClause().isEmpty()) {
                selectElement.addElement(new TextElement("ORDER BY " + String.join(",", statisticsConfiguration.getOrderByClause())));
            }
            parentElement.addElement(selectElement);
        }
    }

    private void addDateFilterCondition(XmlElement selectElement, String aliasDotColumnName) {
        // <if> for startDate
        XmlElement ifStartDate = new XmlElement("if");
        ifStartDate.addAttribute(new Attribute("test", "startDate != null and startDate != ''"));
        ifStartDate.addElement(new TextElement(VStringUtil.format("AND {0} &gt;= #'{'startDate'}'", aliasDotColumnName)));
        selectElement.addElement(ifStartDate);

        // <if> for endDate
        XmlElement ifEndDate = new XmlElement("if");
        ifEndDate.addAttribute(new Attribute("test", "endDate != null and endDate != ''"));
        ifEndDate.addElement(new TextElement(VStringUtil.format("AND {0} &lt;= #'{'endDate'}'", aliasDotColumnName)));
        selectElement.addElement(ifEndDate);
    }

    private boolean isContainDateDimension(StatisticsCustomGeneratorConfiguration statisticsConfiguration) {
        return VStringUtil.stringHasValue(statisticsConfiguration.getDateDimension()) && !SymbolConstants.NONE.equalsIgnoreCase(statisticsConfiguration.getDateDimension());
    }

    private void addDateDimSelectFields(XmlElement selectElement) {
        // <choose> for select date fields
        XmlElement chooseSelect = new XmlElement("choose");
        XmlElement whenYear = new XmlElement("when");
        String dimYear = VStringUtil.format("minDimension == {0}", "'" + StatTimeDimEnum.YEAR.code() + "'");
        whenYear.addAttribute(new Attribute("test", dimYear));
        whenYear.addElement(new TextElement("dt.year_ AS year"));
        chooseSelect.addElement(whenYear);
        XmlElement whenQuarter = new XmlElement("when");
        String dimQuarter = VStringUtil.format("minDimension == {0}", "'" + StatTimeDimEnum.QUARTER.code() + "'");
        whenQuarter.addAttribute(new Attribute("test", dimQuarter));
        whenQuarter.addElement(new TextElement("dt.year_ AS year, dt.quarter_ AS quarter"));
        chooseSelect.addElement(whenQuarter);
        XmlElement whenMonth = new XmlElement("when");
        String dimMonth = VStringUtil.format("minDimension == {0}", "'" + StatTimeDimEnum.MONTH.code() + "'");
        whenMonth.addAttribute(new Attribute("test", dimMonth));
        whenMonth.addElement(new TextElement("dt.year_ AS year, dt.month_ AS month"));
        chooseSelect.addElement(whenMonth);
        XmlElement whenWeek = new XmlElement("when");
        String dimWeek = VStringUtil.format("minDimension == {0}", "'" + StatTimeDimEnum.WEEK.code() + "'");
        whenWeek.addAttribute(new Attribute("test", dimWeek));
        whenWeek.addElement(new TextElement("dt.year_ AS year, dt.week_of_year AS weekOfYear"));
        chooseSelect.addElement(whenWeek);
        XmlElement otherwise = new XmlElement("otherwise");
        otherwise.addElement(new TextElement("dt.year_ AS year, dt.month_ AS month, dt.date_value AS dateValue"));
        chooseSelect.addElement(otherwise);
        selectElement.addElement(chooseSelect);
        selectElement.addElement(new TextElement(","));
    }

    private void addDateDimGroupByFields(XmlElement selectElement) {
        // <choose> for group by date fields
        XmlElement chooseGroupBy = new XmlElement("choose");
        XmlElement whenYear = new XmlElement("when");
        String dimYear = VStringUtil.format("minDimension == {0}", "'" + StatTimeDimEnum.YEAR.code() + "'");
        whenYear.addAttribute(new Attribute("test", dimYear));
        whenYear.addElement(new TextElement("dt.year_"));
        chooseGroupBy.addElement(whenYear);
        XmlElement whenQuarter = new XmlElement("when");
        String dimQuarter = VStringUtil.format("minDimension == {0}", "'" + StatTimeDimEnum.QUARTER.code() + "'");
        whenQuarter.addAttribute(new Attribute("test", dimQuarter));
        whenQuarter.addElement(new TextElement("dt.year_, dt.quarter_"));
        chooseGroupBy.addElement(whenQuarter);
        XmlElement whenMonth = new XmlElement("when");
        String dimMonth = VStringUtil.format("minDimension == {0}", "'" + StatTimeDimEnum.MONTH.code() + "'");
        whenMonth.addAttribute(new Attribute("test", dimMonth));
        whenMonth.addElement(new TextElement("dt.year_, dt.month_"));
        chooseGroupBy.addElement(whenMonth);
        XmlElement whenWeek = new XmlElement("when");
        String dimWeek = VStringUtil.format("minDimension == {0}", "'" + StatTimeDimEnum.WEEK.code() + "'");
        whenWeek.addAttribute(new Attribute("test", dimWeek));
        whenWeek.addElement(new TextElement("dt.year_, dt.week_of_year"));
        chooseGroupBy.addElement(whenWeek);
        XmlElement otherwise = new XmlElement("otherwise");
        otherwise.addElement(new TextElement("dt.year_, dt.month_, dt.date_value"));
        chooseGroupBy.addElement(otherwise);
        selectElement.addElement(chooseGroupBy);
    }
}
