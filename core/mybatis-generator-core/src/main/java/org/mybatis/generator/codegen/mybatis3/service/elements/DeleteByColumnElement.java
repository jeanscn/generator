package org.mybatis.generator.codegen.mybatis3.service.elements;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.service.AbstractServiceElementGenerator;
import org.mybatis.generator.custom.pojo.SelectByColumnGeneratorConfiguration;
import org.mybatis.generator.internal.util.JavaBeansUtil;

import java.util.stream.Collectors;

/**
 * selectByExampleWithRelation实现方法
 *
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-09-23 23:57
 * @version 3.0
 */
public class DeleteByColumnElement extends AbstractServiceElementGenerator {

    private SelectByColumnGeneratorConfiguration configuration;

    public DeleteByColumnElement(SelectByColumnGeneratorConfiguration configuration) {
        super();
        this.configuration = configuration;
    }

    @Override
    public void addElements(TopLevelClass parentElement) {

        String params = configuration.getColumns().stream()
                .map(column -> column.getJavaProperty() + (configuration.getParameterList() ? "s" : ""))
                .collect(Collectors.joining(","));
        Method methodByColumn = serviceMethods.getDeleteByColumnMethod(parentElement, configuration, false);
        methodByColumn.addAnnotation("@Override");
        String sb = "return mapper." + configuration.getDeleteMethodName() +
                "(" + params +  ");";
        methodByColumn.addBodyLine(sb);
        parentElement.addMethod(methodByColumn);
    }
}
