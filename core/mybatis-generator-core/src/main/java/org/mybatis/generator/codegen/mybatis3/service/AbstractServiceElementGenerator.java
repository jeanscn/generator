package org.mybatis.generator.codegen.mybatis3.service;

import org.mybatis.generator.api.CommentGenerator;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.AbstractGenerator;
import org.mybatis.generator.config.*;
import org.mybatis.generator.custom.ConstantsUtil;
import org.mybatis.generator.custom.RelationTypeEnum;
import org.mybatis.generator.custom.ReturnTypeEnum;
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

    protected ServiceMethods serviceMethods;

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
        serviceMethods = new ServiceMethods(context, introspectedTable);
    }

    protected void outSubBatchMethodBody(Method method, String actionType, String entityVar, TopLevelClass parent, List<RelationGeneratorConfiguration> configs, boolean resultInt) {
        for (RelationGeneratorConfiguration config : configs) {
            boolean isCollection = config.getType().equals(RelationTypeEnum.collection);
            if (isCollection) {
                method.addBodyLine("if ({0}({1}, {1}.{2}(), ActionCateEnum.{3})"
                        , config.getPropertyName() + SUFFIX_INSERT_UPDATE_BATCH
                        , entityVar
                        , JavaBeansUtil.getGetterMethodName(config.getPropertyName(), FullyQualifiedJavaType.getStringInstance())
                        , actionType);
                method.addBodyLine("        .stream()");
                method.addBodyLine("        .anyMatch(r -> !r.isSuccess())) {");
            } else {
                method.addBodyLine("if (!{0}({1}, {1}.{2}(), ActionCateEnum.{3})"
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
}
