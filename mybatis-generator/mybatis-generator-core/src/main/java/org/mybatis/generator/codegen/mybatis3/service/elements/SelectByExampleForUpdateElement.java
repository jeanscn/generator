package org.mybatis.generator.codegen.mybatis3.service.elements;

import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.SelectForUpdateValidator;
import org.mybatis.generator.codegen.mybatis3.javamapper.SelectForUpdateGuardGenerator;
import org.mybatis.generator.codegen.mybatis3.service.AbstractServiceElementGenerator;

/**
 * 生成明确条件锁定服务实现，共用 Mapper 门禁并强制既有事务。
 *
 * @author vgosoft
 */
public class SelectByExampleForUpdateElement extends AbstractServiceElementGenerator {
    @Override
    public void addElements(TopLevelClass parentElement) {
        SelectForUpdateValidator.validate(introspectedTable);
        if (!introspectedTable.getRules().generateSelectByExampleForUpdate()) {
            return;
        }
        var method = serviceMethods.getSelectByExampleForUpdateMethod(parentElement, false, true);
        method.addAnnotation("@Override");
        method.addAnnotation("@org.springframework.transaction.annotation.Transactional("
                + "propagation = org.springframework.transaction.annotation.Propagation.MANDATORY, rollbackFor = Exception.class)");
        method.addBodyLine(SelectForUpdateGuardGenerator.guardType(introspectedTable) + ".requireSelectForUpdateTransaction();");
        method.addBodyLine(SelectForUpdateGuardGenerator.guardType(introspectedTable) + ".requireSelectForUpdateExample(example);");
        method.addBodyLine("return ServiceResult.success(mapper." + method.getName() + "(example));");
        parentElement.addMethod(method);
    }
}
