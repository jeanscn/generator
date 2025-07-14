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
public class UpdateBySqlElement extends AbstractServiceElementGenerator {

    public UpdateBySqlElement() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        CacheAnnotationDesc cacheAnnotationDesc = new CacheAnnotationDesc(entityType.getShortName());

        Method updateBySql = serviceMethods.getUpdateBySql(parentElement, false);
        updateBySql.addAnnotation("@Override");
        if (introspectedTable.getRules().isGenerateCachePo()) {
            updateBySql.addAnnotation(cacheAnnotationDesc.toCacheEvictAnnotation(true));
        }
        updateBySql.addBodyLine("return super.{0}(updateSqlBuilder);", introspectedTable.getUpdateBySqlStatementId());
        parentElement.addMethod(updateBySql);
    }
}
