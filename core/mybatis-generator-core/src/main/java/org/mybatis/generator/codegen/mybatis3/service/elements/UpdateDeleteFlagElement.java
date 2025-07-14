package org.mybatis.generator.codegen.mybatis3.service.elements;

import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.service.AbstractServiceElementGenerator;
import org.mybatis.generator.custom.annotations.CacheAnnotationDesc;

/**
 * selectByExampleWithRelation实现方法
 *
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-09-23 23:57
 * @version 3.0
 */
public class UpdateDeleteFlagElement extends AbstractServiceElementGenerator {

    public UpdateDeleteFlagElement() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        Method updateDeleteFlagMethod = serviceMethods.getUpdateDeleteFlagMethod(parentElement, false, true);
        updateDeleteFlagMethod.addAnnotation("@Override");
        if (introspectedTable.getRules().isGenerateCachePo()) {
            CacheAnnotationDesc cacheAnnotationDesc = new CacheAnnotationDesc(entityType.getShortName());
            updateDeleteFlagMethod.addAnnotation(cacheAnnotationDesc.toCacheEvictAnnotation(true));
        }
        updateDeleteFlagMethod.addBodyLine("{0}.setDeleteFlag(deleteFlag);", entityType.getShortNameFirstLowCase());
        updateDeleteFlagMethod.addBodyLine("return mapper.updateByPrimaryKey({0});", entityType.getShortNameFirstLowCase());
        parentElement.addMethod(updateDeleteFlagMethod);
    }
}
