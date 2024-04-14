package org.mybatis.generator.codegen.mybatis3.controller.elements;

import com.vgosoft.core.constant.enums.core.RequestMethodEnum;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.controller.AbstractControllerElementGenerator;
import org.mybatis.generator.custom.annotations.ApiOperationDesc;
import org.mybatis.generator.custom.annotations.RequestMappingDesc;
import org.mybatis.generator.custom.annotations.SystemLogDesc;

import static com.vgosoft.tool.core.VStringUtil.format;
import static org.mybatis.generator.custom.ConstantsUtil.SERVICE_RESULT;

public class DownloadElementGenerator extends AbstractControllerElementGenerator {

    public DownloadElementGenerator() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        parentElement.addImportedType(SERVICE_RESULT);
        parentElement.addImportedType(entityType);
        parentElement.addImportedType("javax.servlet.http.HttpServletResponse");
        parentElement.addImportedType("org.springframework.util.Assert");
        parentElement.addImportedType("org.apache.commons.lang3.BooleanUtils");

        final String methodPrefix = "download";
        Method method = createMethod(methodPrefix);

        Parameter idParameter = new Parameter(FullyQualifiedJavaType.getStringInstance(), "id");
        idParameter.addAnnotation("@PathVariable");
        idParameter.setRemark("唯一标识");
        method.addParameter(idParameter);
        Parameter typeParameter = new Parameter(FullyQualifiedJavaType.getStringInstance(), "type");
        typeParameter.addAnnotation("@PathVariable");
        typeParameter.setRemark("下载后展示方式：1-下载，0-浏览器中打开");
        method.addParameter(typeParameter);
        FullyQualifiedJavaType response = new FullyQualifiedJavaType("javax.servlet.http.HttpServletResponse");
        Parameter parameter = new Parameter(response, "response");
        parameter.setRemark("Http响应");
        method.addParameter(parameter);
        method.addException(new FullyQualifiedJavaType("java.lang.Exception"));
        method.setExceptionRemark("下载处理异常，含IO异常");

        method.addAnnotation(new SystemLogDesc("下载数据",introspectedTable),parentElement);
        RequestMappingDesc requestMappingDesc = new RequestMappingDesc("download/{type}/{id}", RequestMethodEnum.GET);
        requestMappingDesc.addProduces("MediaType.APPLICATION_OCTET_STREAM_VALUE");
        method.addAnnotation(requestMappingDesc,parentElement);
        parentElement.addImportedType("org.springframework.http.MediaType");
        addSecurityPreAuthorize(method,methodPrefix,"下载");
        method.addAnnotation(new ApiOperationDesc("单个文件下载", "单个文件下载接口"),parentElement);

        commentGenerator.addMethodJavaDocLine(method, "单个文件下载");

        method.addBodyLine("Assert.notNull(id, \"资源的id非法！\");");
        method.addBodyLine(format("ServiceResult<{0}> serviceResult = {1}.selectByPrimaryKey(id);", entityType.getShortName(),this.serviceBeanName));
        method.addBodyLine(format("{0} {1} = serviceResult.getResult();", entityType.getShortName(),entityType.getShortNameFirstLowCase()));
        method.addBodyLine(format("Assert.notNull({0}, \"获取文件失败！\");", entityType.getShortNameFirstLowCase()));
        method.addBodyLine(format("byte[] bytes = {0}.getBytes();", entityType.getShortNameFirstLowCase()));
        method.addBodyLine(format("String fileName = {0}.getName() == null ? id : {0}.getName();", entityType.getShortNameFirstLowCase()));
        method.addBodyLine("setResponseContent(fileName, response, bytes, BooleanUtils.toBoolean(type));");
        parentElement.addMethod(method);
    }
}
