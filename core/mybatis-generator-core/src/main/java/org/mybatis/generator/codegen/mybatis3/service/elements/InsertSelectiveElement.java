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
public class InsertSelectiveElement extends AbstractServiceElementGenerator {

    public InsertSelectiveElement() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        parentElement.addImportedType(new FullyQualifiedJavaType(SERVICE_CODE_ENUM));
        boolean containsPreInsertEvent = this.serviceImplConfiguration.getEntityEvent().contains(EntityEventEnum.PRE_INSERT.name());
        boolean containsInsertedEvent = this.serviceImplConfiguration.getEntityEvent().contains(EntityEventEnum.INSERTED.name());

        Method insertSelectiveMethod = serviceMethods.getInsertMethod(parentElement, false, true,true);

        CacheAnnotationDesc cacheAnnotationDesc = new CacheAnnotationDesc(entityType.getShortName());
        if (introspectedTable.getRules().isGenerateCachePO()) {
            insertSelectiveMethod.addAnnotation(cacheAnnotationDesc.toCacheEvictAnnotation(true));
        }

        insertSelectiveMethod.addAnnotation("@Override");
        if (containsPreInsertEvent || containsInsertedEvent) {
            parentElement.addImportedType(ANNOTATION_TRANSACTIONAL);
            parentElement.addImportedType("java.lang.Exception");
            insertSelectiveMethod.addAnnotation("@Transactional(rollbackFor = Exception.class)");
        }
        List<RelationGeneratorConfiguration> configs1 = introspectedTable.getTableConfiguration().getRelationGeneratorConfigurations().stream()
                .filter(RelationGeneratorConfiguration::isEnableInsert)
                .collect(Collectors.toList());
        if (!configs1.isEmpty()) {
            insertSelectiveMethod.addAnnotation("@Transactional(rollbackFor = Exception.class)");
            outSubBatchMethodBody(insertSelectiveMethod, "INSERT", "record", parentElement, configs1, false);
        }
        //增加PRE_INSERT事件发布
        if (containsPreInsertEvent) {
            insertSelectiveMethod.addBodyLine("publisher.publishEvent(record, EntityEventEnum.{0});", EntityEventEnum.PRE_INSERT.name());
        }
        insertSelectiveMethod.addBodyLine("ServiceResult<{0}> serviceResult = super.insertSelective(record);",entityType.getShortName());
        insertSelectiveMethod.addBodyLine("if (serviceResult.hasResult()) {");
        //增加INSERTED事件发布
        if (containsInsertedEvent) {
            insertSelectiveMethod.addBodyLine("publisher.publishEvent(serviceResult.getResult(), EntityEventEnum.{0});", EntityEventEnum.INSERTED.name());
        }
        insertSelectiveMethod.addBodyLine("return serviceResult;");
        insertSelectiveMethod.addBodyLine("} else {\n" +
                "            return ServiceResult.failure(ServiceCodeEnum.WARN, serviceResult.getMessage());\n" +
                "        }");
        parentElement.addMethod(insertSelectiveMethod);
    }
}
