package org.mybatis.generator.codegen.mybatis3.service;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.VOCacheGeneratorConfiguration;
import org.mybatis.generator.custom.ReturnTypeEnum;
import org.mybatis.generator.custom.pojo.SelectByColumnGeneratorConfiguration;
import org.mybatis.generator.custom.pojo.SelectBySqlMethodGeneratorConfiguration;
import org.mybatis.generator.custom.pojo.SelectByTableGeneratorConfiguration;
import org.mybatis.generator.internal.util.JavaBeansUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mybatis.generator.custom.ConstantsUtil.RESPONSE_RESULT;
import static org.mybatis.generator.custom.ConstantsUtil.SERVICE_RESULT;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-10-04 17:36
 * @version 3.0
 */
public class ServiceMethods {

    private final Context context;
    private final IntrospectedTable introspectedTable;
    private final FullyQualifiedJavaType entityType;
    private final FullyQualifiedJavaType exampleType;

    public ServiceMethods(Context context, IntrospectedTable introspectedTable) {
        this.context = context;
        this.introspectedTable = introspectedTable;
        entityType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        exampleType = new FullyQualifiedJavaType(introspectedTable.getExampleType());
    }

    public Method getSelectByPrimaryKeyMethod(CompilationUnit parentElement,boolean isAbstract,boolean isService){
        List<Parameter> parameters = introspectedTable.getPrimaryKeyColumns().stream()
                .map(Parameter::new)
                .collect(Collectors.toList());
        Method method = getMethodByType(introspectedTable.getSelectByPrimaryKeyStatementId(),
                isService?ReturnTypeEnum.SERVICE_RESULT_MODEL:ReturnTypeEnum.MODEL,
                entityType,
                introspectedTable.getRemarks(true) +(isService? "对象ServiceResult封装":"对象"),
                parameters,
                isAbstract,
                parentElement);
        context.getCommentGenerator().addMethodJavaDocLine(method, "基于主键的查询方法");
        return method;

    }

    public Method getDeleteByExampleMethod(CompilationUnit parentElement,boolean isAbstract,boolean isService){

        Method method =  getMethodByType(introspectedTable.getDeleteByExampleStatementId(),
                ReturnTypeEnum.MODEL,
                FullyQualifiedJavaType.getIntInstance(),
                "影响行数",
                Collections.singletonList(new Parameter(exampleType, "example").setRemark("检索条件对象")),
                isAbstract,
                parentElement);
        context.getCommentGenerator().addMethodJavaDocLine(method, "基于example的批量删除方法");
        return method;
    }

    public Method getDeleteByPrimaryKeyMethod(CompilationUnit parentElement,boolean isAbstract,boolean isService){
        List<Parameter> parameters = introspectedTable.getPrimaryKeyColumns().stream()
                .map(Parameter::new)
                .collect(Collectors.toList());
        Method method =  getMethodByType(introspectedTable.getDeleteByPrimaryKeyStatementId(),
                ReturnTypeEnum.MODEL,
                FullyQualifiedJavaType.getIntInstance(),
                "影响行数",
                parameters,
                isAbstract,
                parentElement);
        context.getCommentGenerator().addMethodJavaDocLine(method, "基于主键的删除方法");
        return method;
    }

    public Method getUpdateByExample(CompilationUnit parentElement,boolean isAbstract,boolean isSelective,boolean isService){
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(new Parameter(entityType, "record").setRemark("数据对象"));
        parameters.add(new Parameter(exampleType, "example").setRemark("检索条件对象"));
        Method method =  getMethodByType(
                isSelective?introspectedTable.getUpdateByExampleSelectiveStatementId():introspectedTable.getUpdateByExampleStatementId(),
                ReturnTypeEnum.MODEL,
                FullyQualifiedJavaType.getIntInstance(),
                "影响行数",
                parameters,
                isAbstract,
                parentElement);
        context.getCommentGenerator().addMethodJavaDocLine(method, "基于example的单组数据批量更新的方法");
        return method;
    }

    public Method getUpdateByPrimaryKey(CompilationUnit parentElement,boolean isAbstract,boolean isSelective,boolean isService){
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(new Parameter(entityType, "record").setRemark("主键及其它数据对象"));
        Method method =  getMethodByType(
                isSelective?introspectedTable.getUpdateByPrimaryKeySelectiveStatementId():introspectedTable.getUpdateByPrimaryKeyStatementId(),
                isService?ReturnTypeEnum.SERVICE_RESULT_MODEL:ReturnTypeEnum.MODEL,
                isService?entityType:FullyQualifiedJavaType.getIntInstance(),
                introspectedTable.getRemarks(true)+(isService?"对象ServiceResult封装":"成功更新的记录数"),
                parameters,
                isAbstract,
                parentElement);
        context.getCommentGenerator().addMethodJavaDocLine(method, "基于主键的更新方法");
        return method;
    }

    public Method getUpdateBySql(CompilationUnit parentElement,boolean isAbstract,boolean isService){
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(new Parameter(new FullyQualifiedJavaType("com.vgosoft.mybatis.sqlbuilder.UpdateSqlBuilder"), "updateSqlBuilder").setRemark("SQL语句构造对象"));
        Method method = getMethodByType(
                introspectedTable.getUpdateBySqlStatementId(),
                ReturnTypeEnum.MODEL,
                FullyQualifiedJavaType.getIntInstance(),
                "影响行数",
                parameters,
                isAbstract,
                parentElement);
        context.getCommentGenerator().addMethodJavaDocLine(method, "基于sql构造器的更新方法");
        return method;
    }

    public Method getInsertMethod(CompilationUnit parentElement,boolean isAbstract,boolean isSelective,boolean isService){
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(new Parameter(entityType, "record").setRemark("待插入的数据对象"));
        Method method =  getMethodByType(
                isSelective?introspectedTable.getInsertStatementId():introspectedTable.getInsertSelectiveStatementId(),
                isService?ReturnTypeEnum.SERVICE_RESULT_MODEL:ReturnTypeEnum.MODEL,
                isService?entityType:FullyQualifiedJavaType.getIntInstance(),
                isService?introspectedTable.getRemarks(true)+"对象ServiceResult封装":"成功插入的记录数",
                parameters,
                isAbstract,
                parentElement);
        context.getCommentGenerator().addMethodJavaDocLine(method, "单条记录新增的方法");
        return method;
    }

    public Method getInsertBatchMethod(CompilationUnit parentElement,boolean isAbstract,boolean isService) {
        FullyQualifiedJavaType listInstance = FullyQualifiedJavaType.getNewListInstance();
        listInstance.addTypeArgument(entityType);
        Method method = getMethodByType(introspectedTable.getInsertBatchStatementId(),
                isService?ReturnTypeEnum.SERVICE_RESULT_LIST:ReturnTypeEnum.MODEL,
                isService?entityType:FullyQualifiedJavaType.getIntInstance(),
                isService?introspectedTable.getRemarks(true)+"对象列表ServiceResult封装":"成功插入的记录数",
                listInstance,
                entityType.getShortNameFirstLowCase() + "s",
                "待新增的数据对象列表",
                isAbstract,
                parentElement);
        context.getCommentGenerator().addMethodJavaDocLine(method, "批量插入的方法");
        return method;
    }

    public Method getUpdateBatchMethod(CompilationUnit parentElement,
                                          boolean isAbstract,boolean isService) {
        FullyQualifiedJavaType parameterType = FullyQualifiedJavaType.getNewListInstance();
        parameterType.addTypeArgument(entityType);
        Method method = getMethodByType(introspectedTable.getUpdateBatchStatementId(),
                isService?ReturnTypeEnum.SERVICE_RESULT_LIST:ReturnTypeEnum.MODEL,
                isService?entityType:FullyQualifiedJavaType.getIntInstance(),
                introspectedTable.getRemarks(true)+(isService?"对象列表ServiceResult封装":"成功更新的记录数"),
                parameterType,
                entityType.getShortNameFirstLowCase() + "s",
                "待更新的数据对象列表",
                isAbstract,
                parentElement);
        context.getCommentGenerator().addMethodJavaDocLine(method, "批量更新方法");
        return method;
    }

    public Method getInsertOrUpdateMethod(CompilationUnit parentElement,boolean isAbstract,boolean isService) {
        Method method = getMethodByType(introspectedTable.getInsertOrUpdateStatementId(),
                isService?ReturnTypeEnum.SERVICE_RESULT_MODEL:ReturnTypeEnum.MODEL,
                isService?entityType:FullyQualifiedJavaType.getIntInstance(),
                introspectedTable.getRemarks(true)+(isService?"对象ServiceResult封装":"成功插入或更新的记录数"),
                entityType,
                entityType.getShortNameFirstLowCase(),
                "待新增或更新的数据对象",
                isAbstract,
                parentElement);
        context.getCommentGenerator().addMethodJavaDocLine(method,  "插入数据时如果主键重复则更新数据。");
        return method;
    }

    public Method getSelectWithRelationMethod(FullyQualifiedJavaType entityType,
                                                 FullyQualifiedJavaType exampleType,
                                                 CompilationUnit parentElement,
                                                 boolean isAbstract,boolean isService) {
        Method method = getMethodByType(introspectedTable.getSelectByExampleWithRelationStatementId(),
                ReturnTypeEnum.LIST,
                entityType,
                introspectedTable.getRemarks(true)+"对象列表",
                exampleType,
                "example",
                "查询条件example对象",
                isAbstract,
                parentElement);
        context.getCommentGenerator().addMethodJavaDocLine(method, "带所有子查询（集）的查询方法，该查询方法将执行所有子查询，大量数据返回时慎用。","如果无需返回子查询请使用{@link #selectByExample}方法。");
        return method;
    }

    public Method getSelectByColumnMethod(FullyQualifiedJavaType entityType,
                                             CompilationUnit parentElement,
                                             SelectByColumnGeneratorConfiguration config,
                                             boolean isAbstract,boolean isService) {
        IntrospectedColumn column = config.getColumn();
        boolean isSelectBase = JavaBeansUtil.isSelectBaseByPrimaryKeyMethod(config.getMethodName());
        boolean isListParam = config.getParameterType().equals("list");
        FullyQualifiedJavaType paramType = isListParam?FullyQualifiedJavaType.getNewListInstance():config.getColumn().getFullyQualifiedJavaType();
        if (isListParam) {
            paramType.addTypeArgument(config.getColumn().getFullyQualifiedJavaType());
        }
        Method method = getMethodByType(config.getMethodName(),
                isSelectBase ? ReturnTypeEnum.MODEL : ReturnTypeEnum.LIST,
                config.isReturnPrimaryKey() ? FullyQualifiedJavaType.getStringInstance() : entityType,
                introspectedTable.getRemarks(true)+(config.isReturnPrimaryKey() ?"唯一标识列表":"对象列表"),
                paramType,
                config.getColumn().getJavaProperty()+(isListParam?"s":""),
                column.getRemarks(false),
                isAbstract,
                parentElement);
        context.getCommentGenerator().addMethodJavaDocLine(method,"基于"+config.getColumn().getRemarks(true)+"["+config.getColumn().getActualColumnName()+"]的查询方法。该方法常用于为其它方法提供子查询。");
        return method;
    }

    public Method getDeleteByColumnMethod(CompilationUnit parentElement,
                                          SelectByColumnGeneratorConfiguration config,
                                          boolean isAbstract) {
        IntrospectedColumn column = config.getColumn();
        boolean isListParam = config.getParameterType().equals("list");
        FullyQualifiedJavaType paramType = isListParam?FullyQualifiedJavaType.getNewListInstance():config.getColumn().getFullyQualifiedJavaType();
        if (isListParam) {
            paramType.addTypeArgument(config.getColumn().getFullyQualifiedJavaType());
        }
        Method method = getMethodByType(config.getDeleteMethodName(),
                ReturnTypeEnum.MODEL,
                FullyQualifiedJavaType.getIntInstance(),
                "成功删除的行数",
                paramType,
                config.getColumn().getJavaProperty()+(isListParam?"s":""),
                column.getRemarks(false),
                isAbstract,
                parentElement);
        context.getCommentGenerator().addMethodJavaDocLine(method,"基于"+config.getColumn().getRemarks(true)+"["+config.getColumn().getActualColumnName()+"]的删除方法。");
        return method;
    }

    public Method getSelectByTableMethod(FullyQualifiedJavaType entityType,
                                            CompilationUnit parentElement,
                                            SelectByTableGeneratorConfiguration config,
                                            boolean isAbstract,boolean isService) {
        Method method = getMethodByType(config.getMethodName(),
                ReturnTypeEnum.LIST,
                config.isReturnPrimaryKey() ? FullyQualifiedJavaType.getStringInstance() : entityType,
                "相关数据库数据"+ (config.isReturnPrimaryKey() ? "主键列表" : "对象列表"),
                FullyQualifiedJavaType.getStringInstance(),
                config.getParameterName(),
                "中间表中来自其他表的查询键值,字段名：" + config.getOtherPrimaryKeyColumn(),
                isAbstract,
                parentElement);
        context.getCommentGenerator().addMethodJavaDocLine(method,"基于中间表["+config.getTableName()+"]的查询");
        return method;
    }

    public Method getSelectBySqlMethodMethod(FullyQualifiedJavaType entityType,
                                                CompilationUnit parentElement,
                                                SelectBySqlMethodGeneratorConfiguration config,
                                                boolean isAbstract,boolean isService) {
        Method method = getMethodByType(config.getMethodName(),
                isService?ReturnTypeEnum.SERVICE_RESULT_LIST:ReturnTypeEnum.LIST,
                entityType,
                introspectedTable.getRemarks(true)+(isService?"对象列表ServiceResult封装":"对象列表"),
                config.getParentIdColumn().getFullyQualifiedJavaType(),
                config.getParentIdColumn().getJavaProperty(),
                "基于sql函数["+config.getSqlMethod()+"]的查询",
                isAbstract,
                parentElement);
        context.getCommentGenerator().addMethodJavaDocLine(method,"基于sql函数["+config.getSqlMethod()+"]的查询");
        return method;
    }

    public Method getSelectByKeysDictMethod(CompilationUnit parentElement,
                                               VOCacheGeneratorConfiguration config,
                                               boolean isAbstract,boolean isService) {
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
        Method method = getMethodByType(
                introspectedTable.getSelectByKeysDictStatementId(),
                isService?ReturnTypeEnum.SERVICE_RESULT_MODEL:ReturnTypeEnum.LIST,
                isService?config.getFullyQualifiedJavaType():entityType,
                introspectedTable.getRemarks(true)+(isService?"缓存对象包装ServiceResult包装":"实体对象列表"),
                parameters,
                isAbstract,
                parentElement);
        context.getCommentGenerator().addMethodJavaDocLine(method, "查询"+introspectedTable.getRemarks(true)+"缓存数据，将返回单一对象");
        return method;
    }

    public Method getSplitUnionByTableMethod(CompilationUnit parentElement,
                                                SelectByTableGeneratorConfiguration configuration,
                                                boolean isAbstract, boolean isInsert,boolean isService) {
        List<Parameter> parameters = new ArrayList<>();
        Parameter p1 = new Parameter(configuration.getThisColumn().getFullyQualifiedJavaType(), configuration.getThisColumn().getJavaProperty());
        p1.setRemark("当前对象的键值");
        parameters.add(p1);
        FullyQualifiedJavaType listInstance = FullyQualifiedJavaType.getNewListInstance();
        listInstance.addTypeArgument(configuration.getOtherColumn().getFullyQualifiedJavaType());
        Parameter p2 = new Parameter(listInstance, configuration.getOtherColumn().getJavaProperty() + "s");
        p2.setRemark(isInsert?"待关联数据的主键列表":"待解除关联数据的主键列表");
        parameters.add(p2);
        Method method = getMethodByType(
                isInsert?configuration.getUnionMethodName():configuration.getSplitMethodName(),
                ReturnTypeEnum.MODEL,
                FullyQualifiedJavaType.getIntInstance(),
                "影响记录条数",
                parameters,
                isAbstract,
                parentElement);
        context.getCommentGenerator().addMethodJavaDocLine(method, "添加或删除基于中间表的关联数据");
        return method;
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
    public Method getMethodByType(String methodName,
                                     ReturnTypeEnum returnType,
                                     FullyQualifiedJavaType returnTypeArgument,
                                     String returnRemark,
                                     FullyQualifiedJavaType parameterType,
                                     String parameterName,
                                     String parameterRemark,
                                     boolean isAbstract,
                                     CompilationUnit parentElement) {
        Parameter parameter = new Parameter(parameterType, parameterName).setRemark(parameterRemark);
        return getMethodByType(methodName
                ,returnType
                ,returnTypeArgument
                ,returnRemark
                ,Collections.singletonList(parameter)
                ,isAbstract
                ,parentElement);
    }

    public Method getMethodByType(String methodName,
                                     ReturnTypeEnum returnType,
                                     FullyQualifiedJavaType returnTypeArgument,
                                     String returnRemark,
                                     List<Parameter> parameters,
                                     boolean isAbstract,
                                     CompilationUnit parentElement) {
        Method method = new Method(methodName);
        method.setReturnRemark(returnRemark);
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
        return method;
    }

}