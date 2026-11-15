package org.mybatis.generator.config;

import com.vgosoft.core.constant.enums.core.QueryConditionContainsModeEnum;
import com.vgosoft.core.constant.enums.core.QueryConditionOperatorEnum;
import com.vgosoft.core.constant.enums.core.QueryConditionValueTypeEnum;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * 查询表单组合条件的原子条件，对应 {@code <conditionTerm>} 子节点。
 * <p>
 * {@link #param} 与 {@link #value} 二选一；常量值必须声明 {@link #valueType}。
 *
 * @author vgosoft-agent
 */
@Getter
@Setter
public class ConditionTermConfiguration {

    /**
     * DTD 操作符命名到 {@link QueryConditionOperatorEnum} 的映射。
     */
    private static final Map<String, QueryConditionOperatorEnum> OPERATOR_EXPRESSIONS = Map.of(
            "EqualTo", QueryConditionOperatorEnum.EQ,
            "NotEqualTo", QueryConditionOperatorEnum.NE,
            "GreaterThan", QueryConditionOperatorEnum.GT,
            "GreaterThanOrEqualTo", QueryConditionOperatorEnum.GE,
            "LessThan", QueryConditionOperatorEnum.LT,
            "LessThanOrEqualTo", QueryConditionOperatorEnum.LE,
            "LikeAny", QueryConditionOperatorEnum.LA,
            "In", QueryConditionOperatorEnum.IN,
            "ContainsAll", QueryConditionOperatorEnum.CONTAINS_ALL);

    /**
     * 当前表的物理列名。
     */
    private String column;

    /**
     * 操作符。
     */
    private QueryConditionOperatorEnum operator;

    /**
     * 引用的参数名。
     */
    private String param;

    /**
     * 常量值；仅单值操作符可用。
     */
    private String value;

    /**
     * 常量值类型；引用参数时必须保持 {@link QueryConditionValueTypeEnum#UNSPECIFIED}。
     */
    private QueryConditionValueTypeEnum valueType = QueryConditionValueTypeEnum.UNSPECIFIED;

    /**
     * 全部包含的匹配方式，仅 operator = CONTAINS_ALL 有效。
     */
    private QueryConditionContainsModeEnum containsMode = QueryConditionContainsModeEnum.TOKEN;

    /**
     * 将 DTD 操作符命名（与 %queryExpression; 风格一致）映射为组合条件操作符枚举。
     *
     * @param expression 配置中的 operator 取值
     * @return 操作符枚举
     * @throws IllegalArgumentException 命名不在支持范围内
     */
    public static QueryConditionOperatorEnum operatorOf(String expression) {
        QueryConditionOperatorEnum operator = OPERATOR_EXPRESSIONS.get(expression);
        if (operator == null) {
            throw new IllegalArgumentException("conditionTerm 不支持的 operator: " + expression);
        }
        return operator;
    }

    /**
     * 常量值是否声明了类型。
     */
    public boolean hasValueType() {
        return valueType != null && valueType != QueryConditionValueTypeEnum.UNSPECIFIED;
    }

    /**
     * 当前原子条件是否引用参数（否则为常量值）。
     */
    public boolean hasParam() {
        return param != null && !param.isEmpty();
    }

    /**
     * 当前原子条件是否配置常量值。
     */
    public boolean hasValue() {
        return value != null && !value.isEmpty();
    }
}
