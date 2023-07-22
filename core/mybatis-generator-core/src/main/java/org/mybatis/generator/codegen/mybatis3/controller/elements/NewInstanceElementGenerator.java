package org.mybatis.generator.codegen.mybatis3.controller.elements;

import com.vgosoft.core.constant.enums.core.RequestMethod;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.controller.AbstractControllerElementGenerator;
import org.mybatis.generator.custom.annotations.ApiOperationDesc;
import org.mybatis.generator.custom.annotations.RequestMappingDesc;
import org.mybatis.generator.custom.annotations.SystemLogDesc;

import static org.mybatis.generator.custom.ConstantsUtil.RESPONSE_RESULT;

public class NewInstanceElementGenerator extends AbstractControllerElementGenerator {

    public NewInstanceElementGenerator() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        parentElement.addImportedType(RESPONSE_RESULT);

        FullyQualifiedJavaType type;
        FullyQualifiedJavaType returnType;
        if (introspectedTable.getRules().isGenerateCreateVO()) {
            parentElement.addImportedType(entityCreateVoType);
            type = entityCreateVoType;
            returnType = new FullyQualifiedJavaType("ResponseResult<" + entityVoType.getShortName() + ">");
        } else if (introspectedTable.getRules().isGenerateVoModel()) {
            parentElement.addImportedType(entityVoType);
            type = entityVoType;
            returnType = new FullyQualifiedJavaType("ResponseResult<" + entityVoType.getShortName() + ">");
        } else {
            parentElement.addImportedType(entityType);
            type = entityType;
            returnType = new FullyQualifiedJavaType("ResponseResult<" + entityType.getShortName() + ">");
        }

        final String methodPrefix = "newInstance";
        Method method = createMethod(methodPrefix);
        Parameter parameter = new Parameter(type, type.getShortNameFirstLowCase());
        parameter.setRemark("需要实例化的类型");
        method.addParameter(parameter);
        method.setReturnType(returnType);

        method.addAnnotation(new SystemLogDesc("实例化空对象", introspectedTable), parentElement);
        RequestMappingDesc requestMappingDesc = new RequestMappingDesc("new-instance", RequestMethod.GET);
        method.addAnnotation(requestMappingDesc, parentElement);
        method.addAnnotation(new ApiOperationDesc("实例化对象", "实例化一个空对象，供前端使用"), parentElement);
        commentGenerator.addMethodJavaDocLine(method, "实例化一个空对象，供前端使用.允许提供一些初始化值");
        if (introspectedTable.getRules().isGenerateCreateVO() && introspectedTable.getRules().isGenerateVoModel()) {
            method.addBodyLine("{0} {1} = mappings.from{2}({3});"
                    , entityType.getShortName(), entityType.getShortNameFirstLowCase(), type.getShortName(), type.getShortNameFirstLowCase());
            method.addBodyLine("{0} object = mappings.to{0}({1});"
                    , entityVoType.getShortName(),entityType.getShortNameFirstLowCase());
            method.addBodyLine("return ResponseResult.success(updateNewInstanceDefaultValue(object));");
        }else{
            method.addBodyLine("return ResponseResult.success(updateNewInstanceDefaultValue({0}));",type.getShortNameFirstLowCase());
        }
        parentElement.addMethod(method);
    }
}
