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
public class UpdateByExampleElement extends AbstractServiceElementGenerator {

    public UpdateByExampleElement() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        CacheAnnotation cacheAnnotation = new CacheAnnotation(entityType.getShortName());

        Method updateByExampleSelective = this.getUpdateByExample(parentElement, false, true);
        updateByExampleSelective.addAnnotation("@Override");
        updateByExampleSelective.addAnnotation("@Transactional(rollbackFor = Exception.class)");
        parentElement.addImportedType(ANNOTATION_TRANSACTIONAL);
        if (introspectedTable.getRules().isGenerateCachePO()) {
            updateByExampleSelective.addAnnotation(cacheAnnotation.toCacheEvictAnnotation(true));
        }
        updateByExampleSelective.addBodyLine("return super.{0}(record, example);", introspectedTable.getUpdateByExampleSelectiveStatementId());
        parentElement.addMethod(updateByExampleSelective);


        Method updateByExample = this.getUpdateByExample(parentElement, false, false);
        updateByExample.addAnnotation("@Override");
        if (introspectedTable.getRules().isGenerateCachePO()) {
            updateByExample.addAnnotation(cacheAnnotation.toCacheEvictAnnotation(true));
        }
        updateByExample.addBodyLine("return super.{0}(record, example);", introspectedTable.getUpdateByExampleStatementId());
        parentElement.addMethod(updateByExample);
    }
}