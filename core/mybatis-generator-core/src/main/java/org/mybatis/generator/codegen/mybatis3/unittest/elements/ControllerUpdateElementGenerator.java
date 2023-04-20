package org.mybatis.generator.codegen.mybatis3.unittest.elements;

import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.internal.util.Mb3GenUtil;
import org.mybatis.generator.codegen.mybatis3.unittest.AbstractUnitTestElementGenerator;

import static org.mybatis.generator.custom.ConstantsUtil.RESPONSE_RESULT;

public class ControllerUpdateElementGenerator extends AbstractUnitTestElementGenerator {

    public ControllerUpdateElementGenerator() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        parentElement.addStaticImport("com.vgosoft.test.util.ResponseBodyMatchers.responseBody");
        parentElement.addStaticImport("org.assertj.core.api.Assertions.assertThat");
        parentElement.addImportedType(RESPONSE_RESULT);
        parentElement.addImportedType(RESPONSE_RESULT);
        parentElement.addImportedType("org.springframework.mock.web.MockHttpServletResponse");
        parentElement.addImportedType("org.springframework.http.HttpStatus");
        parentElement.addImportedType("com.vgosoft.core.constant.enums.ServiceCodeEnum");
        if (isGenerateVOModel) {
            parentElement.addImportedType(entityVoType);
            parentElement.addImportedType(entityMappings);
        }else{
            parentElement.addImportedType(entityType);
        }
        parentElement.addImportedType("java.nio.charset.StandardCharsets");

        //updateXXX，预期返回测试方法
        String requestUri = VStringUtil.format("put(\"/{0}\")", Mb3GenUtil.getControllerBaseMappingPath(introspectedTable));
        String methodName = "update" + entityType.getShortName();
        Method method = createMethod(methodName, parentElement, "更新数据-服务层返回预期结果");
        method.addException(new FullyQualifiedJavaType("java.lang.Exception"));
        addMethodComment(method, true, "被调用的service.updateByPrimaryKeySelective()方法有返回值");
        method.addBodyLine("final ServiceResult<{0}> serviceResult = ServiceResult.success({3});\n" +
                        "        when({2}.updateByPrimaryKeySelective(any({1})))\n" +
                        "                .thenReturn(serviceResult);",
                entityType.getShortName(), entityType.getShortName() + ".class", mockServiceImpl, entityInstanceVar);
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

        //updateXXX，服务返回失败的测试方法
        methodName = "update" + entityType.getShortName() + "_ReturnsFailure";
        method = createMethod(methodName, parentElement, "更新数据-服务层返回失败结果");
        method.addException(new FullyQualifiedJavaType("java.lang.Exception"));
        addMethodComment(method, true, "被调用的service.updateByPrimaryKeySelective()方法返回失败");
        method.addBodyLine("final ServiceResult<{0}> serviceResult = ServiceResult.failure(ServiceCodeEnum.FAIL,\n" +
                        "                \"error message\");",
                entityType.getShortName());
        method.addBodyLine(" when({0}.updateByPrimaryKeySelective(any({1}))).thenReturn(serviceResult);\n" +
                        "        final MockHttpServletResponse response = mockMvc.perform({2}\n" +
                        "                        .content(requestBody).contentType(MediaType.APPLICATION_JSON)\n" +
                        "                        .accept(MediaType.APPLICATION_JSON))\n" +
                        "                .andReturn().getResponse();",
                mockServiceImpl, entityType.getShortName() + ".class", requestUri);
        method.addBodyLine("assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());\n" +
                "        String contentAsString = response.getContentAsString(StandardCharsets.UTF_8);\n" +
                "        ResponseResult<?> responseSimple = JSONObject.parseObject(contentAsString, ResponseResult.class);\n" +
                "        assertThat(responseSimple.getStatus()).isEqualTo(1);");

        parentElement.addMethod(method);
    }
}
