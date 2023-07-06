package org.mybatis.generator.custom;

public enum HtmlElementDataSourceEnum {

    DICT_DATA(1, "DictData",""),
    DICT(2, "Dict",""),
    DICT_SYS(3, "DictSys",""),
    DICT_USER(4, "DictUser",""),
    DEPARTMENT(5, "Department","orgDepartmentImpl"),
    USER(6, "User","orgUserImpl"),

    ROLE(7, "Role","orgRoleImpl"),

    ORGANIZATION(8, "Organ","orgOrganizationImpl"),

    DICT_ENUM(7,"DictEnum","");

    private final int value;
    private final String code;

    private final String beanName;

    HtmlElementDataSourceEnum(int value, String code,String beanName) {
        this.value = value;
        this.code = code;
        this.beanName = beanName;
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

    public String getBeanName() {
        return beanName;
    }

}
