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
public class InsertByTableElement extends AbstractServiceElementGenerator {

    public InsertByTableElement() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        introspectedTable.getTableConfiguration().getSelectByTableGeneratorConfiguration().stream()
                .filter(SelectByTableGeneratorConfiguration::isEnableUnion)
                .forEach(c -> {
                    Method method = serviceMethods.getSplitUnionByTableMethod(parentElement, c, false,true,true);
                    method.addBodyLine("return mapper.{0}({1},{2});"
                            ,c.getUnionMethodName()
                            ,c.getThisColumn().getJavaProperty()
                            ,c.getOtherColumn().getJavaProperty()+"s");
                    parentElement.addMethod(method);
                });
    }
}
