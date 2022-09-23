package org.mybatis.generator.custom;

import com.vgosoft.core.constant.enums.ApiCodeEnum;
import com.vgosoft.core.constant.enums.IBaseEnum;

import java.util.EnumSet;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-09-12 20:32
 * @version 3.0
 */
public enum ReturnTypeEnum{

    LIST("l","列表"),
    MODEL("m","对象"),
    SERVICE_RESULT_LIST("sl","服务返回列表"),
    SERVICE_RESULT_MODEL("sm","服务返回对象"),
    RESPONSE_RESULT_MODEL("rm","WEB返回对象"),
    RESPONSE_RESULT_LIST("rl","WEB返回列表");

    ReturnTypeEnum(String code,String codeName){
        this.code = code;
        this.codeName = codeName;
    }

    private final String code;
    private final String codeName;

    public String code(){
        return this.code;
    }

    public String codeName(){return  this.codeName;}

    public static ReturnTypeEnum ofCode(String code){
        return EnumSet.allOf(ReturnTypeEnum.class).stream()
                .filter(e -> e.code.equals(code))
                .findFirst().orElse(null);
    }
}
