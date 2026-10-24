package org.mybatis.generator.codegen.mybatis3.javamapper.elements;

import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.codegen.mybatis3.SelectForUpdateValidator;
import org.mybatis.generator.codegen.mybatis3.service.ServiceMethods;

/**
 * 通过 Mapper 方法生成流程装配完整主键锁定查询。
 *
 * @author vgosoft
 */
public class SelectByPrimaryKeyForUpdateMethodGenerator extends AbstractJavaMapperMethodGenerator {
    @Override
    public void addInterfaceElements(Interface interfaze) {
        SelectForUpdateValidator.validate(introspectedTable);
        if (!introspectedTable.getRules().generateSelectByPrimaryKeyForUpdate()) {
            return;
        }
        var method = new ServiceMethods(context, introspectedTable)
                .getSelectByPrimaryKeyForUpdateMethod(interfaze, true, false);
        if (context.getPlugins().clientSelectByPrimaryKeyForUpdateMethodGenerated(method, interfaze, introspectedTable)) {
            interfaze.addMethod(method);
        }
    }
}
