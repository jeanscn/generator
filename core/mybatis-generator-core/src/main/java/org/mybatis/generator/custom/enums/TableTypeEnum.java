package org.mybatis.generator.custom.enums;

import java.util.EnumSet;

/**
 * @author cen_c
 */

public enum TableTypeEnum {

    /**
     *  普通数据表
     */
    DATA_TABLE("dataTable","普通数据表"),
    /**
     *  关系中间表
     */
    RELATION_TABLE("relationTable","关系中间表"),
    /**
     *  未知类型
     */
    UNKNOWN("unknown","未知类型");

    private final String code;
    private final String codeName;

    TableTypeEnum(String code,String codeName){
        this.code = code;
        this.codeName = codeName;
    }

    public String code(){
        return this.code;
    }

    public String codeName(){return  this.codeName;}

    public static TableTypeEnum ofCode(String code){
        return EnumSet.allOf(TableTypeEnum.class).stream()
                .filter(e -> e.code.equals(code))
                .findFirst().orElse(TableTypeEnum.UNKNOWN);
    }
}
