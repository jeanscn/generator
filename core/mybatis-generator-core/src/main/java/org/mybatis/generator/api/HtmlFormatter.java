package org.mybatis.generator.api;

import org.mybatis.generator.api.dom.html.Document;
import org.mybatis.generator.config.Context;

public interface HtmlFormatter {
    void setContext(Context context);

    String getFormattedContent(Document document);
}
