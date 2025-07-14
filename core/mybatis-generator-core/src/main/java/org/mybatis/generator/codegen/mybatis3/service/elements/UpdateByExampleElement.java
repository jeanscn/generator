package org.mybatis.generator.codegen.mybatis3.service.elements;

import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.service.AbstractServiceElementGenerator;
import org.mybatis.generator.custom.annotations.CacheAnnotationDesc;
import org.mybatis.generator.internal.util.Mb3GenUtil;

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
        CacheAnnotationDesc cacheAnnotationDesc = new CacheAnnotationDesc(entityType.getShortName());
        Method updateByExampleSelective = serviceMethods.getUpdateByExample(parentElement, false, true);
        updateByExampleSelective.addAnnotation("@Override");
        Mb3GenUtil.addTransactionalAnnotation(parentElement,updateByExampleSelective,"READ_COMMITTED");
        if (introspectedTable.getRules().isGenerateCachePo()) {
            updateByExampleSelective.addAnnotation(cacheAnnotationDesc.toCacheEvictAnnotation(true));
        }
        updateByExampleSelective.addBodyLine("return super.{0}(record, example);", introspectedTable.getUpdateByExampleSelectiveStatementId());
        parentElement.addMethod(updateByExampleSelective);

        Method updateByExample = serviceMethods.getUpdateByExample(parentElement, false, false);
        updateByExample.addAnnotation("@Override");
        if (introspectedTable.getRules().isGenerateCachePo()) {
            updateByExample.addAnnotation(cacheAnnotationDesc.toCacheEvictAnnotation(true));
        }
        updateByExample.addBodyLine("return super.{0}(record, example);", introspectedTable.getUpdateByExampleStatementId());
        parentElement.addMethod(updateByExample);
    }
}
