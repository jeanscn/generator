package org.mybatis.generator.api.dom.html.render;

import org.mybatis.generator.api.dom.html.DocTypeVisitor;
import org.mybatis.generator.api.dom.html.PublicDocType;
import org.mybatis.generator.api.dom.html.SystemDocType;

/**
 * @author cen_c
 */
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
