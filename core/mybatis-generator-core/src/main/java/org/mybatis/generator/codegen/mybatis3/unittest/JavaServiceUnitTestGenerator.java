package org.mybatis.generator.codegen.mybatis3.unittest;

import org.mybatis.generator.api.FullyQualifiedTable;
import org.mybatis.generator.api.Plugin;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.config.JavaServiceImplGeneratorConfiguration;
import org.mybatis.generator.custom.ConstantsUtil;
import org.mybatis.generator.custom.TestClassMapEnum;
import org.mybatis.generator.custom.pojo.SelectByColumnGeneratorConfiguration;
import org.mybatis.generator.custom.pojo.SelectByTableGeneratorConfiguration;
import org.mybatis.generator.internal.util.JavaBeansUtil;

import java.util.ArrayList;
import java.util.List;

import static org.mybatis.generator.custom.ConstantsUtil.*;
import static org.mybatis.generator.internal.util.messages.Messages.getString;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-05-17 01:27
 * @version 3.0
 */
public class JavaServiceUnitTestGenerator extends AbstractUnitTestGenerator{

    public static final String MAPPER_PROPERTY_NAME = "mockMapper";

    public JavaServiceUnitTestGenerator(String project) {
        super(project);
    }

    @Override
    public List<CompilationUnit> getCompilationUnits() {
        FullyQualifiedTable table = introspectedTable.getFullyQualifiedTable();
        progressCallback.startTask(getString("Progress.58", table.toString()));
        Plugin plugins = context.getPlugins();

        FullyQualifiedJavaType mapperType = new FullyQualifiedJavaType(introspectedTable.getMyBatis3JavaMapperType());
        FullyQualifiedJavaType entityType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        FullyQualifiedJavaType exampleType = new FullyQualifiedJavaType(introspectedTable.getExampleType());
        FullyQualifiedJavaType serviceImplType = getServiceImplType();

        FullyQualifiedJavaType topClassType = new FullyQualifiedJavaType(serviceImplType.getFullyQualifiedName()+"Test");
        TopLevelClass testClazz = new TopLevelClass(topClassType);
        testClazz.setVisibility(JavaVisibility.PUBLIC);
        FullyQualifiedJavaType serviceUnitTestSupperType = getServiceUnitTestSupperType(serviceImplType, mapperType, entityType, exampleType, testClazz);
        if (serviceUnitTestSupperType != null) {
            testClazz.setSuperClass(serviceUnitTestSupperType);
        }
        addExtendWithAnnotation(testClazz);
        testClazz.addImportedType(mapperType);
        testClazz.addImportedType(entityType);
        testClazz.addImportedType(exampleType);
        testClazz.addImportedType("org.junit.jupiter.api.DisplayName");

        //增加构造器
        Method constructor = new Method(topClassType.getShortName());
        constructor.setVisibility(JavaVisibility.PUBLIC);
        constructor.setConstructor(true);
        Parameter parameter = new Parameter(mapperType, MAPPER_PROPERTY_NAME);
        parameter.addAnnotation("@Mock");
        constructor.addParameter(parameter);
        constructor.addBodyLine("super("+MAPPER_PROPERTY_NAME+");");
        testClazz.addMethod(constructor);
        testClazz.addImportedType("org.mockito.Mock");

        //增加测试配置方法
        Method setup = new Method("setUp");
        setup.addAnnotation("@BeforeEach");
        testClazz.addImportedType("org.junit.jupiter.api.BeforeEach");
        setup.addBodyLine("this.serviceImplUnderTest = new "+serviceImplType.getShortName()+"("+MAPPER_PROPERTY_NAME+");");
        setup.addBodyLine("record = new "+entityType.getShortName()+"();");
        setup.addBodyLine("record.setId(\"id\");");
        setup.addBodyLine("example = new "+exampleType.getShortName()+"();");
        setup.addBodyLine("comSelSqlParameter = new ComSelSqlParameter();");
        testClazz.addMethod(setup);
        testClazz.addImportedType("com.vgosoft.core.entity.ComSelSqlParameter");

        //增加selectByColumnXXX测试方法
        for (SelectByColumnGeneratorConfiguration configuration : introspectedTable.getTableConfiguration().getSelectByColumnGeneratorConfigurations()) {
            Method method = createMethod(configuration.getMethodName(),testClazz,"根据字段:"+configuration.getColumnName()+"查询数据");
            addMethodComment(method,true,"被调用的mapper有返回值");
            if (configuration.getReturnType()==1) {
                method.addBodyLine("final {0} expectedResult = record;", entityType.getShortName());
            }else{
                method.addBodyLine("final List<{0}> expectedResult = Collections.singletonList(record);",entityType.getShortName());
                testClazz.addImportedType(FullyQualifiedJavaType.getNewListInstance());
                testClazz.addImportedType("java.util.Collections");
            }

            method.addBodyLine("when(mockMapper.{0}(any())).thenReturn(expectedResult);",configuration.getMethodName());
            testClazz.addStaticImport(TEST_MOCKITO_WHEN);
            testClazz.addStaticImport("org.mockito.ArgumentMatchers.any");

            String javaProperty = JavaBeansUtil.getColumnExampleValue(configuration.getColumn());
            if (configuration.getReturnType()==1) {
                method.addBodyLine("final {0} result = serviceImplUnderTest.{1}({2});"
                        , entityType.getShortName()
                        ,configuration.getMethodName()
                        ,configuration.isParameterList()?"Collections.singletonList("+javaProperty+")":javaProperty);
                method.addBodyLine("assertThat(result).isEqualTo(expectedResult);");
                testClazz.addImportedType(SERVICE_RESULT);
            }else{
                method.addBodyLine("final List<{0}> result = serviceImplUnderTest.{1}({2});"
                        , entityType.getShortName()
                        ,configuration.getMethodName()
                        ,configuration.isParameterList()?"Collections.singletonList("+javaProperty+")":javaProperty);
                method.addBodyLine("assertThat(result).isEqualTo(expectedResult);");
            }
            testClazz.addStaticImport(TEST_ASSERTIONS_ASSERT_THAT);
            addMockMapper(testClazz,mapperType);
        }

        //增加SelectByTableXXXXX测试方法
        for (SelectByTableGeneratorConfiguration configuration : introspectedTable.getTableConfiguration().getSelectByTableGeneratorConfiguration()) {
            Method method = createMethod(configuration.getMethodName(), testClazz,"通过中间表："+configuration.getTableName()+"查询数据");
            addMethodComment(method, true, "被调用的mapper有返回值");

            boolean isReturnKeys = configuration.getReturnTypeParam().equalsIgnoreCase("primaryKey");

            method.addBodyLine("final List<{0}> expectedResult = Collections.singletonList({1});",
                    isReturnKeys?"String":entityType.getShortName(),
                    isReturnKeys?"\"primaryKey\"":"record");
            testClazz.addImportedType(FullyQualifiedJavaType.getNewListInstance());
            testClazz.addImportedType("java.util.Collections");

            method.addBodyLine("when(mockMapper.{0}(any())).thenReturn(expectedResult);",configuration.getMethodName());
            testClazz.addStaticImport(TEST_MOCKITO_WHEN);
            testClazz.addStaticImport("org.mockito.ArgumentMatchers.any");

            method.addBodyLine("final List<{0}> result = serviceImplUnderTest.{1}(\"{2}\");",
                    isReturnKeys?"String":entityType.getShortName(),
                    configuration.getMethodName(),
                    configuration.getParameterName());

            method.addBodyLine("assertThat(result).isEqualTo(expectedResult);");
            testClazz.addStaticImport(TEST_ASSERTIONS_ASSERT_THAT);
            addMockMapper(testClazz,mapperType);
        }


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
    private FullyQualifiedJavaType getServiceImplType(){
        JavaServiceImplGeneratorConfiguration javaServiceImplGeneratorConfiguration = introspectedTable.getTableConfiguration().getJavaServiceImplGeneratorConfiguration();
        String clazzName = JavaBeansUtil.getFirstCharacterUppercase(introspectedTable.getControllerBeanName());
        return new FullyQualifiedJavaType(javaServiceImplGeneratorConfiguration.getTargetPackage() + "." + clazzName);
    }

    /**
     * 内部方法
     * 获得Service抽象类父类
     */
    private FullyQualifiedJavaType getServiceUnitTestSupperType(FullyQualifiedJavaType serviceImplType,
                                                                FullyQualifiedJavaType mapperType,
                                                                FullyQualifiedJavaType entityType,
                                                                FullyQualifiedJavaType exampleType,
                                                                TopLevelClass topLevelClass) {


        String supperClassName = ConstantsUtil.ABSTRACT_MBG_SERVICE_INTERFACE;
        boolean assignableCurrent = JavaBeansUtil.isAssignable(supperClassName, getAbstractService(introspectedTable), introspectedTable);
        if (assignableCurrent) {
            FullyQualifiedJavaType supperTestType = new FullyQualifiedJavaType(getTestClass(supperClassName));
            supperTestType.addTypeArgument(serviceImplType);
            supperTestType.addTypeArgument(mapperType);
            supperTestType.addTypeArgument(entityType);
            supperTestType.addTypeArgument(exampleType);
            topLevelClass.addImportedType(supperTestType);
            return supperTestType;
        }
        return null;
    }

    /**
     * 内部方法
     * 添加mockMapper属性
     * 并在构造器中初始化
     * */
    private void addMockMapper(TopLevelClass topLevelClass,FullyQualifiedJavaType mapperType){
        long mockMapper = topLevelClass.getFields().stream()
                .filter(f -> f.getName().equalsIgnoreCase(MAPPER_PROPERTY_NAME))
                .count();
        if (mockMapper==0) {
            Field mapper = new Field(MAPPER_PROPERTY_NAME, mapperType);
            mapper.setFinal(true);
            mapper.setVisibility(JavaVisibility.PRIVATE);
            topLevelClass.addField(mapper);
            //添加构造器方法
            topLevelClass.getMethods().stream()
                    .filter(m -> m.getName().equals(topLevelClass.getType().getShortName()))
                    .findFirst()
                    .ifPresent(method -> method.addBodyLine("this.{0} = {0};",MAPPER_PROPERTY_NAME));
        }
    }
}
