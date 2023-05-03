package org.mybatis.generator.api.dom;

import org.mybatis.generator.api.HtmlFormatter;
import org.mybatis.generator.api.XmlFormatter;
import org.mybatis.generator.api.dom.html.Document;
import org.mybatis.generator.api.dom.html.render.DocumentRenderer;
import org.mybatis.generator.config.Context;

/**
 * This class is the default formatter for generated XML.  This class will use the
 * built in document renderer.
 *
 * @author Jeff Butler
 *
 */
public class DefaultHtmlFormatter implements HtmlFormatter {
    protected Context context;

    @Override
    public String getFormattedContent(Document document) {
        return new DocumentRenderer().render(document);
    }

    @Override
    public void setContext(Context context) {
        this.context = context;
    }
}
