package org.mybatis.generator.codegen.mybatis3.service.elements;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.service.AbstractServiceElementGenerator;
import org.mybatis.generator.custom.pojo.SelectByColumnGeneratorConfiguration;
import org.mybatis.generator.custom.pojo.SelectByTableGeneratorConfiguration;
import org.mybatis.generator.internal.util.JavaBeansUtil;

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

        for (SelectByTableGeneratorConfiguration selectByTableGeneratorConfiguration : tc.getSelectByTableGeneratorConfiguration()) {
            Method selectByTable = getSelectByTableMethod(entityType, parentElement, selectByTableGeneratorConfiguration, false);
            selectByTable.addAnnotation("@Override");
            StringBuilder sb = new StringBuilder("return mapper.");
            sb.append(selectByTableGeneratorConfiguration.getMethodName());
            sb.append("(");
            sb.append(selectByTableGeneratorConfiguration.getParameterName());
            sb.append(");");
            selectByTable.addBodyLine(sb.toString());
            parentElement.addMethod(selectByTable);
        }
    }
}
