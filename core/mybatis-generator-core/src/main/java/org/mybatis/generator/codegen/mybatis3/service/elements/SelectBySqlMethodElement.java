package org.mybatis.generator.codegen.mybatis3.service.elements;

import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.service.AbstractServiceElementGenerator;
import org.mybatis.generator.config.SelectBySqlMethodGeneratorConfiguration;

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
        Method method = serviceMethods.getSelectBySqlMethodMethod(parentElement, configuration, false,true);
        method.setReturnRemark(introspectedTable.getRemarks(true)+"数据对象列表");
        method.addAnnotation("@Override");
        method.addBodyLine("List<{0}> result = mapper.{1}({2});"
                ,entityType.getShortName()
                ,configuration.getMethodName()
                ,configuration.getParentIdColumn().getJavaProperty());
        method.addBodyLine("if (result.size() > 0) {\n" +
                "            return ServiceResult.success(result,result.size());\n" +
                "        }else{\n" +
                "            return ServiceResult.failure(ServiceCodeEnum.WARN);\n" +
                "        }");
        parentElement.addMethod(method);
        parentElement.addImportedType(configuration.getParentIdColumn().getFullyQualifiedJavaType());
    }
}
