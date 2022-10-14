package org.mybatis.generator.codegen.mybatis3.service.elements;

import org.mybatis.generator.api.IntrospectedColumn;
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
public class DeleteByPrimaryKeyElement extends AbstractServiceElementGenerator {

    public DeleteByPrimaryKeyElement() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {

        Method method = serviceMethods.getDeleteByPrimaryKeyMethod(parentElement, false,true);
        method.addAnnotation("@Override");
        method.addAnnotation("@Transactional(rollbackFor = Exception.class)");
        parentElement.addImportedType(ANNOTATION_TRANSACTIONAL);
        if (introspectedTable.getRules().isGenerateCachePO()) {
            CacheAnnotation cacheAnnotation = new CacheAnnotation(entityType.getShortName());
            method.addAnnotation(cacheAnnotation.toCacheEvictAnnotation(true));
        }
        String pks = introspectedTable.getPrimaryKeyColumns().stream().map(IntrospectedColumn::getJavaProperty).collect(Collectors.joining(","));
        List<RelationGeneratorConfiguration> deleteConfigs = introspectedTable.getRelationGeneratorConfigurations().stream()
                .filter(RelationGeneratorConfiguration::isEnableDelete)
                .collect(Collectors.toList());
        if (deleteConfigs.size() > 0) {
            method.addBodyLine("ServiceResult<{0}> result = this.selectByPrimaryKey({1});", entityType.getShortName(), pks);
            method.addBodyLine("if (result.hasResult()) {");
            method.addBodyLine("{0} {1} = result.getResult();", entityType.getShortName(), entityType.getShortNameFirstLowCase());
            method.addBodyLine("int affectedRows = super.deleteByPrimaryKey({0});", pks);
            method.addBodyLine("if (affectedRows > 0) {");
            outSubBatchMethodBody(method, "DELETE", entityType.getShortNameFirstLowCase(), parentElement, deleteConfigs, true);
            method.addBodyLine("return affectedRows;");
            method.addBodyLine("}}");
            method.addBodyLine("return 0;");
        } else {
            method.addBodyLine("return super.{0}({1});", introspectedTable.getDeleteByPrimaryKeyStatementId()
                    , introspectedTable.getPrimaryKeyColumns().stream()
                            .map(IntrospectedColumn::getJavaProperty)
                            .collect(Collectors.joining(",")));
        }
        parentElement.addMethod(method);
    }
}
