package org.mybatis.generator.api.dom.xml;

public interface DocTypeVisitor<R> {
    R visit(PublicDocType docType);

    R visit(SystemDocType docType);
}
