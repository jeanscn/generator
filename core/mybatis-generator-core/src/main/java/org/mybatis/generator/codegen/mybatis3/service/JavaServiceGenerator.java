package org.mybatis.generator.codegen.mybatis3.service;

import com.vgosoft.core.constant.GlobalConstant;
import com.vgosoft.core.constant.enums.core.EntityAbstractParentEnum;
import com.vgosoft.core.constant.enums.data.StatTypeEnum;
import com.vgosoft.core.constant.enums.db.DefaultColumnNameEnum;
import com.vgosoft.mybatis.generate.GenerateSqlTemplate;
import com.vgosoft.mybatis.sqlbuilder.InsertSqlBuilder;
import com.vgosoft.tool.core.VMD5Util;
import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.CommentGenerator;
import org.mybatis.generator.api.FullyQualifiedTable;
import org.mybatis.generator.api.AbstractIntrospectedTable;
import org.mybatis.generator.api.Plugin;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.mybatis3.htmlmapper.GenerateUtils;
import org.mybatis.generator.config.*;
import org.mybatis.generator.internal.util.JavaBeansUtil;
import org.mybatis.generator.internal.util.Mb3GenUtil;

import java.util.ArrayList;
import java.util.List;

import static org.mybatis.generator.custom.ConstantsUtil.*;
import static org.mybatis.generator.internal.util.messages.Messages.getString;

/**
 * @author cen_c
 */
public class JavaServiceGenerator extends AbstractServiceGenerator {


    public JavaServiceGenerator(String project) {
        super(project);
    }

    @Override
    public List<CompilationUnit> getCompilationUnits() {

        FullyQualifiedTable table = introspectedTable.getFullyQualifiedTable();
        progressCallback.startTask(getString("Progress.28", table.toString()));
        Plugin plugins = context.getPlugins();
        CommentGenerator commentGenerator = context.getCommentGenerator();
        List<String> classComments = new ArrayList<>();

        JavaServiceGeneratorConfiguration serviceInfConfiguration = introspectedTable.getTableConfiguration().getJavaServiceGeneratorConfiguration();

        Interface bizInf = new Interface(
                getGenInterfaceClassShortName(serviceInfConfiguration.getTargetPackageGen(), entityType.getShortName()));
        commentGenerator.addJavaFileComment(bizInf,false);
        FullyQualifiedJavaType infSuperType = new FullyQualifiedJavaType(getServiceInterface(introspectedTable));
        infSuperType.addTypeArgument(entityType);
        infSuperType.addTypeArgument(exampleType);
        bizInf.addImportedType(infSuperType);
        bizInf.addImportedType(entityType);
        bizInf.addImportedType(exampleType);
        bizInf.setVisibility(JavaVisibility.PUBLIC);
        bizInf.addSuperInterface(infSuperType);
        classComments.add(VStringUtil.format("该类继承自'{'@link {0}'}'，",infSuperType.getShortNameWithoutTypeArguments()));
        ServiceMethods serviceMethods = new ServiceMethods(context, introspectedTable);
        if (introspectedTable.getRules().isGenerateExcelImportWorkflow()) {
            bizInf.addMethod(serviceMethods.getPreviewExcelImportMethod(bizInf, true));
            bizInf.addMethod(serviceMethods.getConfirmExcelImportMethod(bizInf, true));
        }
        if (introspectedTable.getRules().generateCasWriteContract()) {
            bizInf.addMethod(serviceMethods.getCasWriteMethod(bizInf, false, true, true));
            bizInf.addMethod(serviceMethods.getCasWriteMethod(bizInf, true, true, true));
        }
        if (introspectedTable.getRules().generateSelectByPrimaryKeyForUpdate()) {
            bizInf.addMethod(serviceMethods.getSelectByPrimaryKeyForUpdateMethod(bizInf, true, true));
        }
        if (introspectedTable.getRules().generateSelectByExampleForUpdate()) {
            bizInf.addMethod(serviceMethods.getSelectByExampleForUpdateMethod(bizInf, true, true));
        }

        /*
          insertBatch
          */
        if (introspectedTable.getRules().generateInsertBatch()) {
            Method method = serviceMethods.getInsertBatchMethod(bizInf, true, true);
            bizInf.addMethod(method);
        }

        /*
         * updateBatch
         * */
        if (introspectedTable.getRules().generateUpdateBatch()) {
            bizInf.addMethod(serviceMethods.getUpdateBatchMethod(bizInf, true, true));
        }

        /*
         * atomicIncrement
         * */
        if (introspectedTable.getRules().generateAtomicIncrement()) {
            bizInf.addMethod(serviceMethods.getAtomicIncrementMethod(bizInf, true));
        }

        /*
         * insertOrUpdate
         */
        introspectedTable.getColumn(DefaultColumnNameEnum.DELETE_FLAG.columnName()).ifPresent(column -> {
            Method method = serviceMethods.getUpdateDeleteFlagMethod(bizInf, true, true);
            bizInf.addMethod(method);
        });

        /*
         * insertOrUpdate
         * */
        if (introspectedTable.getRules().generateInsertOrUpdate()) {
            bizInf.addMethod(serviceMethods.getInsertOrUpdateMethod(bizInf, true, true));
        }

        //增加selectByExampleWithRelation接口方法
        if (introspectedTable.getRules().generateRelationWithSubSelected()) {
            bizInf.addMethod(serviceMethods.getSelectWithRelationMethod(bizInf, true));
        }

        //增加selectByExampleWithChildrenCount接口方法
        if (introspectedTable.getColumn(DefaultColumnNameEnum.PARENT_ID.columnName()).isPresent()) {
            if (introspectedTable.getTableConfiguration().getJavaModelGeneratorConfiguration().isEnableChildren()) {
                bizInf.addMethod(serviceMethods.getSelectWithChildrenCountMethod(bizInf, true,true));
            }
            bizInf.addMethod(serviceMethods.getSelectByKeysWithAllParentMethod(bizInf, true, true));
            bizInf.addMethod(serviceMethods.getSelectByKeysWithChildrenMethod(bizInf, true, true));
        }

        //增加SelectBySqlMethod
        introspectedTable.getTableConfiguration().getSelectBySqlMethodGeneratorConfigurations().forEach(c -> {
            Method method = serviceMethods.getSelectBySqlMethodMethod(bizInf, c, true, true);
            bizInf.addMethod(method);
        });

        //增加selectByColumnXXX
        if (!introspectedTable.getTableConfiguration().getSelectByColumnGeneratorConfigurations().isEmpty()) {
            for (SelectByColumnGeneratorConfiguration config : introspectedTable.getTableConfiguration().getSelectByColumnGeneratorConfigurations()) {
                Method method = serviceMethods.getSelectByColumnMethod(entityType, bizInf, config, true);
                bizInf.addMethod(method);
            }
        }

        // 增加statisticsTotal
        if (introspectedTable.getRules().isGenerateStatisticsElement(StatTypeEnum.TOTAL)) {
            introspectedTable.getTableConfiguration().getJavaServiceImplGeneratorConfiguration().getStatisticsConfigurations().forEach(c -> {
                Method statisticsTotalMethod = serviceMethods.getStatisticsTotalMethod(bizInf, c, true,true);
                bizInf.addMethod(statisticsTotalMethod);
            });
        }

        // 增加statisticsCustom
        if (introspectedTable.getRules().isGenerateCustomStatisticsElement()) {
            introspectedTable.getTableConfiguration().getJavaServiceImplGeneratorConfiguration().getStatisticsCustomConfigurations().forEach(c -> {
                Method customMethod = serviceMethods.getStatisticsCustomMethod(bizInf, c, true,true);
                bizInf.addMethod(customMethod);
            });
        }

        //增加deleteByColumnXXX
        introspectedTable.getTableConfiguration().getSelectByColumnGeneratorConfigurations().stream()
                .filter(SelectByColumnGeneratorConfiguration::isEnableDelete).forEach(c -> bizInf.addMethod(serviceMethods.getDeleteByColumnMethod(bizInf, c, true)));

        //增加selectByTableXXX
        List<SelectByTableGeneratorConfiguration> selectByTableConfiguration = introspectedTable.getTableConfiguration().getSelectByTableGeneratorConfiguration();
        for (SelectByTableGeneratorConfiguration config : selectByTableConfiguration) {
            Method selectByTable = serviceMethods.getSelectByTableMethod(entityType, bizInf, config, true);
            bizInf.addMethod(selectByTable);
        }

        /*
         *  getSelectByKeysDictMethod
         * */
        if (introspectedTable.getRules().isGenerateCachePo()) {
            var cacheConfiguration = introspectedTable.getTableConfiguration().getVoCacheGeneratorConfiguration();
            bizInf.addMethod(serviceMethods.getSelectByKeysDictMethod(bizInf, cacheConfiguration, true, true));
            if (!cacheConfiguration.getDictCacheOrderColumns().isEmpty()) {
                bizInf.addMethod(serviceMethods.getSelectByKeysDictForPrefetchMethod(
                        bizInf, cacheConfiguration, true));
            }
        }

        //deleteByTableXXXX
        introspectedTable.getTableConfiguration().getSelectByTableGeneratorConfiguration().stream()
                .filter(SelectByTableGeneratorConfiguration::isEnableSplit).forEach(c -> bizInf.addMethod(serviceMethods.getSplitUnionByTableMethod(bizInf, c, true, false)));

        //insertByTableXXXX
        introspectedTable.getTableConfiguration().getSelectByTableGeneratorConfiguration().stream()
                .filter(SelectByTableGeneratorConfiguration::isEnableUnion).forEach(c -> bizInf.addMethod(serviceMethods.getSplitUnionByTableMethod(bizInf, c, true, true)));

        Method method = serviceMethods.getSelectByMultiStringIdsMethod(bizInf, true);
        bizInf.addMethod(method);

        //如果是工作流实例，增加流程相关方法
        if (GenerateUtils.isWorkflowInstance(introspectedTable)) {
            Method cleanupInvalidRecordsMethod = serviceMethods.getCleanupInvalidRecordsMethod(bizInf, true);
            bizInf.addMethod(cleanupInvalidRecordsMethod);
        }

        // 增加类的javaDoc注释
        commentGenerator.addModelClassComment(bizInf,
                VStringUtil.format("{0}服务接口类，该接口提供了{0}的基本业务方法。", introspectedTable.getRemarks(true)),
                true,
                classComments.toArray(new String[0]));

        List<CompilationUnit> answer = new ArrayList<>();
        boolean serviceGenerated = plugins.serviceGenerated(bizInf, introspectedTable);
        if (introspectedTable.getRules().isGenerateExcelImportWorkflow()) {
            if (!serviceGenerated) {
                throw new IllegalArgumentException("excelImport requires the generated Service interface for "
                        + introspectedTable.getFullyQualifiedTable());
            }
            validateExcelImportServiceMethods(bizInf);
        }
        if (serviceGenerated) {
            answer.add(bizInf);
        }

        //生成子接口
        Interface bizSubInf = new Interface(
                getInterfaceClassShortName(serviceInfConfiguration.getTargetPackage(),
                        entityType.getShortName()));
        commentGenerator.addJavaFileComment(bizSubInf,true);
        bizSubInf.setVisibility(JavaVisibility.PUBLIC);
        bizSubInf.addSuperInterface(bizInf.getType());
        bizSubInf.addImportedType(bizInf.getType());

        // 增加类的javaDoc注释
        commentGenerator.addModelClassComment(bizSubInf,
                VStringUtil.format("可重写的{0}的服务接口。", introspectedTable.getRemarks(true)),
                false,
                VStringUtil.format("该类继承自 '{'@link {0}'}'，",bizInf.getType().getShortNameWithoutTypeArguments()),
                VStringUtil.format("用于扩展或重写 {0}。",bizInf.getType().getShortNameWithoutTypeArguments()));
        boolean overridable = Mb3GenUtil.fileOverridable(serviceInfConfiguration.getTargetProject(), serviceInfConfiguration.getTargetPackage(), "I" + entityType.getShortName()+".java");
        if (overridable) {
            boolean subServiceGenerated = plugins.subServiceGenerated(bizSubInf, introspectedTable);
            if (introspectedTable.getRules().isGenerateExcelImportWorkflow() && !subServiceGenerated) {
                throw new IllegalArgumentException("excelImport requires the generated Service extension interface for "
                        + introspectedTable.getFullyQualifiedTable());
            }
            if (subServiceGenerated) {
                answer.add(bizSubInf);
            }
        }

        FullyQualifiedJavaType rootClass = new FullyQualifiedJavaType(JavaBeansUtil.getRootClass(introspectedTable));
        EntityAbstractParentEnum entityAbstractParentEnum = EntityAbstractParentEnum.ofCode(rootClass.getShortName());
        String moduleKey = Mb3GenUtil.getModelKey(introspectedTable);
        String mid = Mb3GenUtil.getModelId(introspectedTable);
        //生成该数据库的模块数据
        if (introspectedTable.getTableConfiguration().isModules() && context.isUpdateModuleData()) {
            String pId = Mb3GenUtil.getModelCateId(context);
            int size = context.getModuleDataScriptLines().size() + 1;
            InsertSqlBuilder sqlForModule = GenerateSqlTemplate.insertSqlForModule();
            sqlForModule.updateStringValues("id_", mid);
            sqlForModule.updateStringValues("code_", moduleKey);
            sqlForModule.updateStringValues("name_", introspectedTable.getRemarks(true));
            sqlForModule.updateStringValues("parent_id", pId);
            sqlForModule.updateValues("sort_", String.valueOf(size));
            sqlForModule.updateValues("wf_apply", entityAbstractParentEnum != null && entityAbstractParentEnum.scope() == 1 ? "1" : "0");
            sqlForModule.updateStringValues("category_", pId);
            sqlForModule.updateStringValues("bean_name", introspectedTable.getControllerBeanName());
            sqlForModule.updateStringValues("res_base_path", Mb3GenUtil.getControllerBaseMappingPath(introspectedTable));
            if (!introspectedTable.getTableConfiguration().getHtmlMapGeneratorConfigurations().isEmpty()) {
                sqlForModule.updateStringValues("view_path", introspectedTable.getTableConfiguration().getHtmlMapGeneratorConfigurations().get(0).getViewPath());
            }
            sqlForModule.updateStringValues("drop_tables", String.join(",", introspectedTable.getTableConfiguration().getEnableDropTables()));
            sqlForModule.updateStringValues("created_id", GlobalConstant.DEFAULT_SYSTEM_ADMIN_ID);
            sqlForModule.updateStringValues("modified_id", GlobalConstant.DEFAULT_SYSTEM_ADMIN_ID);
            sqlForModule.updateStringValues("tenant_id", GlobalConstant.DEFAULT_TENANT_ID);
            sqlForModule.updateStringValues("org_id", GlobalConstant.DEFAULT_ORG_ID);
            context.addModuleDataScriptLine(mid, sqlForModule.toSql() + ";");
        }
        //生成该数据库的流程定义数据
        String processDefinedKey = introspectedTable.getTableConfiguration().getProperty("processDefinedKey");
        if (GenerateUtils.isWorkflowInstance(introspectedTable) && VStringUtil.stringHasValue(processDefinedKey)) {
            List<HtmlGeneratorConfiguration> configurations = introspectedTable.getTableConfiguration().getHtmlMapGeneratorConfigurations();
            final String viewPath = configurations!=null && !configurations.isEmpty() ? configurations.get(0).getViewPath() : "";
            VStringUtil.splitToList(processDefinedKey).stream()
                    .map(String::trim)
                    .filter(VStringUtil::stringHasValue)
                    .forEach(procKey -> {
                        int size = context.getWfProcTypeDataScriptLines().size() + 1;
                        InsertSqlBuilder sqlForWfProcType = GenerateSqlTemplate.insertSqlForWfProcType();
                        String id = VMD5Util.MD5_15(mid + procKey);
                        sqlForWfProcType.updateStringValues("id_", id);
                        sqlForWfProcType.updateStringValues("module_id", mid);
                        sqlForWfProcType.updateValues("sort_", String.valueOf(1000+size));
                        sqlForWfProcType.updateStringValues("file_category", introspectedTable.getRemarks(true));
                        sqlForWfProcType.updateStringValues("proc_def_key", procKey);
                        sqlForWfProcType.updateStringValues("biz_view_path", viewPath);
                        sqlForWfProcType.updateStringValues("bean_name", introspectedTable.getControllerBeanName());
                        sqlForWfProcType.updateStringValues("fld_number_rule", introspectedTable.getRemarks(true));
                        sqlForWfProcType.updateStringValues("created_id", GlobalConstant.DEFAULT_SYSTEM_ADMIN_ID);
                        sqlForWfProcType.updateStringValues("modified_id", GlobalConstant.DEFAULT_SYSTEM_ADMIN_ID);
                        sqlForWfProcType.updateStringValues("tenant_id", GlobalConstant.DEFAULT_TENANT_ID);
                        sqlForWfProcType.updateStringValues("org_id", GlobalConstant.DEFAULT_ORG_ID);
                        context.addWfProcTypeDataScriptLines(id, sqlForWfProcType.toSql() + ";");
                    });
        }
        return answer;
    }

    private void validateExcelImportServiceMethods(Interface service) {
        FullyQualifiedJavaType importVo = introspectedTable.getTableConfiguration()
                .getVoGeneratorConfiguration().getVoExcelImportWorkflowConfiguration().getExcelImportType();
        boolean preview = service.getMethods().stream().anyMatch(method ->
                "previewExcelImport".equals(method.getName())
                        && method.getParameters().size() == 1
                        && method.getReturnType().map(returnType -> returnType.getFullyQualifiedName().startsWith(
                                "com.vgosoft.plugins.excel.pojo.vo.VgoExcelImportPreviewVo")).orElse(false)
                        && "org.springframework.web.multipart.MultipartFile".equals(
                                method.getParameters().get(0).getType().getFullyQualifiedName()));
        boolean confirm = service.getMethods().stream().anyMatch(method ->
                "confirmExcelImport".equals(method.getName())
                        && method.getParameters().size() == 3
                        && method.getReturnType().map(returnType -> "com.vgosoft.plugins.excel.pojo.vo.VgoExcelImportResultVo"
                                .equals(returnType.getFullyQualifiedName())).orElse(false)
                        && "com.vgosoft.plugins.excel.pojo.vo.VgoExcelImportConfirmVo".equals(
                                method.getParameters().get(0).getType().getFullyQualifiedName())
                        && "java.util.List".equals(
                                method.getParameters().get(1).getType().getFullyQualifiedNameWithoutTypeParameters())
                        && method.getParameters().get(1).getType().getTypeArguments().size() == 1
                        && importVo.equals(method.getParameters().get(1).getType().getTypeArguments().get(0))
                        && "java.lang.String".equals(
                                method.getParameters().get(2).getType().getFullyQualifiedName()));
        if (!preview || !confirm) {
            throw new IllegalArgumentException("excelImport Service methods were removed or changed by a plugin for "
                    + introspectedTable.getFullyQualifiedTable());
        }
    }

    /**
     * 获得service类的抽象实现类
     *
     * @param abstractIntrospectedTable 生成基类
     */
    protected String getServiceInterface(AbstractIntrospectedTable abstractIntrospectedTable) {
        if (GenerateUtils.isBlobInstance(abstractIntrospectedTable)) {
            String steamOutType = abstractIntrospectedTable.getConfigPropertyValue(PropertyRegistry.TABLE_JAVA_MODEL_BYTE_STREAM_OUTPUT_MODE);
            switch (steamOutType) {
                case "bytes":
                    return MBG_BLOB_BYTES_SERVICE;
                case "file":
                    return MBG_BLOB_FILE_SERVICE;
                case "string":
                    return MBG_BLOB_STRING_SERVICE;
                default:
                    return MBG_BLOB_SERVICE_INTERFACE;
            }
        }
        return MBG_SERVICE_INTERFACE;
    }
}
