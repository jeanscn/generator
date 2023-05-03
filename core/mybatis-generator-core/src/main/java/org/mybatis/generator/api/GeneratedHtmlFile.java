package org.mybatis.generator.api;

import org.mybatis.generator.api.dom.html.Document;
import org.mybatis.generator.codegen.mybatis3.AbstractGeneratedFile;

public class GeneratedHtmlFile extends AbstractGeneratedFile {

    private final Document document;
    private boolean isMergeable;

    private final HtmlFormatter htmlFormatter;

    private boolean overwriteFile;

    public GeneratedHtmlFile(Document document, String fileName,
                             String targetPackage, String targetProject, boolean isMergeable,
                             HtmlFormatter htmlFormatter,IntrospectedTable introspectedTable) {
        super(targetProject,targetPackage,fileName,introspectedTable);
        this.document = document;
        this.isMergeable = isMergeable;
        this.htmlFormatter = htmlFormatter;
    }

    @Override
    public String getFormattedContent() {
        return htmlFormatter.getFormattedContent(document);
    }

    @Override
    public boolean isMergeable() {
        return isMergeable;
    }

    public void setMergeable(boolean isMergeable) {
        this.isMergeable = isMergeable;
    }

    public boolean isOverwriteFile() {
        return overwriteFile;
    }

    public void setOverwriteFile(boolean overwriteFile) {
        this.overwriteFile = overwriteFile;
    }
}
