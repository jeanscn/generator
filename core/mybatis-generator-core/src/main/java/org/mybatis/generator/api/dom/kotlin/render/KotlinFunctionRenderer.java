package org.mybatis.generator.api.dom.kotlin.render;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.mybatis.generator.api.dom.kotlin.KotlinFunction;

public class KotlinFunctionRenderer {
    public List<String> render(KotlinFunction function) {

        List<String> answer = new ArrayList<>(function.getAnnotations());

        answer.add(renderFirstLine(function));

        answer.addAll(function.getCodeLines().stream().map(KotlinRenderingUtilities::kotlinIndent)
                .collect(Collectors.toList()));

        if (!function.getCodeLines().isEmpty() && !function.isOneLineFunction()) {
            answer.add("}");
        }

        return answer;
    }

    private String renderFirstLine(KotlinFunction function) {
        String firstLine = KotlinRenderingUtilities.renderModifiers(function.getModifiers())
                + "fun "
                + function.getName()
                + "(";

        firstLine += renderArguments(function);
        firstLine += ")";
        firstLine += renderReturnType(function);

        if (!function.getCodeLines().isEmpty()) {
            if (function.isOneLineFunction()) {
                firstLine += " =";
            } else {
                firstLine += " {";
            }
        }

        return firstLine;
    }

    private String renderArguments(KotlinFunction function) {
        KotlinArgRenderer argRenderer = new KotlinArgRenderer();

        return function.getArguments().stream()
                .map(argRenderer::render)
                .collect(Collectors.joining(", "));
    }

    private String renderReturnType(KotlinFunction function) {
        return function.getExplicitReturnType()
                .map(s -> ": " + s)
                .orElse("");
    }
}
