package org.mybatis.generator.codegen;

import org.mybatis.generator.api.dom.html.Document;
import org.mybatis.generator.config.HtmlGeneratorConfiguration;

/**
 * @author cen_c
 */
public abstract class AbstractHtmlGenerator extends AbstractGenerator {

    /**
     * Get HTML document
     * @param htmlGeneratorConfiguration HTML generator configuration
     * @return HTML document
     */
    public abstract Document getDocument(HtmlGeneratorConfiguration htmlGeneratorConfiguration);
}
