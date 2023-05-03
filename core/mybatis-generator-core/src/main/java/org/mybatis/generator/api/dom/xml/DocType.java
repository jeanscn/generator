package org.mybatis.generator.api.dom.xml;

public interface DocType {
    <R> R accept(DocTypeVisitor<R> visitor);
}
