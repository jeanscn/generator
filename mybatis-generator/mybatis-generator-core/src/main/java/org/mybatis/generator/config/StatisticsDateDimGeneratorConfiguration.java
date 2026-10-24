package org.mybatis.generator.config;

import lombok.Data;

import java.util.Objects;


/**
 *
 * @author cen_c
 */
@Data
public class StatisticsDateDimGeneratorConfiguration extends AbstractStatisticsGeneratorConfiguration {

    public StatisticsDateDimGeneratorConfiguration() {
        super();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) { return false; }
        StatisticsDateDimGeneratorConfiguration that = (StatisticsDateDimGeneratorConfiguration) o;
        return Objects.equals(statType, that.statType) && Objects.equals(statColumns, that.statColumns) && Objects.equals(dateDimension, that.dateDimension) && Objects.equals(dateDimColumn, that.dateDimColumn) && Objects.equals(nameSuffix, that.nameSuffix);
    }

    @Override
    public int hashCode() {
        return Objects.hash(statType, statColumns, dateDimension, dateDimColumn, nameSuffix);
    }
}
