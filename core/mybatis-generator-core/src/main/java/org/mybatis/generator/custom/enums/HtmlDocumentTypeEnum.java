package org.mybatis.generator.custom.enums;

import lombok.Getter;

/**
 * html页面应用类型
 *
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-08-11 17:25
 * @version 4.0
 */
@Getter
public enum HtmlDocumentTypeEnum {

    EDITABLE("editable", "可编辑页面"),
    VIEWONLY("viewOnly", "只读页面"),
    PRINT("print", "打印页面");

    private final String code;

    private final String desc;

    HtmlDocumentTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static HtmlDocumentTypeEnum getEnum(String code) {
        for (HtmlDocumentTypeEnum htmlDocumentTypeEnum : HtmlDocumentTypeEnum.values()) {
            if (htmlDocumentTypeEnum.getCode().equals(code)) {
                return htmlDocumentTypeEnum;
            }
        }
        return HtmlDocumentTypeEnum.EDITABLE;
    }

}
