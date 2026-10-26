package org.mybatis.generator.codegen.mybatis3.unittest;

import org.mybatis.generator.api.FullyQualifiedTable;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.Plugin;
import org.mybatis.generator.api.dom.java.CompilationUnit;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.htmlmapper.GenerateUtils;
import org.mybatis.generator.codegen.mybatis3.unittest.elements.ControllerCreateElementGenerator;
import org.mybatis.generator.codegen.mybatis3.unittest.elements.ControllerDeleteElementGenerator;
import org.mybatis.generator.codegen.mybatis3.unittest.elements.ControllerGetElementGenerator;
import org.mybatis.generator.codegen.mybatis3.unittest.elements.ControllerListElementGenerator;
import org.mybatis.generator.codegen.mybatis3.unittest.elements.ControllerUpdateElementGenerator;
import org.mybatis.generator.codegen.mybatis3.unittest.elements.ControllerViewElementGenerator;
import org.mybatis.generator.config.JavaControllerGeneratorConfiguration;
import org.mybatis.generator.config.JavaServiceImplGeneratorConfiguration;
import org.mybatis.generator.config.TableConfiguration;
import org.mybatis.generator.internal.util.JavaBeansUtil;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;
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
        progressCallback.startTask(getString("Progress.68", table.toString()));
        Plugin plugins = context.getPlugins();

        FullyQualifiedJavaType entityType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        FullyQualifiedJavaType serviceImplType = getServiceImplType();
        FullyQualifiedJavaType controllerType = getControllerType();

        String mockServiceImpl = "mock" + serviceImplType.getShortName();
        String entityInstanceVar = JavaBeansUtil.getFirstCharacterLowercase(entityType.getShortName());
        boolean springBootTest = configuration != null && stringHasValue(configuration.getSpringBootApplicationClass());
        boolean workflowInstance = GenerateUtils.isWorkflowInstance(introspectedTable);
        boolean hasFileAttachment = tc.getHtmlMapGeneratorConfigurations().stream()
                .anyMatch(html -> html.getHtmlFileAttachmentConfiguration().stream().anyMatch(htmlAttachment -> htmlAttachment.isGenerate()));
        boolean hasHideListBin = introspectedTable.getRules().isGenerateHideListBin();
        String viewpath = null;
        if (!introspectedTable.getTableConfiguration().getHtmlMapGeneratorConfigurations().isEmpty()) {
            viewpath = introspectedTable.getTableConfiguration().getHtmlMapGeneratorConfigurations().get(0).getViewPath();
        }

        FullyQualifiedJavaType topClassType = new FullyQualifiedJavaType(controllerType.getFullyQualifiedName() + "Test");
        TopLevelClass testClazz = new TopLevelClass(topClassType);
        testClazz.setVisibility(JavaVisibility.PUBLIC);
        if (springBootTest) {
            testClazz.addImportedType(new FullyQualifiedJavaType(configuration.getSpringBootApplicationClass()));
            testClazz.addAnnotation("@ExtendWith(SpringExtension.class)");
            testClazz.addAnnotation("@SpringBootTest(classes = " + new FullyQualifiedJavaType(configuration.getSpringBootApplicationClass()).getShortName() + ".class)");
            testClazz.addImportedType("org.junit.jupiter.api.extension.ExtendWith");
            testClazz.addImportedType("org.springframework.boot.test.context.SpringBootTest");
            testClazz.addImportedType("org.springframework.test.context.junit.jupiter.SpringExtension");
            testClazz.addImportedType("org.springframework.test.context.bean.override.mockito.MockitoBean");
        } else {
            testClazz.addAnnotation("@ExtendWith(MockitoExtension.class)");
            testClazz.addImportedType("org.junit.jupiter.api.extension.ExtendWith");
            testClazz.addImportedType("org.mockito.junit.jupiter.MockitoExtension");
            testClazz.addImportedType("org.mockito.Mock");
        }

        testClazz.addImportedType("org.springframework.beans.factory.annotation.Autowired");
        if (springBootTest && hasJava8DateColumn()) {
            testClazz.addImportedType("java.time.format.DateTimeFormatter");
            for (IntrospectedColumn column : introspectedTable.getAllColumns()) {
                if (column.isJavaLocalDateTimeColumn()) {
                    addDateFormatterField(testClazz, "DATETIME_FORMATTER", "yyyy-MM-dd HH:mm:ss");
                }
                if (column.isJavaLocalDateColumn()) {
                    addDateFormatterField(testClazz, "DATE_FORMATTER", "yyyy-MM-dd");
                }
                if (column.isJavaLocalTimeColumn()) {
                    addDateFormatterField(testClazz, "TIME_FORMATTER", "HH:mm:ss");
                }
            }

        }
        addField(WEB_APPLICATION_CONTEXT, "org.springframework.web.context.WebApplicationContext", "@Autowired", testClazz);
        addField(MOCK_MVC_PROPERTY_NAME, "org.springframework.test.web.servlet.MockMvc", null, testClazz);

        if (springBootTest) {
            addSpringBootMockFields(testClazz, serviceImplType, entityType, workflowInstance, hasFileAttachment, hasHideListBin);
        } else {
            addLegacyMockFields(testClazz, serviceImplType, entityType);
        }

        addSetupMethod(testClazz, springBootTest, entityType, entityInstanceVar, viewpath);

        testClazz.addStaticImport("org.mockito.Mockito.when");
        testClazz.addStaticImport("org.mockito.ArgumentMatchers.any");
        testClazz.addStaticImport("org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*");
        testClazz.addImportedType("org.junit.jupiter.api.Test");
        testClazz.addImportedType("com.vgosoft.tool.core.JsonUtil");
        testClazz.addImportedType("org.springframework.http.MediaType");

        if (viewpath != null) {
            addControllerViewElement(testClazz);
        }
        addControllerGetElement(testClazz);
        addControllerListElement(testClazz);
        addControllerCreateElement(testClazz);
        addControllerUpdateElement(testClazz);
        addControllerDeleteElement(testClazz);

        List<CompilationUnit> answer = new ArrayList<>();
        if (plugins.serviceImplGenerated(testClazz, introspectedTable)) {
            answer.add(testClazz);
        }
        return answer;
    }

    private void addSpringBootMockFields(TopLevelClass testClazz,
                                         FullyQualifiedJavaType serviceImplType,
                                         FullyQualifiedJavaType entityType,
                                         boolean workflowInstance,
                                         boolean fileAttachment,
                                         boolean hideListBin) {
        testClazz.addImportedType("com.vgosoft.system.service.impl.SysLogExceptionImpl");
        testClazz.addImportedType("com.vgosoft.system.service.impl.SysLogInfoImpl");
        testClazz.addImportedType("com.vgosoft.core.adapter.organization.OrganizationMgr");
        testClazz.addImportedType("com.vgosoft.organ.foo.FooDepartment");
        testClazz.addImportedType("com.vgosoft.organ.foo.FooOrganization");
        testClazz.addImportedType("com.vgosoft.organ.foo.FooUserInfo");

        addField("mockSysLogExceptionImpl",
                "com.vgosoft.system.service.impl.SysLogExceptionImpl",
                "@MockitoBean", testClazz);
        addField("mockSysLogInfoImpl",
                "com.vgosoft.system.service.impl.SysLogInfoImpl",
                "@MockitoBean", testClazz);
        addField("mockOrganizationMgr",
                "com.vgosoft.core.adapter.organization.OrganizationMgr<FooUserInfo, ?, ?, FooDepartment, FooOrganization>",
                "@MockitoBean", testClazz);
        addField("mock" + serviceImplType.getShortName(),
                serviceImplType.getFullyQualifiedName(),
                "@MockitoBean", testClazz);

        if (workflowInstance) {
            addField("mockWorkflowTraceInfoImpl",
                    "com.vgosoft.workflow.adapter.service.IWorkflowTraceInfo<" + entityType.getShortName() + ">",
                    "@MockitoBean", testClazz);
        }
        if (fileAttachment) {
            addField("mockVbizFileAttachmentImpl",
                    "com.vgosoft.bizcore.service.IVbizFileAttachment",
                    "@MockitoBean", testClazz);
        }
        if (hideListBin) {
            addField("mockSysPerFilterOutBinImpl",
                    "com.vgosoft.system.service.ISysPerFilterOutBin",
                    "@MockitoBean", testClazz);
            addField("mockSysPerFilterOutBinMappingsImpl",
                    "com.vgosoft.system.pojo.maps.SysPerFilterOutBinMappings",
                    "@MockitoBean", testClazz);
        }

        addCommonMockFields(testClazz, serviceImplType, entityType);
    }

    private void addLegacyMockFields(TopLevelClass testClazz, FullyQualifiedJavaType serviceImplType, FullyQualifiedJavaType entityType) {
        testClazz.addImportedType("com.vgosoft.system.service.impl.SysLogExceptionImpl");
        testClazz.addImportedType("com.vgosoft.system.service.impl.SysLogInfoImpl");
        testClazz.addImportedType("com.vgosoft.core.adapter.organization.OrganizationMgr");
        testClazz.addImportedType("com.vgosoft.organ.foo.FooDepartment");
        testClazz.addImportedType("com.vgosoft.organ.foo.FooOrganization");
        testClazz.addImportedType("com.vgosoft.organ.foo.FooUserInfo");

        addField("mockSysLogExceptionImpl",
                "com.vgosoft.system.service.impl.SysLogExceptionImpl",
                "@Mock", testClazz);
        addField("mockSysLogInfoImpl",
                "com.vgosoft.system.service.impl.SysLogInfoImpl",
                "@Mock", testClazz);
        addField("mockOrganizationMgr",
                "com.vgosoft.core.adapter.organization.OrganizationMgr<FooUserInfo, ?, ?, FooDepartment, FooOrganization>",
                "@Mock", testClazz);
        addField("mock" + serviceImplType.getShortName(),
                serviceImplType.getFullyQualifiedName(),
                "@Mock", testClazz);
        addCommonMockFields(testClazz, serviceImplType, entityType);
    }

    /**
     * 添加共用的MockFiled方法
     */
    private void addCommonMockFields(TopLevelClass testClazz, FullyQualifiedJavaType serviceImplType, FullyQualifiedJavaType entityType) {
        addField(JavaBeansUtil.getFirstCharacterLowercase(entityType.getShortName()),
                entityType.getFullyQualifiedName(),
                "", testClazz);
        addField("id",
                FullyQualifiedJavaType.getStringInstance().getFullyQualifiedName(),
                "", testClazz);
        addField("requestBody",
                FullyQualifiedJavaType.getStringInstance().getFullyQualifiedName(),
                "", testClazz);
    }


    private void addSetupMethod(TopLevelClass testClazz, boolean springBootTest, FullyQualifiedJavaType entityType, String entityInstanceVar, String viewpath) {
        testClazz.addImportedType("org.junit.jupiter.api.BeforeEach");
        testClazz.addImportedType("org.springframework.test.web.servlet.setup.MockMvcBuilders");
        Method setup = new Method("setUp");
        setup.addAnnotation("@BeforeEach");
        setup.addBodyLine("mockMvc = MockMvcBuilders.webAppContextSetup({0}).build();", WEB_APPLICATION_CONTEXT);
        setup.addBodyLine("");
        setup.addBodyLine("{0}= new {1}(0);", entityInstanceVar, entityType.getShortName());
        for (IntrospectedColumn column : introspectedTable.getAllColumns()) {
            if (column.getJdbcType() == Types.DECIMAL) {
                testClazz.addImportedType("java.math.BigDecimal");
            }
            if (isDateColumn(column)) {
                testClazz.addImportedType("com.vgosoft.tool.core.VDateUtils");
            }
            setup.addBodyLine("{0}.set{1}({2});", entityInstanceVar,
                    JavaBeansUtil.getFirstCharacterUppercase(column.getJavaProperty()),
                    getSetupExampleValue(column));
        }
        if (viewpath != null) {
            setup.addBodyLine("{0}.setViewPath(\"{1}\");", entityInstanceVar, viewpath);
        }
        setup.addBodyLine("");
        setup.addBodyLine("id = {0}.getId();", entityInstanceVar);
        setup.addBodyLine("");

        if (springBootTest) {
            testClazz.addImportedType("java.util.LinkedHashMap");
            testClazz.addImportedType("java.util.Map");
            setup.addBodyLine("Map<String, Object> requestBodyMap = new LinkedHashMap<>();");
            for (IntrospectedColumn column : introspectedTable.getAllColumns()) {
                if (isDateColumn(column)) {
                    setup.addBodyLine("requestBodyMap.put(\"{0}\", {1});",
                            column.getJavaProperty(),
                            getRequestBodyDateValueExpression(column, entityInstanceVar));
                } else {
                    setup.addBodyLine("requestBodyMap.put(\"{0}\", {1}.get{2}());",
                            column.getJavaProperty(),
                            entityInstanceVar,
                            JavaBeansUtil.getFirstCharacterUppercase(column.getJavaProperty()));
                }
            }
            if (viewpath != null) {
                setup.addBodyLine("requestBodyMap.put(\"viewPath\", {0}.getViewPath());", entityInstanceVar);
            }
            setup.addBodyLine("requestBody = JsonUtil.toJsonString(requestBodyMap);");
        } else {
            setup.addBodyLine("requestBody = JsonUtil.toJsonString({0});", entityInstanceVar);
        }
        setup.addBodyLine("");

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
    }

    private String getSetupExampleValue(IntrospectedColumn column) {
        if (column.isStringColumn()) {
            int maxLength = column.getLength();
            String example = column.getJavaProperty();
            if (maxLength > 0 && example.length() > maxLength) {
                example = example.substring(0, maxLength);
            }
            if (example.isEmpty()) {
                example = "a";
            }
            return "\"" + example + "\"";
        }
        return JavaBeansUtil.getColumnExampleValue(column);
    }

    private boolean hasJava8DateColumn() {
        for (IntrospectedColumn column : introspectedTable.getAllColumns()) {
            if (column.isJava8TimeColumn()) {
                return true;
            }
        }
        return false;
    }

    private boolean isDateColumn(IntrospectedColumn column) {
        return column.isJava8TimeColumn()
                || column.isJdbcDateColumn()
                || column.isJdbcTimeColumn()
                || column.isJdbcTimeStampColumn()
                || column.getJdbcType() == Types.DATE
                || column.getJdbcType() == Types.TIME
                || column.getJdbcType() == Types.TIMESTAMP;
    }

    private void addDateFormatterField(TopLevelClass testClazz, String fieldName, String pattern) {
        Objects.requireNonNull(testClazz, "testClazz");
        Objects.requireNonNull(fieldName, "fieldName");
        Objects.requireNonNull(pattern, "pattern");

        boolean fieldExists = testClazz.getFields().stream()
                .anyMatch(field -> fieldName.equals(field.getName()));
        if (fieldExists) {
            return;
        }

        Field formatterField = new Field(fieldName, new FullyQualifiedJavaType("java.time.format.DateTimeFormatter"));
        formatterField.setVisibility(JavaVisibility.PRIVATE);
        formatterField.setStatic(true);
        formatterField.setFinal(true);
        formatterField.setInitializationString("DateTimeFormatter.ofPattern(\"" + pattern + "\")");
        testClazz.addField(formatterField);
    }

    static String getRequestBodyDateValueExpression(IntrospectedColumn column, String entityInstanceVar) {
        String getter = entityInstanceVar + ".get" + JavaBeansUtil.getFirstCharacterUppercase(column.getJavaProperty()) + "()";
        if (column.isJavaLocalDateTimeColumn()) {
            return getter + ".format(DATETIME_FORMATTER)";
        }
        if (column.isJavaLocalDateColumn()) {
            return getter + ".format(DATE_FORMATTER)";
        }
        if (column.isJavaLocalTimeColumn()) {
            return getter + ".format(TIME_FORMATTER)";
        }
        if (column.isJdbcTimeStampColumn() || column.getJdbcType() == Types.TIMESTAMP) {
            return "new java.text.SimpleDateFormat(\"yyyy-MM-dd HH:mm:ss\").format(" + getter + ")";
        }
        if (column.isJdbcDateColumn() || column.getJdbcType() == Types.DATE) {
            return "new java.text.SimpleDateFormat(\"yyyy-MM-dd\").format(" + getter + ")";
        }
        if (column.isJdbcTimeColumn() || column.getJdbcType() == Types.TIME) {
            return "new java.text.SimpleDateFormat(\"HH:mm:ss\").format(" + getter + ")";
        }
        return getter;
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
