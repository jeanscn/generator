package org.mybatis.generator.codegen.mybatis3.service.elements;

import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.service.AbstractServiceElementGenerator;

/**
 * 原子递增实现方法。
 *
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * @version 3.0
 */
public class AtomicIncrementElement extends AbstractServiceElementGenerator {

    public AtomicIncrementElement() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        Method method = serviceMethods.getAtomicIncrementMethod(parentElement, false);
        method.addAnnotation("@Override");
        addMutationCacheEviction(method, parentElement);
        StringBuilder invoke = new StringBuilder();
        invoke.append("return mapper.")
                .append(introspectedTable.getTableConfiguration().getAtomicIncrementStatementId())
                .append("(");
        for (int i = 0; i < method.getParameters().size(); i++) {
            if (i > 0) {
                invoke.append(", ");
            }
            invoke.append(method.getParameters().get(i).getName());
        }
        invoke.append(");");
        method.addBodyLine(invoke.toString());
        parentElement.addMethod(method);
    }
}
