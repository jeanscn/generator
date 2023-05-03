package org.mybatis.generator.api.dom.kotlin.render;

import java.util.List;

import org.mybatis.generator.api.dom.kotlin.KotlinFunction;
import org.mybatis.generator.api.dom.kotlin.KotlinNamedItem;
import org.mybatis.generator.api.dom.kotlin.KotlinNamedItemVisitor;
import org.mybatis.generator.api.dom.kotlin.KotlinProperty;
import org.mybatis.generator.api.dom.kotlin.KotlinType;

public class KotlinNamedItemRenderer implements KotlinNamedItemVisitor<List<String>> {

    public List<String> render(KotlinNamedItem namedItem) {
        return namedItem.accept(this);
    }

    @Override
    public List<String> visit(KotlinType kotlinType) {
        return new KotlinTypeRenderer().render(kotlinType);
    }

    @Override
    public List<String> visit(KotlinProperty kotlinProperty) {
        return new KotlinPropertyRenderer().render(kotlinProperty);
    }

    @Override
    public List<String> visit(KotlinFunction kotlinFunction) {
        return new KotlinFunctionRenderer().render(kotlinFunction);
    }
}
