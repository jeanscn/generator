package org.mybatis.generator.codegen.mybatis3.controller.elements;

import com.vgosoft.core.constant.enums.core.RequestMethod;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.controller.AbstractControllerElementGenerator;
import org.mybatis.generator.custom.ReturnTypeEnum;
import org.mybatis.generator.custom.annotations.ApiOperationDesc;
import org.mybatis.generator.custom.annotations.RequestMappingDesc;

import static org.mybatis.generator.custom.ConstantsUtil.SERVICE_RESULT;

public class GetLayuiTableElementGenerator extends AbstractControllerElementGenerator {

    public GetLayuiTableElementGenerator() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        if (!introspectedTable.getRules().isGenerateInnerTable()) {
            return;
        }
        parentElement.addImportedType(SERVICE_RESULT);
        parentElement.addImportedType(entityViewVoType);
        parentElement.addImportedType("com.vgosoft.web.plugins.laytable.Layuitable");
        parentElement.addImportedType("com.vgosoft.web.plugins.laytable.LayuiTableUtil");
        parentElement.addImportedType("org.springframework.web.bind.annotation.RequestParam");

        final String methodPrefix = "getLayuiTable";
        Method method = createMethod(methodPrefix);
        Parameter viewStatus = new Parameter(FullyQualifiedJavaType.getIntegerInstance(), "viewStatus", "@RequestParam(required = false)");
        viewStatus.setRemark("读写状态，0：查看，1：编辑");
        method.addParameter(viewStatus);
        Parameter listKey = new Parameter(FullyQualifiedJavaType.getStringInstance(), "listKey", "@RequestParam(required = false)");
        listKey.setRemark("可选参数，列表配置标识");
        method.addParameter(listKey);
        method.setReturnType(getResponseResult(ReturnTypeEnum.RESPONSE_RESULT_MODEL,
                new FullyQualifiedJavaType("Layuitable"),
                parentElement));
        method.setReturnRemark("layui table配置对象");
        method.addAnnotation(new RequestMappingDesc("lay-table-config", RequestMethod.GET),parentElement);
        method.addAnnotation(new ApiOperationDesc("获得layui table配置对象", "根据ViewVO获得layui table配置对象"),parentElement);
        commentGenerator.addMethodJavaDocLine(method, "根据ViewVO获得layui table配置对象");
        //函数体
        method.addBodyLine("final int edit = viewStatus==null?0:viewStatus;");
        method.addBodyLine("Layuitable layuitable = LayuiTableUtil.getLayuiTable({0}.class,edit,listKey,null);",
                entityViewVoType.getShortName());
        method.addBodyLine("return success(layuitable);");
        parentElement.addMethod(method);
    }
}