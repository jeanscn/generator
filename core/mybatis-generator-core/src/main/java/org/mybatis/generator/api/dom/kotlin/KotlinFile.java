package org.mybatis.generator.api.dom.kotlin;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author cen_c
 */
public class KotlinFile {
    @Getter
    private final String fileName;
    @Getter
    private final List<String> fileCommentLines = new ArrayList<>();
    @Getter
    private final Set<String> imports = new TreeSet<>();
    private String packageDefinition;
    @Getter
    private final List<KotlinNamedItem> namedItems = new ArrayList<>();

    public KotlinFile(String fileName) {
        Objects.requireNonNull(fileName);

        if (fileName.endsWith(".kt")) {
            this.fileName = fileName;
        } else {
            this.fileName = fileName + ".kt";
        }
    }

    public void addFileCommentLine(String fileComentLine) {
        fileCommentLines.add(fileComentLine);
    }

    public void addImport(String i) {
        imports.add(i);
    }

    public void addImports(Collection<String> imports) {
        this.imports.addAll(imports);
    }

    public Optional<String> getPackage() {
        return Optional.ofNullable(packageDefinition);
    }

    public void setPackage(String p) {
        this.packageDefinition = p;
    }

    public void addNamedItem(KotlinNamedItem namedItem) {
        namedItems.add(namedItem);
    }

}
