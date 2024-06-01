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
public class SelectByExampleWithChildrenCountElement extends AbstractServiceElementGenerator {

    public SelectByExampleWithChildrenCountElement() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        /*
         * 增加SelectByExampleWithChildrenCount接口实现方法
         * 此方法可以使byExample方法支持级联查询
         * */
        Method method = serviceMethods.getSelectWithChildrenCountMethod(parentElement, false,true);
        method.addAnnotation("@Override");
        method.addBodyLine("List<{0}> result = mapper.{1}(example);", entityType.getShortName(),introspectedTable.getSelectByExampleWithChildrenCountStatementId());
        method.addBodyLine("return ServiceResult.success(result);");
        parentElement.addMethod(method);
    }
}
