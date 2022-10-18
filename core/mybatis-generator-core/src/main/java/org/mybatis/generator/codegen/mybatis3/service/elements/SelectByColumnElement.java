package org.mybatis.generator.codegen.mybatis3.service.elements;

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
public class SelectByColumnElement extends AbstractServiceElementGenerator {

    public SelectByColumnElement() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        /*
         * 增加selectByExampleWithRelation接口实现方法
         * 当生成的方法包括至少有一个selectByTableXXX、selectByColumnXXX方法时
         * 此方法可以使byExample方法支持级联查询
         * */
        for (SelectByColumnGeneratorConfiguration configuration : tc.getSelectByColumnGeneratorConfigurations()) {
            String params = configuration.getColumns().stream()
                    .map(column -> column.getJavaProperty() + (configuration.getParameterList() ? "s" : ""))
                    .collect(Collectors.joining(","));
            Method methodByColumn = serviceMethods.getSelectByColumnMethod(entityType, parentElement, configuration, false,true);
            methodByColumn.addAnnotation("@Override");
            if (JavaBeansUtil.isSelectBaseByPrimaryKeyMethod(configuration.getMethodName())) {
                methodByColumn.addBodyLine("return mapper.{0}({1});"
                        , introspectedTable.getSelectBaseByPrimaryKeyStatementId()
                        , params);
            } else {
                String sb = "return mapper." + configuration.getMethodName() +
                        "(" + params +  ");";
                methodByColumn.addBodyLine(sb);
                parentElement.addImportedType(FullyQualifiedJavaType.getNewListInstance());
            }
            parentElement.addMethod(methodByColumn);
        }
    }
}
