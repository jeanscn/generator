package org.mybatis.generator.config;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-09-20 12:30
 * @version 4.0
 */
@Getter
@Setter
public class QueryColumnConfiguration extends AbstractFilterConditionConfiguration {

    /**
     * 查询项类型：field 为普通单字段精确查询（默认），condition 为自定义组合条件。
     */
    private String queryType = "field";

    /**
     * 组合条件唯一标识；queryType=condition 时必填，视图内唯一且不得与真实列名冲突。
     */
    private String key;

    /**
     * 组合条件的参数声明列表。
     */
    private List<ConditionParamConfiguration> conditionParamConfigurations = new ArrayList<>();

    /**
     * 组合条件的原子条件列表，至少一项。
     */
    private List<ConditionTermConfiguration> conditionTermConfigurations = new ArrayList<>();

    /**
     * 选项型组合条件的选项列表。
     */
    private List<ConditionOptionConfiguration> conditionOptionConfigurations = new ArrayList<>();

    public QueryColumnConfiguration(TableConfiguration tc) {
        super(tc);
    }

    /**
     * 当前项是否为自定义组合条件。
     */
    public boolean isCondition() {
        return "condition".equals(queryType);
    }
}
