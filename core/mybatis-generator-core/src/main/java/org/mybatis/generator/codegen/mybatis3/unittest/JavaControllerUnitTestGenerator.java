package org.mybatis.generator.codegen.mybatis3.unittest;

import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.FullyQualifiedTable;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.Plugin;
import org.mybatis.generator.api.dom.java.CompilationUnit;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.config.HtmlGeneratorConfiguration;
import org.mybatis.generator.config.JavaControllerGeneratorConfiguration;
import org.mybatis.generator.config.JavaServiceGeneratorConfiguration;
import org.mybatis.generator.config.JavaServiceImplGeneratorConfiguration;
import org.mybatis.generator.internal.util.JavaBeansUtil;

import java.util.ArrayList;
import java.util.List;

import static org.mybatis.generator.internal.util.messages.Messages.getString;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-05-17 01:27
 * @version 3.0
 */
public class JavaControllerUnitTestGenerator extends AbstractUnitTestGenerator {

    public static final String MOCK_MVC_PROPERTY_NAME = "mockMvc";
    public static final String WEB_APPLICATION_CONTEXT = "webApplicationContext";

    public JavaControllerUnitTestGenerator(String project) {
        super(project);
    }

    @Override
    public List<CompilationUnit> getCompilationUnits() {
        FullyQualifiedTable table = introspectedTable.getFullyQualifiedTable();
        JavaControllerGeneratorConfiguration configuration = introspectedTable.getTableConfiguration().getJavaControllerGeneratorConfiguration();
        JavaServiceGeneratorConfiguration serviceGeneratorConfiguration = introspectedTable.getTableConfiguration().getJavaServiceGeneratorConfiguration();

        progressCallback.startTask(getString("Progress.68", table.toString()));
        Plugin plugins = context.getPlugins();

        FullyQualifiedJavaType entityType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        FullyQualifiedJavaType exampleType = new FullyQualifiedJavaType(introspectedTable.getExampleType());
        FullyQualifiedJavaType serviceImplType = getServiceImplType();
        FullyQualifiedJavaType controllerType = getControllerType();

        String mockServiceImpl = "mock" + serviceImplType.getShortName();
        String entityInstanceVar = JavaBeansUtil.getFirstCharacterLowercase(entityType.getShortName());
        String serviceImplVar = JavaBeansUtil.getFirstCharacterLowercase(serviceImplType.getShortName());

        String viewpath = null;
        String basePath = "";
        if (introspectedTable.getTableConfiguration().getHtmlMapGeneratorConfigurations().size() > 0) {
            HtmlGeneratorConfiguration htmlGeneratorConfiguration = introspectedTable.getTableConfiguration().getHtmlMapGeneratorConfigurations().get(0);
            viewpath = htmlGeneratorConfiguration.getViewPath();
            basePath = htmlGeneratorConfiguration.getTargetPackage();
        }

        FullyQualifiedJavaType topClassType = new FullyQualifiedJavaType(controllerType.getFullyQualifiedName() + "Test");
        TopLevelClass testClazz = new TopLevelClass(topClassType);
        if (configuration.getSpringBootApplicationClass() != null) {
            FullyQualifiedJavaType fullyQualifiedJavaType = new FullyQualifiedJavaType(configuration.getSpringBootApplicationClass());
            testClazz.addAnnotation(VStringUtil.format("@SpringBootTest(classes = {0})", fullyQualifiedJavaType.getShortName() + ".class"));
            testClazz.addImportedType(fullyQualifiedJavaType.getFullyQualifiedName());
        } else testClazz.addAnnotation("@SpringBootTest");
        testClazz.addImportedType("org.springframework.boot.test.context.SpringBootTest");
        //添加属性
        addField(WEB_APPLICATION_CONTEXT,
                "org.springframework.web.context.WebApplicationContext",
                "@Autowired", testClazz);
        testClazz.addImportedType("org.springframework.beans.factory.annotation.Autowired");
        addField(MOCK_MVC_PROPERTY_NAME,
                "org.springframework.test.web.servlet.MockMvc",
                null, testClazz);
        addField("mockSysLogExceptionImpl",
                "com.vgosoft.system.service.impl.SysLogExceptionImpl",
                "@MockBean", testClazz);
        testClazz.addImportedType("org.springframework.boot.test.mock.mockito.MockBean");
        addField("mockSysLogInfoImpl",
                "com.vgosoft.system.service.impl.SysLogInfoImpl",
                "@MockBean", testClazz);
        addField("mockOrganizationMgr",
                "com.vgosoft.core.adapter.organization.OrganizationMgr",
                "@MockBean", testClazz);
        addField("requestBody",
                "java.lang.String",
                null, testClazz);

        addField(mockServiceImpl,
                serviceGeneratorConfiguration.getTargetPackage() + ".I" + entityType.getShortName(),
                "@MockBean", testClazz);
        addField(entityInstanceVar,
                entityType.getFullyQualifiedName(),
                null, testClazz);
        addField("id",
                "java.lang.String",
                null, testClazz);

        testClazz.addImportedType(entityType);
        testClazz.addImportedType(exampleType);

        //增加测试配置方法
        Method setup = new Method("setUp");
        setup.addAnnotation("@BeforeEach");
        testClazz.addImportedType("org.junit.jupiter.api.BeforeEach");
        setup.addBodyLine("mockMvc = MockMvcBuilders.webAppContextSetup({0}).build();", WEB_APPLICATION_CONTEXT);
        //创建测试实例对象实例
        setup.addBodyLine("");
        setup.addBodyLine("{0}= new {1}(0);", entityInstanceVar,entityType.getShortName());
        for (IntrospectedColumn column : introspectedTable.getAllColumns()) {
            setup.addBodyLine("{0}.set{1}({2});", entityInstanceVar,
                    JavaBeansUtil.getFirstCharacterUppercase(column.getJavaProperty()),
                    JavaBeansUtil.getColumnExampleValue(column));
        }
        if (viewpath != null) {
            setup.addBodyLine("{0}.setViewPath(\"{1}\");",entityInstanceVar,viewpath);
        }
        setup.addBodyLine("/* {0} = RandomUtils.randomPojo({1}.class,o->'{'",entityInstanceVar,entityType.getShortName());
        if (viewpath != null) {
            setup.addBodyLine("o.setViewPath(\"{0}\");",viewpath);
        }
        setup.addBodyLine("}); */");
        setup.addBodyLine("");
        setup.addBodyLine("id = {0}.getId();",entityInstanceVar);
        setup.addBodyLine("");
        setup.addBodyLine("requestBody = JSONObject.toJSONString({0});", JavaBeansUtil.getFirstCharacterLowercase(entityType.getShortName()));
        setup.addBodyLine("");
        setup.addBodyLine("final IUser fooUser = new FooUserInfo();\n" +
                "        final IOrganization fooOrganization = new FooOrganization();\n" +
                "        final IDepartment fooDepartment = new FooDepartment();\n" +
                "        when(mockOrganizationMgr.getCurrentUser()).thenReturn(fooUser);\n" +
                "        when(mockOrganizationMgr.getOrganization(any())).thenReturn(fooOrganization);\n" +
                "        when(mockOrganizationMgr.getDepartment(any())).thenReturn(fooDepartment);");
        testClazz.addMethod(setup);

        testClazz.addImportedType("org.springframework.test.web.servlet.setup.MockMvcBuilders");
        testClazz.addImportedType("com.alibaba.fastjson.JSONObject");
        testClazz.addImportedType("com.vgosoft.core.adapter.organization.entity.IUser");
        testClazz.addImportedType("com.vgosoft.core.adapter.organization.entity.IOrganization");
        testClazz.addImportedType("com.vgosoft.core.adapter.organization.entity.IDepartment");
        testClazz.addImportedType("com.vgosoft.organ.foo.FooDepartment");
        testClazz.addImportedType("com.vgosoft.organ.foo.FooOrganization");
        testClazz.addImportedType("com.vgosoft.organ.foo.FooUserInfo");
        testClazz.addStaticImport("org.mockito.Mockito.when");
        testClazz.addStaticImport("org.mockito.ArgumentMatchers.any");
        testClazz.addStaticImport("org.assertj.core.api.Assertions.assertThat");
        testClazz.addStaticImport("com.vgosoft.test.util.ResponseBodyMatchers.responseBody");
        testClazz.addStaticImport("org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*");
        testClazz.addImportedType("com.vgosoft.core.adapter.ServiceResult");
        testClazz.addImportedType("org.springframework.mock.web.MockHttpServletResponse");
        testClazz.addImportedType("com.vgosoft.core.constant.enums.ServiceCodeEnum");
        testClazz.addImportedType("org.springframework.http.HttpStatus");
        testClazz.addImportedType("org.springframework.http.MediaType");
        testClazz.addImportedType("com.vgosoft.core.adapter.web.respone.ResponseSimpleImpl");
        testClazz.addImportedType("com.vgosoft.core.adapter.web.respone.ResponseSimpleList");
        testClazz.addImportedType("java.nio.charset.StandardCharsets");
        testClazz.addImportedType("com.vgosoft.tool.core.VDateUtils");
        testClazz.addImportedType("org.junit.jupiter.api.DisplayName");

        String methodName;
        Method method;
        String requestUri;
        if (viewpath != null) {
            //viewXXX，预期返回测试方法
            requestUri = VStringUtil.format("get(\"/{0}/{1}/view\")", basePath,serviceImplVar) ;
            methodName = "view" + entityType.getShortName();
            method = createMethod(methodName, testClazz,"显示或创建一条记录-服务层返回逾期结果");
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
            method.addBodyLine("assertThat(response.getContentAsString().contains(\"{0}\")).isTrue();", introspectedTable.getRemarks());

            //viewXXX，服务返回失败的测试方法
            methodName = "view" + entityType.getShortName() + "_ReturnsFailure";
            method = createMethod(methodName, testClazz,"显示或创建一条记录-服务层返回失败结果");
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
                    MOCK_MVC_PROPERTY_NAME, requestUri, introspectedTable.getRemarks());
        }

        //getXXX，预期返回测试方法
        requestUri = VStringUtil.format("get(\"/{0}/{1}/'{id}'\", id)", basePath,serviceImplVar) ;
        methodName = "get" + entityType.getShortName();
        method = createMethod(methodName, testClazz,"获取记录-服务层返回预期结果");
        method.addException(new FullyQualifiedJavaType("java.lang.Exception"));
        addMethodComment(method, true, "被调用的service.selectByPrimaryKey()方法有返回值");
        method.addBodyLine("final ServiceResult<{0}> serviceResult = ServiceResult.success({1});",
                entityType.getShortName(),
                entityInstanceVar);
        method.addBodyLine("{0} {1} = {3}Mappings.INSTANCE.to{0}({2});",
                entityType.getShortName()+"VO",
                entityType.getShortNameFirstLowCase()+"VO",
                entityType.getShortNameFirstLowCase(),
                entityType.getShortName());
        method.addBodyLine("when({0}.selectByPrimaryKey(id)).thenReturn(serviceResult);",
                mockServiceImpl);
        method.addBodyLine("mockMvc.perform({0}\n" +
                        "                        .accept(MediaType.APPLICATION_JSON))\n" +
                        "                .andExpect(responseBody()\n" +
                        "                        .containsObjectAsJson({1}, {2}.class, ResponseSimpleImpl.class));",
                requestUri, entityInstanceVar+"VO", entityType.getShortName()+"VO");
        String targetPackage = introspectedTable.getTableConfiguration().getVOGeneratorConfiguration().getTargetPackage();
        testClazz.addImportedType(targetPackage+".vo."+entityType.getShortName()+"VO");
        testClazz.addImportedType(targetPackage+".maps."+entityType.getShortName()+"Mappings");
        //getXXX，服务返回失败的测试方法
        methodName = "get" + entityType.getShortName() + "_ReturnsFailure";
        method = createMethod(methodName, testClazz,"获取记录-服务层返回失败结果");
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
                        "        ResponseSimpleImpl responseSimple = JSONObject.parseObject(response.getContentAsString(StandardCharsets.UTF_8), ResponseSimpleImpl.class);\n" +
                        "        assertThat(responseSimple.getAttributes().get(\"error\")).isEqualTo(ServiceCodeEnum.FAIL.codeName());",
                requestUri);

        //listXXX，预期返回测试方法
        requestUri = VStringUtil.format("get(\"/{0}/{1}\")", basePath,serviceImplVar);
        methodName = "list" + entityType.getShortName();
        method = createMethod(methodName, testClazz,"获取列表-服务层返回预期结果");
        method.addException(new FullyQualifiedJavaType("java.lang.Exception"));
        addMethodComment(method, true, "被调用的service.selectByExample()方法有返回值");
        method.addBodyLine("when({0}.selectByExample(any({1}.class)))\n" +
                        "                .thenReturn(Collections.emptyList());",
                mockServiceImpl,exampleType.getShortName());
        method.addBodyLine("final MockHttpServletResponse response = mockMvc.perform({0}\n" +
                        "                        .accept(MediaType.APPLICATION_JSON))\n" +
                        "                .andReturn().getResponse();\n" +
                        "        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());\n" +
                        "        ResponseSimpleList responseSimpleList = JSONObject.parseObject(response.getContentAsString(StandardCharsets.UTF_8), ResponseSimpleList.class);\n" +
                        "        assertThat(responseSimpleList.getList()).isEqualTo(Collections.emptyList());",
                requestUri);
        testClazz.addImportedType("java.util.Collections");

        //createXXX，预期返回测试方法
        requestUri = VStringUtil.format("post(\"/{0}/{1}\")", basePath,serviceImplVar) ;
        methodName = "create" + entityType.getShortName();
        method = createMethod(methodName, testClazz,"添加数据-服务层返回预期结果");
        method.addException(new FullyQualifiedJavaType("java.lang.Exception"));
        addMethodComment(method, true, "被调用的service.insert()方法有返回值");
        method.addBodyLine("final ServiceResult<{0}> serviceResult = ServiceResult.success({1});\n" +
                        "        when({2}.insert(any({0}.class)))\n" +
                        "                .thenReturn(serviceResult);",
                entityType.getShortName(),
                entityInstanceVar,mockServiceImpl);
        method.addBodyLine("final MockHttpServletResponse response = mockMvc.perform({0}\n" +
                        "                        .content(requestBody).contentType(MediaType.APPLICATION_JSON)\n" +
                        "                        .accept(MediaType.APPLICATION_JSON))\n" +
                        "                .andReturn().getResponse();",
                requestUri);
        method.addBodyLine("assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());\n" +
                "        String contentAsString = response.getContentAsString(StandardCharsets.UTF_8);\n" +
                "        ResponseSimpleImpl responseSimple = JSONObject.parseObject(contentAsString, ResponseSimpleImpl.class);\n" +
                "        assertThat(responseSimple.getAttributes().get(\"id\")).isEqualTo(id);");

        //createXXX，服务返回失败的测试方法
        methodName = "create" + entityType.getShortName() + "_ReturnsFailure";
        method = createMethod(methodName, testClazz,"添加数据-服务层返回失败结果");
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
                mockServiceImpl,entityType.getShortName()+".class",requestUri);
        method.addBodyLine("assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());\n" +
                "        String contentAsString = response.getContentAsString(StandardCharsets.UTF_8);\n" +
                "        ResponseSimpleImpl responseSimple = JSONObject.parseObject(contentAsString, ResponseSimpleImpl.class);\n" +
                "        assertThat(responseSimple.getStatus()).isEqualTo(1);");

        //updateXXX，预期返回测试方法
        requestUri = VStringUtil.format("put(\"/{0}/{1}\")", basePath,serviceImplVar) ;
        methodName = "update" + entityType.getShortName();
        method = createMethod(methodName, testClazz,"更新数据-服务层返回预期结果");
        method.addException(new FullyQualifiedJavaType("java.lang.Exception"));
        addMethodComment(method, true, "被调用的service.updateByPrimaryKeySelective()方法有返回值");
        method.addBodyLine("final ServiceResult<{0}> serviceResult = ServiceResult.success({3});\n" +
                "        when({2}.updateByPrimaryKeySelective(any({1})))\n" +
                "                .thenReturn(serviceResult);",
                entityType.getShortName(),entityType.getShortName()+".class",mockServiceImpl,entityInstanceVar);
        method.addBodyLine("mockMvc.perform({0}\n" +
                "                        .content(requestBody).contentType(MediaType.APPLICATION_JSON)\n" +
                "                        .accept(MediaType.APPLICATION_JSON))\n" +
                "                .andExpect(responseBody()\n" +
                "                        .containsObjectAsJson({1}, {2}.class, ResponseSimpleImpl.class));",
                requestUri,entityInstanceVar,entityType.getShortName());

        //updateXXX，服务返回失败的测试方法
        methodName = "update" + entityType.getShortName() + "_ReturnsFailure";
        method = createMethod(methodName, testClazz,"更新数据-服务层返回失败结果");
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
                mockServiceImpl,entityType.getShortName()+".class",requestUri);
        method.addBodyLine("assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());\n" +
                "        String contentAsString = response.getContentAsString(StandardCharsets.UTF_8);\n" +
                "        ResponseSimpleImpl responseSimple = JSONObject.parseObject(contentAsString, ResponseSimpleImpl.class);\n" +
                "        assertThat(responseSimple.getStatus()).isEqualTo(1);");

        //deleteXXX，预期返回测试方法
        requestUri = VStringUtil.format("delete(\"/{0}/{1}/'{id}'\", id)", basePath,serviceImplVar) ;
        methodName = "delete" + entityType.getShortName();
        method = createMethod(methodName, testClazz,"删除一条记录-服务层返回预期结果");
        method.addException(new FullyQualifiedJavaType("java.lang.Exception"));
        addMethodComment(method, true, "被调用的service.deleteByPrimaryKey(id)方法有返回值");
        method.addBodyLine("int exceptResult = 2;\n" +
                "        when({0}.deleteByPrimaryKey(id)).thenReturn(exceptResult);\n" +
                "        final MockHttpServletResponse response = mockMvc.perform({1}\n" +
                "                        .accept(MediaType.APPLICATION_JSON))\n" +
                "                .andReturn().getResponse();",
                mockServiceImpl,requestUri);
        method.addBodyLine("assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());\n" +
                "        String contentAsString = response.getContentAsString(StandardCharsets.UTF_8);\n" +
                "        ResponseSimpleImpl responseSimple = JSONObject.parseObject(contentAsString, ResponseSimpleImpl.class);\n" +
                "        assertThat(responseSimple.getAttributes().get(\"rows\")).isEqualTo(String.valueOf(exceptResult));");

        //deleteBatchXXX，预期返回测试方法
        requestUri = VStringUtil.format("delete(\"/{0}/{1}\")", basePath,serviceImplVar) ;
        methodName = "deleteBatch" + entityType.getShortName();
        method = createMethod(methodName, testClazz,"批量删除数据-服务层返回预期结果");
        method.addException(new FullyQualifiedJavaType("java.lang.Exception"));
        addMethodComment(method, true, "被调用的service.deleteByExample()方法有返回值");
        method.addBodyLine("String ids = JSONObject.toJSONString(List.of(id));\n" +
                        "        int exceptResult = 2;\n" +
                        "        when({0}.deleteByExample(any({1}.class))).thenReturn(exceptResult);\n" +
                        "        final MockHttpServletResponse response = mockMvc.perform({2}\n" +
                        "                        .content(ids).contentType(MediaType.APPLICATION_JSON)\n" +
                        "                        .accept(MediaType.APPLICATION_JSON))\n" +
                        "                .andReturn().getResponse();",
                mockServiceImpl,exampleType.getShortName(),requestUri);
        method.addBodyLine("assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());\n" +
                "        String contentAsString = response.getContentAsString(StandardCharsets.UTF_8);\n" +
                "        ResponseSimpleImpl responseSimple = JSONObject.parseObject(contentAsString, ResponseSimpleImpl.class);\n" +
                "        assertThat(responseSimple.getAttributes().get(\"rows\")).isEqualTo(String.valueOf(exceptResult));");
        testClazz.addImportedType(FullyQualifiedJavaType.getNewListInstance());

        List<CompilationUnit> answer = new ArrayList<>();
        if (plugins.serviceImplGenerated(testClazz, introspectedTable)) {
            answer.add(testClazz);
        }
        return answer;
    }

    /**
     * 内部方法
     * 获得Service实现类描述对象
     */
    private FullyQualifiedJavaType getServiceImplType() {
        JavaServiceImplGeneratorConfiguration javaServiceImplGeneratorConfiguration = introspectedTable.getTableConfiguration().getJavaServiceImplGeneratorConfiguration();
        String clazzName = JavaBeansUtil.getFirstCharacterUppercase(introspectedTable.getControllerBeanName());
        return new FullyQualifiedJavaType(javaServiceImplGeneratorConfiguration.getTargetPackage() + "." + clazzName);
    }

    /**
     * 内部方法
     * 获得Controller实现类描述对象
     */
    private FullyQualifiedJavaType getControllerType() {
        StringBuilder sb = new StringBuilder();
        FullyQualifiedJavaType entityType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        JavaControllerGeneratorConfiguration javaControllerGeneratorConfiguration = introspectedTable.getTableConfiguration().getJavaControllerGeneratorConfiguration();
        sb.append(javaControllerGeneratorConfiguration.getTargetPackage());
        sb.append(".").append(entityType.getShortName()).append("Controller");
        return new FullyQualifiedJavaType(sb.toString());
    }
}
