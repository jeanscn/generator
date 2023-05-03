package org.mybatis.generator.api.dom.xml.render;

import org.mybatis.generator.api.dom.xml.DocTypeVisitor;
import org.mybatis.generator.api.dom.xml.PublicDocType;
import org.mybatis.generator.api.dom.xml.SystemDocType;

public class DocTypeRenderer implements DocTypeVisitor<String> {

    @Override
    public String visit(PublicDocType docType) {
        return "PUBLIC \"" //$NON-NLS-1$
                + docType.getDtdName()
                + "\" \"" //$NON-NLS-1$
                + docType.getDtdLocation()
                + "\""; //$NON-NLS-1$
    }

    @Override
    public String visit(SystemDocType docType) {
        return "SYSTEM \"" //$NON-NLS-1$
                + docType.getDtdLocation()
                + "\""; //$NON-NLS-1$
    }
}
