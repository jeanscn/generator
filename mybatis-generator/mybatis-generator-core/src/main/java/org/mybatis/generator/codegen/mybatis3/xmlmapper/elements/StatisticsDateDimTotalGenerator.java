package org.mybatis.generator.codegen.mybatis3.xmlmapper.elements;

import com.vgosoft.core.constant.SymbolConstants;
import com.vgosoft.core.constant.enums.data.StatTimeDimEnum;
import com.vgosoft.core.constant.enums.data.StatTypeEnum;
import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.config.JavaServiceImplGeneratorConfiguration;
import org.mybatis.generator.config.StatisticsDateDimGeneratorConfiguration;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author cen_c
 */
public class StatisticsDateDimTotalGenerator extends AbstractXmlElementGenerator {

    public StatisticsDateDimTotalGenerator() {
        super();
    }

    @Override
    public void addElements(XmlElement parentElement) {
        JavaServiceImplGeneratorConfiguration serviceImplGeneratorConfiguration = introspectedTable.getTableConfiguration().getJavaServiceImplGeneratorConfiguration();
        if (serviceImplGeneratorConfiguration == null || !serviceImplGeneratorConfiguration.isGenerate() || serviceImplGeneratorConfiguration.getStatisticsConfigurations().isEmpty()) {
            return;
        }
        String alias = introspectedTable.getTableConfiguration().getAlias();
        String aliasDot = alias + SymbolConstants.DOT;
        for (StatisticsDateDimGeneratorConfiguration statisticsConfiguration : serviceImplGeneratorConfiguration.getStatisticsConfigurations()) {
            if (!statisticsConfiguration.getStatType().equals(StatTypeEnum.TOTAL.code())) {
                continue;
            }
            List<String> aliasedStatColumns = statisticsConfiguration.getStatColumns().stream().map(column->aliasDot + column).collect(Collectors.toList());
            if (aliasedStatColumns.isEmpty()) {
                continue;
            } else {
                aliasedStatColumns = aliasedStatColumns.stream().limit(5).collect(Collectors.toList());
            }
            String aliasedJoinColumn = aliasDot + statisticsConfiguration.getDateDimColumn();

            XmlElement selectElement = new XmlElement("select");
            selectElement.addAttribute(new Attribute("id", statisticsConfiguration.getMethodName()));
            selectElement.addAttribute(new Attribute("resultType", StatTypeEnum.TOTAL.dtoClass()));
            context.getCommentGenerator().addComment(selectElement);
            selectElement.addElement(new TextElement("SELECT"));

            // <choose> for select fields
            XmlElement chooseSelect = new XmlElement("choose");
            XmlElement whenYear = new XmlElement("when");
            String dimYear = VStringUtil.format("minDimension == {0}", "'"+StatTimeDimEnum.YEAR.code()+"'");
            whenYear.addAttribute(new Attribute("test", dimYear));
            whenYear.addElement(new TextElement("dt.year_ AS year"));
            chooseSelect.addElement(whenYear);

            XmlElement whenQuarter = new XmlElement("when");
            String dimQuarter = VStringUtil.format("minDimension == {0}", "'"+StatTimeDimEnum.QUARTER.code()+"'");
            whenQuarter.addAttribute(new Attribute("test", dimQuarter));
            whenQuarter.addElement(new TextElement("dt.year_ AS year, dt.quarter_ AS quarter"));
            chooseSelect.addElement(whenQuarter);

            XmlElement whenMonth = new XmlElement("when");
            String dimMonth = VStringUtil.format("minDimension == {0}", "'" + StatTimeDimEnum.MONTH.code() + "'");
            whenMonth.addAttribute(new Attribute("test", dimMonth));
            whenMonth.addElement(new TextElement("dt.year_ AS year, dt.month_ AS month"));
            chooseSelect.addElement(whenMonth);

            XmlElement whenWeek = new XmlElement("when");
            String dimWeek = VStringUtil.format("minDimension == {0}", "'"+StatTimeDimEnum.WEEK.code()+"'");
            whenWeek.addAttribute(new Attribute("test", dimWeek));
            whenWeek.addElement(new TextElement("dt.year_ AS year, dt.week_of_year AS weekOfYear"));
            chooseSelect.addElement(whenWeek);

            XmlElement otherwise = new XmlElement("otherwise");
            otherwise.addElement(new TextElement("dt.year_ AS year, dt.month_ AS month, dt.date_value AS dateValue"));
            chooseSelect.addElement(otherwise);

            selectElement.addElement(chooseSelect);
            selectElement.addElement(new TextElement(","));
            for (int i = 0; i < aliasedStatColumns.size(); i++) {
                selectElement.addElement(new TextElement(VStringUtil.format("COALESCE ( SUM( {0} ), 0 ) AS totalAmount{1},",aliasedStatColumns.get(i), i)));
            }
            if (!statisticsConfiguration.getCustomAggregationExpr().isEmpty()) {
                for (int i = 0; i < statisticsConfiguration.getCustomAggregationExpr().size(); i++) {
                    String expr = statisticsConfiguration.getCustomAggregationExpr().get(i);
                    if (VStringUtil.stringHasValue(expr)) {
                        selectElement.addElement(new TextElement(VStringUtil.format("{0} AS customAmount{1},", VStringUtil.replaceXml(expr), i)));
                    }
                }
            }
            selectElement.addElement(new TextElement(VStringUtil.format("COALESCE ( COUNT( {0} ), 0 ) AS itemCount",aliasDot+ introspectedTable.getPrimaryKeyColumns().get(0).getActualColumnName())));
            selectElement.addElement(new TextElement(VStringUtil.format("FROM {0}", introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime())));
            selectElement.addElement(new TextElement(VStringUtil.format("JOIN sys_dim_date dt ON DATE({0}) = dt.date_value",aliasedJoinColumn)));
            selectElement.addElement(new TextElement(VStringUtil.format("WHERE dt.date_value = DATE({0})",aliasedJoinColumn)));
            if (VStringUtil.stringHasValue(statisticsConfiguration.getDefaultFilterExpr())) {
                selectElement.addElement(new TextElement(VStringUtil.format("AND {0}",VStringUtil.replaceXml(statisticsConfiguration.getDefaultFilterExpr()))));
            }
            // <if> for startDate
            XmlElement ifStartDate = new XmlElement("if");
            ifStartDate.addAttribute(new Attribute("test", "startDate != null and startDate != ''"));
            ifStartDate.addElement(new TextElement(VStringUtil.format("AND {0} &gt;= #'{'startDate'}'",aliasedJoinColumn)));
            selectElement.addElement(ifStartDate);

            // <if> for endDate
            XmlElement ifEndDate = new XmlElement("if");
            ifEndDate.addAttribute(new Attribute("test", "endDate != null and endDate != ''"));
            ifEndDate.addElement(new TextElement(VStringUtil.format("AND {0} &lt;= #'{'endDate'}'",aliasedJoinColumn)));
            selectElement.addElement(ifEndDate);

            // <choose> for group by
            XmlElement chooseGroup = new XmlElement("choose");
            XmlElement whenGroupYear = new XmlElement("when");
            whenGroupYear.addAttribute(new Attribute("test", dimYear));
            whenGroupYear.addElement(new TextElement("GROUP BY dt.year_"));
            chooseGroup.addElement(whenGroupYear);

            XmlElement whenGroupQuarter = new XmlElement("when");
            whenGroupQuarter.addAttribute(new Attribute("test", dimQuarter));
            whenGroupQuarter.addElement(new TextElement("GROUP BY dt.year_, dt.quarter_"));
            chooseGroup.addElement(whenGroupQuarter);

            XmlElement whenGroupMonth = new XmlElement("when");
            whenGroupMonth.addAttribute(new Attribute("test", dimMonth));
            whenGroupMonth.addElement(new TextElement("GROUP BY dt.year_, dt.month_"));
            chooseGroup.addElement(whenGroupMonth);

            XmlElement whenGroupWeek = new XmlElement("when");
            whenGroupWeek.addAttribute(new Attribute("test", dimWeek));
            whenGroupWeek.addElement(new TextElement("GROUP BY dt.year_, dt.week_of_year"));
            chooseGroup.addElement(whenGroupWeek);

            XmlElement otherwiseGroup = new XmlElement("otherwise");
            otherwiseGroup.addElement(new TextElement("GROUP BY dt.year_, dt.month_, dt.date_value"));
            chooseGroup.addElement(otherwiseGroup);
            selectElement.addElement(chooseGroup);

            if (!statisticsConfiguration.getOrderByClause().isEmpty()) {
                selectElement.addElement(new TextElement("ORDER BY " + String.join(",", statisticsConfiguration.getOrderByClause())));
            }
            parentElement.addElement(selectElement);
        }
    }
}
