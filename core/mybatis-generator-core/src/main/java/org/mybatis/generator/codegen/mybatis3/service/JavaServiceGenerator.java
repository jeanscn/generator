package org.mybatis.generator.codegen.mybatis3.service;

import com.vgosoft.core.constant.enums.core.EntityAbstractParentEnum;
import com.vgosoft.mybatis.generate.GenerateSqlTemplate;
import com.vgosoft.mybatis.sqlbuilder.InsertSqlBuilder;
import com.vgosoft.tool.core.VMD5Util;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.CommentGenerator;
import org.mybatis.generator.api.FullyQualifiedTable;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.Plugin;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.config.JavaServiceGeneratorConfiguration;
import org.mybatis.generator.config.PropertyRegistry;
import org.mybatis.generator.custom.ScalableElementEnum;
import org.mybatis.generator.custom.htmlGenerator.GenerateUtils;
import org.mybatis.generator.config.SelectByColumnGeneratorConfiguration;
import org.mybatis.generator.config.SelectByTableGeneratorConfiguration;
import org.mybatis.generator.internal.util.JavaBeansUtil;

import java.util.ArrayList;
import java.util.List;

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
                getGenInterfaceClassShortName(serviceInfConfiguration.getTargetPackageGen(),entityType.getShortName()));
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
         * */
        if (introspectedTable.getRules().generateInsertOrUpdate()) {
            bizINF.addMethod(serviceMethods.getInsertOrUpdateMethod(bizINF, true, true));
        }

        //增加selectByExampleWithRelation接口方法
        if (introspectedTable.getRules().generateRelationWithSubSelected()) {
            bizINF.addMethod(serviceMethods.getSelectWithRelationMethod(entityType, exampleType, bizINF, true));
        }

        //增加SelectBySqlMethod
        introspectedTable.getTableConfiguration().getSelectBySqlMethodGeneratorConfigurations().forEach(c -> {
            Method method = serviceMethods.getSelectBySqlMethodMethod(entityType, bizINF, c, true, true);
            bizINF.addMethod(method);
        });

        //增加selectByColumnXXX
        if (introspectedTable.getTableConfiguration().getSelectByColumnGeneratorConfigurations().size() > 0) {
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

        if (introspectedTable.getRules().isModelEnableChildren()) {
            Method method = serviceMethods.getSelectByMultiStringIdsMethod(bizINF, true);
            bizINF.addMethod(method);
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

        //最后生成该数据库的模块数据
        if (introspectedTable.getTableConfiguration().isModules() && context.isUpdateModuleData()) {
            String moduleKey = StringUtils.lowerCase(context.getModuleKeyword() + "_" + introspectedTable.getTableConfiguration().getDomainObjectName());
            String id = VMD5Util.MD5(moduleKey);
            String pId = VMD5Util.MD5(StringUtils.lowerCase(context.getModuleKeyword()));
            int size = context.getModuleDataScriptLines().size() + 1;
            FullyQualifiedJavaType rootClass = new FullyQualifiedJavaType(JavaBeansUtil.getRootClass(introspectedTable));
            EntityAbstractParentEnum entityAbstractParentEnum = EntityAbstractParentEnum.ofCode(rootClass.getShortName());
            InsertSqlBuilder sqlForModule = GenerateSqlTemplate.insertSqlForModule();
            sqlForModule.updateStringValues("id_", id);
            sqlForModule.updateStringValues("module_tag", moduleKey);
            sqlForModule.updateStringValues("module_name", introspectedTable.getRemarks(true));
            sqlForModule.updateStringValues("parent_id", pId);
            sqlForModule.updateValues("sort_", String.valueOf(size));
            sqlForModule.updateValues("wf_apply", entityAbstractParentEnum != null && entityAbstractParentEnum.scope() == 1 ? "1" : "0");
            sqlForModule.updateStringValues("category_", context.getModuleName());
            context.addModuleDataScriptLine(id, sqlForModule.toSql()+";");
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
