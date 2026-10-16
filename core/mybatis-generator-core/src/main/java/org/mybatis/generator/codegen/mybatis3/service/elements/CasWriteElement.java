package org.mybatis.generator.codegen.mybatis3.service.elements;

import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.service.AbstractServiceElementGenerator;

import java.util.Set;

/**
 * 生成不捕获冲突异常的 CAS 服务，并对旧危险写方法给出显式拒绝。
 *
 * @author vgosoft
 */
public final class CasWriteElement extends AbstractServiceElementGenerator {
    @Override
    public void addElements(TopLevelClass parent) {
        if (!introspectedTable.getTableConfiguration().isCasWriteContractEnabled()) { return; }
        for (var method : parent.getMethods()) {
            String name = method.getName();
            if (name.startsWith("update") || name.startsWith("delete") || name.startsWith("insertOrUpdate")
                    || name.startsWith("cleanup") || name.contains("InsertUpdateBatch")) {
                method.getBodyLines().clear();
                method.addBodyLine("throw new IllegalStateException(\"ENTRY_DISABLED\");");
            } else if (Set.of("insert", "insertSelective", "insertBatch").contains(name)) {
                // 受管新增也复用外层同源事务，批量仍为一条多值 SQL。
                method.getAnnotations().removeIf(a -> a.startsWith("@Transactional")
                        || a.startsWith("@org.springframework.transaction.annotation.Transactional"));
                mandatory(method);
            }
        }
        for (boolean selective : new boolean[]{false, true}) {
            var method = serviceMethods.getCasWriteMethod(parent, selective, true, false);
            method.addAnnotation("@Override");
            mandatory(method);
            method.addBodyLine("int affected = mapper." + method.getName() + "(record, expectedVersion);");
            method.addBodyLine("return ServiceResult.success(record, affected);");
            parent.addMethod(method);
        }
    }

    private static void mandatory(org.mybatis.generator.api.dom.java.Method method) {
        method.addAnnotation("@org.springframework.transaction.annotation.Transactional("
                + "propagation = org.springframework.transaction.annotation.Propagation.MANDATORY, rollbackFor = Exception.class)");
    }
}
