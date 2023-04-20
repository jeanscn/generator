package org.mybatis.generator.codegen.mybatis3.service.elements;

import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.service.AbstractServiceElementGenerator;
import org.mybatis.generator.custom.annotations.CacheAnnotation;
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

        Method method = serviceMethods.getInsertOrUpdateMethod(parentElement, false,true);
        if (introspectedTable.getRules().isGenerateCachePO()) {
            CacheAnnotation cacheAnnotation = new CacheAnnotation(entityType.getShortName());
            method.addAnnotation(cacheAnnotation.toCacheEvictAnnotation(true));
        }
        method.addAnnotation("@Override");
        List<RelationGeneratorConfiguration> configs = introspectedTable.getRelationGeneratorConfigurations().stream()
                .filter(RelationGeneratorConfiguration::isEnableInsertOrUpdate)
                .collect(Collectors.toList());
        if (configs.size() > 0) {
            outSubBatchMethodBody(method, "INSERTORUPDATE", entityType.getShortNameFirstLowCase(), parentElement, configs, false);
        }
        method.addBodyLine("int i = mapper.{0}({1});", introspectedTable.getInsertOrUpdateStatementId(), entityType.getShortNameFirstLowCase());
        method.addBodyLine("if (i > 0) {");
        method.addBodyLine("return ServiceResult.success({0},i);", entityType.getShortNameFirstLowCase());
        method.addBodyLine("}else{");
        if (configs.size() > 0) {
            method.addBodyLine("TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();");
        }
        method.addBodyLine("return ServiceResult.failure(ServiceCodeEnum.WARN);");
        method.addBodyLine("}");
        parentElement.addImportedType(SERVICE_CODE_ENUM);
        parentElement.addMethod(method);

    }
}
