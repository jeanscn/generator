package org.mybatis.generator.codegen.mybatis3.htmlmapper;

import org.mybatis.generator.api.HtmlFormatter;
import org.mybatis.generator.api.AbstractIntrospectedTable;
import org.mybatis.generator.api.dom.html.Document;
import org.mybatis.generator.codegen.mybatis3.AbstractGeneratedFile;

/**
 * @author cen_c
 */
public class GeneratedHtmlFile extends AbstractGeneratedFile {

    private final Document document;
    private boolean isMergeable;

    private final HtmlFormatter htmlFormatter;

    private boolean overwriteFile;

    public GeneratedHtmlFile(Document document, String fileName,
                             String targetPackage, String targetProject, boolean isMergeable,
                             HtmlFormatter htmlFormatter, AbstractIntrospectedTable abstractIntrospectedTable) {
        super(targetProject,targetPackage,fileName, abstractIntrospectedTable);
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
