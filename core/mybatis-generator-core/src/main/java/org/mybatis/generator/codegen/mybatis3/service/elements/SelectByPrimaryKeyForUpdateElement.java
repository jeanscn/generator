package org.mybatis.generator.codegen.mybatis3.service.elements;

import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.SelectForUpdateValidator;
import org.mybatis.generator.codegen.mybatis3.javamapper.SelectForUpdateGuardGenerator;
import org.mybatis.generator.codegen.mybatis3.service.AbstractServiceElementGenerator;

import java.util.stream.Collectors;

/**
 * 生成不缓存、不吞异常且强制既有事务的主键锁定服务实现。
 *
 * @author vgosoft
 */
public class SelectByPrimaryKeyForUpdateElement extends AbstractServiceElementGenerator {
    @Override
    public void addElements(TopLevelClass parentElement) {
        SelectForUpdateValidator.validate(introspectedTable);
        if (!introspectedTable.getRules().generateSelectByPrimaryKeyForUpdate()) {
            return;
        }
        var method = serviceMethods.getSelectByPrimaryKeyForUpdateMethod(parentElement, false, true);
        method.addAnnotation("@Override");
        method.addAnnotation("@org.springframework.transaction.annotation.Transactional("
                + "propagation = org.springframework.transaction.annotation.Propagation.MANDATORY, rollbackFor = Exception.class)");
        method.addBodyLine(SelectForUpdateGuardGenerator.guardType(introspectedTable) + ".requireSelectForUpdateTransaction();");
        String parameters = method.getParameters().stream().map(Parameter::getName).collect(Collectors.joining(", "));
        method.addBodyLine("return ServiceResult.success(mapper." + method.getName() + "(" + parameters + "));");
        parentElement.addMethod(method);
    }
}
