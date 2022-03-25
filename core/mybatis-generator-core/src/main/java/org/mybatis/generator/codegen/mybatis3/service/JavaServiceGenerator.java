package org.mybatis.generator.codegen.mybatis3.service;

import org.mybatis.generator.api.*;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.config.JavaServiceGeneratorConfiguration;
import org.mybatis.generator.config.PropertyRegistry;
import org.mybatis.generator.custom.htmlGenerator.GenerateUtils;
import org.mybatis.generator.custom.pojo.CustomMethodProperty;
import org.mybatis.generator.custom.pojo.SelectByColumnProperty;
import org.mybatis.generator.custom.pojo.SelectByTableProperty;

import java.util.ArrayList;
import java.util.List;

import static org.mybatis.generator.internal.util.messages.Messages.getString;

public class JavaServiceGenerator extends AbstractServiceGenerator {

    //service接口父类
    private static final String mBGServiceInterface = "com.vgosoft.mybatis.inf.IMybatisBGService";
    private static final String mBGBlobServiceInterface = "com.vgosoft.mybatis.inf.IMybatisBGBlobService";
    private static final String mBGBlobFileService = "com.vgosoft.mybatis.inf.IMybatisBGBlobFileService";
    private static final String mBGBlobBytesService = "com.vgosoft.mybatis.inf.IMybatisBGBlobBytesService";
    private static final String mBGBlobStringService = "com.vgosoft.mybatis.inf.IMybatisBGBlobStringService";

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

        FullyQualifiedJavaType entityType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        FullyQualifiedJavaType exampleType = new FullyQualifiedJavaType(introspectedTable.getExampleType());
        String interfaceClassShortName = getInterfaceClassShortName(javaServiceGeneratorConfiguration.getTargetPackage(), entityType.getShortName());
        Interface bizINF = new Interface(interfaceClassShortName);
        commentGenerator.addJavaFileComment(bizINF);
        FullyQualifiedJavaType infSuperType = new FullyQualifiedJavaType(getServiceInterface(introspectedTable));
        infSuperType.addTypeArgument(entityType);
        infSuperType.addTypeArgument(exampleType);
        bizINF.addImportedType(infSuperType);
        bizINF.addImportedType(entityType);
        bizINF.addImportedType(exampleType);
        bizINF.setVisibility(JavaVisibility.PUBLIC);
        bizINF.addSuperInterface(infSuperType);
        /*
        //类名
        FullyQualifiedJavaType type = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        TopLevelClass topLevelClass = new TopLevelClass(type);
        topLevelClass.setVisibility(JavaVisibility.PUBLIC);
        //类文件注释
        commentGenerator.addJavaFileComment(topLevelClass);
        //类注释
        commentGenerator.addModelClassComment(topLevelClass, introspectedTable);*/

        //增加selectByExampleWithRelation接口方法
        if (introspectedTable.getRules().generateRelationMap()) {
            Method example = getMethodByType(introspectedTable.getSelectByExampleWithRelationStatementId(), entityType,
                    exampleType, "example", true, "查询条件example对象");
            bizINF.addMethod(example);
            bizINF.addImportedType(FullyQualifiedJavaType.getNewListInstance());
        }

        //增加selectTreeByParentId
        if (introspectedTable.getCustomAddtionalSelectMethods().size() > 0
                && introspectedTable.getCustomAddtionalSelectMethods().containsKey(introspectedTable.getSelectTreeByParentIdStatementId())) {
            CustomMethodProperty customMethodProperty = introspectedTable.getCustomAddtionalSelectMethods().get(introspectedTable.getSelectTreeByParentIdStatementId());
            addAbstractMethodByColumn(bizINF, entityType, customMethodProperty.getParentIdColumn(), introspectedTable.getSelectTreeByParentIdStatementId());
        }

        if (introspectedTable.getSelectByColumnProperties().size() > 0) {
            for (SelectByColumnProperty selectByColumnProperty : introspectedTable.getSelectByColumnProperties()) {
                if (selectByColumnProperty.isReturnPrimaryKey()) {
                    addAbstractMethodByColumn(bizINF, FullyQualifiedJavaType.getStringInstance(), selectByColumnProperty);
                    bizINF.addImportedType(FullyQualifiedJavaType.getStringInstance());
                }else{
                    addAbstractMethodByColumn(bizINF, entityType, selectByColumnProperty);
                }
            }
        }

        if (introspectedTable.getSelectByTableProperties().size()>0) {
            for (SelectByTableProperty selectByTableProperty : introspectedTable.getSelectByTableProperties()) {
                Method selectByTable;
                if (selectByTableProperty.isReturnPrimaryKey()) {
                    selectByTable = getMethodByType(selectByTableProperty.getMethodName(), FullyQualifiedJavaType.getStringInstance(),
                            FullyQualifiedJavaType.getStringInstance(), selectByTableProperty.getParameterName(), true,
                            "中间表中来自其他表的查询键值");
                    bizINF.addImportedType(FullyQualifiedJavaType.getStringInstance());
                }else{
                    selectByTable = getMethodByType(selectByTableProperty.getMethodName(), entityType,
                            FullyQualifiedJavaType.getStringInstance(), selectByTableProperty.getParameterName(), true,
                            "中间表中来自其他表的查询键值");
                    bizINF.addImportedType(entityType);
                }
                bizINF.addMethod(selectByTable);
                bizINF.addImportedType(FullyQualifiedJavaType.getNewListInstance());
            }
        }

        List<CompilationUnit> answer = new ArrayList<>();
        answer.add(bizINF);
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
                    return mBGBlobBytesService;
                case "file":
                    return mBGBlobFileService;
                case "string":
                    return mBGBlobStringService;
            }
            return mBGBlobServiceInterface;
        }
        return mBGServiceInterface;
    }

    private void addAbstractMethodByColumn(Interface interFace, FullyQualifiedJavaType entityType, SelectByColumnProperty selectByColumnProperty) {
        addAbstractMethodByColumn(interFace, entityType, selectByColumnProperty.getColumn(), selectByColumnProperty.getMethodName());
    }

    private void addAbstractMethodByColumn(Interface interFace, FullyQualifiedJavaType entityType, IntrospectedColumn parameterColumn, String methodName) {
        Method method = getMethodByColumn(entityType, parameterColumn, methodName, true);
        interFace.addMethod(method);
        interFace.addImportedType(FullyQualifiedJavaType.getNewListInstance());
        interFace.addImportedType(parameterColumn.getFullyQualifiedJavaType());
    }

}
