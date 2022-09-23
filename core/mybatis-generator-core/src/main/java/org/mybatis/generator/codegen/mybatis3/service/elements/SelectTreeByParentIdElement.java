package org.mybatis.generator.codegen.mybatis3.service.elements;

import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.service.AbstractServiceElementGenerator;
import org.mybatis.generator.custom.pojo.CustomMethodGeneratorConfiguration;

import java.util.Map;

/**
 * selectByExampleWithRelation实现方法
 *
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-09-23 23:57
 * @version 3.0
 */
public class SelectTreeByParentIdElement extends AbstractServiceElementGenerator {

    public SelectTreeByParentIdElement() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        //增加selectTreeByParentId
        Map<String, CustomMethodGeneratorConfiguration> customAddtionalSelectMethodMap = introspectedTable.getCustomAddtionalSelectMethods();
        CustomMethodGeneratorConfiguration customMethodConfiguration = customAddtionalSelectMethodMap.get(introspectedTable.getSelectTreeByParentIdStatementId());
        Method selectTreeMethod = getSelectTreeByParentIdMethod(entityType, parentElement, customMethodConfiguration, false);
        selectTreeMethod.addAnnotation("@Override");
        String sb = "mapper." + customMethodConfiguration.getMethodName() +
                "(" +
                customMethodConfiguration.getParentIdColumn().getJavaProperty() +
                ");";
        selectTreeMethod.addBodyLine(sb);
        parentElement.addMethod(selectTreeMethod);
        parentElement.addImportedType(customMethodConfiguration.getParentIdColumn().getFullyQualifiedJavaType());

    }
}
