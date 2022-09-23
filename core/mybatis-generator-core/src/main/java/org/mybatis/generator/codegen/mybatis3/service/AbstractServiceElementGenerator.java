package org.mybatis.generator.codegen.mybatis3.service;

import org.mybatis.generator.api.CommentGenerator;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.AbstractGenerator;
import org.mybatis.generator.config.*;
import org.mybatis.generator.custom.RelationTypeEnum;
import org.mybatis.generator.custom.ReturnTypeEnum;
import org.mybatis.generator.custom.htmlGenerator.GenerateUtils;
import org.mybatis.generator.custom.pojo.*;
import org.mybatis.generator.internal.util.JavaBeansUtil;
import org.mybatis.generator.internal.util.StringUtility;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mybatis.generator.codegen.mybatis3.service.JavaServiceImplGenerator.SUFFIX_INSERT_UPDATE_BATCH;
import static org.mybatis.generator.custom.ConstantsUtil.*;

public abstract class AbstractServiceElementGenerator extends AbstractGenerator {

    protected FullyQualifiedJavaType entityType;

    protected FullyQualifiedJavaType exampleType;

    protected CommentGenerator commentGenerator;

    protected String serviceBeanName;

    protected FullyQualifiedJavaType entityMappings;

    protected FullyQualifiedJavaType entityVoType;

    protected FullyQualifiedJavaType entityViewVoType;

    protected FullyQualifiedJavaType entityCreateVoType;

    protected FullyQualifiedJavaType entityRequestVoType;

    protected FullyQualifiedJavaType entityUpdateVoType;

    protected FullyQualifiedJavaType entityCachePoType;

    protected FullyQualifiedJavaType entityExcelVoType;

    protected FullyQualifiedJavaType serviceResult;

    protected TableConfiguration tc;

    public abstract void addElements(TopLevelClass parentElement);

    public AbstractServiceElementGenerator() {
        super();
    }

    protected void initGenerator(){
        tc = introspectedTable.getTableConfiguration();
        entityType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        exampleType = new FullyQualifiedJavaType(introspectedTable.getExampleType());
        commentGenerator = context.getCommentGenerator();
        serviceBeanName = introspectedTable.getControllerBeanName();
        serviceResult = new FullyQualifiedJavaType(SERVICE_RESULT);
        String voTargetPackage = StringUtility.substringBeforeLast(context.getJavaModelGeneratorConfiguration().getTargetPackage(), ".")+".pojo";
        entityMappings = new FullyQualifiedJavaType(String.join(".", voTargetPackage,"maps",entityType.getShortName()+"Mappings"));
        entityVoType = new FullyQualifiedJavaType(String.join(".", voTargetPackage,"vo",entityType.getShortName()+"VO"));
        entityViewVoType = new FullyQualifiedJavaType(String.join(".", voTargetPackage,"vo",entityType.getShortName()+"ViewVO"));
        entityCachePoType = new FullyQualifiedJavaType(String.join(".", voTargetPackage,"po",entityType.getShortName()+"CachePO"));
        entityExcelVoType = new FullyQualifiedJavaType(String.join(".", voTargetPackage,"vo",entityType.getShortName()+"ExcelVO"));
        entityRequestVoType = new FullyQualifiedJavaType(String.join(".", voTargetPackage,"vo",entityType.getShortName()+"RequestVO"));
        entityCreateVoType = new FullyQualifiedJavaType(String.join(".", voTargetPackage,"vo",entityType.getShortName()+"CreateVO"));
        entityUpdateVoType = new FullyQualifiedJavaType(String.join(".", voTargetPackage,"vo",entityType.getShortName()+"UpdateVO"));
    }

    protected Method createMethod(String methodPrefix) {
        Method method = new Method(methodPrefix + entityType.getShortName());
        method.setVisibility(JavaVisibility.PROTECTED);
        commentGenerator.addGeneralMethodComment(method, introspectedTable);
        return method;
    }

    protected Method getSelectByPrimaryKeyMethod(CompilationUnit parentElement,boolean isAbstract){
        List<Parameter> parameters = introspectedTable.getPrimaryKeyColumns().stream()
                .map(Parameter::new)
                .collect(Collectors.toList());
        return getMethodByType(introspectedTable.getSelectByPrimaryKeyStatementId(),
                ReturnTypeEnum.SERVICE_RESULT_MODEL,
                entityType,
                parameters,
                isAbstract,
                parentElement);
    }

    protected Method getInsertMethod(CompilationUnit parentElement,boolean isAbstract,boolean isSelective){
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(new Parameter(entityType, "record").setRemark("待持久化的数据对象"));
        return getMethodByType(
                isSelective?introspectedTable.getInsertStatementId():introspectedTable.getInsertSelectiveStatementId(),
                ReturnTypeEnum.SERVICE_RESULT_MODEL,
                entityType,
                parameters,
                isAbstract,
                parentElement);
    }

    protected Method getDeleteByExampleMethod(CompilationUnit parentElement,boolean isAbstract){

        return getMethodByType(introspectedTable.getDeleteByExampleStatementId(),
                ReturnTypeEnum.MODEL,
                FullyQualifiedJavaType.getIntInstance(),
                Collections.singletonList(new Parameter(exampleType, "example").setRemark("检索条件对象")),
                isAbstract,
                parentElement);
    }

    protected Method getDeleteByPrimaryKeyMethod(CompilationUnit parentElement,boolean isAbstract){
        List<Parameter> parameters = introspectedTable.getPrimaryKeyColumns().stream()
                .map(Parameter::new)
                .collect(Collectors.toList());
        return getMethodByType(introspectedTable.getDeleteByPrimaryKeyStatementId(),
                ReturnTypeEnum.MODEL,
                FullyQualifiedJavaType.getIntInstance(),
                parameters,
                isAbstract,
                parentElement);
    }

    protected Method getUpdateByExample(CompilationUnit parentElement,boolean isAbstract,boolean isSelective){
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(new Parameter(entityType, "record").setRemark("数据对象"));
        parameters.add(new Parameter(exampleType, "example").setRemark("检索条件对象"));
        return getMethodByType(
                isSelective?introspectedTable.getUpdateByExampleSelectiveStatementId():introspectedTable.getUpdateByExampleStatementId(),
                ReturnTypeEnum.MODEL,
                FullyQualifiedJavaType.getIntInstance(),
                parameters,
                isAbstract,
                parentElement);
    }

    protected Method getUpdateByPrimaryKey(CompilationUnit parentElement,boolean isAbstract,boolean isSelective){
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(new Parameter(entityType, "record").setRemark("主键及其它数据对象"));
        return getMethodByType(
                isSelective?introspectedTable.getUpdateByPrimaryKeySelectiveStatementId():introspectedTable.getUpdateByPrimaryKeyStatementId(),
                ReturnTypeEnum.SERVICE_RESULT_MODEL,
                entityType,
                parameters,
                isAbstract,
                parentElement);
    }

    protected Method getUpdateBySql(CompilationUnit parentElement,boolean isAbstract){
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(new Parameter(new FullyQualifiedJavaType("com.vgosoft.mybatis.sqlbuilder.UpdateSqlBuilder"), "updateSqlBuilder").setRemark("SQL语句构造对象"));
        return getMethodByType(
                introspectedTable.getUpdateBySqlStatementId(),
                ReturnTypeEnum.MODEL,
                FullyQualifiedJavaType.getIntInstance(),
                parameters,
                isAbstract,
                parentElement);
    }

    protected Method getInsertBatchMethod(FullyQualifiedJavaType entityType,
                                          CompilationUnit parentElement,
                                          boolean isAbstract) {
        FullyQualifiedJavaType listInstance = FullyQualifiedJavaType.getNewListInstance();
        listInstance.addTypeArgument(entityType);
        return getMethodByType(introspectedTable.getInsertBatchStatementId(),
                ReturnTypeEnum.SERVICE_RESULT_LIST,
                entityType,
                listInstance,
                entityType.getShortNameFirstLowCase() + "s",
                "待新增的数据对象列表",
                isAbstract,
                parentElement);
    }

    protected Method getUpdateBatchMethod(FullyQualifiedJavaType entityType,
                                          CompilationUnit parentElement,
                                          boolean isAbstract) {
        FullyQualifiedJavaType parameterType = FullyQualifiedJavaType.getNewListInstance();
        parameterType.addTypeArgument(entityType);
        return getMethodByType(introspectedTable.getUpdateBatchStatementId(),
                ReturnTypeEnum.SERVICE_RESULT_LIST,
                entityType,
                parameterType,
                entityType.getShortNameFirstLowCase() + "s",
                "待更新的数据对象列表",
                isAbstract,
                parentElement);
    }

    protected Method getInsertOrUpdateMethod(FullyQualifiedJavaType entityType,
                                             CompilationUnit parentElement,
                                             boolean isAbstract) {
        return getMethodByType(introspectedTable.getInsertOrUpdateStatementId(),
                ReturnTypeEnum.SERVICE_RESULT_MODEL,
                entityType,
                entityType,
                entityType.getShortNameFirstLowCase(),
                "待新增或更新的数据对象",
                isAbstract,
                parentElement);
    }

    protected Method getSelectWithRelationMethod(FullyQualifiedJavaType entityType,
                                                 FullyQualifiedJavaType exampleType,
                                                 CompilationUnit parentElement,
                                                 boolean isAbstract) {
        return getMethodByType(introspectedTable.getSelectByExampleWithRelationStatementId(),
                ReturnTypeEnum.LIST,
                entityType,
                exampleType,
                "example",
                "查询条件example对象",
                isAbstract,
                parentElement);
    }

    protected Method getSelectByColumnMethod(FullyQualifiedJavaType entityType,
                                             CompilationUnit parentElement,
                                             SelectByColumnGeneratorConfiguration config,
                                             boolean isAbstract) {
        IntrospectedColumn column = config.getColumn();
        boolean isSelectBase = JavaBeansUtil.isSelectBaseByPrimaryKeyMethod(config.getMethodName());
        return getMethodByType(config.getMethodName(),
                isSelectBase ? ReturnTypeEnum.MODEL : ReturnTypeEnum.LIST,
                config.isReturnPrimaryKey() ? FullyQualifiedJavaType.getStringInstance() : entityType,
                FullyQualifiedJavaType.getStringInstance(),
                column.getJavaProperty(),
                column.getRemarks(false),
                isAbstract,
                parentElement);
    }

    protected Method getSelectByTableMethod(FullyQualifiedJavaType entityType,
                                            CompilationUnit parentElement,
                                            SelectByTableGeneratorConfiguration config,
                                            boolean isAbstract) {
        return getMethodByType(config.getMethodName(),
                ReturnTypeEnum.LIST,
                config.isReturnPrimaryKey() ? FullyQualifiedJavaType.getStringInstance() : entityType,
                FullyQualifiedJavaType.getStringInstance(),
                config.getParameterName(),
                "中间表中来自其他表的查询键值,字段名：" + config.getOtherPrimaryKeyColumn(),
                isAbstract,
                parentElement);
    }

    protected Method getSelectTreeByParentIdMethod(FullyQualifiedJavaType entityType,
                                                   CompilationUnit parentElement,
                                                   CustomMethodGeneratorConfiguration config,
                                                   boolean isAbstract) {
        return getMethodByType(config.getMethodName(),
                ReturnTypeEnum.LIST,
                entityType,
                config.getParentIdColumn().getFullyQualifiedJavaType(),
                config.getParentIdColumn().getJavaProperty(),
                config.getParentIdColumn().getRemarks(false),
                isAbstract,
                parentElement);
    }

    protected Method getSelectByKeysDictMethod(CompilationUnit parentElement,
                                               VOCacheGeneratorConfiguration config,
                                               boolean isAbstract) {
        List<IntrospectedColumn> parameterColumns = Stream.of(config.getTypeColumn(), config.getCodeColumn())
                .map(n -> introspectedTable.getColumn(n).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (!introspectedTable.getRules().isGenerateCachePOWithMultiKey()) {
            parameterColumns = introspectedTable.getPrimaryKeyColumns();
        }
        List<Parameter> parameters = parameterColumns.stream()
                .map(p -> new Parameter(p.getFullyQualifiedJavaType(), p.getJavaProperty()).setRemark(p.getRemarks(false)))
                .collect(Collectors.toList());
        return getMethodByType(
                introspectedTable.getSelectByKeysDictStatementId(),
                ReturnTypeEnum.SERVICE_RESULT_MODEL,
                config.getFullyQualifiedJavaType(),
                parameters,
                isAbstract,
                parentElement);
    }

    protected String getInterfaceClassShortName(String targetPackage, String entityTypeShortName) {
        return targetPackage +
                "." + "I" + entityTypeShortName;
    }

    protected String getGenInterfaceClassShortName(String targetPackage, String entityTypeShortName) {
        return targetPackage +
                "." + "IGen" + entityTypeShortName;
    }

    protected void outSubBatchMethodBody(Method method, String actionType, String entityVar, TopLevelClass parent, List<RelationGeneratorConfiguration> configs, boolean resultInt) {
        for (RelationGeneratorConfiguration config : configs) {
            boolean isCollection = config.getType().equals(RelationTypeEnum.collection);
            if (isCollection) {
                method.addBodyLine("if ({0}({1}.getId(), {1}.{2}(), ActionCateEnum.{3})"
                        , config.getPropertyName() + SUFFIX_INSERT_UPDATE_BATCH
                        , entityVar
                        , JavaBeansUtil.getGetterMethodName(config.getPropertyName(), FullyQualifiedJavaType.getStringInstance())
                        , actionType);
                method.addBodyLine("        .stream()");
                method.addBodyLine("        .anyMatch(r -> !r.isSuccess())) {");
            } else {
                method.addBodyLine("if (!{0}({1}.getId(), {1}.{2}(), ActionCateEnum.{3})"
                        , config.getPropertyName() + SUFFIX_INSERT_UPDATE_BATCH
                        , entityVar
                        , JavaBeansUtil.getGetterMethodName(config.getPropertyName(), FullyQualifiedJavaType.getStringInstance())
                        , actionType);
                method.addBodyLine(".isSuccess()) {");
            }
            method.addBodyLine("TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();");
            if (resultInt) {
                method.addBodyLine("return 0;");
            } else {
                method.addBodyLine("return ServiceResult.failure(ServiceCodeEnum.WARN);");
            }
            method.addBodyLine("}");
        }
        parent.addImportedType("org.springframework.transaction.interceptor.TransactionAspectSupport");
    }

    /**
     * 根据给定信息构造类方法
     *
     * @param methodName         要构建的方法名称
     * @param returnType         方法返回类型。l-list、m-model、r-ServiceResult、sl-ServiceResult<List>、sm-ServiceResult<model>
     * @param returnTypeArgument 返回的参数类型T。
     *                           returnType为l：List<T>、m：T、r：ServiceResult<T>、sl:ServiceResult<List<T>>、sl:ServiceResult<T>
     * @param parameterType      方法的参数类型
     * @param parameterName      方法的参数名称
     * @param isAbstract         是否为抽象方法
     * @param parameterRemark             参数的文字说明
     * @param parentElement      父元素
     */
    protected Method getMethodByType(String methodName,
                                     ReturnTypeEnum returnType,
                                     FullyQualifiedJavaType returnTypeArgument,
                                     FullyQualifiedJavaType parameterType,
                                     String parameterName,
                                     String parameterRemark,
                                     boolean isAbstract,
                                     CompilationUnit parentElement) {
        Parameter parameter = new Parameter(parameterType, parameterName).setRemark(parameterRemark);
        return getMethodByType(methodName
                ,returnType
                ,returnTypeArgument
                ,Collections.singletonList(parameter)
                ,isAbstract
                ,parentElement);
    }

    protected Method getMethodByType(String methodName,
                                     ReturnTypeEnum returnType,
                                     FullyQualifiedJavaType returnTypeArgument,
                                     List<Parameter> parameters,
                                     boolean isAbstract,
                                     CompilationUnit parentElement) {
        Method method = new Method(methodName);
        if (isAbstract) {
            method.setAbstract(true);
        } else {
            method.setVisibility(JavaVisibility.PUBLIC);
        }
        method.getParameters().addAll(parameters);
        parameters.forEach(p->parentElement.addImportedType(p.getType()));
        FullyQualifiedJavaType listType = FullyQualifiedJavaType.getNewListInstance();
        FullyQualifiedJavaType serviceResult = new FullyQualifiedJavaType(SERVICE_RESULT);
        FullyQualifiedJavaType responseResult = new FullyQualifiedJavaType(RESPONSE_RESULT);
        switch (ReturnTypeEnum.ofCode(returnType.code())) {
            case LIST:
                listType.addTypeArgument(returnTypeArgument);
                method.setReturnType(listType);
                parentElement.addImportedType(listType);
                parentElement.addImportedType(returnTypeArgument);
                break;
            case SERVICE_RESULT_MODEL:
                serviceResult.addTypeArgument(returnTypeArgument);
                method.setReturnType(serviceResult);
                parentElement.addImportedType(serviceResult);
                parentElement.addImportedType(returnTypeArgument);
                break;
            case SERVICE_RESULT_LIST:
                listType.addTypeArgument(returnTypeArgument);
                serviceResult.addTypeArgument(listType);
                method.setReturnType(serviceResult);
                parentElement.addImportedType(serviceResult);
                parentElement.addImportedType(returnTypeArgument);
                parentElement.addImportedType(listType);
                break;
            case RESPONSE_RESULT_LIST:
                listType.addTypeArgument(returnTypeArgument);
                responseResult.addTypeArgument(listType);
                method.setReturnType(responseResult);
                parentElement.addImportedType(responseResult);
                parentElement.addImportedType(returnTypeArgument);
                parentElement.addImportedType(listType);
                break;
            case RESPONSE_RESULT_MODEL:
                responseResult.addTypeArgument(returnTypeArgument);
                method.setReturnType(responseResult);
                parentElement.addImportedType(responseResult);
                parentElement.addImportedType(returnTypeArgument);
                break;
            default:
                method.setReturnType(returnTypeArgument);
                parentElement.addImportedType(returnTypeArgument);
                break;
        }

        List<String> collect = parameters.stream()
                .map(p -> "@param " + p.getName() + " " + p.getRemark())
                .collect(Collectors.toList());
        collect.add(0,"这个抽象方法通过定制版Mybatis Generator自动生成");
        collect.add(0,"提示 - @mbg.generated");
        String[] strings = collect.toArray(new String[0]);
        context.getCommentGenerator().addMethodJavaDocLine(method, false,strings);
        return method;
    }

}
