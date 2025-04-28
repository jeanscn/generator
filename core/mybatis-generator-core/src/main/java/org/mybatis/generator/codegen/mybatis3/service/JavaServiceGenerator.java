package org.mybatis.generator.codegen.mybatis3.service;

import com.vgosoft.core.constant.enums.core.EntityAbstractParentEnum;
import com.vgosoft.core.constant.enums.db.DefaultColumnNameEnum;
import com.vgosoft.mybatis.generate.GenerateSqlTemplate;
import com.vgosoft.mybatis.sqlbuilder.InsertSqlBuilder;
import com.vgosoft.tool.core.VCollectionUtil;
import com.vgosoft.tool.core.VMD5Util;
import com.vgosoft.tool.core.VStringUtil;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.CommentGenerator;
import org.mybatis.generator.api.FullyQualifiedTable;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.Plugin;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.config.*;
import org.mybatis.generator.custom.ScalableElementEnum;
import org.mybatis.generator.codegen.mybatis3.htmlmapper.GenerateUtils;
import org.mybatis.generator.internal.util.JavaBeansUtil;
import org.mybatis.generator.internal.util.Mb3GenUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.mybatis.generator.custom.ConstantsUtil.*;
import static org.mybatis.generator.internal.util.messages.Messages.getString;

public class JavaServiceGenerator extends AbstractServiceGenerator {


    public JavaServiceGenerator(String project) {
        super(project);
    }

    @Override
    public List<CompilationUnit> getCompilationUnits() {

        FullyQualifiedTable table = introspectedTable.getFullyQualifiedTable();
        progressCallback.startTask(getString("Progress.28", table.toString())); //$NON-NLS-1$
        Plugin plugins = context.getPlugins();
        CommentGenerator commentGenerator = context.getCommentGenerator();

        JavaServiceGeneratorConfiguration serviceInfConfiguration = introspectedTable.getTableConfiguration().getJavaServiceGeneratorConfiguration();

        Interface bizINF = new Interface(
                getGenInterfaceClassShortName(serviceInfConfiguration.getTargetPackageGen(), entityType.getShortName()));
        commentGenerator.addJavaFileComment(bizINF);
        FullyQualifiedJavaType infSuperType = new FullyQualifiedJavaType(getServiceInterface(introspectedTable));
        infSuperType.addTypeArgument(entityType);
        infSuperType.addTypeArgument(exampleType);
        bizINF.addImportedType(infSuperType);
        bizINF.addImportedType(entityType);
        bizINF.addImportedType(exampleType);
        bizINF.setVisibility(JavaVisibility.PUBLIC);
        bizINF.addSuperInterface(infSuperType);
        ServiceMethods serviceMethods = new ServiceMethods(context, introspectedTable);

        /*
          insertBatch
          */
        if (introspectedTable.getRules().generateInsertBatch()) {
            Method method = serviceMethods.getInsertBatchMethod(bizINF, true, true);
            bizINF.addMethod(method);
        }

        /*
         * updateBatch
         * */
        if (introspectedTable.getRules().generateUpdateBatch()) {
            bizINF.addMethod(serviceMethods.getUpdateBatchMethod(bizINF, true, true));
        }

        /*
         * insertOrUpdate
         */
        introspectedTable.getColumn(DefaultColumnNameEnum.DELETE_FLAG.columnName()).ifPresent(column -> {
            Method method = serviceMethods.getUpdateDeleteFlagMethod(bizINF, true, true);
            bizINF.addMethod(method);
        });

        /*
         * insertOrUpdate
         * */
        if (introspectedTable.getRules().generateInsertOrUpdate()) {
            bizINF.addMethod(serviceMethods.getInsertOrUpdateMethod(bizINF, true, true));
        }

        //增加selectByExampleWithRelation接口方法
        if (introspectedTable.getRules().generateRelationWithSubSelected()) {
            bizINF.addMethod(serviceMethods.getSelectWithRelationMethod(bizINF, true));
        }

        //增加selectByExampleWithChildrenCount接口方法
        if (introspectedTable.getColumn(DefaultColumnNameEnum.PARENT_ID.columnName()).isPresent()) {
            if (introspectedTable.getTableConfiguration().getJavaModelGeneratorConfiguration().isGenerateChildren()) {
                bizINF.addMethod(serviceMethods.getSelectWithChildrenCountMethod(bizINF, true,true));
            }
            bizINF.addMethod(serviceMethods.getSelectByKeysWithAllParentMethod(bizINF, true, true));
            bizINF.addMethod(serviceMethods.getSelectByKeysWithChildrenMethod(bizINF, true, true));
        }

        //增加SelectBySqlMethod
        introspectedTable.getTableConfiguration().getSelectBySqlMethodGeneratorConfigurations().forEach(c -> {
            Method method = serviceMethods.getSelectBySqlMethodMethod(bizINF, c, true, true);
            bizINF.addMethod(method);
        });

        //增加selectByColumnXXX
        if (!introspectedTable.getTableConfiguration().getSelectByColumnGeneratorConfigurations().isEmpty()) {
            for (SelectByColumnGeneratorConfiguration config : introspectedTable.getTableConfiguration().getSelectByColumnGeneratorConfigurations()) {
                Method method = serviceMethods.getSelectByColumnMethod(entityType, bizINF, config, true);
                bizINF.addMethod(method);
            }
        }

        //增加deleteByColumnXXX
        introspectedTable.getTableConfiguration().getSelectByColumnGeneratorConfigurations().stream()
                .filter(SelectByColumnGeneratorConfiguration::isEnableDelete).forEach(c -> bizINF.addMethod(serviceMethods.getDeleteByColumnMethod(bizINF, c, true)));

        //增加selectByTableXXX
        List<SelectByTableGeneratorConfiguration> selectByTableConfiguration = introspectedTable.getTableConfiguration().getSelectByTableGeneratorConfiguration();
        for (SelectByTableGeneratorConfiguration config : selectByTableConfiguration) {
            Method selectByTable = serviceMethods.getSelectByTableMethod(entityType, bizINF, config, true);
            bizINF.addMethod(selectByTable);
        }

        /*
         *  getSelectByKeysDictMethod
         * */
        if (introspectedTable.getRules().isGenerateCachePO()) {
            bizINF.addMethod(serviceMethods.getSelectByKeysDictMethod(bizINF,
                    introspectedTable.getTableConfiguration().getVoCacheGeneratorConfiguration(),
                    true, true));
        }

        //deleteByTableXXXX
        introspectedTable.getTableConfiguration().getSelectByTableGeneratorConfiguration().stream()
                .filter(SelectByTableGeneratorConfiguration::isEnableSplit).forEach(c -> bizINF.addMethod(serviceMethods.getSplitUnionByTableMethod(bizINF, c, true, false)));

        //insertByTableXXXX
        introspectedTable.getTableConfiguration().getSelectByTableGeneratorConfiguration().stream()
                .filter(SelectByTableGeneratorConfiguration::isEnableUnion).forEach(c -> bizINF.addMethod(serviceMethods.getSplitUnionByTableMethod(bizINF, c, true, true)));

        Method method = serviceMethods.getSelectByMultiStringIdsMethod(bizINF, true);
        bizINF.addMethod(method);

        //如果是工作流实例，增加流程相关方法
        if (GenerateUtils.isWorkflowInstance(introspectedTable)) {
            Method cleanupInvalidRecordsMethod = serviceMethods.getCleanupInvalidRecordsMethod(bizINF, true);
            bizINF.addMethod(cleanupInvalidRecordsMethod);
        }

        List<CompilationUnit> answer = new ArrayList<>();
        if (plugins.serviceGenerated(bizINF, introspectedTable)) {
            answer.add(bizINF);
        }

        //生成子接口
        Interface bizSubINF = new Interface(
                getInterfaceClassShortName(serviceInfConfiguration.getTargetPackage(),
                        entityType.getShortName()));
        commentGenerator.addJavaFileComment(bizSubINF);
        bizSubINF.setVisibility(JavaVisibility.PUBLIC);
        bizSubINF.addSuperInterface(bizINF.getType());
        bizSubINF.addImportedType(bizINF.getType());

        boolean forceGenerateScalableElement = introspectedTable.getRules().isForceGenerateScalableElement(ScalableElementEnum.service.name());
        boolean fileNotExist = JavaBeansUtil.javaFileNotExist(serviceInfConfiguration.getTargetProject(), serviceInfConfiguration.getTargetPackage(), "I" + entityType.getShortName());
        if (forceGenerateScalableElement || fileNotExist) {
            if (plugins.subServiceGenerated(bizSubINF, introspectedTable)) {
                answer.add(bizSubINF);
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
            }
            return MBG_BLOB_SERVICE_INTERFACE;
        }
        return MBG_SERVICE_INTERFACE;
    }
}
