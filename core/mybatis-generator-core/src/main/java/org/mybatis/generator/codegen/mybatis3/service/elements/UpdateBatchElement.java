package org.mybatis.generator.codegen.mybatis3.service.elements;

import com.vgosoft.core.constant.enums.core.EntityEventEnum;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.service.AbstractServiceElementGenerator;
import org.mybatis.generator.custom.annotations.CacheAnnotationDesc;
import org.mybatis.generator.config.RelationGeneratorConfiguration;
import org.mybatis.generator.internal.util.Mb3GenUtil;

import java.util.List;
import java.util.stream.Collectors;

import static org.mybatis.generator.custom.ConstantsUtil.ANNOTATION_TRANSACTIONAL;
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

        Method method = serviceMethods.getUpdateBatchMethod(parentElement, false,true);
        method.addAnnotation("@Override");
        if (containsPreUpdateEvent || containsUpdatedEvent) {
            Mb3GenUtil.addTransactionalAnnotation(parentElement,method,"READ_COMMITTED");
            parentElement.addImportedType(new FullyQualifiedJavaType(EntityEventEnum.class.getCanonicalName()));
        }
        if (introspectedTable.getRules().isGenerateCachePO()) {
            CacheAnnotationDesc cacheAnnotationDesc = new CacheAnnotationDesc(entityType.getShortName());
            method.addAnnotation(cacheAnnotationDesc.toCacheEvictAnnotation(true));
        }
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
            method.addBodyLine("publisher.publishEvent({0}, EntityEventEnum.{1});", entityType.getShortNameFirstLowCase() + "s",EntityEventEnum.PRE_UPDATE.name());
        }
        method.addBodyLine("int i = mapper.{0}({1});", introspectedTable.getUpdateBatchStatementId(), entityType.getShortNameFirstLowCase() + "s");
        //增加update事件发布
        if (containsUpdatedEvent) {
            method.addBodyLine("publisher.publishEvent({0}, EntityEventEnum.{1});", entityType.getShortNameFirstLowCase() + "s",EntityEventEnum.UPDATED.name());
        }
        method.addBodyLine("return ServiceResult.success({0}, i);", entityType.getShortNameFirstLowCase() + "s");
        method.addBodyLine("} catch (Exception e) {");
        method.addBodyLine("throw new VgoException(ServiceCodeEnum.FAIL.code(), e.getMessage());");
        method.addBodyLine("}");
        parentElement.addMethod(method);
        parentElement.addImportedType("com.vgosoft.core.exception.VgoException");
    }
}
