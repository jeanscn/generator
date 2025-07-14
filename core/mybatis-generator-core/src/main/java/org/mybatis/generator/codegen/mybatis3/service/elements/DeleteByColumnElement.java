package org.mybatis.generator.codegen.mybatis3.service.elements;

import com.vgosoft.core.constant.enums.core.EntityEventEnum;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.service.AbstractServiceElementGenerator;
import org.mybatis.generator.config.SelectByColumnGeneratorConfiguration;
import org.mybatis.generator.custom.annotations.CacheAnnotationDesc;

import java.util.stream.Collectors;

/**
 * selectByExampleWithRelation实现方法
 *
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-09-23 23:57
 * @version 3.0
 */
public class DeleteByColumnElement extends AbstractServiceElementGenerator {

    private final SelectByColumnGeneratorConfiguration configuration;

    public DeleteByColumnElement(SelectByColumnGeneratorConfiguration configuration) {
        super();
        this.configuration = configuration;
    }

    @Override
    public void addElements(TopLevelClass parentElement) {

        boolean containPreDeleteEvent = this.serviceImplConfiguration.getEntityEvent().contains(EntityEventEnum.PRE_DELETE.name());
        boolean containDeletedEvent = this.serviceImplConfiguration.getEntityEvent().contains(EntityEventEnum.DELETED.name());

        String params = configuration.getColumns().stream()
                .map(column -> column.getJavaProperty() + (configuration.getParameterList() ? "s" : ""))
                .collect(Collectors.joining(","));
        Method methodByColumn = serviceMethods.getDeleteByColumnMethod(parentElement, configuration, false);
        methodByColumn.addAnnotation("@Override");
        if (containPreDeleteEvent || containDeletedEvent) {
            methodByColumn.addBodyLine("int ret = 0;");
            methodByColumn.addBodyLine(" List<{0}> items = mapper.{1}({2});",entityType.getShortName(),configuration.getMethodName(),params);
            methodByColumn.addBodyLine("if (!items.isEmpty()) {");
            if (containPreDeleteEvent) {
                methodByColumn.addBodyLine("publisher.publishEvent(items,EntityEventEnum.{0});", EntityEventEnum.PRE_DELETE.name());
            }
            methodByColumn.addBodyLine("ret = mapper.{0}({1});",configuration.getDeleteMethodName(),params);
            if (containDeletedEvent) {
                methodByColumn.addBodyLine("publisher.publishEvent(items,EntityEventEnum.{0});",EntityEventEnum.DELETED.name());
            }
            methodByColumn.addBodyLine("}");
            methodByColumn.addBodyLine("return ret;");
            parentElement.addImportedType(new FullyQualifiedJavaType(EntityEventEnum.class.getCanonicalName()));
        }else{
            String sb = "return mapper." + configuration.getDeleteMethodName() +
                    "(" + params +  ");";
            methodByColumn.addBodyLine(sb);
        }

        if (introspectedTable.getRules().isGenerateCachePo()) {
            CacheAnnotationDesc cacheAnnotationDesc = new CacheAnnotationDesc(entityType.getShortName());
            methodByColumn.addAnnotation(cacheAnnotationDesc.toCacheEvictAnnotation(true));
        }

        parentElement.addMethod(methodByColumn);
    }
}
