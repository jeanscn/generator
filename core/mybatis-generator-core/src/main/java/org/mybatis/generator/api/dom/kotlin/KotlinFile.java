package org.mybatis.generator.api.dom.kotlin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

public class KotlinFile {
    private final String fileName;
    private final List<String> fileCommentLines = new ArrayList<>();
    private final Set<String> imports = new TreeSet<>();
    private String packageDefinition;
    private final List<KotlinNamedItem> namedItems = new ArrayList<>();

    public KotlinFile(String fileName) {
        Objects.requireNonNull(fileName);

        if (fileName.endsWith(".kt")) { //$NON-NLS-1$
            this.fileName = fileName;
        } else {
            this.fileName = fileName + ".kt"; //$NON-NLS-1$
        }
    }

    public String getFileName() {
        return fileName;
    }

    public List<String> getFileCommentLines() {
        return fileCommentLines;
    }

    public void addFileCommentLine(String fileComentLine) {
        fileCommentLines.add(fileComentLine);
    }

    public Set<String> getImports() {
        return imports;
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

    public List<KotlinNamedItem> getNamedItems() {
        return namedItems;
    }
}
