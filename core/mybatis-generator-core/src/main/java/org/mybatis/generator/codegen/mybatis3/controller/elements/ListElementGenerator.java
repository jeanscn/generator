package org.mybatis.generator.codegen.mybatis3.controller.elements;

import com.vgosoft.core.constant.enums.core.RequestMethod;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.controller.AbstractControllerElementGenerator;
import org.mybatis.generator.custom.ReturnTypeEnum;
import org.mybatis.generator.custom.annotations.ApiOperation;
import org.mybatis.generator.custom.annotations.RequestMapping;
import org.mybatis.generator.custom.annotations.SystemLog;

public class ListElementGenerator extends AbstractControllerElementGenerator {

    public ListElementGenerator() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        parentElement.addImportedType(FullyQualifiedJavaType.getNewListInstance());
        parentElement.addImportedType(FullyQualifiedJavaType.getNewArrayListInstance());
        parentElement.addImportedType(entityType);
        parentElement.addImportedType(exampleType);
        if (introspectedTable.getRules().isGenerateVoModel()) {
            parentElement.addImportedType(entityVoType);
            parentElement.addImportedType(entityMappings);
        }

        final String methodPrefix = "list";
        Method method = createMethod(methodPrefix);
        MethodParameterDescript descript = new MethodParameterDescript(parentElement, "get");
        descript.setValid(true);
        Parameter parameter = buildMethodParameter(descript);
        parameter.setRemark("用于接收属性同名参数");
        parameter.addAnnotation("@Validated(value= ValidateQuery.class)");
        parentElement.addImportedType("com.vgosoft.core.valid.ValidateQuery");
        method.addParameter(parameter);
        Parameter actionType = new Parameter(FullyQualifiedJavaType.getStringInstance(), "actionType");
        actionType.addAnnotation("@RequestParam(required = false)");
        actionType.setRemark("可选参数，查询场景标识");
        method.addParameter(actionType);
        method.setReturnType(getResponseResult(ReturnTypeEnum.RESPONSE_RESULT_LIST,
                introspectedTable.getRules().isGenerateVoModel() ? entityVoType : entityType,
                parentElement));
        method.setReturnRemark("结果对象列表");

        method.addAnnotation(new SystemLog("查看数据列表", introspectedTable), parentElement);
        method.addAnnotation(new RequestMapping("", RequestMethod.GET), parentElement);
        addSecurityPreAuthorize(method, methodPrefix, "数据列表");
        method.addAnnotation(new ApiOperation("获得列表数据", "根据给定条件获取多条或所有数据列表，可以根据需要传入属性同名参数"), parentElement);

        commentGenerator.addMethodJavaDocLine(method, "获取条件实体对象列表");

        selectByExampleWithPagehelper(parentElement, method);
        method.addBodyLine("if (result.hasResult()) {");
        if (introspectedTable.getRules().isGenerateVoModel() && introspectedTable.getRules().isGenerateRequestVO()) {
            method.addBodyLine("return ResponsePagehelperResult.success(mappings.to{0}s(result.getResult()),page);", entityVoType.getShortName());
            parentElement.addImportedType(responsePagehelperResult);
        } else if(introspectedTable.getRules().isGenerateVoModel()){
            method.addBodyLine("return ResponseResult.success(mappings.to{0}s(result.getResult()));", entityVoType.getShortName());
            parentElement.addImportedType(responseResult);
        }else if(introspectedTable.getRules().isGenerateRequestVO()){
            method.addBodyLine("return ResponsePagehelperResult.success(result.getResult(),page);");
            parentElement.addImportedType(responsePagehelperResult);
        } else{
            method.addBodyLine("return ResponseResult.success(result.getResult());");
            parentElement.addImportedType(responseResult);
        }
        method.addBodyLine("}else{");
        method.addBodyLine("return ResponseResult.success(new ArrayList<>());");
        method.addBodyLine("}");
        parentElement.addMethod(method);
    }


}
