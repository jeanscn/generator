package org.mybatis.generator.config;

import com.vgosoft.core.constant.enums.db.FieldTypeEnum;
import com.vgosoft.core.constant.enums.view.HtmlElementTagTypeEnum;
import lombok.Getter;
import lombok.Setter;

/**
 * 查询表单组合条件的参数声明，对应 {@code <conditionParam>} 子节点。
 *
 * @author vgosoft-agent
 */
@Getter
@Setter
public class ConditionParamConfiguration {

    /**
     * 参数名，条件内唯一，供 conditionTerm 引用。
     */
    private String name;

    /**
     * 参数显示名称。
     */
    private String remark;

    /**
     * 可选：借用该列在表单配置中的表单项（字典、选项等）。
     */
    private String column;

    /**
     * 输入控件类型；未知时由字段类型推断。
     */
    private HtmlElementTagTypeEnum tagName;

    /**
     * 参数值类型，服务端按该类型严格转换。
     */
    private FieldTypeEnum fieldType;

    /**
     * 下拉、单选、复选等控件获取选项的接口地址。
     */
    private String dataUrl;

    /**
     * 是否多值；IN 与 CONTAINS_ALL 引用的参数必须为多值。
     */
    private boolean multiple;

    /**
     * 是否必填；非必填且未填写时跳过引用该参数的 term。
     */
    private boolean required = true;
}
