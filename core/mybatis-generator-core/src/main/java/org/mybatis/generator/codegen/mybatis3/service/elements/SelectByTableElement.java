package org.mybatis.generator.codegen.mybatis3.service.elements;

import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.service.AbstractServiceElementGenerator;
import org.mybatis.generator.config.SelectByTableGeneratorConfiguration;
import org.mybatis.generator.custom.annotations.CacheAnnotationDesc;
import org.mybatis.generator.codegen.mybatis3.service.CacheConfigurationResolver;
import org.mybatis.generator.codegen.mybatis3.service.ManagedRelationGeneration;

import java.util.ArrayList;
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
            String parameterName = configuration.getParameterName() + ("list".equals(configuration.getParameterType()) ? "s" : "");
            Method selectByTable = serviceMethods.getSelectByTableMethod(entityType, parentElement, configuration, false);
            selectByTable.addAnnotation("@Override");
            if (configuration.isManagedRelation()) {
                ManagedRelationGeneration.validate(introspectedTable, configuration);
            }
            List<String> cacheAnnotations = new ArrayList<>();
            for (List<String> cacheNames : CacheConfigurationResolver.queryCacheNameGroups(configuration)) {
                CacheAnnotationDesc cacheAnnotationDesc = new CacheAnnotationDesc(entityType.getShortName());
                cacheAnnotationDesc.setCacheNames(cacheNames);
                cacheAnnotationDesc.setOtherKey(cacheMethodIdentity(configuration.getMethodName()));
                cacheAnnotationDesc.setParameters(selectByTable.getParameters());
                if (configuration.isManagedRelation()) {
                    cacheAnnotationDesc.setCondition("!T(org.springframework.transaction.support.TransactionSynchronizationManager).isActualTransactionActive()");
                }
                cacheAnnotations.add(cacheAnnotationDesc.toCacheableAnnotation(parentElement));
            }
            addCacheEnableAnnotations(cacheAnnotations, selectByTable, parentElement);
            String sb = "return mapper." + configuration.getMethodName() +
                    "(" +
                    parameterName +
                    ");";
            selectByTable.addBodyLine(sb);
            parentElement.addMethod(selectByTable);
        }
    }
}
