package org.mybatis.generator.codegen.mybatis3.controller.elements;

import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.controller.AbstractControllerElementGenerator;
import org.mybatis.generator.config.VOCacheGeneratorConfiguration;
import org.mybatis.generator.custom.ReturnTypeEnum;

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

        final String methodPrefix = "getDict";
        Method method = createMethod(methodPrefix);
        addSystemLogAnnotation(method, parentElement);
        VOCacheGeneratorConfiguration config = introspectedTable.getTableConfiguration().getVoCacheGeneratorConfiguration();
        List<IntrospectedColumn> parametersColumn;
        if (introspectedTable.getRules().isGenerateCachePOWithMultiKey()) {
            parametersColumn = Stream.of(config.getTypeColumn(), config.getCodeColumn())
                    .map(c -> introspectedTable.getColumn(c).orElse(null))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } else {
            parametersColumn = introspectedTable.getPrimaryKeyColumns();
        }
        parametersColumn.stream()
                .map(p -> {
                    Parameter parameter = new Parameter(p.getFullyQualifiedJavaType(), p.getJavaProperty());
                    parameter.addAnnotation("@RequestParam");
                    return parameter;
                }).forEach(method::addParameter);
        method.setReturnType(getResponseResult(ReturnTypeEnum.RESPONSE_RESULT_MODEL,
                entityCachePoType,
                parentElement));
        addControllerMapping(method, "dict", "get");
        addSecurityPreAuthorize(method, methodPrefix, "查询字典");

        //函数体
        String param = parametersColumn.stream()
                .map(IntrospectedColumn::getJavaProperty).collect(Collectors.joining(","));
        method.addBodyLine(VStringUtil.format("ServiceResult<{0}> serviceResult = {1}.{2}({3});"
                , entityCachePoType.getShortName()
                , serviceBeanName
                , introspectedTable.getSelectByKeysDictStatementId()
                , param));
        method.addBodyLine("if (serviceResult.hasResult()) {");
        method.addBodyLine("return success(serviceResult.getResult());");
        method.addBodyLine("}else{");
        method.addBodyLine("return failure(ApiCodeEnum.FAIL_NOT_FOUND);");
        method.addBodyLine("}");
        parentElement.addMethod(method);

    }
}
