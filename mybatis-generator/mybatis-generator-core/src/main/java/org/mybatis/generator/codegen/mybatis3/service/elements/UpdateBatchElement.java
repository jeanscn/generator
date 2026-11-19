package org.mybatis.generator.codegen.mybatis3.service.elements;

import com.vgosoft.core.constant.enums.core.EntityEventEnum;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.service.AbstractServiceElementGenerator;
import org.mybatis.generator.codegen.mybatis3.VersionCheckGenerationValidator;
import org.mybatis.generator.config.RelationGeneratorConfiguration;
import org.mybatis.generator.internal.util.Mb3GenUtil;

import java.util.List;
import java.util.stream.Collectors;

import static org.mybatis.generator.custom.ConstantsUtil.SERVICE_CODE_ENUM;

/**
 * selectByExampleWithRelation实现方法
 *
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-09-23 23:57
 * @version 3.0
 */
public class UpdateBatchElement extends AbstractServiceElementGenerator {

    public UpdateBatchElement() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        parentElement.addImportedType(new FullyQualifiedJavaType(SERVICE_CODE_ENUM));
        boolean containsPreUpdateEvent = this.serviceImplConfiguration.getEntityEvent().contains(EntityEventEnum.PRE_UPDATE.name());
        boolean containsUpdatedEvent = this.serviceImplConfiguration.getEntityEvent().contains(EntityEventEnum.UPDATED.name());
        boolean versionCheck = VersionCheckGenerationValidator.isEnabled(introspectedTable);

        Method method = serviceMethods.getUpdateBatchMethod(parentElement, false,true);
        method.addAnnotation("@Override");
        if (containsPreUpdateEvent || containsUpdatedEvent || (versionCheck && configsEmpty())) {
            Mb3GenUtil.addTransactionalAnnotation(parentElement,method,"READ_COMMITTED");
        }
        if (containsPreUpdateEvent || containsUpdatedEvent) {
            parentElement.addImportedType(new FullyQualifiedJavaType(EntityEventEnum.class.getCanonicalName()));
        }
        addMutationCacheEviction(method, parentElement);
        List<RelationGeneratorConfiguration> configs = introspectedTable.getTableConfiguration().getRelationGeneratorConfigurations().stream()
                .filter(RelationGeneratorConfiguration::isEnableUpdate)
                .collect(Collectors.toList());
        if (!configs.isEmpty()) {
            Mb3GenUtil.addTransactionalAnnotation(parentElement,method,"READ_COMMITTED");
            method.addBodyLine("for ({0} {1} : {1}s) '{'", entityType.getShortName(), entityType.getShortNameFirstLowCase());
            outSubBatchMethodBody(method, "UPDATE", entityType.getShortNameFirstLowCase(), parentElement, configs, false);
            method.addBodyLine("}");
        }
        method.addBodyLine("try {");
        //增加update事件发布
        if (containsPreUpdateEvent) {
            method.addBodyLine("if (publisher != null) {");
            method.addBodyLine("    publisher.publishEvent({0}, EntityEventEnum.{1});", entityType.getShortNameFirstLowCase() + "s",EntityEventEnum.PRE_UPDATE.name());
            method.addBodyLine("}");
        }
        method.addBodyLine("int i = mapper.{0}({1});", introspectedTable.getUpdateBatchStatementId(), entityType.getShortNameFirstLowCase() + "s");
        if (versionCheck) {
            method.addBodyLine("if (i != {0}.size()) '{'", entityType.getShortNameFirstLowCase() + "s");
            method.addBodyLine("TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();");
            method.addBodyLine("return ServiceResult.failure(ServiceCodeEnum.WARN, \"批量更新存在版本冲突，请刷新后重试\");");
            method.addBodyLine("'}'");
            parentElement.addImportedType("org.springframework.transaction.interceptor.TransactionAspectSupport");
        }
        //增加update事件发布
        if (containsUpdatedEvent) {
            method.addBodyLine("if (publisher != null) {");
            method.addBodyLine("    publisher.publishEvent({0}, EntityEventEnum.{1});", entityType.getShortNameFirstLowCase() + "s",EntityEventEnum.UPDATED.name());
            method.addBodyLine("}");
        }
        method.addBodyLine("return ServiceResult.success({0}, i);", entityType.getShortNameFirstLowCase() + "s");
        method.addBodyLine("} catch (Exception e) {");
        method.addBodyLine("throw new VgoException(e.getMessage(), e).setCode(ServiceCodeEnum.FAIL.code());");
        method.addBodyLine("}");
        parentElement.addMethod(method);
        parentElement.addImportedType("com.vgosoft.core.exception.VgoException");
    }

    private boolean configsEmpty() {
        return introspectedTable.getTableConfiguration().getRelationGeneratorConfigurations().stream()
                .noneMatch(RelationGeneratorConfiguration::isEnableUpdate);
    }
}
