package org.mybatis.generator.codegen.mybatis3.controller.elements;

import com.vgosoft.core.constant.enums.core.RequestMethodEnum;
import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.controller.AbstractControllerElementGenerator;
import org.mybatis.generator.config.SelectByColumnGeneratorConfiguration;
import org.mybatis.generator.custom.annotations.RequestMappingDesc;
import org.mybatis.generator.custom.annotations.swagger3.OperationDesc;
import org.mybatis.generator.custom.enums.ReturnTypeEnum;

import java.util.List;
import java.util.stream.Collectors;

import static org.mybatis.generator.custom.ConstantsUtil.SERVICE_RESULT;

/**
 * @author cen_c
 */
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
        if (config.isLogicalDelete()) {
            addLogicalDelete(parentElement);
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
            method.addAnnotation(new RequestMappingDesc(VStringUtil.toHyphenCase(config.getDeleteMethodName()), RequestMethodEnum.POST),parentElement);
            method.addAnnotation(new OperationDesc("根据指定列（多值）获取匹配的记录集合", "根据指定列（多值）获取匹配的记录集合接口"),parentElement);
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
            }).toList();
            parameters.forEach(method::addParameter);
            method.addAnnotation(new RequestMappingDesc(VStringUtil.toHyphenCase(config.getDeleteMethodName()), RequestMethodEnum.GET),parentElement);
            method.addAnnotation(new OperationDesc("根据指定列获取匹配的记录集合", "根据指定列获取匹配的记录集合接口"),parentElement);
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

    private void addLogicalDelete(TopLevelClass parentElement) {
        Method method = createMethod(config.getDeleteMethodName());
        method.setReturnType(getResponseResult(ReturnTypeEnum.RESPONSE_RESULT_MODEL,
                FullyQualifiedJavaType.getIntegerInstance(), parentElement));
        for (var column : config.getColumns()) {
            FullyQualifiedJavaType type = column.getFullyQualifiedJavaType();
            String name = column.getJavaProperty();
            if (config.getParameterList()) {
                FullyQualifiedJavaType list = FullyQualifiedJavaType.getNewListInstance();
                list.addTypeArgument(type);
                type = list;
                name += "s";
                parentElement.addImportedType("java.util.List");
            }
            parentElement.addImportedType(column.getFullyQualifiedJavaType());
            Parameter parameter = new Parameter(type, name);
            parameter.addAnnotation("@RequestParam(value = \"" + name + "\")");
            parameter.setRemark(column.getRemarks(false));
            method.addParameter(parameter);
        }
        parentElement.addImportedType("org.springframework.web.bind.annotation.RequestParam");
        method.addAnnotation(new RequestMappingDesc(VStringUtil.toHyphenCase(config.getDeleteMethodName()),
                RequestMethodEnum.POST), parentElement);
        addSecurityPreAuthorize(method, "delete", "删除");
        method.addAnnotation(new org.mybatis.generator.custom.annotations.SystemLogDesc("按条件逻辑删除记录", introspectedTable),
                parentElement);
        method.addAnnotation(new OperationDesc("按指定条件逻辑删除", "按指定条件逻辑删除，版本表须传入客户端旧版本"), parentElement);
        commentGenerator.addMethodJavaDocLine(method, "按指定条件逻辑删除，冲突或校验失败由服务异常传播");
        method.setReturnRemark("逻辑删除的记录数");
        method.addBodyLine("return ResponseResult.success(" + serviceBeanName + "." + config.getDeleteMethodName()
                + "(" + method.getParameters().stream().map(Parameter::getName).collect(Collectors.joining(", ")) + "));");
        parentElement.addMethod(method);
    }

}
