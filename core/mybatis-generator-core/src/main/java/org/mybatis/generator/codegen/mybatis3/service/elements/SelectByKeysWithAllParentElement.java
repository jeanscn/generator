package org.mybatis.generator.codegen.mybatis3.service.elements;

import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.service.AbstractServiceElementGenerator;

/**
 * selectByExampleWithRelation实现方法
 *
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-09-23 23:57
 * @version 3.0
 */
public class SelectByKeysWithAllParentElement extends AbstractServiceElementGenerator {

    public SelectByKeysWithAllParentElement() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        Method method = serviceMethods.getSelectByKeysWithAllParentMethod(parentElement, false,true);
        method.addAnnotation("@Override");
        method.addBodyLine("List<{0}> result = mapper.{1}(ids);", entityType.getShortName(),introspectedTable.getSelectByKeysWithAllParent());
        method.addBodyLine("return ServiceResult.success(result);");
        parentElement.addMethod(method);
        parentElement.addImportedType(new FullyQualifiedJavaType("java.util.List"));
    }
}
