package org.mybatis.generator.api.dom.kotlin.render;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.mybatis.generator.api.dom.java.render.RenderingUtilities;
import org.mybatis.generator.api.dom.kotlin.KotlinNamedItem;
import org.mybatis.generator.api.dom.kotlin.KotlinProperty;
import org.mybatis.generator.api.dom.kotlin.KotlinType;
import org.mybatis.generator.internal.util.CustomCollectors;

public class KotlinTypeRenderer {

    public List<String> render(KotlinType kotlinType) {
        KotlinNamedItemRenderer renderer = new KotlinNamedItemRenderer();

        List<String> answer = new ArrayList<>(kotlinType.getAnnotations());

        String renderedModifiersAndName = KotlinRenderingUtilities.renderModifiers(kotlinType.getModifiers())
                + kotlinType.getType().getValue() + " " //$NON-NLS-1$
                + kotlinType.getName();
        String renderedSuperTypes = kotlinType.getSuperTypes().stream()
                .sorted()
                .collect(CustomCollectors.joining(", ", " : ", "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        if (kotlinType.getNamedItems().isEmpty()) {
            if (kotlinType.getConstructorProperties().isEmpty()) {
                answer.add(renderedModifiersAndName + renderedSuperTypes);
            } else {
                answer.add(renderedModifiersAndName + "("); //$NON-NLS-1$
                answer.addAll(renderConstructorItems(kotlinType));
                answer.add(")" + renderedSuperTypes); //$NON-NLS-1$
            }
        } else {
            if (kotlinType.getConstructorProperties().isEmpty()) {
                answer.add(renderedModifiersAndName + renderedSuperTypes + " {"); //$NON-NLS-1$
            } else {
                answer.add(renderedModifiersAndName + "("); //$NON-NLS-1$
                answer.addAll(renderConstructorItems(kotlinType));
                answer.add(")" + renderedSuperTypes + " {"); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }

        for (KotlinNamedItem namedItem : kotlinType.getNamedItems()) {
            answer.addAll(renderer.render(namedItem).stream().map(KotlinRenderingUtilities::kotlinIndent)
                    .collect(Collectors.toList()));
            answer.add(""); //$NON-NLS-1$
        }

        answer = RenderingUtilities.removeLastEmptyLine(answer);

        if (!kotlinType.getNamedItems().isEmpty()) {
            answer.add("}"); //$NON-NLS-1$
        }

        return answer;
    }

    private List<String> renderConstructorItems(KotlinType kotlinType) {
        List<String> lines = new ArrayList<>();
        KotlinPropertyRenderer renderer = new KotlinPropertyRenderer();

        Iterator<KotlinProperty> iter = kotlinType.getConstructorProperties().iterator();
        while (iter.hasNext()) {
            lines.addAll(renderer.render(iter.next()).stream().map(KotlinRenderingUtilities::kotlinIndent)
                    .collect(Collectors.toList()));
            if (iter.hasNext()) {
                lines.set(lines.size() - 1,
                        lines.get(lines.size() - 1) + ","); //$NON-NLS-1$
            }
        }

        return lines;
    }
}
