package org.mybatis.generator.api.dom.kotlin.render;

import org.mybatis.generator.api.dom.kotlin.KotlinArg;
import org.mybatis.generator.internal.util.CustomCollectors;

public class KotlinArgRenderer {
    public String render(KotlinArg kotlinArg) {
        return renderAnnotations(kotlinArg)
                + kotlinArg.getName()
                + kotlinArg.getDataType().map(dt -> ": " + dt).orElse("") //$NON-NLS-1$ //$NON-NLS-2$
                + kotlinArg.getInitializationString().map(s -> " = " + s).orElse(""); //$NON-NLS-1$ //$NON-NLS-2$
    }

    private String renderAnnotations(KotlinArg kotlinArg) {
        return kotlinArg.getAnnotations().stream()
                .collect(CustomCollectors.joining(" ", "", " ")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
}
