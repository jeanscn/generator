package org.mybatis.generator.codegen.mybatis3.controller.elements;

import com.vgosoft.core.constant.enums.core.RequestMethod;
import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.controller.AbstractControllerElementGenerator;
import org.mybatis.generator.custom.ReturnTypeEnum;
import org.mybatis.generator.custom.annotations.ApiOperation;
import org.mybatis.generator.custom.annotations.RequestMapping;
import org.mybatis.generator.custom.annotations.SystemLog;

import java.util.List;
import java.util.stream.Collectors;

import static org.mybatis.generator.custom.ConstantsUtil.SERVICE_RESULT;

public class GetElementGenerator extends AbstractControllerElementGenerator {

    public GetElementGenerator() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        parentElement.addImportedType(SERVICE_RESULT);
        parentElement.addImportedType(entityType);
        if (introspectedTable.getRules().isGenerateVoModel()) {
            parentElement.addImportedType(entityVoType);
            parentElement.addImportedType(entityMappings);
        }

        final String methodPrefix = "get";
        Method method = createMethod(methodPrefix);
        List<Parameter> parameters = introspectedTable.getPrimaryKeyColumns().stream().map(c -> {
            Parameter parameter = new Parameter(c.getFullyQualifiedJavaType(), c.getJavaProperty());
            parameter.setRemark(c.getRemarks(false));
            parameter.addAnnotation("@PathVariable");
            return parameter;
        }).collect(Collectors.toList());
        String pathVariable = parameters.stream().map(p -> "{" + p.getName() + "}").collect(Collectors.joining("/"));
        parameters.forEach(method::addParameter);
        method.setReturnType(getResponseResult(ReturnTypeEnum.RESPONSE_RESULT_MODEL,
                introspectedTable.getRules().isGenerateVoModel()?entityVoType:entityType,
                parentElement));
        method.setReturnRemark("查询结果数据对象");

        method.addAnnotation(new SystemLog("根据主键查询单条",introspectedTable),parentElement);
        method.addAnnotation(new RequestMapping(pathVariable, RequestMethod.GET),parentElement);
        addSecurityPreAuthorize(method,methodPrefix,"查看详情");
        method.addAnnotation(new ApiOperation("获得单条记录", "根据给定id获取单个实体"),parentElement);

        commentGenerator.addMethodJavaDocLine(method, "根据主键查询单条");

        //函数体
        method.addBodyLine(VStringUtil.format("ServiceResult<{0}> serviceResult = {1}.selectByPrimaryKey(id);",
                entityType.getShortName(), serviceBeanName));
        method.addBodyLine("if (serviceResult.hasResult()) {");
        method.addBodyLine("return success({0});",
                introspectedTable.getRules().isGenerateVoModel()?"mappings.to"+entityVoType.getShortName()+"(serviceResult.getResult())":"serviceResult.getResult()");
        method.addBodyLine("}else{");
        method.addBodyLine("return failure(ApiCodeEnum.FAIL_NOT_FOUND,\"{0}\");",introspectedTable.getRemarks(true)+"数据记录");
        method.addBodyLine("}");
        parentElement.addMethod(method);
    }
}
