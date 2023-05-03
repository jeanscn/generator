package org.mybatis.generator.api.dom.kotlin.render;

import java.util.ArrayList;
import java.util.List;

import org.mybatis.generator.api.dom.kotlin.KotlinProperty;

public class KotlinPropertyRenderer {

    public List<String> render(KotlinProperty kotlinProperty) {
        List<String> answer = new ArrayList<>(kotlinProperty.getAnnotations());
        answer.add(renderProperty(kotlinProperty));
        return answer;
    }

    private String renderProperty(KotlinProperty kotlinProperty) {
        return KotlinRenderingUtilities.renderModifiers(kotlinProperty.getModifiers())
                + kotlinProperty.getType().getValue()
                + " " //$NON-NLS-1$
                + kotlinProperty.getName()
                + kotlinProperty.getDataType().map(dt -> ": " + dt).orElse("") //$NON-NLS-1$ //$NON-NLS-2$
                + kotlinProperty.getInitializationString().map(s -> " = " + s).orElse(""); //$NON-NLS-1$ //$NON-NLS-2$
    }
}
