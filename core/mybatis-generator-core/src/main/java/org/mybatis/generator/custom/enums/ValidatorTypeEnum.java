package org.mybatis.generator.custom.enums;

import com.vgosoft.core.constant.enums.IBaseEnum;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum ValidatorTypeEnum implements IBaseEnum<String> {
    STRING("string", "字符串","{0}类型错误，存在无效文本"),
    NUMBER("number", "数字","{0}类型错误，应该是数字类型"),
    BOOLEAN("boolean", "布尔值","{0}类型错误，应该是布尔类型"),
    METHOD("method", "方法","{0}类型错误，应该是一个方法"),
    REGEXP("regexp", "表达式","{0}表达式错误，请输入正确的表达式格式"),
    INTEGER("integer", "整数","{0}类型错误，应该是整数"),
    FLOAT_TYPE("float", "小数","{0}类型错误，请输入有效的小数"),
    ARRAY("array", "数组","{0}类型错误，应该是一组数据"),
    OBJECT("object", "对象","{0}类型错误，应该是一个对象"),
    ENUM("enum", "枚举","{0}类型错误，应该是枚举类型"),
    DATE("date", "日期","{0}格式错误，应该是日期格式"),
    URL("url", "URL","{0}URL格式错误"),
    HEX_TYPE("hex", "十六进制","{0}类型错误，应该是十六进制数据"),
    EMAIL("email", "电子邮件","{0}邮件格式错误，请输入正确的邮件地址");

    private final String code;
    private final String codeName;
    private final String message;

    @Override
    public String code() {
        return code;
    }

    @Override
    public String codeName() {
        return codeName;
    }

    public String message() {
        return message;
    }

    public static ValidatorTypeEnum ofCode(String code) {
        for (ValidatorTypeEnum value : ValidatorTypeEnum.values()) {
            if (value.code().equals(code)) {
                return value;
            }
        }
        return STRING; // 默认返回STRING类型
    }
}
