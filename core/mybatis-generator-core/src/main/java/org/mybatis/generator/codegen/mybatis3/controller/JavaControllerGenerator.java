package org.mybatis.generator.codegen.mybatis3.controller;

import cn.hutool.core.collection.CollectionUtil;
import com.vgosoft.core.constant.enums.db.DefaultColumnNameEnum;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.CommentGenerator;
import org.mybatis.generator.api.FullyQualifiedTable;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.AbstractJavaGenerator;
import org.mybatis.generator.internal.util.Mb3GenUtil;
import org.mybatis.generator.codegen.mybatis3.controller.elements.*;
import org.mybatis.generator.config.*;
import org.mybatis.generator.custom.ScalableElementEnum;
import org.mybatis.generator.custom.annotations.RequestMappingDesc;
import org.mybatis.generator.config.FormOptionGeneratorConfiguration;
import org.mybatis.generator.config.SelectByTableGeneratorConfiguration;
import org.mybatis.generator.internal.util.JavaBeansUtil;
import org.mybatis.generator.internal.util.VoGenService;

import java.sql.JDBCType;
import java.util.*;
import java.util.stream.Collectors;

import static org.mybatis.generator.custom.ConstantsUtil.*;
import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;
import static org.mybatis.generator.internal.util.messages.Messages.getString;

public class JavaControllerGenerator extends AbstractJavaGenerator {
    public JavaControllerGenerator(String project) {
        super(project);
    }

    @Override
    public List<CompilationUnit> getCompilationUnits() {
        TableConfiguration tc = introspectedTable.getTableConfiguration();
        VoGenService voGenService = new VoGenService(introspectedTable);
        String voTargetPackage = context.getJavaModelGeneratorConfiguration()
                .getBaseTargetPackage() + ".pojo";
        FullyQualifiedJavaType exampleType = new FullyQualifiedJavaType(introspectedTable.getExampleType());
        FullyQualifiedJavaType entityType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        FullyQualifiedJavaType entityVoType = new FullyQualifiedJavaType(String.join(".", voTargetPackage, "vo", entityType.getShortName() + "VO"));
        FullyQualifiedJavaType entityRequestVoType = new FullyQualifiedJavaType(String.join(".", voTargetPackage, "vo", entityType.getShortName() + "RequestVO"));
        FullyQualifiedJavaType entityExcelImportVoType = new FullyQualifiedJavaType(String.join(".", voTargetPackage, "vo", entityType.getShortName() + "ExcelImportVO"));

        List<CompilationUnit> answer = new ArrayList<>();
        FullyQualifiedTable table = introspectedTable.getFullyQualifiedTable();
        progressCallback.startTask(getString("Progress.48", table.toString()));
        CommentGenerator commentGenerator = context.getCommentGenerator();
        JavaControllerGeneratorConfiguration javaControllerGeneratorConfiguration = tc.getJavaControllerGeneratorConfiguration();
        JavaServiceGeneratorConfiguration javaServiceGeneratorConfiguration = tc.getJavaServiceGeneratorConfiguration();

        String controllerName = "Gen" + entityType.getShortName() + "Controller";
        StringBuilder sb = new StringBuilder();
        sb.append(javaControllerGeneratorConfiguration.getTargetPackageGen());
        sb.append(".").append(controllerName);
        FullyQualifiedJavaType conClazzType = new FullyQualifiedJavaType(sb.toString());
        TopLevelClass conTopClazz = new TopLevelClass(conClazzType);
        conTopClazz.setVisibility(JavaVisibility.PUBLIC);
        conTopClazz.setAbstract(true);
        commentGenerator.addJavaFileComment(conTopClazz);
        FullyQualifiedJavaType supClazzType = new FullyQualifiedJavaType(ABSTRACT_BASE_CONTROLLER);
        if (introspectedTable.getRules().isGenerateVoModel()) {
            supClazzType.addTypeArgument(entityType);
            supClazzType.addTypeArgument(entityVoType);
            conTopClazz.addImportedType(entityVoType);
        } else {
            supClazzType.addTypeArgument(entityType);
            supClazzType.addTypeArgument(entityType);
        }
        conTopClazz.setSuperClass(supClazzType);

        sb.setLength(0);
        sb.append(javaServiceGeneratorConfiguration.getTargetPackage()).append(".I");
        sb.append(entityType.getShortName());
        String infName = sb.toString();
        conTopClazz.addImportedType(infName);
        conTopClazz.addImportedType(supClazzType);
        conTopClazz.addImportedType("org.springframework.web.bind.annotation.*");
        conTopClazz.addStaticImport(RESPONSE_RESULT + ".*");
        conTopClazz.addImportedType(new FullyQualifiedJavaType(API_CODE_ENUM));
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
        method.addBodyLine("this.{0} = {0};", introspectedTable.getControllerBeanName());
        //增加Mappings属性
        FullyQualifiedJavaType entityMappings = new FullyQualifiedJavaType(
                String.join(".",
                        voTargetPackage, "maps", entityType.getShortName() + "Mappings"));
        if (introspectedTable.getRules().isGenerateAnyVO()) {
            Field mappings = new Field("mappings", entityMappings);
            mappings.setFinal(true);
            mappings.setVisibility(JavaVisibility.PRIVATE);
            conTopClazz.addField(mappings);
            conTopClazz.addImportedType(entityMappings);
            method.addParameter(new Parameter(entityMappings, "mappings"));
            method.addBodyLine("this.mappings = mappings;");
        }
        conTopClazz.addMethod(method);

        tc.getHtmlMapGeneratorConfigurations().stream()
                .filter(hc -> stringHasValue(hc.getViewPath()))
                .findFirst()
                .map(HtmlGeneratorConfiguration::getViewPath)
                .ifPresent(viewpath -> addViewElement(conTopClazz));
        addNewInstanceElement(conTopClazz);
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

        if (tc.getJavaControllerGeneratorConfiguration().getFormOptionGeneratorConfigurations().size() > 0) {
            addOptionElement(conTopClazz);
        }
        addGetDictElement(conTopClazz);

        addDeleteByTableElement(conTopClazz);
        addInsertByTableElement(conTopClazz);
        addGetTreeElement(conTopClazz);
        addGetLayuiTableElement(conTopClazz);


        //重写getListData，如果存在VO的时候
        if (introspectedTable.getRules().isGenerateVoModel()) {
            Method getListData = new Method("getListData");
            getListData.setVisibility(JavaVisibility.PROTECTED);
            getListData.addAnnotation("@Override");
            FullyQualifiedJavaType returnType = new FullyQualifiedJavaType("com.github.pagehelper.Page");
            returnType.addTypeArgument(entityType);
            getListData.setReturnType(returnType);
            getListData.setReturnRemark("查询语句执行结果列表的PageHelper的Page封装对象");
            Parameter parameter1 = new Parameter(FullyQualifiedJavaType.getStringInstance(), "beanName");
            parameter1.setRemark("        操作服务类beanName。ioc中的beanId");
            getListData.addParameter(parameter1);
            Parameter parameter2 = new Parameter(new FullyQualifiedJavaType("com.vgosoft.mybatis.sqlbuilder.SelectSqlBuilder"), "selectSqlBuilder");
            parameter2.setRemark("列表查询sql builder，已经完成相关条件构建");
            getListData.addParameter(parameter2);
            FullyQualifiedJavaType listInstance = FullyQualifiedJavaType.getNewListInstance();
            listInstance.addTypeArgument(entityVoType);
            Parameter parameter3 = new Parameter(listInstance, "returnResult");
            parameter3.setRemark("    接口最终返回的数据列表");
            getListData.addParameter(parameter3);
            commentGenerator.addMethodJavaDocLine(getListData, "为了转换返回结果为VO对象而重写父类的方法");
            getListData.addBodyLine("ServiceResult<List<{0}>> result = {1}.selectBySql(selectSqlBuilder);", entityType.getShortName(), introspectedTable.getControllerBeanName());
            getListData.addBodyLine("if (result.hasResult()) {");
            getListData.addBodyLine("returnResult.addAll(mappings.to{0}s(result.getResult()));", entityVoType.getShortName());
            getListData.addBodyLine("}else{");
            getListData.addBodyLine("returnResult.addAll(new ArrayList<>());");
            getListData.addBodyLine("}");
            getListData.addBodyLine("return (Page<{0}>) result.getResult();", entityType.getShortName());
            conTopClazz.addMethod(getListData);
            conTopClazz.addImportedType("java.util.List");
            conTopClazz.addImportedType("com.vgosoft.mybatis.sqlbuilder.SelectSqlBuilder");
            conTopClazz.addImportedType("com.github.pagehelper.Page");
        }

        //追加一个构造导入Excel模板的样例数据方法
        if (introspectedTable.getRules().isGenerateExcelVO()) {
            Method buildTemplateSampleData = new Method("buildTemplateSampleData");
            buildTemplateSampleData.setVisibility(JavaVisibility.PROTECTED);
            FullyQualifiedJavaType retType = FullyQualifiedJavaType.getNewListInstance();
            retType.addTypeArgument(entityExcelImportVoType);
            method.setReturnRemark("模板数据列表对象");
            buildTemplateSampleData.setReturnType(retType);
            commentGenerator.addMethodJavaDocLine(buildTemplateSampleData,
                    "[请在子类中重写此方法]", "构造导入Excel模板中的样例数据，",
                    "当前方法根据类型生成，请重写该方法，以便于样例数据看起来更真实。");
            if (context.getJdkVersion() > 8) {
                buildTemplateSampleData.addBodyLine("return  List.of(");
                conTopClazz.addImportedType(FullyQualifiedJavaType.getNewListInstance());
            } else {
                buildTemplateSampleData.addBodyLine("return Collections.singletonList(");
                conTopClazz.addImportedType("java.util.Collections");
            }
            buildTemplateSampleData.addBodyLine("        {0}.builder()", entityExcelImportVoType.getShortName());
            VOExcelGeneratorConfiguration voExcelConfiguration = introspectedTable.getTableConfiguration().getVoGeneratorConfiguration().getVoExcelConfiguration();
            List<IntrospectedColumn> introspectedColumns = voGenService.getAllVoColumns(null, voExcelConfiguration.getImportIncludeColumns(), voExcelConfiguration.getImportExcludeColumns());
            CollectionUtil.addAllIfNotContains(introspectedColumns, voGenService.getAbstractVOColumns());
            for (IntrospectedColumn excelVOColumn : introspectedColumns) {
                buildTemplateSampleData.addBodyLine("                .{0}({1})",
                        excelVOColumn.getJavaProperty(),
                        JavaBeansUtil.getColumnExampleValue(excelVOColumn));
                if (excelVOColumn.isJDBCDateColumn() || excelVOColumn.isJDBCTimeColumn() || excelVOColumn.isJDBCTimeStampColumn()
                        || excelVOColumn.isJava8TimeColumn()) {
                    conTopClazz.addImportedType("com.vgosoft.tool.core.VDateUtils");
                } else if (excelVOColumn.getJdbcType() == JDBCType.DECIMAL.getVendorTypeNumber()) {
                    conTopClazz.addImportedType("java.math.BigDecimal");
                } else if (excelVOColumn.getJdbcType() == JDBCType.BOOLEAN.getVendorTypeNumber()) {
                    conTopClazz.addImportedType("java.lang.Boolean");
                }
            }
            buildTemplateSampleData.addBodyLine("                .build());");
            conTopClazz.addMethod(buildTemplateSampleData);
            conTopClazz.addImportedType(entityExcelImportVoType);
        }

        //追加一个example构造方法
        Map<String, String> nameFragments = new HashMap<>();
        if (introspectedTable.getTableConfiguration().getVoGeneratorConfiguration() != null
                && introspectedTable.getTableConfiguration().getVoGeneratorConfiguration().getVoRequestConfiguration() != null
                && introspectedTable.getTableConfiguration().getVoGeneratorConfiguration().getVoRequestConfiguration().getVoNameFragmentGeneratorConfigurations() != null) {
            nameFragments = introspectedTable.getTableConfiguration().getVoGeneratorConfiguration().getVoRequestConfiguration().getVoNameFragmentGeneratorConfigurations()
                    .stream().collect(Collectors.toMap(VoNameFragmentGeneratorConfiguration::getColumn, VoNameFragmentGeneratorConfiguration::getFragment, (k1, k2) -> k2));
        }

        Method buildExample = new Method("buildExample");
        buildExample.setVisibility(JavaVisibility.PROTECTED);
        //actionType 用来区分查询应用场景的类型标识
        Parameter actionType = new Parameter(FullyQualifiedJavaType.getStringInstance(), "actionType");
        actionType.setRemark("用来区分查询应用场景的类型标识");
        buildExample.addParameter(actionType);
        //对象参数
        FullyQualifiedJavaType paramType = introspectedTable.getRules().isGenerateRequestVO() ? entityRequestVoType : introspectedTable.getRules().isGenerateVoModel() ? entityVoType : entityType;
        Parameter parameter = new Parameter(paramType, paramType.getShortNameFirstLowCase());
        parameter.setRemark("传入的条件值");
        buildExample.setReturnRemark("查询对象");
        buildExample.addParameter(parameter);
        buildExample.setReturnType(exampleType);
        commentGenerator.addMethodJavaDocLine(buildExample, "[请在子类中重写此方法]", "根据actionType构造不同的查询条件");

        FullyQualifiedJavaType type = parameter.getType();
        List<IntrospectedColumn> columns;
        boolean isContainOrderByClause = false;
        if (introspectedTable.getTopLevelClassExampleFields().containsKey(type.getShortName())) {
            final List<String> fNames = introspectedTable.getTopLevelClassExampleFields().get(type.getShortName());
            isContainOrderByClause = fNames.contains("orderByClause");
            columns = introspectedTable.getNonBLOBColumns().stream()
                    .filter(c -> fNames.contains(c.getJavaProperty()))
                    .collect(Collectors.toList());
        } else {
            columns = introspectedTable.getNonBLOBColumns();
        }
        if (columns.size() > 0) {
            buildExample.addBodyLine("{0} example = new {0}();\n" +
                    "        {0}.Criteria criteria = example.createCriteria();", exampleType.getShortName());
            for (IntrospectedColumn column : columns) {
                String getterMethodName = JavaBeansUtil.getGetterMethodName(column.getJavaProperty(), column.getFullyQualifiedJavaType());
                boolean isBetween = nameFragments.containsKey(column.getActualColumnName()) && "between".equalsIgnoreCase(nameFragments.get(column.getActualColumnName()));
                if (column.isJdbcCharacterColumn()) {
                    if (isBetween) {
                        buildExample.addBodyLine("if (!VStringUtil.isBlank({0}.{1}())  && !VStringUtil.isBlank({0}.{1}Other())) '{'", type.getShortNameFirstLowCase(), getterMethodName);
                    } else {
                        buildExample.addBodyLine("if (!VStringUtil.isBlank({0}.{1}())) '{'", type.getShortNameFirstLowCase(), getterMethodName);
                    }
                    conTopClazz.addImportedType(V_STRING_UTIL);
                } else {
                    if (isBetween) {
                        buildExample.addBodyLine("if ({0}.{1}() != null  && {0}.{1}Other() != null) '{'", type.getShortNameFirstLowCase(), getterMethodName);
                    } else {
                        buildExample.addBodyLine("if ({0}.{1}() != null) '{'", type.getShortNameFirstLowCase(), getterMethodName);
                    }
                }

                String methodName = initializeAndMethodName(column) + nameFragments.getOrDefault(column.getActualColumnName(), "EqualTo");
                if (isBetween) {
                    buildExample.addBodyLine("criteria.{0}({1}.{2}(),{1}.{2}Other());"
                            , methodName
                            , type.getShortNameFirstLowCase()
                            , getterMethodName);
                } else {
                    buildExample.addBodyLine("criteria.{0}({1}.{2}());"
                            , methodName
                            , type.getShortNameFirstLowCase()
                            , getterMethodName);
                }
                if (isBetween) {
                    if (column.isJdbcCharacterColumn()) {
                        buildExample.addBodyLine("'}'else if (!VStringUtil.isBlank({0}.{1}())) '{'", type.getShortNameFirstLowCase(), getterMethodName);
                    } else {
                        buildExample.addBodyLine("'}'else if ({0}.{1}() != null) '{'", type.getShortNameFirstLowCase(), getterMethodName);
                    }
                    buildExample.addBodyLine("criteria.{0}({1}.{2}());"
                            , initializeAndMethodName(column) + "EqualTo"
                            , type.getShortNameFirstLowCase()
                            , getterMethodName);
                }
                buildExample.addBodyLine("}");
            }

            //排序语句
            List<IntrospectedColumn> sort = columns.stream().filter(c -> c.getActualColumnName().equalsIgnoreCase("SORT_")).collect(Collectors.toList());
            if (sort.size() > 0) {
                if (isContainOrderByClause) {
                    buildExample.addBodyLine("if (!VStringUtil.isBlank({0}.getOrderByClause())) '{'", type.getShortNameFirstLowCase());
                    buildExample.addBodyLine("example.setOrderByClause({0}.getOrderByClause());", type.getShortNameFirstLowCase());
                    buildExample.addBodyLine("}else{");
                    buildExample.addBodyLine("example.setOrderByClause(\"{0}\"); ", sort.get(0).getActualColumnName());
                    buildExample.addBodyLine("}");
                    conTopClazz.addImportedType(V_STRING_UTIL);
                } else {
                    buildExample.addBodyLine("example.setOrderByClause(\"{0}\"); ", sort.get(0).getActualColumnName());
                }
            } else {
                if (isContainOrderByClause) {
                    buildExample.addBodyLine("if (!VStringUtil.isBlank({0}.getOrderByClause())) '{'", type.getShortNameFirstLowCase());
                    buildExample.addBodyLine("example.setOrderByClause({0}.getOrderByClause());", type.getShortNameFirstLowCase());
                    buildExample.addBodyLine("}");
                    conTopClazz.addImportedType(V_STRING_UTIL);
                }
            }

            buildExample.addBodyLine("return example;");
        }

        if (buildExample.getBodyLines().isEmpty()) {
            buildExample.addBodyLine("return new {0}();", exampleType.getShortName());
        }
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
        conSubTopClazz.addAnnotation(new RequestMappingDesc(Mb3GenUtil.getControllerBaseMappingPath(introspectedTable)).toAnnotation());
        //构造器
        Method conMethod = new Method(subControllerName);
        conMethod.addParameter(new Parameter(bizInfType, introspectedTable.getControllerBeanName()));
        conSubTopClazz.addImportedType(bizInfType);
        conMethod.setConstructor(true);
        conMethod.setVisibility(JavaVisibility.PUBLIC);

        if (introspectedTable.getRules().isGenerateAnyVO()) {
            conSubTopClazz.addImportedType(entityMappings);
            conMethod.addParameter(new Parameter(entityMappings, "mappings"));
            conMethod.addBodyLine("super({0}, mappings{1});", introspectedTable.getControllerBeanName());
        } else {
            conMethod.addBodyLine("super({0}{1});", introspectedTable.getControllerBeanName());
        }
        conSubTopClazz.addMethod(conMethod);
        boolean fileNotExist = JavaBeansUtil.javaFileNotExist(javaControllerGeneratorConfiguration.getTargetProject(), conSubClazzType.getPackageName(), subControllerName);
        if (introspectedTable.getRules().isForceGenerateScalableElement(ScalableElementEnum.controller.name()) || fileNotExist) {
            if (context.getPlugins().subControllerGenerated(conSubTopClazz, introspectedTable)) {
                answer.add(conSubTopClazz);
            }
        }
        return answer;
    }

    private void addNewInstanceElement(TopLevelClass conTopClazz) {
        AbstractControllerElementGenerator elementGenerator = new NewInstanceElementGenerator();
        initializeAndExecuteGenerator(elementGenerator, conTopClazz);
    }

    private void addGetLayuiTableElement(TopLevelClass conTopClazz) {
        if (introspectedTable.getRules().isGenerateInnerTable()) {
            AbstractControllerElementGenerator elementGenerator = new GetLayuiTableElementGenerator();
            initializeAndExecuteGenerator(elementGenerator, conTopClazz);
        }
    }

    private void addGetTreeElement(TopLevelClass parentElement) {
        Set<String> columnNames = introspectedTable.getTableConfiguration().getColumnNames();
        if (columnNames.contains(DefaultColumnNameEnum.PARENT_ID.columnName())
                && columnNames.contains(DefaultColumnNameEnum.ID.columnName())
                && columnNames.contains(DefaultColumnNameEnum.NAME.columnName())) {
            AbstractControllerElementGenerator elementGenerator = new FetchTreeDataElementGenerator();
            initializeAndExecuteGenerator(elementGenerator, parentElement);
        }
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
        if (htmlGeneratorConfigurations.stream().anyMatch(h -> h.isGenerate() && stringHasValue(h.getViewPath()))) {
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

    private void addGetDefaultViewElement(TopLevelClass parentElement) {
        if (introspectedTable.getRules().isGenerateViewVO()) {
            AbstractControllerElementGenerator elementGenerator = new GetDefaultViewElementGenerator();
            initializeAndExecuteGenerator(elementGenerator, parentElement);
        }
    }

    private void addGetDefaultViewConfigElement(TopLevelClass parentElement) {
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

    //内部类
    private String initializeAndMethodName(IntrospectedColumn introspectedColumn) {
        StringBuilder sb = new StringBuilder();
        sb.append(introspectedColumn.getJavaProperty());
        sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
        sb.insert(0, "and"); //$NON-NLS-1$
        return sb.toString();
    }
}
