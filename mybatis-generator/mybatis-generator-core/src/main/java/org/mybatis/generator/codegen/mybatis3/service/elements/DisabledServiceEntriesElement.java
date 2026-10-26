package org.mybatis.generator.codegen.mybatis3.service.elements;

import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.service.AbstractServiceElementGenerator;
import org.mybatis.generator.codegen.mybatis3.service.DisabledServiceEntries;

/**
 * 沿既有元素初始化链装配显式停用覆盖及受保护内部原语。
 *
 * @author vgosoft-agent
 */
public final class DisabledServiceEntriesElement extends AbstractServiceElementGenerator {
    @Override
    public void addElements(TopLevelClass parentElement) {
        DisabledServiceEntries.apply(introspectedTable, parentElement, serviceMethods);
    }
}
