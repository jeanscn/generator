package org.mybatis.generator.api.dom.html.render;

import org.mybatis.generator.api.dom.html.DocTypeVisitor;
import org.mybatis.generator.api.dom.html.PublicDocType;
import org.mybatis.generator.api.dom.html.SystemDocType;

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
