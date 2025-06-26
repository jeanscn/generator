package org.mybatis.generator.codegen.mybatis3.unittest.elements;

import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.unittest.AbstractUnitTestElementGenerator;
import org.mybatis.generator.internal.util.Mb3GenUtil;

import static org.mybatis.generator.custom.ConstantsUtil.*;

public class ControllerGetElementGenerator extends AbstractUnitTestElementGenerator {

    public ControllerGetElementGenerator() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        parentElement.addStaticImport("com.vgosoft.test.util.ResponseBodyMatchers.responseBody");
        parentElement.addStaticImport("org.assertj.core.api.Assertions.assertThat");
        parentElement.addImportedType(SERVICE_RESULT);
        parentElement.addImportedType("org.springframework.mock.web.MockHttpServletResponse");
        parentElement.addImportedType("org.springframework.http.HttpStatus");
        parentElement.addImportedType("com.alibaba.fastjson2.JSONObject");
        parentElement.addImportedType(SERVICE_CODE_ENUM);
        if (isGenerateVOModel) {
            parentElement.addImportedType(entityVoType);
            parentElement.addImportedType(entityMappings);
        }else{
            parentElement.addImportedType(entityType);
        }
        parentElement.addImportedType(new FullyQualifiedJavaType(API_CODE_ENUM));
        parentElement.addImportedType("java.nio.charset.StandardCharsets");

        FullyQualifiedJavaType returnEntityType = entityType;
        if (introspectedTable.getRules().isGenerateVoModel()) {
            returnEntityType = entityVoType;
        }

        //getXXX，预期返回测试方法
        String requestUri = VStringUtil.format("get(\"/{0}/'{id}'\", id)", Mb3GenUtil.getControllerBaseMappingPath(introspectedTable));
        String methodName = "get" + entityType.getShortName();
        Method method = createMethod(methodName, parentElement, "获取记录-服务层返回预期结果");
        method.addException(new FullyQualifiedJavaType("java.lang.Exception"));
        addMethodComment(method, true, "被调用的service.selectByPrimaryKey()方法有返回值");
        method.addBodyLine("final ServiceResult<{0}> serviceResult = ServiceResult.success({1});",
                entityType.getShortName(),
                entityInstanceVar);
        if (introspectedTable.getRules().isGenerateVoModel()) {
            method.addBodyLine("{0} {1} = mappings.to{0}({2});",
                    entityType.getShortName() + "VO",
                    entityType.getShortNameFirstLowCase() + "VO",
                    entityType.getShortNameFirstLowCase());
        }else{
            method.addBodyLine("{0} {1} = serviceResult.getResult();"
                    ,returnEntityType.getShortName(),returnEntityType.getShortNameFirstLowCase());
        }
        method.addBodyLine("when({0}.selectByPrimaryKey(id)).thenReturn(serviceResult);",
                mockServiceImpl);
        method.addBodyLine("mockMvc.perform({0}\n" +
                        "                        .accept(MediaType.APPLICATION_JSON))\n" +
                        "                .andExpect(responseBody()\n" +
                        "                        .containsObjectAsJson({1}, {2}.class, ResponseResult.class));",
                requestUri, returnEntityType.getShortNameFirstLowCase(), returnEntityType.getShortName());
        parentElement.addMethod(method);

        //getXXX，服务返回失败的测试方法
        methodName = "get" + entityType.getShortName() + "_ReturnsFailure";
        method = createMethod(methodName, parentElement, "获取记录-服务层返回失败结果");
        method.addException(new FullyQualifiedJavaType("java.lang.Exception"));
        addMethodComment(method, true, "被调用的service.selectByPrimaryKey()方法返回失败");
        method.addBodyLine("final ServiceResult<{0}> serviceResult = ServiceResult.failure(ServiceCodeEnum.FAIL,new Exception(\"err message\"));",
                entityType.getShortName());
        method.addBodyLine("when({0}.selectByPrimaryKey(id)).thenReturn(serviceResult);",
                mockServiceImpl);
        method.addBodyLine("final MockHttpServletResponse response = mockMvc.perform({0}\n" +
                        "                        .accept(MediaType.APPLICATION_JSON))\n" +
                        "                .andReturn()\n" +
                        "                .getResponse();\n" +
                        "        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());\n" +
                        "        ResponseResult<?> responseSimple = JSONObject.parseObject(response.getContentAsString(StandardCharsets.UTF_8), ResponseResult.class);\n" +
                        "        assertThat(responseSimple.getCode()).isEqualTo(ApiCodeEnum.FAIL_NOT_FOUND.code());",
                requestUri);
        parentElement.addMethod(method);
    }
}
