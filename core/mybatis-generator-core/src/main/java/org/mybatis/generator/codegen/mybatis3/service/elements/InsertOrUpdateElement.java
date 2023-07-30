package org.mybatis.generator.codegen.mybatis3.service.elements;

import com.vgosoft.core.constant.enums.core.EntityEventEnum;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.service.AbstractServiceElementGenerator;
import org.mybatis.generator.custom.annotations.CacheAnnotationDesc;
import org.mybatis.generator.config.RelationGeneratorConfiguration;

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
public class InsertOrUpdateElement extends AbstractServiceElementGenerator {

    public InsertOrUpdateElement() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        parentElement.addImportedType(SERVICE_CODE_ENUM);

        Method method = serviceMethods.getInsertOrUpdateMethod(parentElement, false,true);
        if (introspectedTable.getRules().isGenerateCachePO()) {
            CacheAnnotationDesc cacheAnnotationDesc = new CacheAnnotationDesc(entityType.getShortName());
            method.addAnnotation(cacheAnnotationDesc.toCacheEvictAnnotation(true));
        }
        method.addAnnotation("@Override");
        List<RelationGeneratorConfiguration> configs = introspectedTable.getTableConfiguration().getRelationGeneratorConfigurations().stream()
                .filter(RelationGeneratorConfiguration::isEnableInsertOrUpdate)
                .collect(Collectors.toList());
        if (!configs.isEmpty()) {
            outSubBatchMethodBody(method, "INSERTORUPDATE", entityType.getShortNameFirstLowCase(), parentElement, configs, false);
        }
        if (this.serviceImplConfiguration.getEntityEvent().contains(EntityEventEnum.PRE_UPDATE.name())) {
            method.addBodyLine("publisher.publishEvent({0}, EntityEventEnum.{1});", entityType.getShortNameFirstLowCase(),EntityEventEnum.PRE_UPDATE.name());
        }
        method.addBodyLine("int i = mapper.{0}({1});", introspectedTable.getInsertOrUpdateStatementId(), entityType.getShortNameFirstLowCase());
        method.addBodyLine("if (i > 0) {");
        if (this.serviceImplConfiguration.getEntityEvent().contains(EntityEventEnum.UPDATED.name())) {
            method.addBodyLine("publisher.publishEvent({0}, EntityEventEnum.{1});", entityType.getShortNameFirstLowCase(),EntityEventEnum.UPDATED.name());
        }
        method.addBodyLine("return ServiceResult.success({0},i);", entityType.getShortNameFirstLowCase());
        method.addBodyLine("}else{");
        method.addBodyLine("return ServiceResult.failure(ServiceCodeEnum.WARN);");
        method.addBodyLine("}");
        parentElement.addMethod(method);

    }
}
