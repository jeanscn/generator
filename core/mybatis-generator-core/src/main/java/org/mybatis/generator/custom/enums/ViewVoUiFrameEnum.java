package org.mybatis.generator.custom.enums;

import lombok.Getter;

@Getter
public enum ViewVoUiFrameEnum {

    DATATABLES("dataTables", "dataTables"),
    LAY_TABLE("layTable", "layTable"),
    EL_PLUS_TABLE("elPlusTable", "elPlusTable"),
    VXE_TABLE("vxeTable", "vxeTable"),
    ANT_D("antd", "antd");

    private final String code;

    private final String desc;

    ViewVoUiFrameEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static ViewVoUiFrameEnum getEnum(String code) {
        for (ViewVoUiFrameEnum viewVoUiFrameEnum : ViewVoUiFrameEnum.values()) {
            if (viewVoUiFrameEnum.getCode().equals(code)) {
                return viewVoUiFrameEnum;
            }
        }
        return ViewVoUiFrameEnum.EL_PLUS_TABLE;
    }
}
