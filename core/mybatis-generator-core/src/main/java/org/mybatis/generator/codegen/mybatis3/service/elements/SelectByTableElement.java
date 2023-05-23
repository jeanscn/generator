package org.mybatis.generator.codegen.mybatis3.service.elements;

import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.service.AbstractServiceElementGenerator;
import org.mybatis.generator.config.SelectByTableGeneratorConfiguration;

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
            Method selectByTable = serviceMethods.getSelectByTableMethod(entityType, parentElement, configuration, false);
            selectByTable.addAnnotation("@Override");
            StringBuilder sb = new StringBuilder("return mapper.");
            sb.append(configuration.getMethodName());
            sb.append("(");
            sb.append(configuration.getParameterName()).append(configuration.getParameterType().equals("list") ? "s" : "");
            sb.append(");");
            selectByTable.addBodyLine(sb.toString());
            parentElement.addMethod(selectByTable);
        }
    }
}
