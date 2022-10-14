package org.mybatis.generator.codegen.mybatis3.service.elements;

import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.service.AbstractServiceElementGenerator;
import org.mybatis.generator.custom.pojo.CacheAnnotation;
import org.mybatis.generator.custom.pojo.RelationGeneratorConfiguration;

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

        Method deleteByExampleMethod = serviceMethods.getDeleteByExampleMethod(parentElement, false,true);
        deleteByExampleMethod.addAnnotation("@Override");
        deleteByExampleMethod.addAnnotation("@Transactional(rollbackFor = Exception.class)");
        parentElement.addImportedType(ANNOTATION_TRANSACTIONAL);
        if (introspectedTable.getRules().isGenerateCachePO()) {
            CacheAnnotation cacheAnnotation = new CacheAnnotation(entityType.getShortName());
            deleteByExampleMethod.addAnnotation(cacheAnnotation.toCacheEvictAnnotation(true));
        }
        List<RelationGeneratorConfiguration> collect = introspectedTable.getRelationGeneratorConfigurations().stream()
                .filter(RelationGeneratorConfiguration::isEnableDelete)
                .collect(Collectors.toList());
        if (collect.size() > 0) {
            deleteByExampleMethod.addBodyLine("List<{0}> {1}s = this.selectByExample(example);", entityType.getShortName(), entityType.getShortNameFirstLowCase());
            deleteByExampleMethod.addBodyLine("int affectedRows = super.deleteByExample(example);");
            deleteByExampleMethod.addBodyLine("if (affectedRows > 0) {");
            deleteByExampleMethod.addBodyLine("for ({0} {1} : {1}s) '{'", entityType.getShortName(), entityType.getShortNameFirstLowCase());
            outSubBatchMethodBody(deleteByExampleMethod, "DELETE", entityType.getShortNameFirstLowCase(), parentElement, collect, true);
            deleteByExampleMethod.addBodyLine("}");
            deleteByExampleMethod.addBodyLine("return affectedRows;");
            deleteByExampleMethod.addBodyLine("}");
            deleteByExampleMethod.addBodyLine("return 0;");
        } else {
            deleteByExampleMethod.addBodyLine("return super.{0}(example);", introspectedTable.getDeleteByExampleStatementId());
        }

        parentElement.addMethod(deleteByExampleMethod);
    }
}
