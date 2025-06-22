package org.mybatis.generator.codegen.mybatis3.controller.elements;

import com.vgosoft.core.constant.enums.core.EntityEventEnum;
import com.vgosoft.core.constant.enums.core.RequestMethodEnum;
import com.vgosoft.core.constant.enums.db.DbFiledDefaultValueEnum;
import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.mybatis3.controller.AbstractControllerElementGenerator;
import org.mybatis.generator.custom.annotations.ApiOperationDesc;
import org.mybatis.generator.custom.annotations.RequestMappingDesc;
import org.mybatis.generator.custom.annotations.SystemLogDesc;
import org.mybatis.generator.internal.util.JavaBeansUtil;

import java.util.List;
import java.util.stream.Collectors;

import static org.mybatis.generator.custom.ConstantsUtil.RESPONSE_RESULT;

public class NewInstanceElementGenerator extends AbstractControllerElementGenerator {

    public NewInstanceElementGenerator() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        parentElement.addImportedType(RESPONSE_RESULT);

        boolean createdEvent = introspectedTable.getTableConfiguration().getJavaServiceImplGeneratorConfiguration().getEntityEvent().contains(EntityEventEnum.CREATED.name());

        //为类添加属性protected EntityEventPublisher publisher;
        if (createdEvent) {
            Field field = new Field("publisher", new FullyQualifiedJavaType("com.vgosoft.core.event.entity.EntityEventPublisher"));
            field.setVisibility(JavaVisibility.PROTECTED);
            field.addAnnotation("@Resource");
            parentElement.addField(field);
            parentElement.addImportedType(new FullyQualifiedJavaType("com.vgosoft.core.event.entity.EntityEventPublisher"));
            parentElement.addImportedType(new FullyQualifiedJavaType("com.vgosoft.core.constant.enums.core.EntityEventEnum"));
            parentElement.addImportedType(new FullyQualifiedJavaType("javax.annotation.Resource"));
        }

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
        RequestMappingDesc requestMappingDesc = new RequestMappingDesc("new-instance", RequestMethodEnum.GET);
        method.addAnnotation(requestMappingDesc, parentElement);
        method.addAnnotation(new ApiOperationDesc("实例化对象", "实例化一个空对象，供前端使用"), parentElement);
        commentGenerator.addMethodJavaDocLine(method, "实例化一个空对象，供前端使用.允许提供一些初始化值");
        if (introspectedTable.getRules().isGenerateCreateVO() && introspectedTable.getRules().isGenerateVoModel()) {
            addIocInitialDefaultValue(introspectedTable, method, parentElement, type);
            method.addBodyLine("{0} {1} = mappings.from{2}({3});", entityType.getShortName(), entityType.getShortNameFirstLowCase(), type.getShortName(), type.getShortNameFirstLowCase());
            if (createdEvent) {
                method.addBodyLine("publisher.publishEvent({0}, EntityEventEnum.CREATED);", entityType.getShortNameFirstLowCase());
            }
            method.addBodyLine("{0} object = mappings.to{0}({1});", entityVoType.getShortName(), entityType.getShortNameFirstLowCase());
            method.addBodyLine("return ResponseResult.success(updateNewInstanceDefaultValue(object));");
        } else {
            addIocInitialDefaultValue(introspectedTable, method, parentElement, type);
            method.addBodyLine("return ResponseResult.success(updateNewInstanceDefaultValue({0}));", type.getShortNameFirstLowCase());
        }
        parentElement.addMethod(method);
    }
}
