package org.mybatis.generator.codegen.mybatis3.unittest.elements;

import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.internal.util.Mb3GenUtil;
import org.mybatis.generator.codegen.mybatis3.unittest.AbstractUnitTestElementGenerator;

import static org.mybatis.generator.custom.ConstantsUtil.RESPONSE_RESULT;

public class ControllerListElementGenerator extends AbstractUnitTestElementGenerator {

    public ControllerListElementGenerator() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        parentElement.addStaticImport("org.assertj.core.api.Assertions.assertThat");
        parentElement.addImportedType("org.springframework.mock.web.MockHttpServletResponse");
        parentElement.addImportedType("org.springframework.http.HttpStatus");
        parentElement.addImportedType(RESPONSE_RESULT);
        parentElement.addImportedType("java.nio.charset.StandardCharsets");
        parentElement.addImportedType("java.util.Collections");
        parentElement.addImportedType(exampleType);

        String requestUri = VStringUtil.format("get(\"/{0}\")", Mb3GenUtil.getControllerBaseMappingPath(introspectedTable));
        String methodName = "list" + entityType.getShortName();
        Method method = createMethod(methodName, parentElement, "获取列表-服务层返回预期结果");
        method.addException(new FullyQualifiedJavaType("java.lang.Exception"));
        addMethodComment(method, true, "被调用的service.selectByExample()方法有返回值");
        if (introspectedTable.getRules().generateRelationWithSubSelected()) {
            method.addBodyLine("when({0}.selectByExampleWithRelation(any({1}.class)))\n" +
                            "                .thenReturn(Collections.emptyList());",
                    mockServiceImpl, exampleType.getShortName());
        }
        method.addBodyLine("when({0}.selectByExample(any({1}.class)))\n" +
                        "                .thenReturn(ServiceResult.success(Collections.emptyList()));",
                mockServiceImpl, exampleType.getShortName());
        method.addBodyLine("final MockHttpServletResponse response = mockMvc.perform({0}\n" +
                        "                        .accept(MediaType.APPLICATION_JSON))\n" +
                        "                .andReturn().getResponse();\n" +
                        "        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());",
                requestUri);
        parentElement.addMethod(method);
    }
}
