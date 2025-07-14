package org.mybatis.generator.codegen.mybatis3.controller.elements;

import com.vgosoft.core.constant.enums.core.RequestMethodEnum;
import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.controller.AbstractControllerElementGenerator;
import org.mybatis.generator.codegen.mybatis3.service.ServiceMethods;
import org.mybatis.generator.config.VoCacheGeneratorConfiguration;
import org.mybatis.generator.custom.enums.ReturnTypeEnum;
import org.mybatis.generator.custom.annotations.ApiOperationDesc;
import org.mybatis.generator.custom.annotations.RequestMappingDesc;
import org.mybatis.generator.custom.annotations.SystemLogDesc;

import java.util.List;

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
        parentElement.addImportedType(FullyQualifiedJavaType.getOptionalFullyQualifiedJavaType());
        VoCacheGeneratorConfiguration configuration = introspectedTable.getTableConfiguration().getVoCacheGeneratorConfiguration();
        final String methodPrefix = "getDict";
        Method method = createMethod(methodPrefix);
        method.setReturnType(getResponseResult(ReturnTypeEnum.RESPONSE_RESULT_LIST,
                entityCachePoType,
                parentElement));
        method.setReturnRemark("缓存数据对象");
        method.addAnnotation(new SystemLogDesc("查询字典数据",introspectedTable),parentElement);
        method.addAnnotation(new RequestMappingDesc("dict", RequestMethodEnum.GET),parentElement);
        addSecurityPreAuthorize(method,methodPrefix,"查询字典");
        method.addAnnotation(new ApiOperationDesc("字典数据查询", "获取字典数据并缓存"),parentElement);

        commentGenerator.addMethodJavaDocLine(method, "查询字典数据");

        //函数体
        ServiceMethods serviceMethods = new ServiceMethods(context, introspectedTable);
        List<Parameter> selectDictByKeysParameters = serviceMethods.getDictControllerMethodParameters(introspectedTable.getTableConfiguration().getVoCacheGeneratorConfiguration());
        selectDictByKeysParameters.forEach(method::addParameter);
        method.addBodyLine(VStringUtil.format("ServiceResult<List<{0}>> serviceResult = {1}.{2}({3});"
                , entityCachePoType.getShortName()
                , serviceBeanName
                , introspectedTable.getSelectByKeysDictStatementId()
                , configuration.getTypeColumn() == null ? "Optional.empty(),keys" : "Optional.of(types),keys"));
        method.addBodyLine("if (serviceResult.hasResult()) {");
        method.addBodyLine("return success(serviceResult.getResult());");
        method.addBodyLine("}else{");
        method.addBodyLine("return failure(ApiCodeEnum.FAIL_NOT_FOUND,\"{0}\");",introspectedTable.getRemarks(true)+"字典数据");
        method.addBodyLine("}");
        parentElement.addMethod(method);
        selectDictByKeysParameters.forEach(parameter -> parentElement.addImportedType(parameter.getType()));
    }
}
