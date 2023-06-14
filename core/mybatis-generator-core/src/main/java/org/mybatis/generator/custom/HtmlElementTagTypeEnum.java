package org.mybatis.generator.custom;

import org.apache.ibatis.annotations.Select;

public enum HtmlElementTagTypeEnum {

    DROPDOWN_LIST(1, "dropdownlist"),
    RADIO(2, "radio"),
    CHECKBOX(3, "checkbox"),
    SWITCH(4, "switch"),
    SELECT(5, "select"),
    INPUT(6,"input");

    private final int value;
    private final String code;

    HtmlElementTagTypeEnum(int value, String code) {
        this.value = value;
        this.code = code;
    }

    public int getValue() {
        return value;
    }

    public static HtmlElementTagTypeEnum getEnum(int value) {
        for (HtmlElementTagTypeEnum htmlElementTagTypeEnum : HtmlElementTagTypeEnum.values()) {
            if (htmlElementTagTypeEnum.getValue() == value) {
                return htmlElementTagTypeEnum;
            }
        }
        return null;
    }

    public static HtmlElementTagTypeEnum getEnum(String code) {
        for (HtmlElementTagTypeEnum htmlElementTagTypeEnum : HtmlElementTagTypeEnum.values()) {
            if (htmlElementTagTypeEnum.getCode().equals(code)) {
                return htmlElementTagTypeEnum;
            }
        }
        return null;
    }
    public String getCode() {
        return code;
    }

}
