package org.mybatis.generator.codegen.mybatis3.service.elements;

import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.service.AbstractServiceElementGenerator;
import org.mybatis.generator.config.EnableCacheConfiguration;
import org.mybatis.generator.custom.annotations.CacheAnnotationDesc;
import org.mybatis.generator.config.SelectByTableGeneratorConfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
                    //是否需要缓存
                    List<EnableCacheConfiguration> cacheConfigurationList =  c.getCacheConfigurationList();
                    if (cacheConfigurationList != null && !cacheConfigurationList.isEmpty()) {
                        cacheConfigurationList.forEach(cacheConfiguration -> {
                            if (cacheConfiguration.isEnableCache()) {
                                CacheAnnotationDesc cacheAnnotationDesc = new CacheAnnotationDesc();
                                if (cacheConfiguration.getCacheNames().isEmpty()) {
                                    cacheAnnotationDesc.setCacheNames(Collections.singletonList(method.getParameters().get(0).getName()+ VStringUtil.getFirstCharacterUppercase(method.getParameters().get(1).getName())+"Cache"));
                                }
                                annotations.add(cacheAnnotationDesc.toCacheEvictAnnotation(true));
                            }
                        });
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
