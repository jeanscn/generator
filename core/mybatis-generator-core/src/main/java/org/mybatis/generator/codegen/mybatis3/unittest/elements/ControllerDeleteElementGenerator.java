package org.mybatis.generator.codegen.mybatis3.unittest.elements;

import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.internal.util.Mb3GenUtil;
import org.mybatis.generator.codegen.mybatis3.unittest.AbstractUnitTestElementGenerator;

import static com.vgosoft.tool.core.VStringUtil.toHyphenCase;
import static org.mybatis.generator.custom.ConstantsUtil.RESPONSE_RESULT;

public class ControllerDeleteElementGenerator extends AbstractUnitTestElementGenerator {

    public ControllerDeleteElementGenerator() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        parentElement.addStaticImport("com.vgosoft.test.util.ResponseBodyMatchers.responseBody");
        parentElement.addImportedType(RESPONSE_RESULT);
        parentElement.addImportedType("java.util.List");
        parentElement.addImportedType(exampleType);
        parentElement.addImportedType("com.alibaba.fastjson2.JSONObject");

        //deleteXXX，预期返回测试方法
        String requestUri = VStringUtil.format("delete(\"/{0}/'{id}'\", id)", Mb3GenUtil.getControllerBaseMappingPath(introspectedTable));
        String methodName = "delete" + entityType.getShortName();
        Method method = createMethod(methodName, parentElement, "删除一条记录-服务层返回预期结果");
        method.addException(new FullyQualifiedJavaType("java.lang.Exception"));
        addMethodComment(method, true, "被调用的service.deleteByPrimaryKey(id)方法有返回值");
        method.addBodyLine("int exceptResult = 2;\n" +
                        "        when({0}.deleteByPrimaryKey(id)).thenReturn(ServiceResult.success(exceptResult));\n" +
                        "        mockMvc.perform({1}\n" +
                        "                        .accept(MediaType.APPLICATION_JSON))",
                mockServiceImpl, requestUri);
        method.addBodyLine("                .andExpect(responseBody()\n" +
                "                        .containsObjectAsJson((long) exceptResult, Long.class, ResponseResult.class));");

        parentElement.addMethod(method);

        //deleteBatchXXX，预期返回测试方法
        requestUri = VStringUtil.format("delete(\"/{0}/{1}\")", basePath, toHyphenCase(serviceBeanName));
        methodName = "deleteBatch" + entityType.getShortName();
        method = createMethod(methodName, parentElement, "批量删除数据-服务层返回预期结果");
        method.addException(new FullyQualifiedJavaType("java.lang.Exception"));
        addMethodComment(method, true, "被调用的service.deleteByExample()方法有返回值");
        if (context.getJdkVersion()>8) {
            method.addBodyLine("String ids = JSONObject.toJSONString(List.of(id));");
        }else{
            method.addBodyLine("String ids = JSONObject.toJSONString(Collections.singleton(id));");
            parentElement.addImportedType("java.util.Collections");
        }
        method.addBodyLine("int exceptResult = 2;\n" +
                        "        when({0}.deleteByExample(any({1}.class))).thenReturn(ServiceResult.success(exceptResult));\n" +
                        "        mockMvc.perform({2}\n" +
                        "                        .content(ids).contentType(MediaType.APPLICATION_JSON)\n" +
                        "                        .accept(MediaType.APPLICATION_JSON))\n",
                mockServiceImpl, exampleType.getShortName(), requestUri);
        method.addBodyLine(".andExpect(responseBody()\n" +
                "                        .containsObjectAsJson(exceptResult, Integer.class, ResponseResult.class));");
        parentElement.addImportedType(FullyQualifiedJavaType.getNewListInstance());

        parentElement.addMethod(method);
    }
}
