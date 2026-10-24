package org.mybatis.generator.api.dom.xml;

import java.util.Optional;

public class Document {

    private DocType docType;

    private XmlElement rootElement;

    public Document(String publicId, String systemId) {
        docType = new PublicDocType(publicId, systemId);
    }

    public Document(String systemId) {
        docType = new SystemDocType(systemId);
    }

    public Document() {
        super();
    }

    public XmlElement getRootElement() {
        return rootElement;
    }

    public void setRootElement(XmlElement rootElement) {
        this.rootElement = rootElement;
    }

    public Optional<DocType> getDocType() {
        return Optional.ofNullable(docType);
    }
}
