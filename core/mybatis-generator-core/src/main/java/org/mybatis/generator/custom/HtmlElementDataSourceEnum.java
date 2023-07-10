package org.mybatis.generator.custom;

public enum HtmlElementDataSourceEnum {

    DICT_DATA(1, "DictData","","DictData"),
    DICT(2, "Dict","","Dict"),
    DICT_SYS(3, "DictSys","","DictSys"),
    DICT_USER(4, "DictUser","","DictUser"),
    DEPARTMENT(5, "Department","orgDepartmentImpl","Dict"),
    USER(6, "User","orgUserImpl","Dict"),
    ROLE(7, "Role","orgRoleImpl","Dict"),
    ORGANIZATION(8, "Organ","orgOrganizationImpl","Dict"),
    DICT_ENUM(7,"DictEnum","","DictEnum"),
    DICT_MODULE(8,"DictModule","","DictModule");

    private final int value;
    private final String code;
    private final String beanName;
    private final String annotationName;

    HtmlElementDataSourceEnum(int value, String code,String beanName,String annotationName) {
        this.value = value;
        this.code = code;
        this.beanName = beanName;
        this.annotationName = annotationName;
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

    public String getAnnotationName() {
        return annotationName;
    }

}
