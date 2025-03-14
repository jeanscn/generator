package org.mybatis.generator.codegen.mybatis3.service.elements;

import com.vgosoft.core.constant.enums.core.EntityEventEnum;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.service.AbstractServiceElementGenerator;
import org.mybatis.generator.config.RelationGeneratorConfiguration;
import org.mybatis.generator.custom.annotations.CacheAnnotationDesc;
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
public class InsertElement extends AbstractServiceElementGenerator {

    public InsertElement() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        parentElement.addImportedType(new FullyQualifiedJavaType(SERVICE_CODE_ENUM));
        boolean containsPreInsertEvent = this.serviceImplConfiguration.getEntityEvent().contains(EntityEventEnum.PRE_INSERT.name());
        boolean containsInsertedEvent = this.serviceImplConfiguration.getEntityEvent().contains(EntityEventEnum.INSERTED.name());

        Method insertMethod = serviceMethods.getInsertMethod(parentElement, false, false,true);
        CacheAnnotationDesc cacheAnnotationDesc = new CacheAnnotationDesc(entityType.getShortName());
        if (introspectedTable.getRules().isGenerateCachePO()) {
            insertMethod.addAnnotation(cacheAnnotationDesc.toCacheEvictAnnotation(true));
        }
        insertMethod.addAnnotation("@Override");
        if (containsPreInsertEvent || containsInsertedEvent) {
            Mb3GenUtil.addTransactionalAnnotation(parentElement,insertMethod,"READ_COMMITTED");
        }
        List<RelationGeneratorConfiguration> configs = introspectedTable.getTableConfiguration().getRelationGeneratorConfigurations().stream()
                .filter(RelationGeneratorConfiguration::isEnableInsert)
                .collect(Collectors.toList());
        if (!configs.isEmpty()) {
            Mb3GenUtil.addTransactionalAnnotation(parentElement,insertMethod,"READ_COMMITTED");
            outSubBatchMethodBody(insertMethod, "INSERT", "record", parentElement, configs, false);
        }
        insertMethod.addBodyLine("try {");
        //增加PRE_UPDATE事件发布
        if (containsPreInsertEvent) {
            insertMethod.addBodyLine("publisher.publishEvent(record, EntityEventEnum.{0});", EntityEventEnum.PRE_INSERT.name());
        }
        insertMethod.addBodyLine("int i = mapper.insert(record);");
        insertMethod.addBodyLine("if (i > 0) {");
        //增加UPDATED事件发布
        if (containsInsertedEvent) {
            insertMethod.addBodyLine("publisher.publishEvent(record, EntityEventEnum.{0});", EntityEventEnum.INSERTED.name());
        }
        insertMethod.addBodyLine("return ServiceResult.success(record, i);");
        insertMethod.addBodyLine("} else {");
        insertMethod.addBodyLine("return ServiceResult.failure(ServiceCodeEnum.WARN, \"插入失败\");");
        insertMethod.addBodyLine("}");

        insertMethod.addBodyLine("} catch (Exception e) {");
        insertMethod.addBodyLine("throw new VgoException(ServiceCodeEnum.FAIL.code(), e.getMessage());");
        insertMethod.addBodyLine("}");
        parentElement.addMethod(insertMethod);
    }
}
