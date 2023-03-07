package org.mybatis.generator.custom;

public enum DictTypeEnum{
    /**
     * 1:用户字典
     */
    DICT(1,"Dict"),
    /**
     * 2:用户字典
     */
    DICT_USER(2,"DictUser"),
    /**
     * 3:系统字典
     */
    DICT_SYS(3,"DictSys");

    private final int value;
    private final String code;

    DictTypeEnum(int value, String code) {
        this.value = value;
        this.code = code;
    }

    public int getValue() {
        return value;
    }

    public static DictTypeEnum getEnum(int value) {
        for (DictTypeEnum dictTypeEnum : DictTypeEnum.values()) {
            if (dictTypeEnum.getValue() == value) {
                return dictTypeEnum;
            }
        }
        return null;
    }

    public static DictTypeEnum getEnum(String code) {
        for (DictTypeEnum dictTypeEnum : DictTypeEnum.values()) {
            if (dictTypeEnum.getCode().equals(code)) {
                return dictTypeEnum;
            }
        }
        return null;
    }
    public String getCode() {
        return code;
    }
}
