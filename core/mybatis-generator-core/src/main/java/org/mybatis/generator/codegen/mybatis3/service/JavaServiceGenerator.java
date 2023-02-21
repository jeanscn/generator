package org.mybatis.generator.codegen.mybatis3.service;

import com.vgosoft.core.constant.enums.EntityAbstractParentEnum;
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
import org.mybatis.generator.custom.pojo.SelectByColumnGeneratorConfiguration;
import org.mybatis.generator.custom.pojo.SelectByTableGeneratorConfiguration;
import org.mybatis.generator.internal.util.JavaBeansUtil;

import java.io.File;
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

        JavaServiceGeneratorConfiguration javaServiceGeneratorConfiguration = introspectedTable.getTableConfiguration().getJavaServiceGeneratorConfiguration();


        Interface bizINF = new Interface(
                getGenInterfaceClassShortName(javaServiceGeneratorConfiguration.getTargetPackageGen(),
                        entityType.getShortName()));
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
            bizINF.addMethod(serviceMethods.getSelectWithRelationMethod(entityType, exampleType, bizINF, true, true));
        }

        //增加SelectBySqlMethod
        introspectedTable.getTableConfiguration().getSelectBySqlMethodGeneratorConfigurations().forEach(c -> {
            Method method = serviceMethods.getSelectBySqlMethodMethod(entityType, bizINF, c, true, true);
            bizINF.addMethod(method);
        });

        //增加selectByColumnXXX
        if (introspectedTable.getTableConfiguration().getSelectByColumnGeneratorConfigurations().size() > 0) {
            for (SelectByColumnGeneratorConfiguration config : introspectedTable.getTableConfiguration().getSelectByColumnGeneratorConfigurations()) {
                Method method = serviceMethods.getSelectByColumnMethod(entityType, bizINF, config, true, true);
                bizINF.addMethod(method);
            }
        }

        //增加deleteByColumnXXX
        introspectedTable.getTableConfiguration().getSelectByColumnGeneratorConfigurations().stream()
                .filter(SelectByColumnGeneratorConfiguration::isEnableDelete).forEach(c -> bizINF.addMethod(serviceMethods.getDeleteByColumnMethod(bizINF, c, true)));

        //增加selectByTableXXX
        List<SelectByTableGeneratorConfiguration> selectByTableConfiguration = introspectedTable.getTableConfiguration().getSelectByTableGeneratorConfiguration();
        for (SelectByTableGeneratorConfiguration config : selectByTableConfiguration) {
            Method selectByTable = serviceMethods.getSelectByTableMethod(entityType, bizINF, config, true, true);
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
                .filter(SelectByTableGeneratorConfiguration::isEnableSplit).forEach(c -> bizINF.addMethod(serviceMethods.getSplitUnionByTableMethod(bizINF, c, true, false, true)));

        //insertByTableXXXX
        introspectedTable.getTableConfiguration().getSelectByTableGeneratorConfiguration().stream()
                .filter(SelectByTableGeneratorConfiguration::isEnableUnion).forEach(c -> bizINF.addMethod(serviceMethods.getSplitUnionByTableMethod(bizINF, c, true, true, true)));

        List<CompilationUnit> answer = new ArrayList<>();
        if (plugins.serviceGenerated(bizINF, introspectedTable)) {
            answer.add(bizINF);
        }

        //生成子接口
        Interface bizSubINF = new Interface(
                getInterfaceClassShortName(javaServiceGeneratorConfiguration.getTargetPackage(),
                        entityType.getShortName()));
        commentGenerator.addJavaFileComment(bizSubINF);
        bizSubINF.setVisibility(JavaVisibility.PUBLIC);
        bizSubINF.addSuperInterface(bizINF.getType());
        bizSubINF.addImportedType(bizINF.getType());

        boolean forceGenerateScalableElement = introspectedTable.getRules().isForceGenerateScalableElement(ScalableElementEnum.service.name());
        boolean fileNotExist = JavaBeansUtil.javaFileNotExist(javaServiceGeneratorConfiguration.getTargetProject(), javaServiceGeneratorConfiguration.getTargetPackage(), "I" + entityType.getShortName());
        if (forceGenerateScalableElement || fileNotExist) {
            if (plugins.subServiceGenerated(bizSubINF, introspectedTable)) {
                answer.add(bizSubINF);
            }
        }

        //最后生成该数据库的模块数据
        if (introspectedTable.getTableConfiguration().isModules() && !context.getModuleDataSqlFile().exists()) {
            String moduleKey = StringUtils.lowerCase(context.getModuleKeyword() + "_" + introspectedTable.getTableConfiguration().getDomainObjectName());
            String id = VMD5Util.MD5(moduleKey);
            String pId = VMD5Util.MD5(StringUtils.lowerCase(context.getModuleKeyword()));
            int size = context.getModuleDataScriptLines().size() + 1;
            StringBuilder sb = new StringBuilder("INSERT INTO `SYS_CFG_MODULES` (");
            sb.append("ID_, DELETE_FLAG, MODULE_TAG, MODULE_NAME, PARENT_ID, SORT_, WF_APPLY, CATEGORY_, ORG_ID, MODULE_MANAGER, ");
            sb.append("VERSION_, CREATED_, MODIFIED_, CREATED_ID, MODIFIED_ID, TENANT_ID");
            sb.append(") VALUES (");
            sb.append("'").append(id).append("'");          //id
            sb.append(",").append("0");                                 //DELETE_FLAG
            sb.append(",").append("'").append(moduleKey).append("'");   //MODULE_TAG
            sb.append(",").append("'").append(introspectedTable.getRemarks(true)).append("'");   //MODULE_NAME
            sb.append(",").append("'").append(pId).append("'");   //PARENT_ID
            sb.append(",").append(size);                                //SORT_
            FullyQualifiedJavaType rootClass = new FullyQualifiedJavaType(JavaBeansUtil.getRootClass(introspectedTable));
            EntityAbstractParentEnum entityAbstractParentEnum = EntityAbstractParentEnum.ofCode(rootClass.getShortName());
            if (entityAbstractParentEnum != null && entityAbstractParentEnum.scope() == 1) {
                sb.append(",").append(1);                                //WF_APPLY  是
            } else {
                sb.append(",").append(0);                               //WF_APPLY  否
            }
            sb.append(",").append("'").append(context.getModuleName()).append("'");             //CATEGORY_
            sb.append(",").append("'1503582043420889088'");   //ORG_ID
            sb.append(",").append("'1000010000'");   //MODULE_MANAGER
            sb.append(",").append("1");   //VERSION_
            sb.append(",").append("now()");   //CREATED_
            sb.append(",").append("now()");   //MODIFIED_
            sb.append(",").append("'1000010000'");   //CREATED_ID
            sb.append(",").append("'1000010000'");   //MODIFIED_ID
            sb.append(",").append("'10000'");   //TENANT_ID
            sb.append(");");
            context.addModuleDataScriptLine(id, sb.toString());
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
