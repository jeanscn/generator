package org.mybatis.generator.codegen.mybatis3.service;

import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.*;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.config.JavaServiceImplGeneratorConfiguration;
import org.mybatis.generator.config.PropertyRegistry;
import org.mybatis.generator.custom.htmlGenerator.GenerateUtils;
import org.mybatis.generator.custom.pojo.CustomMethodGeneratorConfiguration;
import org.mybatis.generator.custom.pojo.RelationGeneratorConfiguration;
import org.mybatis.generator.custom.pojo.SelectByColumnGeneratorConfiguration;
import org.mybatis.generator.custom.pojo.SelectByTableGeneratorConfiguration;
import org.mybatis.generator.internal.util.JavaBeansUtil;

import java.util.ArrayList;
import java.util.List;

import static org.mybatis.generator.custom.ConstantsUtil.*;
import static org.mybatis.generator.internal.util.messages.Messages.getString;

public class JavaServiceImplGenerator extends AbstractServiceGenerator {


    public JavaServiceImplGenerator(String project) {
        super(project);
    }

    @Override
    public List<CompilationUnit> getCompilationUnits() {
        JavaServiceImplGeneratorConfiguration javaServiceImplGeneratorConfiguration = introspectedTable.getTableConfiguration().getJavaServiceImplGeneratorConfiguration();
        FullyQualifiedTable table = introspectedTable.getFullyQualifiedTable();
        progressCallback.startTask(getString("Progress.38", table.toString()));
        CommentGenerator commentGenerator = context.getCommentGenerator();
        Plugin plugins = context.getPlugins();

        FullyQualifiedJavaType entityType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        FullyQualifiedJavaType exampleType = new FullyQualifiedJavaType(introspectedTable.getExampleType());


        FullyQualifiedJavaType importAnnotation = new FullyQualifiedJavaType(ANNOTATION_SERVICE);
        FullyQualifiedJavaType implSuperType = getServiceSupperType(entityType, exampleType, introspectedTable);
        String interfaceClassShortName = getInterfaceClassShortName(introspectedTable.getTableConfiguration().getJavaServiceGeneratorConfiguration().getTargetPackage(), entityType.getShortName());

        Interface bizINF = new Interface(interfaceClassShortName);
        String implClazzName = JavaBeansUtil.getFirstCharacterUppercase(introspectedTable.getControllerBeanName());
        FullyQualifiedJavaType bizClazzImplType = new FullyQualifiedJavaType(javaServiceImplGeneratorConfiguration.getTargetPackage() + "." + implClazzName);
        TopLevelClass bizClazzImpl = new TopLevelClass(bizClazzImplType);
        commentGenerator.addJavaFileComment(bizClazzImpl);
        bizClazzImpl.addImportedType(implSuperType);
        bizClazzImpl.addImportedType(entityType);
        bizClazzImpl.addImportedType(exampleType);
        bizClazzImpl.addImportedType(bizINF.getType());
        bizClazzImpl.addImportedType("lombok.RequiredArgsConstructor");
        bizClazzImpl.setVisibility(JavaVisibility.PUBLIC);
        bizClazzImpl.setSuperClass(implSuperType);
        bizClazzImpl.addSuperInterface(bizINF.getType());
        bizClazzImpl.addAnnotation("@RequiredArgsConstructor");

        StringBuilder sb = new StringBuilder();
        //增加selectByExampleWithRelation接口实现方法
        if (introspectedTable.getRelationGeneratorConfigurations().size() > 0) {
            long count = introspectedTable.getRelationGeneratorConfigurations().stream().filter(RelationGeneratorConfiguration::isSubSelected).count();
            if (count>0) {
                Method example = getMethodByType(introspectedTable.getSelectByExampleWithRelationStatementId(),
                        entityType, exampleType, "example", false, "查询条件example对象");
                example.addAnnotation("@Override");
                sb.setLength(0);
                sb.append("return mapper.");
                sb.append(introspectedTable.getSelectByExampleWithRelationStatementId());
                sb.append("(example);");
                example.addBodyLine(sb.toString());
                bizClazzImpl.addMethod(example);
                bizClazzImpl.addImportedType(FullyQualifiedJavaType.getNewListInstance());
                addJavaMapper(introspectedTable, bizClazzImpl);
            }
        }
        if (introspectedTable.getTableConfiguration().getSelectByColumnGeneratorConfigurations().size() > 0) {
            for (SelectByColumnGeneratorConfiguration selectByColumnGeneratorConfiguration : introspectedTable.getTableConfiguration().getSelectByColumnGeneratorConfigurations()) {
                IntrospectedColumn foreignKeyColumn = selectByColumnGeneratorConfiguration.getColumn();
                Method methodByColumn;
                if (selectByColumnGeneratorConfiguration.isReturnPrimaryKey()) {
                    methodByColumn = getMethodByColumn(FullyQualifiedJavaType.getStringInstance(), foreignKeyColumn, selectByColumnGeneratorConfiguration.getMethodName(), false);
                    bizClazzImpl.addImportedType(FullyQualifiedJavaType.getStringInstance().getFullyQualifiedName());
                }else{
                    methodByColumn = getMethodByColumn(entityType, foreignKeyColumn, selectByColumnGeneratorConfiguration.getMethodName(), false);
                }
                methodByColumn.addAnnotation("@Override");
                addJavaMapper(introspectedTable, bizClazzImpl);
                if (JavaBeansUtil.isSelectBaseByPrimaryKeyMethod(selectByColumnGeneratorConfiguration.getMethodName())) {
                    bizClazzImpl.addImportedType(SERVICE_RESULT);
                    bizClazzImpl.addImportedType(SERVICE_CODE_ENUM);
                    String entityVar = JavaBeansUtil.getFirstCharacterLowercase(entityType.getShortName());
                    methodByColumn.addBodyLine("try{");
                    String format = VStringUtil.format("{0} {1} = mapper.selectBaseByPrimaryKey({2});",entityType.getShortName(), entityVar, foreignKeyColumn.getJavaProperty());
                    methodByColumn.addBodyLine(format);
                    sb.setLength(0);
                    sb.append("if (").append(entityVar).append("!=null) {");
                    methodByColumn.addBodyLine(sb.toString());
                    methodByColumn.addBodyLine(VStringUtil.format("return ServiceResult.success({0});",entityVar));
                    methodByColumn.addBodyLine("}");
                    methodByColumn.addBodyLine("return ServiceResult.failure(ServiceCodeEnum.WARN);");
                    methodByColumn.addBodyLine("} catch (Exception e) {");
                    methodByColumn.addBodyLine("return ServiceResult.failure(ServiceCodeEnum.RUNTIME_ERROR,e);");
                    methodByColumn.addBodyLine("}");
                }else{
                    sb.setLength(0);
                    sb.append("return mapper.");
                    sb.append(selectByColumnGeneratorConfiguration.getMethodName());
                    sb.append("(");
                    sb.append(foreignKeyColumn.getJavaProperty());
                    sb.append(");");
                    methodByColumn.addBodyLine(sb.toString());
                    bizClazzImpl.addImportedType(FullyQualifiedJavaType.getNewListInstance());
                }
                bizClazzImpl.addMethod(methodByColumn);
                bizClazzImpl.addImportedType(foreignKeyColumn.getFullyQualifiedJavaType());
            }
        }

        //增加selectTreeByParentId
        if (introspectedTable.getCustomAddtionalSelectMethods().size() > 0
                && introspectedTable.getCustomAddtionalSelectMethods().containsKey(introspectedTable.getSelectTreeByParentIdStatementId())) {
            CustomMethodGeneratorConfiguration customMethodGeneratorConfiguration = introspectedTable.getCustomAddtionalSelectMethods().get(introspectedTable.getSelectTreeByParentIdStatementId());
            Method methodByColumn = getMethodByColumn(entityType, customMethodGeneratorConfiguration.getParentIdColumn(),
                    customMethodGeneratorConfiguration.getMethodName(), false);
            methodByColumn.addAnnotation("@Override");
            addJavaMapper(introspectedTable, bizClazzImpl);
            sb.setLength(0);
            sb.append("return mapper.");
            sb.append(customMethodGeneratorConfiguration.getMethodName());
            sb.append("(");
            sb.append(customMethodGeneratorConfiguration.getParentIdColumn().getJavaProperty());
            sb.append(");");
            methodByColumn.addBodyLine(sb.toString());
            bizClazzImpl.addMethod(methodByColumn);
            bizClazzImpl.addImportedType(FullyQualifiedJavaType.getNewListInstance());
            bizClazzImpl.addImportedType(customMethodGeneratorConfiguration.getParentIdColumn().getFullyQualifiedJavaType());
        }

        //增加selectByTable方法
        for (SelectByTableGeneratorConfiguration selectByTableGeneratorConfiguration : introspectedTable.getTableConfiguration().getSelectByTableGeneratorConfiguration()) {
            Method selectByTable;
            if (selectByTableGeneratorConfiguration.isReturnPrimaryKey()) {
                selectByTable = getMethodByType(selectByTableGeneratorConfiguration.getMethodName(), FullyQualifiedJavaType.getStringInstance(),
                        FullyQualifiedJavaType.getStringInstance(), selectByTableGeneratorConfiguration.getParameterName(), false,
                        "中间表中来自其他表的查询键值");
            }else{
                selectByTable = getMethodByType(selectByTableGeneratorConfiguration.getMethodName(), entityType,
                        FullyQualifiedJavaType.getStringInstance(), selectByTableGeneratorConfiguration.getParameterName(), false,
                        "中间表中来自其他表的查询键值");
            }
            selectByTable.setVisibility(JavaVisibility.PUBLIC);
            selectByTable.addAnnotation("@Override");
            addJavaMapper(introspectedTable, bizClazzImpl);
            sb.setLength(0);
            sb.append("return mapper.");
            sb.append(selectByTableGeneratorConfiguration.getMethodName());
            sb.append("(");
            sb.append(selectByTableGeneratorConfiguration.getParameterName());
            sb.append(");");
            selectByTable.addBodyLine(sb.toString());
            bizClazzImpl.addMethod(selectByTable);
            bizClazzImpl.addImportedType(FullyQualifiedJavaType.getNewListInstance());
            bizClazzImpl.addImportedType(FullyQualifiedJavaType.getStringInstance());
        }


        /*是否添加@Service注解*/
        boolean noServiceAnnotation = introspectedTable.getRules().isNoServiceAnnotation();
        if (!noServiceAnnotation) {
            bizClazzImpl.addImportedType(importAnnotation);
            sb.setLength(0);
            sb = new StringBuilder("@Service(\"").append(getTableBeanName(introspectedTable)).append("\")");
            bizClazzImpl.addAnnotation(sb.toString());
        }

        List<CompilationUnit> answer = new ArrayList<>();
        if (plugins.serviceImplGenerated(bizClazzImpl, introspectedTable)) {
            answer.add(bizClazzImpl);
        }
        return answer;
    }

    /**
     * 内部类
     * 获得Service抽象类父类
     */
    private FullyQualifiedJavaType getServiceSupperType(FullyQualifiedJavaType entityType, FullyQualifiedJavaType exampleType, IntrospectedTable introspectedTable) {
        FullyQualifiedJavaType supperType = new FullyQualifiedJavaType(getAbstractService(introspectedTable));
        supperType.addTypeArgument(entityType);
        supperType.addTypeArgument(exampleType);
        return supperType;
    }

    /**
     * 获得service类的抽象实现类
     *
     * @param introspectedTable 生成基类
     */
    private String getAbstractService(IntrospectedTable introspectedTable) {
        if (GenerateUtils.isBlobInstance(introspectedTable)) {
            String steamOutType = introspectedTable.getConfigPropertyValue(PropertyRegistry.TABLE_JAVA_MODEL_BYTE_STREAM_OUTPUT_MODE);
            if (GenerateUtils.isBusinessInstance(introspectedTable)) {
                switch (steamOutType) {
                    case "bytes":
                        return ABSTRACT_BLOB_BYTES_SERVICE_BUSINESS;
                    case "file":
                        return ABSTRACT_BLOB_FILE_SERVICE_BUSINESS;
                    case "string":
                        return ABSTRACT_BLOB_STRING_SERVICE_BUSINESS;
                }
                return ABSTRACT_SERVICE_BUSINESS;
            } else {
                switch (steamOutType) {
                    case "bytes":
                        return ABSTRACT_MBG_BLOB_BYTES_SERVICE;
                    case "file":
                        return ABSTRACT_MBG_BLOB_FILE_SERVICE;
                    case "string":
                        return ABSTRACT_MBG_BLOB_STRING_SERVICE;
                }
                return ABSTRACT_MBG_BLOB_SERVICE_INTERFACE;
            }
        }
        return ABSTRACT_MBG_SERVICE_INTERFACE;
    }

    private void addJavaMapper(IntrospectedTable introspectedTable, TopLevelClass bizClazzImpl) {
        long mapper1 = bizClazzImpl.getFields().stream().filter(f -> f.getName().equalsIgnoreCase("mapper")).count();
        if (mapper1 == 0) {
            Field mapperProperty = getMapperProperty(introspectedTable);
            bizClazzImpl.addField(mapperProperty);
            bizClazzImpl.addImportedType(mapperProperty.getType());
        }
    }

    private Field getMapperProperty(IntrospectedTable introspectedTable) {
        FullyQualifiedJavaType mapperType = new FullyQualifiedJavaType(introspectedTable.getMyBatis3JavaMapperType());
        Field mapper = new Field("mapper", mapperType);
        mapper.setFinal(true);
        mapper.setVisibility(JavaVisibility.PRIVATE);
        return mapper;
    }

    /**
     * 获得对应的操作Bean的名称
     */
    private String getTableBeanName(IntrospectedTable introspectedTable) {
        String implClazzName = introspectedTable.getControllerBeanName();
        return JavaBeansUtil.getFirstCharacterLowercase(implClazzName);
    }
}
