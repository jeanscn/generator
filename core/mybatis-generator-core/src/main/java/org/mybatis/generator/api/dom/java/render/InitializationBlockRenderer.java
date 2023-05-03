package org.mybatis.generator.api.dom.java.render;

import java.util.ArrayList;
import java.util.List;

import org.mybatis.generator.api.dom.java.InitializationBlock;

public class InitializationBlockRenderer {

    private final BodyLineRenderer bodyLineRenderer = new BodyLineRenderer();

    public List<String> render(InitializationBlock initializationBlock) {

        List<String> lines = new ArrayList<>(initializationBlock.getJavaDocLines());
        lines.add(renderFirstLine(initializationBlock));
        lines.addAll(bodyLineRenderer.render(initializationBlock.getBodyLines()));
        lines.add("}"); //$NON-NLS-1$

        return lines;
    }

    private String renderFirstLine(InitializationBlock initializationBlock) {
        if (initializationBlock.isStatic()) {
            return "static {"; //$NON-NLS-1$
        } else {
            return "{"; //$NON-NLS-1$
        }
    }
}
