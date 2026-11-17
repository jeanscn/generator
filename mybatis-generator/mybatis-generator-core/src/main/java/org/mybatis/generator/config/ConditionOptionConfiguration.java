package org.mybatis.generator.config;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 查询表单选项型组合条件的选项配置，对应 {@code <conditionOption>} 子节点。
 *
 * @author vgosoft-agent
 */
@Getter
@Setter
public class ConditionOptionConfiguration {

    /**
     * 选项提交值，条件内唯一。
     */
    private String value;

    /**
     * 选项显示名称。
     */
    private String remark;

    /**
     * 是否为清除条件的“全部”选项。
     */
    private boolean clear;

    /**
     * 选项对应的常量原子条件列表。
     */
    private List<ConditionTermConfiguration> conditionTermConfigurations = new ArrayList<>();
}
