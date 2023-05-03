package org.mybatis.generator.codegen.mybatis3.controller.elements;

import com.vgosoft.core.constant.enums.RequestMethod;
import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.controller.AbstractControllerElementGenerator;
import org.mybatis.generator.codegen.mybatis3.service.ServiceMethods;
import org.mybatis.generator.config.VOCacheGeneratorConfiguration;
import org.mybatis.generator.custom.ReturnTypeEnum;
import org.mybatis.generator.custom.annotations.ApiOperation;
import org.mybatis.generator.custom.annotations.RequestMapping;
import org.mybatis.generator.custom.annotations.SystemLog;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mybatis.generator.custom.ConstantsUtil.SERVICE_RESULT;

public class GetDictElementGenerator extends AbstractControllerElementGenerator {

    public GetDictElementGenerator() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        parentElement.addImportedType(SERVICE_RESULT);
        parentElement.addImportedType(entityType);
        parentElement.addImportedType(entityCachePoType);
        parentElement.addImportedType(FullyQualifiedJavaType.getNewArrayListInstance());

        final String methodPrefix = "getDict";
        Method method = createMethod(methodPrefix);
        List<IntrospectedColumn> parametersColumn = (new ServiceMethods(context,introspectedTable)).getSelectDictParameterColumns(
                introspectedTable.getTableConfiguration().getVoCacheGeneratorConfiguration(), introspectedTable);
        parametersColumn.stream()
                .map(p -> {
                    Parameter parameter = new Parameter(p.getFullyQualifiedJavaType(), p.getJavaProperty());
                    parameter.addAnnotation("@RequestParam");
                    parameter.setRemark(p.getRemarks(false));
                    return parameter;
                }).forEach(method::addParameter);
        method.setReturnType(getResponseResult(ReturnTypeEnum.RESPONSE_RESULT_LIST,
                entityCachePoType,
                parentElement));
        method.setReturnRemark("缓存数据对象");

        method.addAnnotation(new SystemLog("查询字典数据",introspectedTable),parentElement);
        method.addAnnotation(new RequestMapping("dict", RequestMethod.GET),parentElement);
        addSecurityPreAuthorize(method,methodPrefix,"查询字典");
        method.addAnnotation(new ApiOperation("字典数据查询", "获取字典数据并缓存"),parentElement);

        commentGenerator.addMethodJavaDocLine(method, "查询字典数据");

        //函数体
        String param = parametersColumn.stream()
                .map(IntrospectedColumn::getJavaProperty).collect(Collectors.joining(","));
        method.addBodyLine(VStringUtil.format("ServiceResult<List<{0}>> serviceResult = {1}.{2}({3});"
                , entityCachePoType.getShortName()
                , serviceBeanName
                , introspectedTable.getSelectByKeysDictStatementId()
                , param));
        method.addBodyLine("if (serviceResult.hasResult()) {");
        method.addBodyLine("return success(serviceResult.getResult());");
        method.addBodyLine("}else{");
        method.addBodyLine("return failure(ApiCodeEnum.FAIL_NOT_FOUND,\"{0}\");",introspectedTable.getRemarks(true)+"字典数据");
        method.addBodyLine("}");
        parentElement.addMethod(method);

    }
}
