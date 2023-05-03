package org.mybatis.generator.api.dom.html;

public interface DocType {
    <R> R accept(DocTypeVisitor<R> visitor);
}
