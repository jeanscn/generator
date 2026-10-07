package org.mybatis.generator.codegen.mybatis3.service.elements;

import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.service.AbstractServiceElementGenerator;
import org.mybatis.generator.codegen.mybatis3.service.CacheConfigurationResolver;
import org.mybatis.generator.codegen.mybatis3.service.ManagedRelationGeneration;
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
public class InsertByTableElement extends AbstractServiceElementGenerator {

    public InsertByTableElement() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        introspectedTable.getTableConfiguration().getSelectByTableGeneratorConfiguration().stream()
                .filter(SelectByTableGeneratorConfiguration::isEnableUnion)
                .forEach(c -> {
                    Method method = serviceMethods.getSplitUnionByTableMethod(parentElement, c, false,true);
                    method.addAnnotation("@Override");
                    if (c.isManagedRelation()) {
                        ManagedRelationGeneration.validate(introspectedTable, c);
                        ManagedRelationGeneration.injectWriteSupport(parentElement);
                        ManagedRelationGeneration.addMutation(method, parentElement, c);
                        String currentId = method.getParameters().get(0).getName();
                        String otherIds = method.getParameters().get(1).getName();
                        method.addBodyLine("return relationWriteSupport.union(" + currentId + ", " + otherIds
                                + ", (otherId, relationId) -> mapper." + c.getRowInsertMethodName()
                                + "(" + currentId + ", otherId, relationId),");
                        method.addBodyLine("        otherId -> mapper." + c.getExistsMethodName()
                                + "(" + currentId + ", otherId) != null);");
                        parentElement.addMethod(method);
                        return;
                    }
                    final List<String> annotations = new ArrayList<>();
                    List<String> cacheNames = CacheConfigurationResolver.relationTableCacheNames(context, c.getTableName());
                    if (!cacheNames.isEmpty()) {
                        CacheAnnotationDesc cacheAnnotationDesc = new CacheAnnotationDesc();
                        cacheAnnotationDesc.setCacheNames(cacheNames);
                        annotations.add(cacheAnnotationDesc.toCacheEvictAnnotation(true));
                    }
                    addCacheCacheEvictAnnotations(annotations,method,parentElement);
                    method.addBodyLine("return mapper.{0}({1},{2});"
                            ,c.getUnionMethodName()
                            ,method.getParameters().get(0).getName()
                            ,method.getParameters().get(1).getName());
                    parentElement.addMethod(method);
                });
    }
}
