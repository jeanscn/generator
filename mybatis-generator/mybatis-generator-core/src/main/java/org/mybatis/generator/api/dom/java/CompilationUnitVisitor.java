package org.mybatis.generator.api.dom.java;

public interface CompilationUnitVisitor<R> {
    R visit(TopLevelClass topLevelClass);

    R visit(TopLevelEnumeration topLevelEnumeration);

    R visit(Interface topLevelInterface);
}
