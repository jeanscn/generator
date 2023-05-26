package org.mybatis.generator.codegen.mybatis3.unittest.elements;

import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.unittest.AbstractUnitTestElementGenerator;
import org.mybatis.generator.internal.util.Mb3GenUtil;

import static org.mybatis.generator.custom.ConstantsUtil.*;

public class ControllerCreateElementGenerator extends AbstractUnitTestElementGenerator {

    public ControllerCreateElementGenerator() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        parentElement.addStaticImport("com.vgosoft.test.util.ResponseBodyMatchers.responseBody");
        parentElement.addStaticImport("org.assertj.core.api.Assertions.assertThat");
        parentElement.addImportedType(RESPONSE_RESULT);
        parentElement.addImportedType(SERVICE_RESULT);
        parentElement.addImportedType("org.springframework.mock.web.MockHttpServletResponse");
        parentElement.addImportedType("org.springframework.http.HttpStatus");
        parentElement.addImportedType(SERVICE_CODE_ENUM);
        parentElement.addImportedType("java.nio.charset.StandardCharsets");
        if (isGenerateVOModel) {
            parentElement.addImportedType(entityVoType);
            parentElement.addImportedType(entityMappings);
        }else{
            parentElement.addImportedType(entityType);
        }

        //createXXX，预期返回测试方法
        String requestUri = VStringUtil.format("post(\"/{0}\")", Mb3GenUtil.getControllerBaseMappingPath(introspectedTable));
        String methodName = "create" + entityType.getShortName();
        Method method = createMethod(methodName, parentElement, "添加数据-服务层返回预期结果");
        method.addException(new FullyQualifiedJavaType("java.lang.Exception"));
        addMethodComment(method, true, "被调用的service.insert()方法有返回值");

        if (introspectedTable.getRules().generateInsertOrUpdate()) {
            method.addBodyLine("ServiceResult<{0}> serviceResult;",entityType.getShortName());
            method.addBodyLine("serviceResult = ServiceResult.success({1});\n" +
                            "        when({2}.insertOrUpdate(any({0}.class)))\n" +
                            "                .thenReturn(serviceResult);",
                    entityType.getShortName(),
                    entityInstanceVar, mockServiceImpl);
            method.addBodyLine("serviceResult = ServiceResult.success({1});\n" +
                            "        when({2}.insert(any({0}.class)))\n" +
                            "                .thenReturn(serviceResult);",
                    entityType.getShortName(),
                    entityInstanceVar, mockServiceImpl);
        }else{
            method.addBodyLine("final ServiceResult<{0}> serviceResult = ServiceResult.success({1});\n" +
                            "        when({2}.insert(any({0}.class)))\n" +
                            "                .thenReturn(serviceResult);",
                    entityType.getShortName(),
                    entityInstanceVar, mockServiceImpl);
        }
        if (isGenerateVOModel) {
            method.addBodyLine("{0} {1} = mappings.to{0}({2});", entityVoType.getShortName(), entityVoInstanceVar, entityInstanceVar);
        }
        method.addBodyLine("mockMvc.perform({0}\n" +
                "                        .content(requestBody).contentType(MediaType.APPLICATION_JSON)\n" +
                "                        .accept(MediaType.APPLICATION_JSON))", requestUri);
        method.addBodyLine(".andExpect(responseBody()\n" +
                        "                        .containsObjectAsJson({0}, {1}.class, ResponseResult.class));",
                isGenerateVOModel ? entityVoInstanceVar : entityInstanceVar,
                isGenerateVOModel ? entityVoType.getShortName() : entityType.getShortName());
        parentElement.addMethod(method);

        //createXXX，服务返回失败的测试方法
        methodName = "create" + entityType.getShortName() + "_ReturnsFailure";
        method = createMethod(methodName, parentElement, "添加数据-服务层返回失败结果");
        method.addException(new FullyQualifiedJavaType("java.lang.Exception"));
        addMethodComment(method, true, "被调用的service.insert()方法返回失败");
        method.addBodyLine("final ServiceResult<{0}> serviceResult = ServiceResult.failure(ServiceCodeEnum.FAIL,\n" +
                        "                \"error message\");",
                entityType.getShortName());
        method.addBodyLine("when({0}.insert(any({1}))).thenReturn(serviceResult);\n" +
                        "        final MockHttpServletResponse response = mockMvc.perform({2}\n" +
                        "                        .content(requestBody).contentType(MediaType.APPLICATION_JSON)\n" +
                        "                        .accept(MediaType.APPLICATION_JSON))\n" +
                        "                .andReturn().getResponse();",
                mockServiceImpl, entityType.getShortName() + ".class", requestUri);
        method.addBodyLine("assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());\n" +
                "        String contentAsString = response.getContentAsString(StandardCharsets.UTF_8);\n" +
                "        ResponseResult<?> responseResult = JSONObject.parseObject(contentAsString, ResponseResult.class);\n" +
                "        assertThat(responseResult.getStatus()).isEqualTo(1);");
        parentElement.addMethod(method);
    }
}
