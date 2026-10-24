package org.mybatis.generator.codegen.mybatis3.javamapper.elements;

import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.codegen.mybatis3.SelectForUpdateValidator;
import org.mybatis.generator.codegen.mybatis3.service.ServiceMethods;

/**
 * 通过 Mapper 方法生成流程装配明确条件锁定查询。
 *
 * @author vgosoft
 */
public class SelectByExampleForUpdateMethodGenerator extends AbstractJavaMapperMethodGenerator {
    @Override
    public void addInterfaceElements(Interface interfaze) {
        SelectForUpdateValidator.validate(introspectedTable);
        if (!introspectedTable.getRules().generateSelectByExampleForUpdate()) {
            return;
        }
        var method = new ServiceMethods(context, introspectedTable)
                .getSelectByExampleForUpdateMethod(interfaze, true, false);
        if (context.getPlugins().clientSelectByExampleForUpdateMethodGenerated(method, interfaze, introspectedTable)) {
            interfaze.addMethod(method);
        }
    }
}
