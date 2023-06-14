package org.mybatis.generator.codegen.mybatis3.unittest;

import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.CommentGenerator;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.AbstractGenerator;
import org.mybatis.generator.config.HtmlGeneratorConfiguration;
import org.mybatis.generator.config.TableConfiguration;
import org.mybatis.generator.codegen.mybatis3.htmlmapper.GenerateUtils;
import org.mybatis.generator.internal.rules.BaseRules;
import org.mybatis.generator.internal.util.JavaBeansUtil;
import org.mybatis.generator.internal.util.StringUtility;

import static org.mybatis.generator.custom.ConstantsUtil.RESPONSE_RESULT;
import static org.mybatis.generator.custom.ConstantsUtil.RESPONSE_SIMPLE;

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

    protected boolean isGenerateVO;

    protected boolean isGenerateVOModel;

    protected String basePath;

    protected String viewpath = null;

    public abstract void addElements(TopLevelClass parentElement);

    public AbstractUnitTestElementGenerator() {
        super();
    }

    protected void initGenerator(){
        BaseRules rules = introspectedTable.getRules();
        TableConfiguration tc = introspectedTable.getTableConfiguration();
        isGenerateVO = introspectedTable.getRules().isGenerateVO();
        isGenerateVOModel = introspectedTable.getRules().isGenerateVoModel();

        entityType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        exampleType = new FullyQualifiedJavaType(introspectedTable.getExampleType());
        entityInstanceVar = JavaBeansUtil.getFirstCharacterLowercase(entityType.getShortName());
        entityVoInstanceVar = entityInstanceVar+"VO";

        commentGenerator = context.getCommentGenerator();
        serviceBeanName = introspectedTable.getControllerBeanName();
        mockServiceImpl = "mock" + JavaBeansUtil.getFirstCharacterUppercase(serviceBeanName);

        entityNameKey = GenerateUtils.isWorkflowInstance(introspectedTable)?"business":"entity";
        if (introspectedTable.getTableConfiguration().getHtmlMapGeneratorConfigurations().size()>0) {
            htmlGeneratorConfiguration = introspectedTable.getTableConfiguration().getHtmlMapGeneratorConfigurations().get(0);
        }
        entityParameter = new Parameter(entityType, JavaBeansUtil.getFirstCharacterLowercase(entityType.getShortName()));
        responseSimple = new FullyQualifiedJavaType(RESPONSE_SIMPLE);
        responseResult = new FullyQualifiedJavaType(RESPONSE_RESULT);

        String voTargetPackage = tc.getJavaModelGeneratorConfiguration().getBaseTargetPackage()+".pojo";
        entityMappings = new FullyQualifiedJavaType(String.join(".", voTargetPackage,"maps",entityType.getShortName()+"Mappings"));
        entityVoType = new FullyQualifiedJavaType(String.join(".", voTargetPackage,"vo",entityType.getShortName()+"VO"));
        entityViewVoType = new FullyQualifiedJavaType(String.join(".", voTargetPackage,"vo",entityType.getShortName()+"ViewVO"));
        entityExcelVoType = new FullyQualifiedJavaType(String.join(".", voTargetPackage,"vo",entityType.getShortName()+"ExcelVo"));
        basePath = tc.getServiceApiBasePath();
        if (introspectedTable.getTableConfiguration().getHtmlMapGeneratorConfigurations().size() > 0) {
            HtmlGeneratorConfiguration htmlGeneratorConfiguration = introspectedTable.getTableConfiguration().getHtmlMapGeneratorConfigurations().get(0);
            viewpath = htmlGeneratorConfiguration.getViewPath();
        }
    }

    /**
     * 内部方法
     * 创建测试方法
     * */
    protected Method createMethod(String testedMethodName,TopLevelClass testClazz,String methodDescript){
        Method method = new Method("test"+JavaBeansUtil.getFirstCharacterUppercase(testedMethodName));
        method.addAnnotation("@Test");
        testClazz.addImportedType("org.junit.jupiter.api.Test");
        if (StringUtility.stringHasValue(methodDescript)) {
            method.addAnnotation("@DisplayName(\""+methodDescript+"\")");
        }
        return method;
    }

    /**
     * 内部方法
     * 添加方法注释
     * */
    protected void addMethodComment(Method method,boolean isMock,String mockScene){
        CommentGenerator commentGenerator = context.getCommentGenerator();
        String comm1 = VStringUtil.format("被测试方法：{0}", JavaBeansUtil.getFirstCharacterLowercase(
                StringUtility.substringAfter(method.getName(),"test")));
        if (isMock) {
            String comm2 = VStringUtil.format("mock场景：{0}", mockScene);
            commentGenerator.addMethodJavaDocLine(method, false, comm1,comm2);
        }else{
            commentGenerator.addMethodJavaDocLine(method, false, comm1);
        }
    }
}
