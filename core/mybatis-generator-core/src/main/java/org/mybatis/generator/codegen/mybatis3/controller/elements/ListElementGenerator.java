package org.mybatis.generator.codegen.mybatis3.controller.elements;

import com.vgosoft.core.constant.enums.core.RequestMethodEnum;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.controller.AbstractControllerElementGenerator;
import org.mybatis.generator.custom.enums.ReturnTypeEnum;
import org.mybatis.generator.custom.annotations.ApiOperationDesc;
import org.mybatis.generator.custom.annotations.RequestMappingDesc;
import org.mybatis.generator.custom.annotations.SystemLogDesc;

/**
 * @author cen_c
 */
public class ListElementGenerator extends AbstractControllerElementGenerator {

    public ListElementGenerator() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        final String methodPrefix = "list";
        Method method = createMethod(methodPrefix);
        MethodParameterDescriptor descriptor = new MethodParameterDescriptor(parentElement, "get");
        descriptor.setValid(true);
        Parameter parameter = buildMethodParameter(descriptor);
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
        method.addAnnotation(new SystemLogDesc("查看数据列表", introspectedTable), parentElement);
        method.addAnnotation(new RequestMappingDesc("", RequestMethodEnum.GET), parentElement);
        addSecurityPreAuthorize(method, methodPrefix, "数据列表");
        method.addAnnotation(new ApiOperationDesc("获得列表数据", "根据给定条件获取多条或所有数据列表，可以根据需要传入属性同名参数"), parentElement);
        commentGenerator.addMethodJavaDocLine(method, "获取条件实体对象列表");
        method.addBodyLine("if (actionType != null) {0}.setActionType(actionType);", introspectedTable.getRules().isGenerateRequestVo() ? entityRequestVoType.getShortNameFirstLowCase() :
                introspectedTable.getRules().isGenerateVoModel() ? entityVoType.getShortNameFirstLowCase() : entityType.getShortNameFirstLowCase());
        method.addBodyLine("{0} example = buildExample({1});",
                exampleType.getShortName(),
                introspectedTable.getRules().isGenerateRequestVo() ? entityRequestVoType.getShortNameFirstLowCase() :
                        introspectedTable.getRules().isGenerateVoModel() ? entityVoType.getShortNameFirstLowCase() : entityType.getShortNameFirstLowCase());
        addSelectByExampleWithPagehelper(parentElement, method,entityRequestVoType.getShortNameFirstLowCase());
        resultPartBodyLines(parentElement, method);
        parentElement.addMethod(method);

        //生成post方法
        Method postMethod = createMethod(methodPrefix + "Post");
        MethodParameterDescriptor postDescription = new MethodParameterDescriptor(parentElement, "list");
        postDescription.setValid(true);
        postDescription.setRequestBody(true);
        Parameter postParameter = buildMethodParameter(postDescription);
        postParameter.setRemark("用于接收属性同名参数,包含filterMap");
        postMethod.addParameter(postParameter);
        postMethod.addParameter(actionType);
        postMethod.setReturnType(getResponseResult(ReturnTypeEnum.RESPONSE_RESULT_LIST,
                introspectedTable.getRules().isGenerateVoModel() ? entityVoType : entityType,
                parentElement));
        postMethod.setReturnRemark("结果对象列表");
        postMethod.addAnnotation(new SystemLogDesc("查看数据列表", introspectedTable), parentElement);
        postMethod.addAnnotation(new RequestMappingDesc("list", RequestMethodEnum.POST), parentElement);
        addSecurityPreAuthorize(postMethod, methodPrefix, "数据列表");
        postMethod.addAnnotation(new ApiOperationDesc("获得列表数据", "根据给定条件获取多条或所有数据列表，可以根据需要传入属性同名参数"), parentElement);
        commentGenerator.addMethodJavaDocLine(postMethod, "获取条件实体对象列表");
        postMethod.addBodyLine("if (actionType != null) {0}.setActionType(actionType);", introspectedTable.getRules().isGenerateRequestVo() ? entityRequestVoType.getShortNameFirstLowCase() :
                introspectedTable.getRules().isGenerateVoModel() ? entityVoType.getShortNameFirstLowCase() : entityType.getShortNameFirstLowCase());
        postMethod.addBodyLine("{0} example = buildExample({1});",
                exampleType.getShortName(),
                introspectedTable.getRules().isGenerateRequestVo() ? entityRequestVoType.getShortNameFirstLowCase() :
                        introspectedTable.getRules().isGenerateVoModel() ? entityVoType.getShortNameFirstLowCase() : entityType.getShortNameFirstLowCase());
        postMethod.addBodyLine(" FilterParam filterParam = {0}.getFilterParam();", introspectedTable.getRules().isGenerateRequestVo() ? entityRequestVoType.getShortNameFirstLowCase() :
                introspectedTable.getRules().isGenerateVoModel() ? entityVoType.getShortNameFirstLowCase() : entityType.getShortNameFirstLowCase());
        postMethod.addBodyLine(" if (filterParam != null) {");
        postMethod.addBodyLine("String filterSql = ParameterUtil.parseFilterMap(filterParam, \"{0}\");", introspectedTable.getTableConfiguration().getAlias());
        postMethod.addBodyLine("if (VStringUtil.stringHasValue(filterSql)) {");
        postMethod.addBodyLine(" example.getOredCriteria().forEach(criteria -> criteria.andAnyCondition(filterSql));");
        postMethod.addBodyLine("}");
        postMethod.addBodyLine("}");
        addSelectByExampleWithPagehelper(parentElement, postMethod,entityRequestVoType.getShortNameFirstLowCase());
        resultPartBodyLines(parentElement, postMethod);
        parentElement.addMethod(postMethod);
        // 添加导入的类型
        addImportedType(parentElement);
    }

    private void addImportedType(TopLevelClass parentElement) {
        parentElement.addImportedType(FullyQualifiedJavaType.getNewListInstance());
        parentElement.addImportedType(FullyQualifiedJavaType.getNewArrayListInstance());
        parentElement.addImportedType(entityType);
        parentElement.addImportedType(exampleType);
        if (introspectedTable.getRules().isGenerateVoModel()) {
            parentElement.addImportedType(entityVoType);
            parentElement.addImportedType(entityMappings);
        }
        parentElement.addImportedType("com.vgosoft.web.utils.ParameterUtil");
        parentElement.addImportedType("com.vgosoft.core.adapter.web.FilterParam");
        parentElement.addImportedType("com.vgosoft.tool.core.VStringUtil");
        parentElement.addImportedType("java.lang.Boolean");
        parentElement.addImportedType("org.springframework.validation.annotation.Validated");
        parentElement.addImportedType("org.springframework.web.bind.annotation.RequestParam");
        parentElement.addImportedType("org.springframework.web.bind.annotation.RequestBody");
    }

    private void resultPartBodyLines(TopLevelClass parentElement, Method method) {
        boolean pageParam = introspectedTable.getTableConfiguration().getVoGeneratorConfiguration().getVoRequestConfiguration().isIncludePageParam();
        method.addBodyLine("if (result.hasResult()) {");
        if (introspectedTable.getRules().isGenerateVoModel() && introspectedTable.getRules().isGenerateRequestVo()) {
            if (pageParam) {
                method.addBodyLine("if (page != null) {");
                method.addBodyLine("return ResponsePagehelperResult.success(mappings.to{0}s(result.getResult()), page);", entityVoType.getShortName());
                method.addBodyLine("} else {");
                method.addBodyLine("return ResponseResult.success(mappings.to{0}s(result.getResult()));", entityVoType.getShortName());
                method.addBodyLine("}");
            } else {
                method.addBodyLine("return ResponseResult.success(mappings.to{0}s(result.getResult()));", entityVoType.getShortName());
            }
            parentElement.addImportedType(responsePagehelperResult);
        } else if (introspectedTable.getRules().isGenerateVoModel()) {
            method.addBodyLine("return ResponseResult.success(mappings.to{0}s(result.getResult()));", entityVoType.getShortName());
            parentElement.addImportedType(responseResult);
        } else if (introspectedTable.getRules().isGenerateRequestVo()) {
            if (pageParam) {
                method.addBodyLine("if (page!=null) {");
                method.addBodyLine("return ResponsePagehelperResult.success(result.getResult(),page);");
                method.addBodyLine("} else {");
                method.addBodyLine("return ResponseResult.success(result.getResult());");
                method.addBodyLine("}");
            } else {
                method.addBodyLine("return ResponseResult.success(result.getResult());");
            }
            parentElement.addImportedType(responsePagehelperResult);
        } else {
            method.addBodyLine("return ResponseResult.success(result.getResult());");
            parentElement.addImportedType(responseResult);
        }
        method.addBodyLine("} else {");
        method.addBodyLine("return ResponseResult.success(new ArrayList<>());");
        method.addBodyLine("}");
    }
}
