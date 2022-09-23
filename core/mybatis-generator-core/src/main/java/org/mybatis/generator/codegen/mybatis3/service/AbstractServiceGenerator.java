package org.mybatis.generator.codegen.mybatis3.service;

import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.AbstractJavaGenerator;
import org.mybatis.generator.config.VOCacheGeneratorConfiguration;
import org.mybatis.generator.custom.ReturnTypeEnum;
import org.mybatis.generator.custom.pojo.CacheAnnotation;
import org.mybatis.generator.custom.pojo.CustomMethodGeneratorConfiguration;
import org.mybatis.generator.custom.pojo.SelectByColumnGeneratorConfiguration;
import org.mybatis.generator.custom.pojo.SelectByTableGeneratorConfiguration;
import org.mybatis.generator.internal.util.JavaBeansUtil;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractServiceGenerator extends AbstractJavaGenerator {

    protected FullyQualifiedJavaType entityType;

    protected FullyQualifiedJavaType exampleType;

    public AbstractServiceGenerator(String project) {
        super(project);
    }


    @Override
    public abstract List<CompilationUnit> getCompilationUnits();

    @Override
    public void setIntrospectedTable(IntrospectedTable introspectedTable) {
        super.setIntrospectedTable(introspectedTable);
        entityType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        exampleType = new FullyQualifiedJavaType(introspectedTable.getExampleType());

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
}
