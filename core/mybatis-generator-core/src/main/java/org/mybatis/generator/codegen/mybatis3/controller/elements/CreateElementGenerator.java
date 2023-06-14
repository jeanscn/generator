package org.mybatis.generator.codegen.mybatis3.controller.elements;

import com.vgosoft.core.constant.enums.core.RequestMethod;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.controller.AbstractControllerElementGenerator;
import org.mybatis.generator.custom.ReturnTypeEnum;
import org.mybatis.generator.custom.annotations.ApiOperation;
import org.mybatis.generator.custom.annotations.RequestMapping;
import org.mybatis.generator.custom.annotations.SystemLog;

import static org.mybatis.generator.custom.ConstantsUtil.SERVICE_RESULT;

public class CreateElementGenerator extends AbstractControllerElementGenerator {

    public CreateElementGenerator() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        parentElement.addImportedType(SERVICE_RESULT);
        parentElement.addImportedType(entityType);
        if (introspectedTable.getRules().isGenerateCreateVO()) {
            parentElement.addImportedType(entityCreateVoType);
            parentElement.addImportedType(entityMappings);
        } else if (introspectedTable.getRules().isGenerateVoModel()) {
            parentElement.addImportedType(entityVoType);
            parentElement.addImportedType(entityMappings);
        }
        final String methodPrefix = "create";
        Method method = createMethod(methodPrefix);

        MethodParameterDescript descript = new MethodParameterDescript(parentElement,"post");
        descript.setValid(true);
        descript.setRequestBody(true);
        Parameter parameter = buildMethodParameter(descript);
        parameter.setRemark("接收请求待持久化的数据（对象）");
        method.addParameter(parameter);

        method.setReturnType(getResponseResult(ReturnTypeEnum.MODEL,
                introspectedTable.getRules().isGenerateVoModel() ? entityVoType : entityType,
                parentElement));
        method.setReturnRemark("更新后的数据（对象）");

        method.addAnnotation(new SystemLog("添加了一条记录",introspectedTable),parentElement);
        method.addAnnotation(new RequestMapping("", RequestMethod.POST),parentElement);
        addSecurityPreAuthorize(method,methodPrefix,"创建");
        method.addAnnotation(new ApiOperation("新增一条记录", "新增一条记录,返回json，包含影响条数及消息"),parentElement);

        commentGenerator.addMethodJavaDocLine(method, "新增一条记录");

        method.addBodyLine("ServiceResult<{0}> serviceResult;", entityType.getShortName());
        if (introspectedTable.getRules().isGenerateCreateVO()) {
            if (introspectedTable.getRules().createEnableSelective()) {
                method.addBodyLine("if ({0}CreateVO.isSelectiveUpdate() && VStringUtil.isNotBlank({0}CreateVO.getId())) '{'", entityType.getShortNameFirstLowCase());
                method.addBodyLine("serviceResult = {0}.updateByPrimaryKeySelective({1});"
                        , serviceBeanName, getServiceMethodEntityParameter(false, "create"));
                method.addBodyLine("'}' else if ({0}CreateVO.isSelectiveUpdate()) '{'", entityType.getShortNameFirstLowCase());
                method.addBodyLine("serviceResult = {0}.insertSelective({1});"
                        , serviceBeanName, getServiceMethodEntityParameter(false, "create"));
                method.addBodyLine("'}' else if (VStringUtil.isNotBlank({0}CreateVO.getId())) '{'", entityType.getShortNameFirstLowCase());
            }else{
                method.addBodyLine("if (VStringUtil.isNotBlank({0}CreateVO.getId())) '{'", entityType.getShortNameFirstLowCase());
            }
        }else if(introspectedTable.getRules().isGenerateVoModel()){
            method.addBodyLine("if (VStringUtil.isNotBlank({0}VO.getId())) '{'", entityType.getShortNameFirstLowCase());
        }else{
            method.addBodyLine("if (VStringUtil.isNotBlank({0}.getId())) '{'", entityType.getShortNameFirstLowCase());
        }
        method.addBodyLine("serviceResult = {0}.updateByPrimaryKey({1});"
                , serviceBeanName, getServiceMethodEntityParameter(false, "create"));
        method.addBodyLine("} else {");
        method.addBodyLine("serviceResult = {0}.insert({1});"
                , serviceBeanName, getServiceMethodEntityParameter(false, "create"));
        method.addBodyLine("}");
        if (introspectedTable.getRules().generateInsertOrUpdate()) {
            method.addBodyLine("if (!serviceResult.isSuccess()) {");
            method.addBodyLine("serviceResult = {0}.insertOrUpdate({1});",serviceBeanName,getServiceMethodEntityParameter(false, "create"));
            method.addBodyLine("}");
        }

        method.addBodyLine("if (serviceResult.hasResult()) {");
        method.addBodyLine("return success({0},serviceResult.getAffectedRows());"
                , introspectedTable.getRules().isGenerateVoModel() ? "mappings.to" + entityVoType.getShortName() + "(serviceResult.getResult())" : "serviceResult.getResult()");
        method.addBodyLine("}else{");
        method.addBodyLine("return failure(ApiCodeEnum.FAIL_OPERATION);");
        method.addBodyLine("}");

        parentElement.addMethod(method);
    }


}
