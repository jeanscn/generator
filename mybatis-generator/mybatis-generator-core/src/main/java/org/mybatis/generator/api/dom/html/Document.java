package org.mybatis.generator.api.dom.html;

import java.util.Optional;

public class Document {

    private DocType docType;

    private HtmlElement rootElement;

    public Document(String publicId, String systemId) {
        docType = new PublicDocType(publicId, systemId);
    }

    public Document(String systemId) {
        docType = new SystemDocType(systemId);
    }

    public Document() {
        super();
    }

    public HtmlElement getRootElement() {
        return rootElement;
    }

    public Optional<DocType> getDocType() {
        return Optional.ofNullable(docType);
    }

    public void setRootElement(HtmlElement rootElement) {
        this.rootElement = rootElement;
    }
}
