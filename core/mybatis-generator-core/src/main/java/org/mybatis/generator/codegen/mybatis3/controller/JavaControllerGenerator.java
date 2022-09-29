package org.mybatis.generator.codegen.mybatis3.controller;

import org.mybatis.generator.api.CommentGenerator;
import org.mybatis.generator.api.FullyQualifiedTable;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.AbstractJavaGenerator;
import org.mybatis.generator.codegen.mybatis3.controller.elements.*;
import org.mybatis.generator.config.*;
import org.mybatis.generator.custom.pojo.FormOptionGeneratorConfiguration;
import org.mybatis.generator.custom.pojo.SelectByTableGeneratorConfiguration;
import org.mybatis.generator.internal.util.JavaBeansUtil;

import java.sql.JDBCType;
import java.util.ArrayList;
import java.util.List;

import static org.mybatis.generator.custom.ConstantsUtil.*;
import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;
import static org.mybatis.generator.internal.util.messages.Messages.getString;

public class JavaControllerGenerator  extends AbstractJavaGenerator{

    public JavaControllerGenerator(String project) {
        super(project);
          }

    @Override
    public List<CompilationUnit> getCompilationUnits() {
        TableConfiguration tc = introspectedTable.getTableConfiguration();
        String voTargetPackage = context.getJavaModelGeneratorConfiguration()
                .getBaseTargetPackage()+".pojo";
        FullyQualifiedJavaType exampleType = new FullyQualifiedJavaType(introspectedTable.getExampleType());
        FullyQualifiedJavaType entityType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        FullyQualifiedJavaType entityVoType = new FullyQualifiedJavaType(String.join(".", voTargetPackage, "vo", entityType.getShortName() + "VO"));
        FullyQualifiedJavaType entityRequestVoType = new FullyQualifiedJavaType(String.join(".", voTargetPackage, "vo", entityType.getShortName() + "RequestVO"));
        FullyQualifiedJavaType entityExcelVoType = new FullyQualifiedJavaType(String.join(".", voTargetPackage, "vo", entityType.getShortName() + "ExcelVO"));

        List<CompilationUnit> answer = new ArrayList<>();
        FullyQualifiedTable table = introspectedTable.getFullyQualifiedTable();
        progressCallback.startTask(getString("Progress.48", table.toString()));
        CommentGenerator commentGenerator = context.getCommentGenerator();
        JavaControllerGeneratorConfiguration javaControllerGeneratorConfiguration = tc.getJavaControllerGeneratorConfiguration();
        JavaServiceGeneratorConfiguration javaServiceGeneratorConfiguration = tc.getJavaServiceGeneratorConfiguration();

        String controllerName = "Gen"+ entityType.getShortName() + "Controller";
        StringBuilder sb = new StringBuilder();
        sb.append(javaControllerGeneratorConfiguration.getTargetPackageGen());
        sb.append(".").append(controllerName);
        FullyQualifiedJavaType conClazzType = new FullyQualifiedJavaType(sb.toString());
        TopLevelClass conTopClazz = new TopLevelClass(conClazzType);
        conTopClazz.setVisibility(JavaVisibility.PUBLIC);
        conTopClazz.setAbstract(true);
        commentGenerator.addJavaFileComment(conTopClazz);
        FullyQualifiedJavaType supClazzType = new FullyQualifiedJavaType(ABSTRACT_BASE_CONTROLLER);
        conTopClazz.setSuperClass(supClazzType);

        sb.setLength(0);
        sb.append(javaServiceGeneratorConfiguration.getTargetPackage()).append(".I");
        sb.append(entityType.getShortName());
        String infName = sb.toString();
        conTopClazz.addImportedType(infName);
        conTopClazz.addImportedType(supClazzType);
        conTopClazz.addImportedType("org.springframework.web.bind.annotation.*");
        conTopClazz.addStaticImport(RESPONSE_RESULT+".*");
        conTopClazz.addImportedType(API_CODE_ENUM);
        if (introspectedTable.getRules().isIntegrateSpringSecurity()) {
            conTopClazz.addImportedType("org.springframework.security.access.prepost.PreAuthorize");
        }
        FullyQualifiedJavaType bizInfType = new FullyQualifiedJavaType(infName);
        Field field = new Field(introspectedTable.getControllerBeanName(), bizInfType);
        field.setVisibility(JavaVisibility.PRIVATE);
        field.setFinal(true);
        conTopClazz.addField(field);

        //构造器
        Method method = new Method(controllerName);
        method.addParameter(new Parameter(bizInfType, introspectedTable.getControllerBeanName()));
        method.setConstructor(true);
        method.setVisibility(JavaVisibility.PUBLIC);
        method.addBodyLine("this.{0} = {0};",introspectedTable.getControllerBeanName());
        //增加Mappings属性
        FullyQualifiedJavaType entityMappings = new FullyQualifiedJavaType(
                String.join(".",
                        voTargetPackage,"maps",entityType.getShortName()+"Mappings"));
        if (introspectedTable.getRules().isGenerateVoModel()) {
            Field mappings = new Field("mappings", entityMappings);
            mappings.setFinal(true);
            mappings.setVisibility(JavaVisibility.PRIVATE);
            conTopClazz.addField(mappings);
            conTopClazz.addImportedType(entityMappings);
            method.addParameter(new Parameter(entityMappings, "mappings"));
            method.addBodyLine("this.mappings = mappings;");
        }
        conTopClazz.addMethod(method);



        String viewpath = null;
        if (tc.getHtmlMapGeneratorConfigurations().size()>0) {
            viewpath = tc.getHtmlMapGeneratorConfigurations().get(0).getViewPath();
        }
        if (viewpath != null) {
            addViewElement(conTopClazz);
        }
        addGetElement(conTopClazz);
        addListElement(conTopClazz);
        addCreateElement(conTopClazz);
        addCreateBatchElement(conTopClazz);
        addUpdateElement(conTopClazz);
        addUpdateBatchElement(conTopClazz);
        addDeleteElement(conTopClazz);
        addDeleteBatchElement(conTopClazz);

        if (introspectedTable.getRules().isGenerateViewVO()) {
            addGetDefaultViewConfigElement(conTopClazz);
            addGetDefaultViewElement(conTopClazz);
        }

        if (introspectedTable.hasBLOBColumns()) {
            addUploadElement(conTopClazz);
            addDownloadElement(conTopClazz);
        }
        if (introspectedTable.getRules().isGenerateExcelVO()) {
            addTemplateElement(conTopClazz);
            addImportElement(conTopClazz);
            addExportElement(conTopClazz);
        }

        if (tc.getJavaControllerGeneratorConfiguration().getFormOptionGeneratorConfigurations().size()>0) {
            addOptionElement(conTopClazz);
        }
        addGetDictElement(conTopClazz);

        addDeleteByTableElement(conTopClazz);
        addInsertByTableElement(conTopClazz);

        //追加一个构造导入Excel模板的样例数据方法
        if (introspectedTable.getRules().isGenerateExcelVO()) {
            Method buildTemplateSampleData = new Method("buildTemplateSampleData");
            buildTemplateSampleData.setVisibility(JavaVisibility.PROTECTED);
            FullyQualifiedJavaType retType = FullyQualifiedJavaType.getNewListInstance();
            retType.addTypeArgument(entityExcelVoType);
            buildTemplateSampleData.setReturnType(retType);
            commentGenerator.addMethodJavaDocLine(buildTemplateSampleData, false,
                    "[请在子类中重写此方法]","构造导入Excel模板中的样例数据，",
                    "当前方法根据类型生成，请重写该方法，以便于样例数据看起来更真实。","","@return 数据列表对象");
            if (context.getJdkVersion()>8) {
                buildTemplateSampleData.addBodyLine("return  List.of(");
                conTopClazz.addImportedType(FullyQualifiedJavaType.getNewListInstance());
            }else{
                buildTemplateSampleData.addBodyLine("return Collections.singletonList(");
                conTopClazz.addImportedType("java.util.Collections");
            }
            buildTemplateSampleData.addBodyLine("        {0}.builder()",entityExcelVoType.getShortName());
            for (IntrospectedColumn excelVOColumn : JavaBeansUtil.getAllExcelVOColumns(introspectedTable)) {
                buildTemplateSampleData.addBodyLine("                .{0}({1})",
                        excelVOColumn.getJavaProperty(),
                        JavaBeansUtil.getColumnExampleValue(excelVOColumn));
                if (excelVOColumn.isJDBCDateColumn() || excelVOColumn.isJDBCTimeColumn() || excelVOColumn.isJDBCTimeStampColumn()) {
                    conTopClazz.addImportedType("com.vgosoft.tool.core.VDateUtils");
                } else if (excelVOColumn.getJdbcType()== JDBCType.DECIMAL.getVendorTypeNumber()) {
                    conTopClazz.addImportedType("java.math.BigDecimal");
                } else if(excelVOColumn.getJdbcType() == JDBCType.BOOLEAN.getVendorTypeNumber()){
                    conTopClazz.addImportedType("java.lang.Boolean");
                }
            }
            buildTemplateSampleData.addBodyLine("                .build());");
            conTopClazz.addMethod(buildTemplateSampleData);
        }

        //追加一个example构造方法
        String p1,p2;
        p2 = exampleType.getShortName();
        Method buildExample = new Method("buildExample");
        buildExample.setVisibility(JavaVisibility.PROTECTED);
        buildExample.addParameter(new Parameter(FullyQualifiedJavaType.getStringInstance(), "actionType"));
        if (introspectedTable.getRules().isGenerateRequestVO()) {
            buildExample.addParameter(new Parameter(entityRequestVoType, entityRequestVoType.getShortNameFirstLowCase()));
            p1=entityRequestVoType.getShortNameFirstLowCase();
        }else if(introspectedTable.getRules().isGenerateVoModel()){
            buildExample.addParameter(new Parameter(entityVoType, entityVoType.getShortNameFirstLowCase()));
            p1=entityVoType.getShortNameFirstLowCase();
        }else{
            buildExample.addParameter(new Parameter(entityType,entityType.getShortNameFirstLowCase()));
            p1=entityType.getShortNameFirstLowCase();
        }
        buildExample.setReturnType(exampleType);
        buildExample.addBodyLine("return new {0}();", exampleType.getShortName());
        commentGenerator.addMethodJavaDocLine(buildExample, false,
                "[请在子类中重写此方法]","根据actionType构造不同的查询条件","",
                "@param actionType 类型标识。尽量使用有表意的字符串，如“byParentId”、“byNameAndNotes”等",
                "@param "+p1+" 入参对象，传入的条件值",
                "@return "+p2+"对象");
        conTopClazz.addMethod(buildExample);

        //追加到列表
        if (context.getPlugins().controllerGenerated(conTopClazz, introspectedTable)) {
            answer.add(conTopClazz);
        }

        //生成子类
        String subControllerName = entityType.getShortName() + "Controller";
        sb.setLength(0);
        sb.append(javaControllerGeneratorConfiguration.getTargetPackage());
        sb.append(".").append(subControllerName);
        FullyQualifiedJavaType conSubClazzType = new FullyQualifiedJavaType(sb.toString());
        TopLevelClass conSubTopClazz = new TopLevelClass(conSubClazzType);
        conSubTopClazz.setVisibility(JavaVisibility.PUBLIC);
        if (introspectedTable.getRules().isGenerateCachePO()) {
            addCacheConfig(conSubTopClazz);
        }
        conSubTopClazz.setSuperClass(conClazzType);
        conSubTopClazz.addImportedType(conClazzType);
        conSubTopClazz.addImportedType("org.springframework.web.bind.annotation.*");
        conSubTopClazz.addAnnotation("@RestController");
        conSubTopClazz.addAnnotation("@RequestMapping(value = \"/" + introspectedTable.getControllerSimplePackage() + "\")");
        //构造器
        Method conMethod = new Method(subControllerName);
        conMethod.addParameter(new Parameter(bizInfType, introspectedTable.getControllerBeanName()));
        conSubTopClazz.addImportedType(bizInfType);
        conMethod.setConstructor(true);
        conMethod.setVisibility(JavaVisibility.PUBLIC);
        if (introspectedTable.getRules().isGenerateVoModel()) {
            conSubTopClazz.addImportedType(entityMappings);
            conMethod.addParameter(new Parameter(entityMappings, "mappings"));
            conMethod.addBodyLine("super({0}, mappings);",introspectedTable.getControllerBeanName());
        }else{
            conMethod.addBodyLine("super({0});",introspectedTable.getControllerBeanName());
        }
        conSubTopClazz.addMethod(conMethod);
        boolean fileNotExist = JavaBeansUtil.javaFileNotExist(javaControllerGeneratorConfiguration.getTargetProject(), conSubClazzType.getPackageName(), subControllerName);
        if (introspectedTable.getRules().isForceGenerateScalableElement() || fileNotExist) {
            if (context.getPlugins().subControllerGenerated(conSubTopClazz, introspectedTable)){
                answer.add(conSubTopClazz);
            }
        }
        return answer;
    }

    private void addDeleteByTableElement(TopLevelClass parentElement) {
        if (introspectedTable.getTableConfiguration().getSelectByTableGeneratorConfiguration().stream().anyMatch(SelectByTableGeneratorConfiguration::isEnableSplit)) {
            AbstractControllerElementGenerator elementGenerator = new DeleteByTableGenerator();
            initializeAndExecuteGenerator(elementGenerator, parentElement);
        }
    }
    private void addInsertByTableElement(TopLevelClass parentElement) {
        if (introspectedTable.getTableConfiguration().getSelectByTableGeneratorConfiguration().stream().anyMatch(SelectByTableGeneratorConfiguration::isEnableUnion)) {
            AbstractControllerElementGenerator elementGenerator = new InsertByTableGenerator();
            initializeAndExecuteGenerator(elementGenerator, parentElement);
        }
    }

    private void addViewElement(TopLevelClass parentElement) {
        List<HtmlGeneratorConfiguration> htmlGeneratorConfigurations = introspectedTable.getTableConfiguration().getHtmlMapGeneratorConfigurations();
        long count = htmlGeneratorConfigurations.stream().filter(h->h.isGenerate() && stringHasValue(h.getViewPath())).count();
        if (count>0) {
            AbstractControllerElementGenerator elementGenerator = new ViewElementGenerator();
            initializeAndExecuteGenerator(elementGenerator, parentElement);
        }
    }

    private void addGetElement(TopLevelClass parentElement) {
        if (introspectedTable.getRules().generateSelectByPrimaryKey()) {
            AbstractControllerElementGenerator elementGenerator = new GetElementGenerator();
            initializeAndExecuteGenerator(elementGenerator, parentElement);
        }
    }

    private void addGetDictElement(TopLevelClass parentElement) {
        if (introspectedTable.getRules().isGenerateCachePO()) {
            AbstractControllerElementGenerator elementGenerator = new GetDictElementGenerator();
            initializeAndExecuteGenerator(elementGenerator, parentElement);
        }
    }

    private void addListElement(TopLevelClass parentElement) {
        AbstractControllerElementGenerator elementGenerator = new ListElementGenerator();
        initializeAndExecuteGenerator(elementGenerator, parentElement);
    }

    private void addCreateElement(TopLevelClass parentElement) {
        AbstractControllerElementGenerator elementGenerator = new CreateElementGenerator();
        initializeAndExecuteGenerator(elementGenerator, parentElement);
    }

    private void addCreateBatchElement(TopLevelClass parentElement) {
        if (introspectedTable.getRules().generateInsertBatch()) {
            AbstractControllerElementGenerator elementGenerator = new CreateBatchElementGenerator();
            initializeAndExecuteGenerator(elementGenerator, parentElement);
        }
    }

    private void addUpdateElement(TopLevelClass parentElement) {
        if (introspectedTable.getRules().generateUpdateByPrimaryKeySelective()) {
            AbstractControllerElementGenerator elementGenerator = new UpdateElementGenerator();
            initializeAndExecuteGenerator(elementGenerator, parentElement);
        }
    }

    private void addUpdateBatchElement(TopLevelClass parentElement) {
        if (introspectedTable.getRules().generateUpdateBatch()) {
            AbstractControllerElementGenerator elementGenerator = new UpdateBatchElementGenerator();
            initializeAndExecuteGenerator(elementGenerator, parentElement);
        }
    }

    private void addDeleteElement(TopLevelClass parentElement) {
        if (introspectedTable.getRules().generateDeleteByPrimaryKey()) {
            AbstractControllerElementGenerator elementGenerator = new DeleteElementGenerator();
            initializeAndExecuteGenerator(elementGenerator, parentElement);
        }
    }

    private void addDeleteBatchElement(TopLevelClass parentElement) {
        if (introspectedTable.getRules().generateDeleteByExample()) {
            AbstractControllerElementGenerator elementGenerator = new DeleteBatchElementGenerator();
            initializeAndExecuteGenerator(elementGenerator, parentElement);
        }
    }

    private void addUploadElement(TopLevelClass parentElement) {
        if (introspectedTable.getRules().generateFileUpload()) {
            AbstractControllerElementGenerator elementGenerator = new UploadElementGenerator();
            initializeAndExecuteGenerator(elementGenerator, parentElement);
        }
    }

    private void addDownloadElement(TopLevelClass parentElement) {
        if (introspectedTable.getRules().generateFileUpload()) {
            AbstractControllerElementGenerator elementGenerator = new DownloadElementGenerator();
            initializeAndExecuteGenerator(elementGenerator, parentElement);
        }
    }

    private void addGetDefaultViewElement(TopLevelClass parentElement){
        if (introspectedTable.getRules().isGenerateViewVO()) {
            AbstractControllerElementGenerator elementGenerator = new GetDefaultViewElementGenerator();
            initializeAndExecuteGenerator(elementGenerator, parentElement);
        }
    }

    private void addGetDefaultViewConfigElement(TopLevelClass parentElement){
        if (introspectedTable.getRules().isGenerateViewVO()) {
            AbstractControllerElementGenerator elementGenerator = new GetDefaultViewConfigElementGenerator();
            initializeAndExecuteGenerator(elementGenerator, parentElement);
        }
    }

    private void addOptionElement(TopLevelClass parentElement) {
        for (FormOptionGeneratorConfiguration formOptionGeneratorConfiguration : introspectedTable.getTableConfiguration().getJavaControllerGeneratorConfiguration().getFormOptionGeneratorConfigurations()) {
            AbstractControllerElementGenerator elementGenerator = new OptionElementGenerator(formOptionGeneratorConfiguration);
            initializeAndExecuteGenerator(elementGenerator, parentElement);
        }
    }

    private void addTemplateElement(TopLevelClass parentElement) {
        if (introspectedTable.getRules().isGenerateExcelVO()) {
            AbstractControllerElementGenerator elementGenerator = new TemplateElementGenerator();
            initializeAndExecuteGenerator(elementGenerator, parentElement);
        }
    }

    private void addImportElement(TopLevelClass parentElement) {
        if (introspectedTable.getRules().isGenerateExcelVO()) {
            AbstractControllerElementGenerator elementGenerator = new ImportElementGenerator();
            initializeAndExecuteGenerator(elementGenerator, parentElement);
        }
    }

    private void addExportElement(TopLevelClass parentElement) {
        if (introspectedTable.getRules().isGenerateExcelVO()) {
            AbstractControllerElementGenerator elementGenerator = new ExportElementGenerator();
            initializeAndExecuteGenerator(elementGenerator, parentElement);
        }
    }

    protected void initializeAndExecuteGenerator(
            AbstractControllerElementGenerator elementGenerator,
            TopLevelClass parentElement) {
        elementGenerator.setContext(context);
        elementGenerator.setIntrospectedTable(introspectedTable);
        elementGenerator.setProgressCallback(progressCallback);
        elementGenerator.setWarnings(warnings);
        elementGenerator.initGenerator();
        elementGenerator.addElements(parentElement);
    }
}
