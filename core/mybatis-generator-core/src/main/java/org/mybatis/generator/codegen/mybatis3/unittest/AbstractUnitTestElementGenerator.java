package org.mybatis.generator.codegen.mybatis3.unittest;

import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.CommentGenerator;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.AbstractGenerator;
import org.mybatis.generator.codegen.mybatis3.htmlmapper.GenerateUtils;
import org.mybatis.generator.config.HtmlFileAttachmentConfiguration;
import org.mybatis.generator.config.JavaControllerGeneratorConfiguration;
import org.mybatis.generator.config.HtmlGeneratorConfiguration;
import org.mybatis.generator.config.TableConfiguration;
import org.mybatis.generator.config.VoAdditionalPropertyGeneratorConfiguration;
import org.mybatis.generator.internal.util.JavaBeansUtil;
import org.mybatis.generator.internal.util.StringUtility;

import java.sql.Types;
import java.util.List;

import static org.mybatis.generator.custom.ConstantsUtil.RESPONSE_RESULT;
import static org.mybatis.generator.custom.ConstantsUtil.RESPONSE_SIMPLE;

/**
 * @author cen_c
 */
public abstract class AbstractUnitTestElementGenerator extends AbstractGenerator {

    public static final String MOCK_MVC_PROPERTY_NAME = "mockMvc";

    protected FullyQualifiedJavaType entityType;

    protected FullyQualifiedJavaType exampleType;

    protected CommentGenerator commentGenerator;

    protected String serviceBeanName;

    protected  String mockServiceImpl;

    protected String entityNameKey;

    protected String entityInstanceVar;

    protected String entityVoInstanceVar;

    protected FullyQualifiedJavaType entityMappings;

    protected FullyQualifiedJavaType entityVoType;

    protected FullyQualifiedJavaType entityViewVoType;

    protected FullyQualifiedJavaType entityExcelVoType;

    protected FullyQualifiedJavaType responseResult;

    protected Parameter entityParameter;

    protected HtmlGeneratorConfiguration htmlGeneratorConfiguration;

    protected FullyQualifiedJavaType responseSimple;

    protected boolean isGenerateVo;

    protected boolean isGenerateVoModel;

    protected boolean useSpringBootTest;

    protected boolean hasWorkflowInstance;

    protected boolean hasFileAttachment;

    protected boolean hasHideListBin;

    protected String springBootApplicationClass;

    protected String basePath;

    protected String viewpath = null;

    /**
     * 添加元素到父类
     * @param parentElement 父类
     */
    public abstract void addElements(TopLevelClass parentElement);

    public AbstractUnitTestElementGenerator() {
        super();
    }

    protected void initGenerator(){
        TableConfiguration tc = introspectedTable.getTableConfiguration();
        JavaControllerGeneratorConfiguration javaControllerGeneratorConfiguration = tc.getJavaControllerGeneratorConfiguration();
        isGenerateVo = introspectedTable.getRules().isGenerateVo();
        isGenerateVoModel = introspectedTable.getRules().isGenerateVoModel();
        springBootApplicationClass = javaControllerGeneratorConfiguration == null ? null : javaControllerGeneratorConfiguration.getSpringBootApplicationClass();
        useSpringBootTest = StringUtility.stringHasValue(springBootApplicationClass);
        hasWorkflowInstance = GenerateUtils.isWorkflowInstance(introspectedTable);
        hasHideListBin = introspectedTable.getRules().isGenerateHideListBin();
        hasFileAttachment = tc.getHtmlMapGeneratorConfigurations().stream()
                .anyMatch(configuration -> configuration.getHtmlFileAttachmentConfiguration().stream()
                        .anyMatch(HtmlFileAttachmentConfiguration::isGenerate));

        entityType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        exampleType = new FullyQualifiedJavaType(introspectedTable.getExampleType());
        entityInstanceVar = JavaBeansUtil.getFirstCharacterLowercase(entityType.getShortName());
        entityVoInstanceVar = entityInstanceVar+"Vo";

        commentGenerator = context.getCommentGenerator();
        serviceBeanName = introspectedTable.getControllerBeanName();
        mockServiceImpl = "mock" + JavaBeansUtil.getFirstCharacterUppercase(serviceBeanName);

        entityNameKey = GenerateUtils.isWorkflowInstance(introspectedTable)?"business":"entity";
        if (!introspectedTable.getTableConfiguration().getHtmlMapGeneratorConfigurations().isEmpty()) {
            htmlGeneratorConfiguration = introspectedTable.getTableConfiguration().getHtmlMapGeneratorConfigurations().get(0);
        }
        entityParameter = new Parameter(entityType, JavaBeansUtil.getFirstCharacterLowercase(entityType.getShortName()));
        responseSimple = new FullyQualifiedJavaType(RESPONSE_SIMPLE);
        responseResult = new FullyQualifiedJavaType(RESPONSE_RESULT);

        String voTargetPackage = tc.getJavaModelGeneratorConfiguration().getBaseTargetPackage()+".pojo";
        entityMappings = new FullyQualifiedJavaType(String.join(".", voTargetPackage,"maps",entityType.getShortName()+"Mappings"));
        entityVoType = new FullyQualifiedJavaType(String.join(".", voTargetPackage,"vo",entityType.getShortName()+"Vo"));
        entityViewVoType = new FullyQualifiedJavaType(String.join(".", voTargetPackage,"vo",entityType.getShortName()+"ViewVo"));
        entityExcelVoType = new FullyQualifiedJavaType(String.join(".", voTargetPackage,"vo",entityType.getShortName()+"ExcelVo"));
        basePath = tc.getServiceApiBasePath();
        if (!introspectedTable.getTableConfiguration().getHtmlMapGeneratorConfigurations().isEmpty()) {
            HtmlGeneratorConfiguration htmlGeneratorConfiguration = introspectedTable.getTableConfiguration().getHtmlMapGeneratorConfigurations().get(0);
            viewpath = htmlGeneratorConfiguration.getViewPath();
        }
    }

    protected void addNormalizedVoAssertions(Method method, String entityVar, String voVar) {
        for (IntrospectedColumn column : introspectedTable.getAllColumns()) {
            method.addBodyLine(isDateTimeColumn(column) ? "{0}.set{1}({0}.get{1}().withNano(0));" : "{0}.set{1}({0}.get{1}());",
                    voVar,
                    JavaBeansUtil.getFirstCharacterUppercase(column.getJavaProperty()));
        }

        if (!useSpringBootTest || !isGenerateVoModel) {
            return;
        }

        if (introspectedTable.getTableConfiguration().getVoGeneratorConfiguration() == null
                || introspectedTable.getTableConfiguration().getVoGeneratorConfiguration().getVoModelConfiguration() == null) {
            return;
        }

        List<VoAdditionalPropertyGeneratorConfiguration> additionalProperties =
                introspectedTable.getTableConfiguration().getVoGeneratorConfiguration().getVoModelConfiguration()
                        .getAdditionalPropertyConfigurations();
        for (VoAdditionalPropertyGeneratorConfiguration additionalProperty : additionalProperties) {
            String sourceProperty = resolveSourceProperty(additionalProperty.getAnnotations());
            if (!StringUtility.stringHasValue(sourceProperty)) {
                continue;
            }

            String targetProperty = JavaBeansUtil.getFirstCharacterUppercase(additionalProperty.getName());
            String sourceGetter = entityVar + ".get" + JavaBeansUtil.getFirstCharacterUppercase(sourceProperty) + "()";
            if (containsAnnotation(additionalProperty.getAnnotations(), "DictModule")
                    || containsAnnotation(additionalProperty.getAnnotations(), "DictData")) {
                method.addBodyLine("{0}.set{1}(\"\");", voVar, targetProperty);
                continue;
            }

            if (containsAnnotation(additionalProperty.getAnnotations(), "DictEnum")) {
                if (containsAnnotation(additionalProperty.getAnnotations(), "WorkFlowStatusEnum")) {
                    method.addBodyLine("{0}.set{1}(\"未提交\");", voVar, targetProperty);
                } else {
                    method.addBodyLine("{0}.set{1}(String.valueOf({2}));", voVar, targetProperty, sourceGetter);
                }
                continue;
            }

            method.addBodyLine("{0}.set{1}({2});", voVar, targetProperty, sourceGetter);
        }
    }

    private boolean isDateTimeColumn(IntrospectedColumn column) {
        return column.isJava8TimeColumn()
                || column.isJdbcTimeColumn()
                || column.isJdbcTimeStampColumn()
                || column.getJdbcType() == Types.TIME
                || column.getJdbcType() == Types.TIMESTAMP;
    }

    private String resolveSourceProperty(List<String> annotations) {
        for (String annotation : annotations) {
            int sourceIndex = annotation.indexOf("source = \"");
            if (sourceIndex < 0) {
                sourceIndex = annotation.indexOf("source=\"");
            }
            if (sourceIndex < 0) {
                continue;
            }
            int start = annotation.indexOf('"', sourceIndex);
            int end = annotation.indexOf('"', start + 1);
            if (start >= 0 && end > start) {
                return annotation.substring(start + 1, end);
            }
        }
        return null;
    }

    private boolean containsAnnotation(List<String> annotations, String token) {
        for (String annotation : annotations) {
            if (annotation.contains(token)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 内部方法
     * 创建测试方法
     * */
    protected Method createMethod(String testedMethodName,TopLevelClass testClazz,String methodDescription){
        Method method = new Method("test"+JavaBeansUtil.getFirstCharacterUppercase(testedMethodName));
        method.addAnnotation("@Test");
        testClazz.addImportedType("org.junit.jupiter.api.Test");
        if (StringUtility.stringHasValue(methodDescription)) {
            method.addAnnotation("@DisplayName(\""+methodDescription+"\")");
        }
        return method;
    }

    /**
     * 内部方法
     * 添加方法注释
     * */
    protected void addMethodComment(Method method, String mockScene){
        CommentGenerator commentGenerator = context.getCommentGenerator();
        String comm1 = VStringUtil.format("被测试方法：{0}", JavaBeansUtil.getFirstCharacterLowercase(
                StringUtility.substringAfter(method.getName(),"test")));
        String comm2 = VStringUtil.format("mock场景：{0}", mockScene);
        commentGenerator.addMethodJavaDocLine(method, false, comm1,comm2);
    }
}
