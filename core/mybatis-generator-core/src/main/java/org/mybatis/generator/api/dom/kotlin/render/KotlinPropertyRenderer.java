package org.mybatis.generator.api.dom.kotlin.render;

import java.util.ArrayList;
import java.util.List;

import org.mybatis.generator.api.dom.kotlin.KotlinProperty;

/**
 * @author cen_c
 */
public class KotlinPropertyRenderer {

    public List<String> render(KotlinProperty kotlinProperty) {
        List<String> answer = new ArrayList<>(kotlinProperty.getAnnotations());
        answer.add(renderProperty(kotlinProperty));
        return answer;
    }

    private String renderProperty(KotlinProperty kotlinProperty) {
        return KotlinRenderingUtilities.renderModifiers(kotlinProperty.getModifiers())
                + kotlinProperty.getType().getValue()
                + " "
                + kotlinProperty.getName()
                + kotlinProperty.getDataType().map(dt -> ": " + dt).orElse("")  
                + kotlinProperty.getInitializationString().map(s -> " = " + s).orElse("");  
    }
}
