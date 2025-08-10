package org.mybatis.generator.api.dom.xml.render;

import org.mybatis.generator.api.dom.xml.DocTypeVisitor;
import org.mybatis.generator.api.dom.xml.PublicDocType;
import org.mybatis.generator.api.dom.xml.SystemDocType;

public class DocTypeRenderer implements DocTypeVisitor<String> {

    @Override
    public String visit(PublicDocType docType) {
        return "PUBLIC \""
                + docType.getDtdName()
                + "\" \""
                + docType.getDtdLocation()
                + "\"";
    }

    @Override
    public String visit(SystemDocType docType) {
        return "SYSTEM \""
                + docType.getDtdLocation()
                + "\"";
    }
}
