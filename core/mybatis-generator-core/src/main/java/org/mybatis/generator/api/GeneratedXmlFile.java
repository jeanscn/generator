package org.mybatis.generator.api;

import org.mybatis.generator.api.dom.xml.Document;

public class GeneratedXmlFile extends GeneratedFile {

    private final Document document;

    private final String fileName;

    private final String targetPackage;

    private boolean isMergeable;

    private final XmlFormatter xmlFormatter;

    public GeneratedXmlFile(Document document, String fileName,
            String targetPackage, String targetProject, boolean isMergeable,
            XmlFormatter xmlFormatter) {
        super(targetProject);
        this.document = document;
        this.fileName = fileName;
        this.targetPackage = targetPackage;
        this.isMergeable = isMergeable;
        this.xmlFormatter = xmlFormatter;
    }

    @Override
    public String getFormattedContent() {
        return xmlFormatter.getFormattedContent(document);
    }

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
        return isMergeable;
    }

    public void setMergeable(boolean isMergeable) {
        this.isMergeable = isMergeable;
    }

    @Override
    public String getFileEncoding() {
        return "UTF-8"; //$NON-NLS-1$
    }
}
