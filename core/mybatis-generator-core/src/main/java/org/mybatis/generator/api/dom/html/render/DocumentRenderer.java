package org.mybatis.generator.api.dom.html.render;

import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.mybatis.generator.api.dom.html.DocType;
import org.mybatis.generator.api.dom.html.Document;

public class DocumentRenderer {

    public String render(Document document) {
        return Stream.of(renderDocType(document),
                renderRootElement(document))
                .flatMap(Function.identity())
                .collect(Collectors.joining(System.getProperty("line.separator"))); //$NON-NLS-1$
    }

    private Stream<String> renderXmlHeader() {
        return Stream.of("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"); //$NON-NLS-1$
    }

    private Stream<String>   renderDocType(Document document) {
        return Stream.of("<!DOCTYPE " //$NON-NLS-1$
                + document.getRootElement().getName()
                + ">"); //$NON-NLS-1$
    }

    private String renderDocType(DocType docType) {
        return " " + docType.accept(new DocTypeRenderer()); //$NON-NLS-1$
    }

    private Stream<String> renderRootElement(Document document) {
        return document.getRootElement().accept(new ElementRenderer());
    }
}
