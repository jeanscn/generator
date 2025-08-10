package org.mybatis.generator.api;

import lombok.Getter;
import org.mybatis.generator.api.dom.java.CompilationUnit;

/**
 * @author cen_c
 */
public class GeneratedJavaFile extends GeneratedFile {

    /**
     * -- GETTER --
     *  This method is required by the Eclipse Java merger. If you are not
     *  running in Eclipse, or some other system that implements the Java merge
     *  function, you may return null from this method.
     *
     */
    @Getter
    private final CompilationUnit compilationUnit;

    private final String fileEncoding;

    private final JavaFormatter javaFormatter;

    public GeneratedJavaFile(CompilationUnit compilationUnit,
            String targetProject,
            String fileEncoding,
            JavaFormatter javaFormatter) {
        super(targetProject);
        this.compilationUnit = compilationUnit;
        this.fileEncoding = fileEncoding;
        this.javaFormatter = javaFormatter;
    }

    public GeneratedJavaFile(CompilationUnit compilationUnit,
            String targetProject,
            JavaFormatter javaFormatter) {
        this(compilationUnit, targetProject, null, javaFormatter);
    }

    @Override
    public String getFormattedContent() {
        return javaFormatter.getFormattedContent(compilationUnit);
    }

    @Override
    public String getFileName() {
        return compilationUnit.getType().getShortNameWithoutTypeArguments() + ".java";
    }

    @Override
    public String getTargetPackage() {
        return compilationUnit.getType().getPackageName();
    }

    /**
     * A Java file is mergeable if the getCompilationUnit() method returns a valid compilation unit.
     *
     * @return true, if is mergeable
     */
    @Override
    public boolean isMergeable() {
        return true;
    }

    @Override
    public String getFileEncoding() {
        return fileEncoding;
    }
}
