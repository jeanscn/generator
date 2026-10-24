package org.mybatis.generator.config;

import com.vgosoft.core.constant.enums.data.StatTimeDimEnum;
import com.vgosoft.core.constant.enums.data.StatTypeEnum;
import com.vgosoft.core.constant.enums.db.DefaultColumnNameEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;


/**
 * @author cen_c
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AbstractStatisticsGeneratorConfiguration extends PropertyHolder {

    protected String statType = StatTypeEnum.TOTAL.code();

    protected List<String> statColumns = new ArrayList<>();

    protected List<String> customAggregationExpr = new ArrayList<>();

    protected String defaultFilterExpr;

    protected String nameSuffix;

    protected List<String> orderByClause = new ArrayList<>();

    protected String methodName;

    protected Boolean genControllerMethod = false;

    protected String dateDimColumn = DefaultColumnNameEnum.CREATE_TIME.columnName();

    protected String dateDimension = StatTimeDimEnum.MONTH.code();

    public AbstractStatisticsGeneratorConfiguration() {
        super();
    }
}
