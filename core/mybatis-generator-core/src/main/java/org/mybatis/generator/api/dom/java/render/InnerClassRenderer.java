package org.mybatis.generator.api.dom.java.render;

import java.util.ArrayList;
import java.util.List;

import org.mybatis.generator.api.dom.java.CompilationUnit;
import org.mybatis.generator.api.dom.java.InnerClass;
import org.mybatis.generator.api.dom.java.JavaDomUtils;
import org.mybatis.generator.internal.util.CustomCollectors;

/**
 * @author cen_c
 */
public class InnerClassRenderer {

    public List<String> render(InnerClass innerClass, CompilationUnit compilationUnit) {
        List<String> lines = new ArrayList<>();

        lines.addAll(innerClass.getJavaDocLines());
        lines.addAll(innerClass.getAnnotations());
        lines.add(renderFirstLine(innerClass, compilationUnit));
        lines.addAll(RenderingUtilities.renderFields(innerClass.getFields(), compilationUnit));
        lines.addAll(RenderingUtilities.renderInitializationBlocks(innerClass.getInitializationBlocks()));
        lines.addAll(RenderingUtilities.renderClassOrEnumMethods(innerClass.getMethods(), compilationUnit));
        lines.addAll(RenderingUtilities.renderInnerClasses(innerClass.getInnerClasses(), compilationUnit));
        lines.addAll(RenderingUtilities.renderInnerInterfaces(innerClass.getInnerInterfaces(), compilationUnit));
        lines.addAll(RenderingUtilities.renderInnerEnums(innerClass.getInnerEnums(), compilationUnit));

        lines = RenderingUtilities.removeLastEmptyLine(lines);

        lines.add("}");

        return lines;
    }

    private String renderFirstLine(InnerClass innerClass, CompilationUnit compilationUnit) {
        StringBuilder sb = new StringBuilder();

        sb.append(innerClass.getVisibility().getValue());

        if (innerClass.isAbstract()) {
            sb.append("abstract ");
        }

        if (innerClass.isStatic()) {
            sb.append("static ");
        }

        if (innerClass.isFinal()) {
            sb.append("final ");
        }

        sb.append("class ");
        sb.append(innerClass.getType().getShortName());
        sb.append(RenderingUtilities.renderTypeParameters(innerClass.getTypeParameters(), compilationUnit));
        sb.append(renderSuperClass(innerClass, compilationUnit));
        sb.append(renderSuperInterfaces(innerClass, compilationUnit));
        sb.append(" {");

        return sb.toString();
    }

    private String renderSuperClass(InnerClass innerClass, CompilationUnit compilationUnit) {
        return innerClass.getSuperClass()
                .map(sc -> " extends " + JavaDomUtils.calculateTypeName(compilationUnit, sc))
                .orElse("");
    }

    /**
     * Render the super interfaces of the inner class.
     * @param innerClass the inner class to render
     * @param compilationUnit the compilation unit to which the inner class belongs
     * @return a string representing the super interfaces, or an empty string if there are none
     */
    private String renderSuperInterfaces(InnerClass innerClass, CompilationUnit compilationUnit) {
        return innerClass.getSuperInterfaceTypes().stream()
                .map(tp -> JavaDomUtils.calculateTypeName(compilationUnit, tp))
                .collect(CustomCollectors.joining(", ", " implements ", ""));   
    }
}
