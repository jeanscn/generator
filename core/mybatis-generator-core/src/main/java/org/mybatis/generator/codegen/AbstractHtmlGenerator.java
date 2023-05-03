package org.mybatis.generator.codegen;

import org.mybatis.generator.api.dom.html.Document;
import org.mybatis.generator.config.HtmlGeneratorConfiguration;

public abstract class AbstractHtmlGenerator extends AbstractGenerator {
    public abstract Document getDocument(HtmlGeneratorConfiguration htmlGeneratorConfiguration);
}
