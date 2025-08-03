package org.mybatis.generator.codegen.mybatis3.unittest;

import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.FullyQualifiedTable;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.Plugin;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.mybatis3.unittest.elements.*;
import org.mybatis.generator.config.*;
import org.mybatis.generator.internal.util.JavaBeansUtil;

import java.sql.Types;
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
        TableConfiguration tc = introspectedTable.getTableConfiguration();
        JavaControllerGeneratorConfiguration configuration = tc.getJavaControllerGeneratorConfiguration();
        JavaServiceGeneratorConfiguration serviceGeneratorConfiguration = tc.getJavaServiceGeneratorConfiguration();
        progressCallback.startTask(getString("Progress.68", table.toString()));
        Plugin plugins = context.getPlugins();

        FullyQualifiedJavaType entityType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        FullyQualifiedJavaType serviceImplType = getServiceImplType();
        FullyQualifiedJavaType controllerType = getControllerType();

        String mockServiceImpl = "mock" + serviceImplType.getShortName();
        String entityInstanceVar = JavaBeansUtil.getFirstCharacterLowercase(entityType.getShortName());
        String viewpath = null;
        if (!introspectedTable.getTableConfiguration().getHtmlMapGeneratorConfigurations().isEmpty()) {
            HtmlGeneratorConfiguration htmlGeneratorConfiguration = introspectedTable.getTableConfiguration().getHtmlMapGeneratorConfigurations().get(0);
            viewpath = htmlGeneratorConfiguration.getViewPath();
        }

        FullyQualifiedJavaType topClassType = new FullyQualifiedJavaType(controllerType.getFullyQualifiedName() + "Test");
        TopLevelClass testClazz = new TopLevelClass(topClassType);
        testClazz.setVisibility(JavaVisibility.PUBLIC);
        if (configuration.getSpringBootApplicationClass() != null) {
            FullyQualifiedJavaType fullyQualifiedJavaType = new FullyQualifiedJavaType(configuration.getSpringBootApplicationClass());
            testClazz.addAnnotation(VStringUtil.format("@SpringBootTest(classes = {0})", fullyQualifiedJavaType.getShortName() + ".class"));
            testClazz.addImportedType(fullyQualifiedJavaType.getFullyQualifiedName());
        } else testClazz.addAnnotation("@SpringBootTest");
        testClazz.addImportedType("org.springframework.boot.test.context.SpringBootTest");

        //添加属性
        testClazz.addImportedType("org.springframework.beans.factory.annotation.Autowired");
        testClazz.addImportedType("org.springframework.boot.test.mock.mockito.MockBean");
        addField(WEB_APPLICATION_CONTEXT,
                "org.springframework.web.context.WebApplicationContext",
                "@Autowired", testClazz);
        if (introspectedTable.getRules().isGenerateVo()) {
            String voTargetPackage = tc.getJavaModelGeneratorConfiguration().getBaseTargetPackage()+".pojo";
            FullyQualifiedJavaType entityMappings = new FullyQualifiedJavaType(String.join(".", voTargetPackage,"maps",entityType.getShortName()+"Mappings"));
            addField("mappings",
                    entityMappings.getFullyQualifiedName(),
                    "@Autowired", testClazz);
            testClazz.addImportedType(entityMappings);
        }

        addField(MOCK_MVC_PROPERTY_NAME,
                "org.springframework.test.web.servlet.MockMvc",
                null, testClazz);
        addField("mockSysLogExceptionImpl",
                "com.vgosoft.system.service.impl.SysLogExceptionImpl",
                "@MockBean", testClazz);
        addField("mockSysLogInfoImpl",
                "com.vgosoft.system.service.impl.SysLogInfoImpl",
                "@MockBean", testClazz);
        addField("mockOrganizationMgr",
                "com.vgosoft.core.adapter.organization.OrganizationMgr<FooUserInfo,?,?,FooDepartment,FooOrganization>",
                "@MockBean", testClazz);
        addField(mockServiceImpl,
                serviceGeneratorConfiguration.getTargetPackage() + ".I" + entityType.getShortName(),
                "@MockBean", testClazz);
        addField(entityInstanceVar,
                entityType.getFullyQualifiedName(),
                null, testClazz);
        addField("id",
                "java.lang.String",
                null, testClazz);
        addField("requestBody",
                "java.lang.String",
                null, testClazz);
        //增加测试配置方法
        testClazz.addImportedType("org.junit.jupiter.api.BeforeEach");
        testClazz.addImportedType("org.springframework.test.web.servlet.setup.MockMvcBuilders");
        Method setup = new Method("setUp");
        setup.addAnnotation("@BeforeEach");
        setup.addBodyLine("mockMvc = MockMvcBuilders.webAppContextSetup({0}).build();", WEB_APPLICATION_CONTEXT);
        //创建测试实例对象实例
        setup.addBodyLine("");
        setup.addBodyLine("{0}= new {1}(0);", entityInstanceVar, entityType.getShortName());
        for (IntrospectedColumn column : introspectedTable.getAllColumns()) {
            if (column.getJdbcType() == Types.DECIMAL) {
                testClazz.addImportedType("java.math.BigDecimal");
            }
            if (column.isJava8TimeColumn() || column.isJDBCDateColumn() || column.isJDBCTimeColumn() || column.isJDBCTimeStampColumn()) {
                testClazz.addImportedType("com.vgosoft.tool.core.VDateUtils");
            }
            setup.addBodyLine("{0}.set{1}({2});", entityInstanceVar,
                    JavaBeansUtil.getFirstCharacterUppercase(column.getJavaProperty()),
                    JavaBeansUtil.getColumnExampleValue(column));
        }
        if (viewpath != null) {
            setup.addBodyLine("{0}.setViewPath(\"{1}\");", entityInstanceVar, viewpath);
            setup.addBodyLine("//{0} = RandomUtils.randomPojo({1}.class,o->'{'o.setViewPath(\"{2}\");'}');", entityInstanceVar,entityType.getShortName(), viewpath);
        }else{
            setup.addBodyLine("//{0} = RandomUtils.randomPojo();", entityInstanceVar,entityType.getShortName());
        }

        setup.addBodyLine("");
        setup.addBodyLine("id = {0}.getId();", entityInstanceVar);
        setup.addBodyLine("");
        setup.addBodyLine("requestBody = JsonUtil.toJsonString({0});", JavaBeansUtil.getFirstCharacterLowercase(entityType.getShortName()));
        setup.addBodyLine("");
        //组织机构相关mock
        /*testClazz.addImportedType("com.vgosoft.core.adapter.organization.entity.IUser");
        testClazz.addImportedType("com.vgosoft.core.adapter.organization.entity.IOrganization");
        testClazz.addImportedType("com.vgosoft.core.adapter.organization.entity.IDepartment");*/
        testClazz.addImportedType("com.vgosoft.organ.foo.FooDepartment");
        testClazz.addImportedType("com.vgosoft.organ.foo.FooOrganization");
        testClazz.addImportedType("com.vgosoft.organ.foo.FooUserInfo");
        setup.addBodyLine("final FooUserInfo fooUser = new FooUserInfo();\n" +
                "        final FooOrganization fooOrganization = new FooOrganization();\n" +
                "        final FooDepartment fooDepartment = new FooDepartment();\n" +
                "        when(mockOrganizationMgr.getCurrentUser()).thenReturn(fooUser);\n" +
                "        when(mockOrganizationMgr.getOrganization(any())).thenReturn(fooOrganization);\n" +
                "        when(mockOrganizationMgr.getDepartment(any())).thenReturn(fooDepartment);");
        testClazz.addMethod(setup);

        //各方法通用引入内容
        testClazz.addStaticImport("org.mockito.Mockito.when");
        testClazz.addStaticImport("org.mockito.ArgumentMatchers.any");
        testClazz.addStaticImport("org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*");
        testClazz.addImportedType("org.junit.jupiter.api.Test");
        testClazz.addImportedType("org.junit.jupiter.api.DisplayName");
        testClazz.addImportedType("com.vgosoft.tool.core.JsonUtil");
        testClazz.addImportedType("org.springframework.http.MediaType");

        if (viewpath != null) {
            /* 1、viewXXX，预期返回测试方法 2、viewXXX，服务返回失败的测试方法 */
            addControllerViewElement(testClazz);
        }

        /* 1、getXXX，预期返回测试方法  2、getXXX，服务返回失败的测试方法 */
        addControllerGetElement(testClazz);

        /*listXXX，预期返回测试方法*/
        addControllerListElement(testClazz);

        /*1、createXXX，预期返回测试方法  2、createXXX，服务返回失败的测试方法*/
        addControllerCreateElement(testClazz);

        /*1、updateXXX，预期返回测试方法   2、updateXXX，服务返回失败的测试方法*/
        addControllerUpdateElement(testClazz);

        /*1、deleteXXX，预期返回测试方法   2、deleteBatchXXX，预期返回测试方法*/
        addControllerDeleteElement(testClazz);

        List<CompilationUnit> answer = new ArrayList<>();
        if (plugins.serviceImplGenerated(testClazz, introspectedTable)) {
            answer.add(testClazz);
        }
        return answer;
    }

    private void addControllerViewElement(TopLevelClass testClazz) {
        AbstractUnitTestElementGenerator elementGenerator = new ControllerViewElementGenerator();
        initializeAndExecuteGenerator(elementGenerator, testClazz);
    }

    private void addControllerGetElement(TopLevelClass testClazz) {
        AbstractUnitTestElementGenerator elementGenerator = new ControllerGetElementGenerator();
        initializeAndExecuteGenerator(elementGenerator, testClazz);

    }

    private void addControllerListElement(TopLevelClass testClazz) {
        AbstractUnitTestElementGenerator elementGenerator = new ControllerListElementGenerator();
        initializeAndExecuteGenerator(elementGenerator, testClazz);
    }

    private void addControllerCreateElement(TopLevelClass testClazz) {
        AbstractUnitTestElementGenerator elementGenerator = new ControllerCreateElementGenerator();
        initializeAndExecuteGenerator(elementGenerator, testClazz);
    }

    private void addControllerUpdateElement(TopLevelClass testClazz) {
        AbstractUnitTestElementGenerator elementGenerator = new ControllerUpdateElementGenerator();
        initializeAndExecuteGenerator(elementGenerator, testClazz);
    }

    private void addControllerDeleteElement(TopLevelClass testClazz) {
        AbstractUnitTestElementGenerator elementGenerator = new ControllerDeleteElementGenerator();
        initializeAndExecuteGenerator(elementGenerator, testClazz);
    }

    protected void initializeAndExecuteGenerator(
            AbstractUnitTestElementGenerator elementGenerator,
            TopLevelClass parentElement) {
        elementGenerator.setContext(context);
        elementGenerator.setIntrospectedTable(introspectedTable);
        elementGenerator.setProgressCallback(progressCallback);
        elementGenerator.setWarnings(warnings);
        elementGenerator.initGenerator();
        elementGenerator.addElements(parentElement);
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
