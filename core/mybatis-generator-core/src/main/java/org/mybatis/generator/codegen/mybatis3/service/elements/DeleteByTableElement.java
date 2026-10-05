package org.mybatis.generator.codegen.mybatis3.service.elements;

import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.service.AbstractServiceElementGenerator;
import org.mybatis.generator.codegen.mybatis3.service.CacheConfigurationResolver;
import org.mybatis.generator.config.SelectByTableGeneratorConfiguration;
import org.mybatis.generator.custom.annotations.CacheAnnotationDesc;

import java.util.ArrayList;
import java.util.List;

/**
 * selectByExampleWithRelation实现方法
 *
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-09-23 23:57
 * @version 3.0
 */
public class DeleteByTableElement extends AbstractServiceElementGenerator {

    public DeleteByTableElement() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        introspectedTable.getTableConfiguration().getSelectByTableGeneratorConfiguration().stream()
                .filter(SelectByTableGeneratorConfiguration::isEnableSplit)
                .forEach(c -> {
                    Method method = serviceMethods.getSplitUnionByTableMethod(parentElement, c, false,false);
                    method.addAnnotation("@Override");
                    final List<String> annotations = new ArrayList<>();
                    List<String> cacheNames = CacheConfigurationResolver.relationTableCacheNames(context, c.getTableName());
                    if (!cacheNames.isEmpty()) {
                        CacheAnnotationDesc cacheAnnotationDesc = new CacheAnnotationDesc();
                        cacheAnnotationDesc.setCacheNames(cacheNames);
                        annotations.add(cacheAnnotationDesc.toCacheEvictAnnotation(true));
                    }
                    addCacheCacheEvictAnnotations(annotations,method,parentElement);
                    method.addBodyLine("return mapper.{0}({1},{2});"
                            ,c.getSplitMethodName()
                            ,method.getParameters().get(0).getName()
                            ,method.getParameters().get(1).getName());

                    parentElement.addMethod(method);
                });
    }
}
