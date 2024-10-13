package org.mybatis.generator.api.dom.java;

import org.mybatis.generator.config.VoAdditionalPropertyGeneratorConfiguration;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * This interface describes methods common to all Java compilation units (Java
 * classes, interfaces, and enums).
 *
 * @author Jeff Butler
 */
public interface CompilationUnit {

    Set<FullyQualifiedJavaType> getImportedTypes();

    Set<String> getStaticImports();

    FullyQualifiedJavaType getType();

    void addImportedType(FullyQualifiedJavaType importedType);

    void addImportedTypes(Set<FullyQualifiedJavaType> importedTypes);

    void addStaticImport(String staticImport);

    void addStaticImports(Set<String> staticImports);

    List<Field> getAdditionalPropertiesFields(TreeSet<VoAdditionalPropertyGeneratorConfiguration> configurations);

    /**
     * Comments will be written at the top of the file as is, we do not append any start or end comment characters.
     *
     * <p>Note that in the Eclipse plugin, file comments will not be merged.
     *
     * @param commentLine
     *            the comment line
     */
    void addFileCommentLine(String commentLine);

    List<String> getFileCommentLines();

    <R> R accept(CompilationUnitVisitor<R> visitor);
}
