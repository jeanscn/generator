package org.mybatis.generator.codegen.mybatis3.javamapper.elements;

import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.codegen.mybatis3.service.ServiceMethods;

/**
 * 在原 Mapper 方法装配层增加独立旧版本的完整及选择性 CAS 接口。
 *
 * @author vgosoft
 */
public final class CasWriteMethodGenerator extends AbstractJavaMapperMethodGenerator {
    @Override
    public void addInterfaceElements(Interface target) {
        if (!introspectedTable.getTableConfiguration().isCasWriteContractEnabled()) { return; }
        var serviceMethods = new ServiceMethods(context, introspectedTable);
        target.addMethod(serviceMethods.getCasWriteMethod(target, false, false, true));
        target.addMethod(serviceMethods.getCasWriteMethod(target, true, false, true));
    }
}
