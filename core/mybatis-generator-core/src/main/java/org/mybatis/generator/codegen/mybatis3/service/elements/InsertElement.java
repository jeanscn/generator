package org.mybatis.generator.codegen.mybatis3.service.elements;

import com.vgosoft.core.constant.enums.core.EntityEventEnum;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.service.AbstractServiceElementGenerator;
import org.mybatis.generator.config.RelationGeneratorConfiguration;
import org.mybatis.generator.custom.annotations.CacheAnnotationDesc;

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
public class InsertElement extends AbstractServiceElementGenerator {

    public InsertElement() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        parentElement.addImportedType(ANNOTATION_TRANSACTIONAL);
        parentElement.addImportedType(new FullyQualifiedJavaType(SERVICE_CODE_ENUM));

        Method insertMethod = serviceMethods.getInsertMethod(parentElement, false, false,true);

        CacheAnnotationDesc cacheAnnotationDesc = new CacheAnnotationDesc(entityType.getShortName());
        if (introspectedTable.getRules().isGenerateCachePO()) {
            insertMethod.addAnnotation(cacheAnnotationDesc.toCacheEvictAnnotation(true));
        }

        insertMethod.addAnnotation("@Override");
        List<RelationGeneratorConfiguration> configs = introspectedTable.getTableConfiguration().getRelationGeneratorConfigurations().stream()
                .filter(RelationGeneratorConfiguration::isEnableInsert)
                .collect(Collectors.toList());
        if (!configs.isEmpty()) {
            insertMethod.addAnnotation("@Transactional(rollbackFor = Exception.class)");
            outSubBatchMethodBody(insertMethod, "INSERT", "record", parentElement, configs, false);
        }
        //增加PRE_UPDATE事件发布
        if (this.serviceImplConfiguration.getEntityEvent().contains(EntityEventEnum.PRE_UPDATE.name())) {
            insertMethod.addBodyLine("publisher.publishEvent(record, EntityEventEnum.{0});", EntityEventEnum.PRE_UPDATE.name());
        }
        insertMethod.addBodyLine("ServiceResult<{0}> serviceResult = super.insert(record);",entityType.getShortName());
        insertMethod.addBodyLine("if (serviceResult.hasResult()) {");
        //增加UPDATED事件发布
        if (this.serviceImplConfiguration.getEntityEvent().contains(EntityEventEnum.UPDATED.name())) {
            insertMethod.addBodyLine("publisher.publishEvent(serviceResult.getResult(), EntityEventEnum.{0});", EntityEventEnum.UPDATED.name());
        }
        insertMethod.addBodyLine("return serviceResult;");
        insertMethod.addBodyLine("} else {");
        insertMethod.addBodyLine("return ServiceResult.failure(ServiceCodeEnum.WARN);");
        insertMethod.addBodyLine("}");
        parentElement.addMethod(insertMethod);
    }
}
