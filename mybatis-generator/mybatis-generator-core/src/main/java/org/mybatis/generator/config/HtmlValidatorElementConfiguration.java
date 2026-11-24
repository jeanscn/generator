package org.mybatis.generator.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class HtmlValidatorElementConfiguration   extends TypedPropertyHolder{
    private String type;

    private Boolean required;

    private String message;

    private String trigger;

    private String min;

    private String max;

    private Integer len;

    private String pattern;

    private Boolean whitespace;

    private String enumList;

    private String transform;

    private String validator;

    private String column;

    private int scope = 0;

    /**
     * 跨字段对照字段：equalTo/dateNotBefore/dateNotAfter 适用。
     * 可填数据库列名（生成时解析为 Java 属性名）或表单属性名
     */
    private String compareField;

    /**
     * 对照字段提示名，用于错误文案；缺省取对照列注释
     */
    private String compareLabel;

    /**
     * 直接给定对照值（未配置 compareField 时生效）
     */
    private String compareValue;

    /**
     * 日期比较是否允许相等，默认 true；false 时要求严格大于/小于
     */
    private Boolean allowEqual;

    /**
     * 小数位数，money 验证器适用，默认 2
     */
    private String precision;

    /**
     * 是否允许负数，money 验证器适用，默认 false
     */
    private Boolean allowNegative;

    /**
     * 是否强制包含特殊字符，password 验证器适用，默认 false
     */
    private Boolean requireSpecial;
}
