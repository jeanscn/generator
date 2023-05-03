package org.mybatis.generator.api.dom.kotlin;

public interface KotlinNamedItemVisitor<R> {

    R visit(KotlinType kotlinType);

    R visit(KotlinProperty kotlinProperty);

    R visit(KotlinFunction kotlinFunction);
}
