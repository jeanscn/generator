package org.mybatis.generator.api;

import org.mybatis.generator.api.dom.kotlin.KotlinFile;

public class GeneratedKotlinFile extends GeneratedFile {

    private final KotlinFile kotlinFile;

    private final String fileEncoding;

    private final KotlinFormatter kotlinFormatter;

    public GeneratedKotlinFile(KotlinFile kotlinFile,
            String targetProject,
            String fileEncoding,
            KotlinFormatter kotlinFormatter) {
        super(targetProject);
        this.kotlinFile = kotlinFile;
        this.fileEncoding = fileEncoding;
        this.kotlinFormatter = kotlinFormatter;
    }

    @Override
    public String getFormattedContent() {
        return kotlinFormatter.getFormattedContent(kotlinFile);
    }

    @Override
    public String getFileName() {
        return kotlinFile.getFileName();
    }

    @Override
    public String getTargetPackage() {
        return kotlinFile.getPackage().orElse(""); //$NON-NLS-1$
    }

    @Override
    public boolean isMergeable() {
        return false;
    }

    @Override
    public String getFileEncoding() {
        return fileEncoding;
    }
}
