package org.mybatis.generator.codegen.mybatis3.unittest.elements;

import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.unittest.AbstractUnitTestElementGenerator;
import org.mybatis.generator.internal.util.Mb3GenUtil;

import static org.mybatis.generator.custom.ConstantsUtil.SERVICE_CODE_ENUM;
import static org.mybatis.generator.custom.ConstantsUtil.SERVICE_RESULT;

public class ControllerViewElementGenerator extends AbstractUnitTestElementGenerator {

    public ControllerViewElementGenerator() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        parentElement.addStaticImport("org.assertj.core.api.Assertions.assertThat");
        parentElement.addImportedType(SERVICE_RESULT);
        parentElement.addImportedType("org.springframework.mock.web.MockHttpServletResponse");
        parentElement.addImportedType("org.springframework.http.HttpStatus");
        parentElement.addImportedType(SERVICE_CODE_ENUM);

        parentElement.addImportedType(entityType);

        if (isGenerateVOModel) {
            parentElement.addImportedType(entityVoType);
            parentElement.addImportedType(entityMappings);
        }

        //viewXXX，预期返回测试方法
        String requestUri = VStringUtil.format("get(\"/{0}/view\")", Mb3GenUtil.getControllerBaseMappingPath(introspectedTable));
        String methodName = "view" + entityType.getShortName();
        Method method = createMethod(methodName, parentElement, "显示或创建一条记录-服务层返回逾期结果");
        method.addException(new FullyQualifiedJavaType("java.lang.Exception"));
        addMethodComment(method, true, "被调用的service.selectByPrimaryKey()方法有返回值");
        method.addBodyLine("final ServiceResult<{0}> serviceResult = ServiceResult.success({1});",
                entityType.getShortName(),
                entityInstanceVar);
        method.addBodyLine("when({0}.selectByPrimaryKey(id)).thenReturn(serviceResult);",
                mockServiceImpl);
        method.addBodyLine(" final MockHttpServletResponse response = {0}.perform({1}\n" +
                "                        .param(\"id\", id)\n" +
                "                        .param(\"viewStatus\", \"0\")\n" +
                "                        .accept(MediaType.TEXT_HTML))\n" +
                "                .andReturn().getResponse();", MOCK_MVC_PROPERTY_NAME, requestUri);
        method.addBodyLine("assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());");
        method.addBodyLine("assertThat(response.getContentAsString().contains(\"{0}\")).isTrue();", introspectedTable.getRemarks(true));
        parentElement.addMethod(method);

        //viewXXX，服务返回失败的测试方法
        methodName = "view" + entityType.getShortName() + "_ReturnsFailure";
        method = createMethod(methodName, parentElement, "显示或创建一条记录-服务层返回失败结果");
        method.addException(new FullyQualifiedJavaType("java.lang.Exception"));
        addMethodComment(method, true, "被调用的service.selectByPrimaryKey()方法返回失败");
        method.addBodyLine("final ServiceResult<{0}> serviceResult = ServiceResult.failure(ServiceCodeEnum.FAIL,new Exception(\"err message\"));",
                entityType.getShortName());
        method.addBodyLine("when({0}.selectByPrimaryKey(id)).thenReturn(serviceResult);",
                mockServiceImpl);
        method.addBodyLine("final MockHttpServletResponse response = {0}.perform({1}\n" +
                        "                        .param(\"id\", id)\n" +
                        "                        .param(\"viewStatus\", \"1\")\n" +
                        "                        .accept(MediaType.TEXT_HTML))\n" +
                        "                .andReturn().getResponse();\n" +
                        "        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());\n" +
                        "        assertThat(response.getContentAsString().contains(\"{2}\")).isTrue();",
                MOCK_MVC_PROPERTY_NAME, requestUri, introspectedTable.getRemarks(true));
        parentElement.addMethod(method);
    }
}
