package org.mybatis.generator.config;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * @author cen_c
 */
@Data
public class StatisticsCustomGeneratorConfiguration extends AbstractStatisticsGeneratorConfiguration {

    private List<JoinTableClauseGeneratorConfiguration> joinTableClauseGeneratorConfigurations = new ArrayList<>();

    public StatisticsCustomGeneratorConfiguration() {
        super();
    }

    public void addJoinTableClauseGeneratorConfiguration(JoinTableClauseGeneratorConfiguration configuration) {
        this.joinTableClauseGeneratorConfigurations.add(configuration);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) { return false; }
        StatisticsCustomGeneratorConfiguration that = (StatisticsCustomGeneratorConfiguration) o;
        return Objects.equals(statType, that.statType) && Objects.equals(statColumns, that.statColumns) && Objects.equals(customAggregationExpr, that.customAggregationExpr) && Objects.equals(nameSuffix, that.nameSuffix) && Objects.equals(dateDimension, that.dateDimension) && Objects.equals(dateDimColumn, that.dateDimColumn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(statType, statColumns, customAggregationExpr, nameSuffix, dateDimension, dateDimColumn);
    }
}
