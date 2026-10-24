package org.mybatis.generator.codegen.mybatis3.unittest.elements;

import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.unittest.AbstractUnitTestElementGenerator;
import org.mybatis.generator.internal.util.Mb3GenUtil;

import static org.mybatis.generator.custom.ConstantsUtil.*;

/**
 * @author cen_c
 */
public class ControllerGetElementGenerator extends AbstractUnitTestElementGenerator {

    public ControllerGetElementGenerator() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        parentElement.addStaticImport("org.assertj.core.api.Assertions.assertThat");
        parentElement.addStaticImport("org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath");
        parentElement.addStaticImport("org.springframework.test.web.servlet.result.MockMvcResultMatchers.status");
        parentElement.addImportedType(SERVICE_RESULT);
        parentElement.addImportedType("org.springframework.mock.web.MockHttpServletResponse");
        parentElement.addImportedType("org.springframework.http.HttpStatus");
        parentElement.addImportedType("com.vgosoft.tool.core.JsonUtil");
        parentElement.addImportedType(SERVICE_CODE_ENUM);
        parentElement.addImportedType(entityType);
        parentElement.addImportedType(new FullyQualifiedJavaType(API_CODE_ENUM));
        parentElement.addImportedType("java.nio.charset.StandardCharsets");

        String requestUri = "get(\"/" + Mb3GenUtil.getControllerBaseMappingPath(introspectedTable) + "/{id}\", id)";
        String methodName = "get" + entityType.getShortName();
        Method method = createMethod(methodName, parentElement, "获取记录-服务层返回预期结果");
        method.addException(new FullyQualifiedJavaType("java.lang.Exception"));
        addMethodComment(method, "被调用的service.selectByPrimaryKey()方法有返回值");
        method.addBodyLine("final ServiceResult<{0}> serviceResult = ServiceResult.success({1});",
                entityType.getShortName(),
                entityInstanceVar);
        if (introspectedTable.getRules().isGenerateVoModel()) {
            addMinimalRecordAssertions(method);
        } else {
            method.addBodyLine("{0} {1} = serviceResult.getResult();",
                    entityType.getShortName(), entityType.getShortNameFirstLowCase());
        }
        method.addBodyLine("when({0}.selectByPrimaryKey(id)).thenReturn(serviceResult);",
                mockServiceImpl);
        method.addBodyLine("mockMvc.perform({0}\n" +
                        "                        .accept(MediaType.APPLICATION_JSON))\n" +
                        "                .andExpect(status().isOk())\n" +
                        "                .andExpect(jsonPath(\"$.data.id\").value(\"id\"));",
                requestUri);
        parentElement.addMethod(method);

        methodName = "get" + entityType.getShortName() + "_ReturnsFailure";
        method = createMethod(methodName, parentElement, "获取记录-服务层返回失败结果");
        method.addException(new FullyQualifiedJavaType("java.lang.Exception"));
        addMethodComment(method, "被调用的service.selectByPrimaryKey()方法返回失败");
        method.addBodyLine("final ServiceResult<{0}> serviceResult = ServiceResult.failure(ServiceCodeEnum.FAIL,new Exception(\"err message\"));",
                entityType.getShortName());
        method.addBodyLine("when({0}.selectByPrimaryKey(id)).thenReturn(serviceResult);",
                mockServiceImpl);
        method.addBodyLine("final MockHttpServletResponse response = mockMvc.perform({0}\n" +
                        "                        .accept(MediaType.APPLICATION_JSON))\n" +
                        "                .andReturn()\n" +
                        "                .getResponse();\n" +
                        "        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());\n" +
                        "        ResponseResult<?> responseSimple = JsonUtil.parseObject(response.getContentAsString(StandardCharsets.UTF_8), ResponseResult.class);\n" +
                        "        assertThat(responseSimple.getCode()).isEqualTo(ApiCodeEnum.FAIL_NOT_FOUND.code());",
                requestUri);
        parentElement.addMethod(method);
    }
}
