package org.mybatis.generator.api.dom.html;

public interface DocTypeVisitor<R> {
    R visit(PublicDocType docType);

    R visit(SystemDocType docType);
}
