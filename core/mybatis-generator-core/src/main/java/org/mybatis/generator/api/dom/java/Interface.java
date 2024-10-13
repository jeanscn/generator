package org.mybatis.generator.api.dom.java;

import org.mybatis.generator.config.VoAdditionalPropertyGeneratorConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class Interface extends InnerInterface implements CompilationUnit {

    private final Set<FullyQualifiedJavaType> importedTypes = new TreeSet<>();

    private final Set<String> staticImports = new TreeSet<>();

    private final List<String> fileCommentLines = new ArrayList<>();

    public Interface(FullyQualifiedJavaType type) {
        super(type);
    }

    public Interface(String type) {
        this(new FullyQualifiedJavaType(type));
    }

    @Override
    public Set<FullyQualifiedJavaType> getImportedTypes() {
        return importedTypes;
    }

    @Override
    public void addImportedType(FullyQualifiedJavaType importedType) {
        if (importedType.isExplicitlyImported()
                && !importedType.getPackageName().equals(getType().getPackageName())) {
            importedTypes.add(importedType);
        }
    }

    @Override
    public void addFileCommentLine(String commentLine) {
        fileCommentLines.add(commentLine);
    }

    @Override
    public List<String> getFileCommentLines() {
        return fileCommentLines;
    }

    @Override
    public void addImportedTypes(Set<FullyQualifiedJavaType> importedTypes) {
        this.importedTypes.addAll(importedTypes);
    }

    @Override
    public Set<String> getStaticImports() {
        return staticImports;
    }

    @Override
    public void addStaticImport(String staticImport) {
        staticImports.add(staticImport);
    }

    @Override
    public void addStaticImports(Set<String> staticImports) {
        this.staticImports.addAll(staticImports);
    }

    @Override
    public List<Field> getAdditionalPropertiesFields(TreeSet<VoAdditionalPropertyGeneratorConfiguration> configurations) {
        List<Field> fields = new ArrayList<>();
        configurations.stream()
                .filter(c -> !(c.getName() == null || c.getType() == null))
                .forEach(c -> {
                    FullyQualifiedJavaType type = new FullyQualifiedJavaType(c.getType());
                    org.mybatis.generator.api.dom.java.Field field = new Field(c.getName(), type);
                    if (!c.getTypeArguments().isEmpty()) {
                        for (String typeArgument : c.getTypeArguments()) {
                            type.addTypeArgument(new FullyQualifiedJavaType(typeArgument));
                        }
                    }
                    fields.add(field);
                });
        return fields;
    }

    @Override
    public <R> R accept(CompilationUnitVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
