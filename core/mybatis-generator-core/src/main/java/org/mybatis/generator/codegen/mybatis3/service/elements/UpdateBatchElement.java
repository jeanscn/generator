package org.mybatis.generator.codegen.mybatis3.service.elements;

import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.service.AbstractServiceElementGenerator;
import org.mybatis.generator.custom.annotations.CacheAnnotationDesc;
import org.mybatis.generator.config.RelationGeneratorConfiguration;

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
        parentElement.addImportedType(ANNOTATION_TRANSACTIONAL);

        Method method = serviceMethods.getUpdateBatchMethod(parentElement, false,true);
        method.addAnnotation("@Override");
        if (introspectedTable.getRules().isGenerateCachePO()) {
            CacheAnnotationDesc cacheAnnotationDesc = new CacheAnnotationDesc(entityType.getShortName());
            method.addAnnotation(cacheAnnotationDesc.toCacheEvictAnnotation(true));
        }
        method.addAnnotation("@Transactional(rollbackFor = Exception.class)");
        List<RelationGeneratorConfiguration> configs = introspectedTable.getTableConfiguration().getRelationGeneratorConfigurations().stream()
                .filter(RelationGeneratorConfiguration::isEnableUpdate)
                .collect(Collectors.toList());
        if (configs.size() > 0) {
            method.addBodyLine("for ({0} {1} : {1}s) '{'", entityType.getShortName(), entityType.getShortNameFirstLowCase());
            outSubBatchMethodBody(method, "UPDATE", entityType.getShortNameFirstLowCase(), parentElement, configs, false);
            method.addBodyLine("}");
        }
        method.addBodyLine("int i = mapper.{0}({1});", introspectedTable.getUpdateBatchStatementId(), entityType.getShortNameFirstLowCase() + "s");
        method.addBodyLine("if (i > 0) {");
        method.addBodyLine("return ServiceResult.success({0},i);", entityType.getShortNameFirstLowCase() + "s");
        method.addBodyLine("}else{");
        if (configs.size() > 0) {
            method.addBodyLine("TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();");
        }
        method.addBodyLine("return ServiceResult.failure(ServiceCodeEnum.WARN);");
        method.addBodyLine("}");
        parentElement.addMethod(method);
    }
}
