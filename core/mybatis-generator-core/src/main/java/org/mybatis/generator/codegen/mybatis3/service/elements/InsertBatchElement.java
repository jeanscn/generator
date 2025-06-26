package org.mybatis.generator.codegen.mybatis3.service.elements;

import com.vgosoft.core.constant.enums.core.EntityEventEnum;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.service.AbstractServiceElementGenerator;
import org.mybatis.generator.config.RelationGeneratorConfiguration;
import org.mybatis.generator.internal.util.Mb3GenUtil;

import java.util.List;
import java.util.stream.Collectors;

import static org.mybatis.generator.custom.ConstantsUtil.*;

/**
 * selectByExampleWithRelation实现方法
 *
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-09-23 23:57
 * @version 3.0
 */
public class InsertBatchElement extends AbstractServiceElementGenerator {

    public InsertBatchElement() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        parentElement.addImportedType(FullyQualifiedJavaType.getNewListInstance());
        parentElement.addImportedType(SERVICE_CODE_ENUM);

        boolean containsPreInsertEvent = this.serviceImplConfiguration.getEntityEvent().contains(EntityEventEnum.PRE_INSERT.name());
        boolean containsInsertedEvent = this.serviceImplConfiguration.getEntityEvent().contains(EntityEventEnum.INSERTED.name());

        Method method = serviceMethods.getInsertBatchMethod(parentElement, false,true);
        method.addAnnotation("@Override");
        if (containsPreInsertEvent || containsInsertedEvent) {
            Mb3GenUtil.addTransactionalAnnotation(parentElement,method,"READ_COMMITTED");
            parentElement.addImportedType(new FullyQualifiedJavaType(EntityEventEnum.class.getCanonicalName()));
        }
        List<RelationGeneratorConfiguration> configs = introspectedTable.getTableConfiguration().getRelationGeneratorConfigurations().stream()
                .filter(RelationGeneratorConfiguration::isEnableInsert)
                .collect(Collectors.toList());
        //增加PRE_INSERT事件发布
        if (containsPreInsertEvent) {
            method.addBodyLine("publisher.publishEvent({0}s, EntityEventEnum.{1});", entityType.getShortNameFirstLowCase(),EntityEventEnum.PRE_INSERT.name());
        }
        if (!configs.isEmpty()) {
            Mb3GenUtil.addTransactionalAnnotation(parentElement,method,"READ_COMMITTED");
            method.addBodyLine("for ({0} {1} : {1}s) '{'", entityType.getShortName(), entityType.getShortNameFirstLowCase());
            outSubBatchMethodBody(method, "INSERT", entityType.getShortNameFirstLowCase(), parentElement, configs, false);
            method.addBodyLine("}");
        }
        method.addBodyLine("int i = mapper.{0}({1});", introspectedTable.getInsertBatchStatementId(), entityType.getShortNameFirstLowCase() + "s");
        method.addBodyLine("if (i > 0) {");
        //增加INSERTED事件发布
        if (containsInsertedEvent) {
            method.addBodyLine("publisher.publishEvent({0}, EntityEventEnum.{1});", entityType.getShortNameFirstLowCase() + "s",EntityEventEnum.INSERTED.name());
        }
        method.addBodyLine("return ServiceResult.success({0},i);",entityType.getShortNameFirstLowCase() + "s");
        method.addBodyLine("}else{");
        method.addBodyLine("return ServiceResult.failure(ServiceCodeEnum.WARN);");
        method.addBodyLine("}");
        parentElement.addMethod(method);
    }
}
