package org.mybatis.generator.codegen.mybatis3.service.elements;

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
public class SelectByExampleWithRelationElement extends AbstractServiceElementGenerator {

    public SelectByExampleWithRelationElement() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        /*
         * 增加selectByExampleWithRelation接口实现方法
         * 当生成的方法包括至少有一个selectByTableXXX、selectByColumnXXX方法时
         * 此方法可以使byExample方法支持级联查询
         * */
        Method method = serviceMethods.getSelectWithRelationMethod(entityType, exampleType, parentElement, false);
        method.addAnnotation("@Override");
        method.addBodyLine("return mapper.{0}(example);", introspectedTable.getSelectByExampleWithRelationStatementId());
        parentElement.addMethod(method);
    }
}
