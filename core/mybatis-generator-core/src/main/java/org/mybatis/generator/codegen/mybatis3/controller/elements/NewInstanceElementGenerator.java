package org.mybatis.generator.codegen.mybatis3.controller.elements;

import com.vgosoft.core.constant.enums.core.RequestMethod;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.controller.AbstractControllerElementGenerator;
import org.mybatis.generator.custom.annotations.ApiOperation;
import org.mybatis.generator.custom.annotations.RequestMapping;
import org.mybatis.generator.custom.annotations.SystemLog;

import static org.mybatis.generator.custom.ConstantsUtil.RESPONSE_RESULT;

public class NewInstanceElementGenerator extends AbstractControllerElementGenerator {

    public NewInstanceElementGenerator() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        parentElement.addImportedType(RESPONSE_RESULT);

        FullyQualifiedJavaType type;
        if (introspectedTable.getRules().isGenerateCreateVO()) {
            parentElement.addImportedType(entityCreateVoType);
            type = entityCreateVoType;
        }else if(introspectedTable.getRules().isGenerateVoModel()){
            parentElement.addImportedType(entityVoType);
            type = entityVoType;
        }else{
            parentElement.addImportedType(entityType);
            type = entityType;
        }

        final String methodPrefix = "newInstance";
        Method method = createMethod(methodPrefix);

        Parameter parameter = new Parameter(type, type.getShortNameFirstLowCase());
        parameter.setRemark("需要实例化的类型");
        method.addParameter(parameter);

        FullyQualifiedJavaType returnType = new FullyQualifiedJavaType("ResponseResult<" + type.getShortName() + ">");
        method.setReturnType(returnType);

        method.addAnnotation(new SystemLog("实例化空对象",introspectedTable),parentElement);
        RequestMapping requestMapping = new RequestMapping("new-instance", RequestMethod.GET);
        method.addAnnotation(requestMapping,parentElement);
        method.addAnnotation(new ApiOperation("实例化对象", "实例化一个空对象，供前端使用"),parentElement);
        commentGenerator.addMethodJavaDocLine(method, "实例化一个空对象，供前端使用.允许提供一些初始化值");
        method.addBodyLine("return ResponseResult.success({0});",type.getShortNameFirstLowCase());
        parentElement.addMethod(method);
    }
}
