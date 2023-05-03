package org.mybatis.generator.custom;

public enum HtmlElementDataSourceEnum {

    DICT_DATA(1, "DictData"),
    DICT(2, "Dict"),
    DICT_SYS(3, "DictSys"),
    DICT_USER(4, "DictUser"),
    DEPARTMENT(5, "Department"),
    USER(6, "User");

    private final int value;
    private final String code;

    HtmlElementDataSourceEnum(int value, String code) {
        this.value = value;
        this.code = code;
    }

    public int getValue() {
        return value;
    }

    public static HtmlElementDataSourceEnum getEnum(int value) {
        for (HtmlElementDataSourceEnum htmlElementTagTypeEnum : HtmlElementDataSourceEnum.values()) {
            if (htmlElementTagTypeEnum.getValue() == value) {
                return htmlElementTagTypeEnum;
            }
        }
        return null;
    }

    public static HtmlElementDataSourceEnum getEnum(String code) {
        for (HtmlElementDataSourceEnum htmlElementTagTypeEnum : HtmlElementDataSourceEnum.values()) {
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
