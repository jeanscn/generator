package org.mybatis.generator.api.dom.java;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author cen_c
 */
@Getter
public class InitializationBlock {

    @Setter
    private boolean isStatic;

    private final List<String> bodyLines = new ArrayList<>();
    private final List<String> javaDocLines = new ArrayList<>();

    public InitializationBlock() {
        this(false);
    }

    public InitializationBlock(boolean isStatic) {
        this.isStatic = isStatic;
    }

    public void addBodyLine(String line) {
        bodyLines.add(line);
    }

    public void addBodyLine(int index, String line) {
        bodyLines.add(index, line);
    }

    public void addBodyLines(Collection<String> lines) {
        bodyLines.addAll(lines);
    }

    public void addBodyLines(int index, Collection<String> lines) {
        bodyLines.addAll(index, lines);
    }

    public void addJavaDocLine(String javaDocLine) {
        javaDocLines.add(javaDocLine);
    }
}
