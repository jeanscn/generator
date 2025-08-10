package org.mybatis.generator.api.dom.java.render;

import java.util.ArrayList;
import java.util.List;

import org.mybatis.generator.api.dom.java.CompilationUnit;
import org.mybatis.generator.api.dom.java.InnerInterface;
import org.mybatis.generator.api.dom.java.JavaDomUtils;
import org.mybatis.generator.internal.util.CustomCollectors;

/**
 * @author cen_c
 */
public class InnerInterfaceRenderer {

    public List<String> render(InnerInterface innerInterface, CompilationUnit compilationUnit) {
        List<String> lines = new ArrayList<>();

        lines.addAll(innerInterface.getJavaDocLines());
        lines.addAll(innerInterface.getAnnotations());
        lines.add(renderFirstLine(innerInterface, compilationUnit));
        lines.addAll(RenderingUtilities.renderFields(innerInterface.getFields(), compilationUnit));
        lines.addAll(RenderingUtilities.renderInterfaceMethods(innerInterface.getMethods(), compilationUnit));
        lines.addAll(RenderingUtilities.renderInnerClasses(innerInterface.getInnerClasses(), compilationUnit));
        lines.addAll(RenderingUtilities.renderInnerInterfaces(innerInterface.getInnerInterfaces(), compilationUnit));
        lines.addAll(RenderingUtilities.renderInnerEnums(innerInterface.getInnerEnums(), compilationUnit));

        lines = RenderingUtilities.removeLastEmptyLine(lines);

        lines.add("}");

        return lines;
    }

    private String renderFirstLine(InnerInterface innerInterface, CompilationUnit compilationUnit) {
        StringBuilder sb = new StringBuilder();

        sb.append(innerInterface.getVisibility().getValue());

        if (innerInterface.isStatic()) {
            sb.append("static ");
        }

        sb.append("interface ");
        sb.append(innerInterface.getType().getShortName());
        sb.append(RenderingUtilities.renderTypeParameters(innerInterface.getTypeParameters(), compilationUnit));
        sb.append(renderSuperInterfaces(innerInterface, compilationUnit));
        sb.append(" {");

        return sb.toString();
    }

    /**
     * Renders the super interfaces of the inner interface.
     * @param innerInterface 内部接口
     * @param compilationUnit 编译单元
     * @return 字符串表示的超接口
     */
    private String renderSuperInterfaces(InnerInterface innerInterface, CompilationUnit compilationUnit) {
        return innerInterface.getSuperInterfaceTypes().stream()
                .map(tp -> JavaDomUtils.calculateTypeName(compilationUnit, tp))
                .collect(CustomCollectors.joining(", ", " extends ", ""));
    }
}
