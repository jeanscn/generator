package org.mybatis.generator.codegen.mybatis3.service.elements;

import com.vgosoft.core.constant.enums.core.EntityEventEnum;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.service.AbstractServiceElementGenerator;
import org.mybatis.generator.custom.annotations.CacheAnnotationDesc;
import org.mybatis.generator.config.RelationGeneratorConfiguration;
import org.mybatis.generator.internal.util.Mb3GenUtil;

import java.util.List;
import java.util.stream.Collectors;

import static org.mybatis.generator.custom.ConstantsUtil.ANNOTATION_TRANSACTIONAL;

/**
 * selectByExampleWithRelation实现方法
 *
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-09-23 23:57
 * @version 3.0
 */
public class DeleteByExampleElement extends AbstractServiceElementGenerator {

    public DeleteByExampleElement() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        boolean containPreDeleteEvent = this.serviceImplConfiguration.getEntityEvent().contains(EntityEventEnum.PRE_DELETE.name());
        boolean containDeletedEvent = this.serviceImplConfiguration.getEntityEvent().contains(EntityEventEnum.DELETED.name());

        Method deleteByExampleMethod = serviceMethods.getDeleteByExampleMethod(parentElement, false);
        deleteByExampleMethod.addAnnotation("@Override");
        if(containPreDeleteEvent || containDeletedEvent) {
            Mb3GenUtil.addTransactionalAnnotation(parentElement,deleteByExampleMethod,"DEFAULT");
        }

        if (introspectedTable.getRules().isGenerateCachePO()) {
            CacheAnnotationDesc cacheAnnotationDesc = new CacheAnnotationDesc(entityType.getShortName());
            deleteByExampleMethod.addAnnotation(cacheAnnotationDesc.toCacheEvictAnnotation(true));
        }
        List<RelationGeneratorConfiguration> collect = introspectedTable.getTableConfiguration().getRelationGeneratorConfigurations().stream()
                .filter(RelationGeneratorConfiguration::isEnableDelete)
                .collect(Collectors.toList());
        deleteByExampleMethod.addBodyLine("ServiceResult<List<{0}>> result = this.selectByExample(example);", entityType.getShortName(), entityType.getShortNameFirstLowCase());
        deleteByExampleMethod.addBodyLine("if (result.getResult().isEmpty()) return ServiceResult.success(0, 0);");
        if (containPreDeleteEvent) {
            deleteByExampleMethod.addBodyLine("publisher.publishEvent(result.getResult(),EntityEventEnum.{0});", EntityEventEnum.PRE_DELETE.name());
        }
        if (!collect.isEmpty()) {
            Mb3GenUtil.addTransactionalAnnotation(parentElement,deleteByExampleMethod,"DEFAULT");
            deleteByExampleMethod.addBodyLine("for ({0} {1} : result.getResult()) '{'", entityType.getShortName(), entityType.getShortNameFirstLowCase());
            outSubBatchMethodBody(deleteByExampleMethod, "DELETE", entityType.getShortNameFirstLowCase(), parentElement, collect, false);
            deleteByExampleMethod.addBodyLine("}");
        }
        deleteByExampleMethod.addBodyLine("int affectedRows = mapper.deleteByExample(example);");
        deleteByExampleMethod.addBodyLine("if (affectedRows > 0) {");
        if (containDeletedEvent) {
            deleteByExampleMethod.addBodyLine("publisher.publishEvent(result.getResult(),EntityEventEnum.{0});", EntityEventEnum.DELETED.name());
        }
        deleteByExampleMethod.addBodyLine("return ServiceResult.success(affectedRows,affectedRows);");
        deleteByExampleMethod.addBodyLine("}");
        deleteByExampleMethod.addBodyLine("return ServiceResult.failure(ServiceCodeEnum.FAIL);");

        parentElement.addMethod(deleteByExampleMethod);
    }
}
