package org.mybatis.generator.custom.enums;

import lombok.Getter;

/**
 * @author cen_c
 */

@Getter
public enum UiFrameTypeEnum {
    /**
     * 前端技术框架类型
     * * elPlus：Element Plus
     * layui：layui
     * zui：ZUI
     * antd：Ant Design
     */
    EL_PLUS("elPlus", "elPlus"),
    LAYUI("layui", "layui"),
    ZUI("zui", "zui"),

    ANTD("antd", "antd");

    private final String code;
    private final String desc;

    UiFrameTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
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
