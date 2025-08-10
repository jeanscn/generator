package org.mybatis.generator.api.dom.kotlin.render;

import java.util.List;

import org.mybatis.generator.api.dom.kotlin.KotlinModifier;
import org.mybatis.generator.internal.util.CustomCollectors;

/**
 * @author cen_c
 */
public class KotlinRenderingUtilities {

    private KotlinRenderingUtilities() {}

    public static final String KOTLIN_INDENT = "    ";

    public static String renderModifiers(List<KotlinModifier> modifiers) {
        return modifiers.stream().map(KotlinModifier::getValue)
                .collect(CustomCollectors.joining(" ", "", " "));
    }

    public static String kotlinIndent(String in) {
        if (in.isEmpty()) {
            return in;
        }

        return KOTLIN_INDENT + in;
    }
}
