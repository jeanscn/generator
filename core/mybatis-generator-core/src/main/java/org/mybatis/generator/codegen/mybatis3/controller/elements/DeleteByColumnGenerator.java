package org.mybatis.generator.codegen.mybatis3.controller.elements;

import com.vgosoft.core.constant.enums.core.RequestMethodEnum;
import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.controller.AbstractControllerElementGenerator;
import org.mybatis.generator.config.SelectByColumnGeneratorConfiguration;
import org.mybatis.generator.custom.enums.ReturnTypeEnum;
import org.mybatis.generator.custom.annotations.ApiOperationDesc;
import org.mybatis.generator.custom.annotations.RequestMappingDesc;

import java.util.List;
import java.util.stream.Collectors;

import static org.mybatis.generator.custom.ConstantsUtil.SERVICE_RESULT;

public class DeleteByColumnGenerator extends AbstractControllerElementGenerator {

    private final SelectByColumnGeneratorConfiguration config;

    /**
     * 构造方法
     */
    public DeleteByColumnGenerator(SelectByColumnGeneratorConfiguration config) {
        super();
        this.config = config;
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        if (!config.getGenControllerMethod() || config.getColumns().isEmpty() || !config.isEnableDelete()) {
            return;
        }
        if (introspectedTable.getRules().isGenerateVoModel()) {
            parentElement.addImportedType(entityVoType);
            parentElement.addImportedType(entityMappings);
        }
        Method method = createMethod(config.getDeleteMethodName());
        method.setReturnType(getResponseResult(ReturnTypeEnum.RESPONSE_RESULT_MODEL,
                FullyQualifiedJavaType.getIntegerInstance(),
                parentElement));
        if (config.getParameterList()) {
            Parameter parameter = new Parameter(new FullyQualifiedJavaType("Map<String, List<String>>"),"paramsMap");
            parameter.addAnnotation("@RequestBody");
            parameter.setRemark("id列表 Map<columnProperty , List<String>>");
            method.addParameter(parameter);
            method.addAnnotation(new RequestMappingDesc(VStringUtil.toHyphenCase(config.getMethodName()), RequestMethodEnum.POST),parentElement);
            method.addAnnotation(new ApiOperationDesc("根据指定列（多值）获取匹配的记录集合", "根据指定列（多值）获取匹配的记录集合接口"),parentElement);
            commentGenerator.addMethodJavaDocLine(method, "根据指定字段的集合获取匹配的记录结合");
            config.getColumns().forEach(column -> {
                method.addBodyLine("List<{0}> {1}s = paramsMap.get(\"{1}s\");", column.getFullyQualifiedJavaType().getShortName(), column.getJavaProperty());
                method.addBodyLine(" if ({0}s.isEmpty()) '{'", column.getJavaProperty());
                method.addBodyLine("return ResponseResult.failure(ApiCodeEnum.FAIL_CUSTOM, \"{0}列表不能为空\");", column.getJavaProperty());
                method.addBodyLine("}");
            });
            parentElement.addImportedType("java.util.Map");
            parentElement.addImportedType("java.util.List");
        } else {
            List<Parameter> parameters = config.getColumns().stream().map(column -> {
                FullyQualifiedJavaType columnType = column.getFullyQualifiedJavaType();
                Parameter parameter = new Parameter(columnType, column.getJavaProperty());
                parameter.addAnnotation("@RequestParam(value = \"" + column.getJavaProperty() + "\")");
                parameter.setRemark(column.getRemarks(false));
                return parameter;
            }).collect(Collectors.toList());
            parameters.forEach(method::addParameter);
            method.addAnnotation(new RequestMappingDesc(VStringUtil.toHyphenCase(config.getDeleteMethodName()), RequestMethodEnum.GET),parentElement);
            method.addAnnotation(new ApiOperationDesc("根据指定列获取匹配的记录集合", "根据指定列获取匹配的记录集合接口"),parentElement);
            commentGenerator.addMethodJavaDocLine(method, "根据指定字段获取匹配的记录结合");
            config.getColumns().forEach(column -> method.addBodyLine("Assert.notNull({0}, \"参数{0}非法！\");", column.getJavaProperty()));
            parentElement.addImportedType("org.springframework.util.Assert");
        }
        method.setReturnRemark("返回符合条件的记录集合");
        method.addBodyLine("int i = {0}.{1}({2});"
                , serviceBeanName
                , config.getDeleteMethodName()
                , config.getColumns().stream()
                        .map(column -> column.getJavaProperty() + (config.getParameterList() ?"s":""))
                        .collect(Collectors.joining(", ")));
        method.addBodyLine("if (i > 0) {");
        method.addBodyLine("return ResponseResult.success(i);");
        method.addBodyLine("} else {");
        method.addBodyLine("return ResponseResult.failure(ApiCodeEnum.FAIL_CUSTOM, \"删除失败\");");
        method.addBodyLine("}");
        parentElement.addImportedType(SERVICE_RESULT);
        parentElement.addMethod(method);
    }


}
