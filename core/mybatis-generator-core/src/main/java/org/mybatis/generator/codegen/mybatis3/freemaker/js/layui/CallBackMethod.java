package org.mybatis.generator.codegen.mybatis3.freemaker.js.layui;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-05-26 14:46
 * @version 4.0
 */
@Setter
@Getter
public class CallBackMethod {

    private String methodName;

    /**
     * 1:列表回调方法（一个参数data） 2:普通select回调方法（两个参数o，n）
     */
    private int type = 1;

    private String tagName;

    private String columnName;

    private String params;

    private List<String> methodBodyLines = new ArrayList<>();

    private List<String> remarks = new ArrayList<>();


    public CallBackMethod(String methodName) {
        this.methodName = methodName;
    }

}
