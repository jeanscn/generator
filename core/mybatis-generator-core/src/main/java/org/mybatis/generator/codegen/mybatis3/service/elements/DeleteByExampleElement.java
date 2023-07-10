package org.mybatis.generator.codegen.mybatis3.service.elements;

import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.service.AbstractServiceElementGenerator;
import org.mybatis.generator.custom.annotations.CacheAnnotationDesc;
import org.mybatis.generator.config.RelationGeneratorConfiguration;

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

        Method deleteByExampleMethod = serviceMethods.getDeleteByExampleMethod(parentElement, false);
        deleteByExampleMethod.addAnnotation("@Override");
        deleteByExampleMethod.addAnnotation("@Transactional(rollbackFor = Exception.class)");
        parentElement.addImportedType(ANNOTATION_TRANSACTIONAL);
        if (introspectedTable.getRules().isGenerateCachePO()) {
            CacheAnnotationDesc cacheAnnotationDesc = new CacheAnnotationDesc(entityType.getShortName());
            deleteByExampleMethod.addAnnotation(cacheAnnotationDesc.toCacheEvictAnnotation(true));
        }
        List<RelationGeneratorConfiguration> collect = introspectedTable.getTableConfiguration().getRelationGeneratorConfigurations().stream()
                .filter(RelationGeneratorConfiguration::isEnableDelete)
                .collect(Collectors.toList());
        if (collect.size() > 0) {
            deleteByExampleMethod.addBodyLine("ServiceResult<List<{0}>> result = this.selectByExample(example);", entityType.getShortName(), entityType.getShortNameFirstLowCase());
            deleteByExampleMethod.addBodyLine("for ({0} {1} : result.getResult()) '{'", entityType.getShortName(), entityType.getShortNameFirstLowCase());
            outSubBatchMethodBody(deleteByExampleMethod, "DELETE", entityType.getShortNameFirstLowCase(), parentElement, collect, false);
            deleteByExampleMethod.addBodyLine("}");
            deleteByExampleMethod.addBodyLine("int affectedRows = mapper.deleteByExample(example);");
            deleteByExampleMethod.addBodyLine("if (affectedRows > 0) {");
            deleteByExampleMethod.addBodyLine("return ServiceResult.success(affectedRows,affectedRows);");
            deleteByExampleMethod.addBodyLine("}");
            deleteByExampleMethod.addBodyLine("TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();");
            deleteByExampleMethod.addBodyLine("return ServiceResult.failure(ServiceCodeEnum.FAIL);");
        } else {
            deleteByExampleMethod.addBodyLine("return super.{0}(example);", introspectedTable.getDeleteByExampleStatementId());
        }

        parentElement.addMethod(deleteByExampleMethod);
    }
}
