package org.mybatis.generator.custom;

public enum UiFrameTypeEnum {
    EL_PLUS("elPlus", "elPlus"),
    LAYUI("layui", "layui"),
    ZUI("zui", "zui"),

    ANTD("antd", "antd");

    private final String code;
    private final  String desc;

    UiFrameTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public UiFrameTypeEnum getEnu(String code) {
        for (UiFrameTypeEnum value : UiFrameTypeEnum.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return EL_PLUS;
    }

}
