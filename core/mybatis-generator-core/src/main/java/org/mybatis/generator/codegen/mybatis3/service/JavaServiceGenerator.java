package org.mybatis.generator.codegen.mybatis3.service;

import org.mybatis.generator.api.*;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.config.JavaServiceGeneratorConfiguration;
import org.mybatis.generator.config.PropertyRegistry;
import org.mybatis.generator.custom.ReturnTypeEnum;
import org.mybatis.generator.custom.htmlGenerator.GenerateUtils;
import org.mybatis.generator.custom.pojo.CustomMethodGeneratorConfiguration;
import org.mybatis.generator.custom.pojo.SelectByColumnGeneratorConfiguration;
import org.mybatis.generator.custom.pojo.SelectByTableGeneratorConfiguration;
import org.mybatis.generator.internal.util.JavaBeansUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

        /**
         * insertBatch
         * */
        if (introspectedTable.getRules().generateInsertBatch()) {
            bizINF.addMethod(getInsertBatchMethod(entityType, bizINF, true));
        }

        /**
         * updateBatch
         * */
        if (introspectedTable.getRules().generateUpdateBatch()) {
            bizINF.addMethod(getUpdateBatchMethod(entityType, bizINF, true));
        }

        /**
         * insertOrUpdate
         * */
        if (introspectedTable.getRules().generateInsertOrUpdate()) {
            bizINF.addMethod(getInsertOrUpdateMethod(entityType, bizINF,true));
        }

        //增加selectByExampleWithRelation接口方法
        if (introspectedTable.getRules().generateRelationWithSubSelected()) {
            bizINF.addMethod(getSelectWithRelationMethod(entityType, exampleType, bizINF,true));
        }

        //增加selectTreeByParentId
        if (introspectedTable.getCustomAddtionalSelectMethods().containsKey(introspectedTable.getSelectTreeByParentIdStatementId())) {
            CustomMethodGeneratorConfiguration customMethodGeneratorConfiguration = introspectedTable.getCustomAddtionalSelectMethods().get(introspectedTable.getSelectTreeByParentIdStatementId());
            Method method = getSelectTreeByParentIdMethod(entityType, bizINF, customMethodGeneratorConfiguration, true);
            bizINF.addMethod(method);
        }

        //增加selectByColumnXXX
        if (introspectedTable.getTableConfiguration().getSelectByColumnGeneratorConfigurations().size() > 0) {
            for (SelectByColumnGeneratorConfiguration config : introspectedTable.getTableConfiguration().getSelectByColumnGeneratorConfigurations()) {
                Method method = getSelectByColumnMethod(entityType, bizINF, config,true);
                bizINF.addMethod(method);
            }
        }

        //增加selectByTableXXX
        List<SelectByTableGeneratorConfiguration> selectByTableConfiguration = introspectedTable.getTableConfiguration().getSelectByTableGeneratorConfiguration();
        if (selectByTableConfiguration.size()>0) {
            for (SelectByTableGeneratorConfiguration config : selectByTableConfiguration) {
                Method selectByTable = getSelectByTableMethod(entityType, bizINF, config,true);
                bizINF.addMethod(selectByTable);
            }
        }

        /*
         *  getSelectByKeysDictMethod
         * */
        if (introspectedTable.getRules().isGenerateCachePO()) {
            bizINF.addMethod(getSelectByKeysDictMethod(bizINF,
                    introspectedTable.getTableConfiguration().getVoCacheGeneratorConfiguration(),
                    true));
        }

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

        boolean forceGenerateScalableElement = introspectedTable.getRules().isForceGenerateScalableElement();
        boolean fileNotExist = JavaBeansUtil.javaFileNotExist(javaServiceGeneratorConfiguration.getTargetProject(), javaServiceGeneratorConfiguration.getTargetPackage(), "I"+entityType.getShortName());
        if (forceGenerateScalableElement || fileNotExist) {
            if (plugins.subServiceGenerated(bizSubINF, introspectedTable)){
                answer.add(bizSubINF);
            }
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
