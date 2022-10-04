package org.mybatis.generator.codegen.mybatis3.service.elements;

import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.service.AbstractServiceElementGenerator;
import org.mybatis.generator.custom.pojo.SelectBySqlMethodGeneratorConfiguration;

import java.util.Map;

/**
 * selectByExampleWithRelation实现方法
 *
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-09-23 23:57
 * @version 3.0
 */
public class SelectBySqlMethodElement extends AbstractServiceElementGenerator {

    final private SelectBySqlMethodGeneratorConfiguration configuration;

    public SelectBySqlMethodElement(SelectBySqlMethodGeneratorConfiguration configuration) {
        super();
        this.configuration = configuration;
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        Method selectTreeMethod = serviceMethods.getSelectBySqlMethodMethod(entityType, parentElement, configuration, false,true);
        selectTreeMethod.setReturnRemark(introspectedTable.getRemarks(true)+"数据对象列表");
        selectTreeMethod.addAnnotation("@Override");
        String sb = "mapper." + configuration.getMethodName() +
                "(" +
                configuration.getParentIdColumn().getJavaProperty() +
                ");";
        selectTreeMethod.addBodyLine(sb);
        parentElement.addMethod(selectTreeMethod);
        parentElement.addImportedType(configuration.getParentIdColumn().getFullyQualifiedJavaType());

    }
}
