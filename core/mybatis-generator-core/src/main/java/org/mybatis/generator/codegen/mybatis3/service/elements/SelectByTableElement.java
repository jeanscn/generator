package org.mybatis.generator.codegen.mybatis3.service.elements;

import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.service.AbstractServiceElementGenerator;
import org.mybatis.generator.config.EnableCacheConfiguration;
import org.mybatis.generator.config.SelectByTableGeneratorConfiguration;
import org.mybatis.generator.custom.annotations.CacheAnnotationDesc;
import org.springframework.cache.annotation.CacheEvict;

import java.util.Collections;
import java.util.List;

/**
 * selectByExampleWithRelation实现方法
 *
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-09-23 23:57
 * @version 3.0
 */
public class SelectByTableElement extends AbstractServiceElementGenerator {

    public SelectByTableElement() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {

        for (SelectByTableGeneratorConfiguration configuration : tc.getSelectByTableGeneratorConfiguration()) {
            String parameterName = configuration.getParameterName() + (configuration.getParameterType().equals("list") ? "s" : "");
            //是否需要缓存
            List<EnableCacheConfiguration> cacheConfigurationList =  configuration.getCacheConfigurationList();
            Method selectByTable = serviceMethods.getSelectByTableMethod(entityType, parentElement, configuration, false);
            selectByTable.addAnnotation("@Override");
            if (cacheConfigurationList != null && !cacheConfigurationList.isEmpty()) {
                cacheConfigurationList.forEach(cacheConfiguration -> {
                    if (cacheConfiguration.isEnableCache()) {
                        CacheAnnotationDesc cacheAnnotationDesc = new CacheAnnotationDesc();
                        if (cacheConfiguration.getCacheNames().isEmpty()) {
                            cacheAnnotationDesc.setCacheNames(Collections.singletonList(configuration.getParameterName() + VStringUtil.getFirstCharacterUppercase(configuration.getThisColumn().getJavaProperty())+"sCache"));
                        }
                        cacheAnnotationDesc.setParameters(selectByTable.getParameters());
                        selectByTable.addAnnotation(cacheAnnotationDesc.toCacheableAnnotation());
                        parentElement.addImportedType("org.springframework.cache.annotation.Cacheable");
                    }
                });
            }
            String sb = "return mapper." + configuration.getMethodName() +
                    "(" +
                    parameterName +
                    ");";
            selectByTable.addBodyLine(sb);
            parentElement.addMethod(selectByTable);
        }
    }
}
