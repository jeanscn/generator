package org.mybatis.generator.codegen.mybatis3;

import org.mybatis.generator.api.GeneratedFile;
import org.mybatis.generator.api.IntrospectedTable;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-04-06 16:21
 * @version 3.0
 */
public abstract class AbstractGeneratedFile extends GeneratedFile {

    protected final String fileName;
    protected final String targetPackage;
    protected final IntrospectedTable introspectedTable;

    public AbstractGeneratedFile(String targetProject, String targetPackage, String fileName,  IntrospectedTable introspectedTable) {
        super(targetProject);
        this.fileName = fileName;
        this.targetPackage = targetPackage;
        this.introspectedTable = introspectedTable;
    }

    public abstract String getFormattedContent();

    @Override
    public String getFileName() {
        return fileName;
    }

    @Override
    public String getTargetPackage() {
        return targetPackage;
    }

    @Override
    public boolean isMergeable() {
        return false;
    }

    @Override
    public String getFileEncoding() {
        return "UTF-8";
    }
}
