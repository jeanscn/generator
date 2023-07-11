package org.mybatis.generator.custom;

import com.vgosoft.core.constant.enums.IBaseEnum;

import java.util.EnumSet;

public enum ThymeleafValueScopeEnum implements IBaseEnum<Integer> {

        EDIT(1,"编辑状态"),
        READ(2,"读状态"),
        READONLY(3,"只读状态"),
        UNKNOWN(0,"未知状态");

        ThymeleafValueScopeEnum(Integer code,String codeName){
            this.code = code;
            this.codeName = codeName;
        }

        private final Integer code;
        private final String codeName;

        public Integer code(){
            return this.code;
        }

        public String codeName(){return  this.codeName;}

        public static ThymeleafValueScopeEnum ofCode(Integer code){
            return EnumSet.allOf(ThymeleafValueScopeEnum.class).stream()
                    .filter(e -> e.code.equals(code))
                    .findFirst().orElse(ThymeleafValueScopeEnum.UNKNOWN);
        }

}
