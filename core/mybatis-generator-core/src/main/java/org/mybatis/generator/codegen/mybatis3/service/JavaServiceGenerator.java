package org.mybatis.generator.codegen.mybatis3.service;

import com.vgosoft.core.constant.enums.core.EntityAbstractParentEnum;
import com.vgosoft.core.constant.enums.db.DefaultColumnNameEnum;
import com.vgosoft.mybatis.generate.GenerateSqlTemplate;
import com.vgosoft.mybatis.sqlbuilder.InsertSqlBuilder;
import com.vgosoft.tool.core.VMD5Util;
import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.CommentGenerator;
import org.mybatis.generator.api.FullyQualifiedTable;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.Plugin;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.config.*;
import org.mybatis.generator.custom.enums.ScalableElementEnum;
import org.mybatis.generator.codegen.mybatis3.htmlmapper.GenerateUtils;
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
        commentGenerator.addJavaFileComment(bizInf);
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
            bizInf.addMethod(serviceMethods.getSelectByKeysDictMethod(bizInf,
                    introspectedTable.getTableConfiguration().getVoCacheGeneratorConfiguration(),
                    true, true));
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
                VStringUtil.format("{0}服务接口类，该接口提供了{0}的基本业务方法。",introspectedTable.getRemarks(true)),
                true,
                classComments.toArray(new String[0]));

        List<CompilationUnit> answer = new ArrayList<>();
        if (plugins.serviceGenerated(bizInf, introspectedTable)) {
            answer.add(bizInf);
        }

        //生成子接口
        Interface bizSubInf = new Interface(
                getInterfaceClassShortName(serviceInfConfiguration.getTargetPackage(),
                        entityType.getShortName()));
        commentGenerator.addJavaFileComment(bizSubInf);
        bizSubInf.setVisibility(JavaVisibility.PUBLIC);
        bizSubInf.addSuperInterface(bizInf.getType());
        bizSubInf.addImportedType(bizInf.getType());

        boolean forceGenerateScalableElement = introspectedTable.getRules().isForceGenerateScalableElement(ScalableElementEnum.service.name());
        boolean fileNotExist = JavaBeansUtil.javaFileNotExist(serviceInfConfiguration.getTargetProject(), serviceInfConfiguration.getTargetPackage(), "I" + entityType.getShortName());
        if (forceGenerateScalableElement || fileNotExist) {
            if (plugins.subServiceGenerated(bizSubInf, introspectedTable)) {
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
                        context.addWfProcTypeDataScriptLines(id, sqlForWfProcType.toSql() + ";");
                    });
        }
        return answer;
    }

    /**
     * 获得service类的抽象实现类
     *
     * @param introspectedTable 生成基类
     */
    protected String getServiceInterface(IntrospectedTable introspectedTable) {
        if (GenerateUtils.isBlobInstance(introspectedTable)) {
            String steamOutType = introspectedTable.getConfigPropertyValue(PropertyRegistry.TABLE_JAVA_MODEL_BYTE_STREAM_OUTPUT_MODE);
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
