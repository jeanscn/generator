package org.mybatis.generator.api.dom.kotlin.render;

import org.mybatis.generator.api.dom.kotlin.KotlinArg;
import org.mybatis.generator.internal.util.CustomCollectors;

/**
 * @author cen_c
 */
public class KotlinArgRenderer {
    public String render(KotlinArg kotlinArg) {
        return renderAnnotations(kotlinArg)
                + kotlinArg.getName()
                + kotlinArg.getDataType().map(dt -> ": " + dt).orElse("")  
                + kotlinArg.getInitializationString().map(s -> " = " + s).orElse("");  
    }

    private String renderAnnotations(KotlinArg kotlinArg) {
        return kotlinArg.getAnnotations().stream()
                .collect(CustomCollectors.joining(" ", "", " "));   
    }
}
