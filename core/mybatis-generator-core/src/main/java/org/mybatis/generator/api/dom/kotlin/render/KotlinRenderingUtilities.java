package org.mybatis.generator.api.dom.kotlin.render;

import java.util.List;

import org.mybatis.generator.api.dom.kotlin.KotlinModifier;
import org.mybatis.generator.internal.util.CustomCollectors;

public class KotlinRenderingUtilities {

    private KotlinRenderingUtilities() {}

    public static final String KOTLIN_INDENT = "    "; //$NON-NLS-1$

    public static String renderModifiers(List<KotlinModifier> modifiers) {
        return modifiers.stream().map(KotlinModifier::getValue)
                .collect(CustomCollectors.joining(" ", "", " ")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    public static String kotlinIndent(String in) {
        if (in.isEmpty()) {
            return in; // don't indent empty lines
        }

        return KOTLIN_INDENT + in;
    }
}
