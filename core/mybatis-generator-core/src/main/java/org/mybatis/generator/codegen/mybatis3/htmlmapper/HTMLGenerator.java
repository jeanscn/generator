package org.mybatis.generator.codegen.mybatis3.htmlmapper;

import org.mybatis.generator.api.FullyQualifiedTable;
import org.mybatis.generator.api.dom.html.Attribute;
import org.mybatis.generator.api.dom.html.Document;
import org.mybatis.generator.api.dom.html.HtmlElement;
import org.mybatis.generator.codegen.AbstractHtmlGenerator;
import org.mybatis.generator.codegen.mybatis3.htmlmapper.elements.AbstractThymeleafHtmlElementGenerator;
import org.mybatis.generator.config.HtmlGeneratorConfiguration;

import static org.mybatis.generator.codegen.mybatis3.htmlmapper.HtmlConstant.MYBATIS3_THYMELEAF_XMLNS_SEC;
import static org.mybatis.generator.codegen.mybatis3.htmlmapper.HtmlConstant.MYBATIS3_THYMELEAF_XMLNS_TH;
import static org.mybatis.generator.internal.util.messages.Messages.getString;

public class HTMLGenerator extends AbstractHtmlGenerator {


    public HTMLGenerator() {
        super();
    }

    protected HtmlElement getHtmlMapElement(HtmlGeneratorConfiguration htmlGeneratorConfiguration) {
        FullyQualifiedTable table = introspectedTable.getFullyQualifiedTable();
        progressCallback.startTask(getString("Progress.121", table.toString(), htmlGeneratorConfiguration.getHtmlFileName())); //$NON-NLS-1$
        HtmlElement answer = new HtmlElement("html");
        answer.addAttribute(new Attribute("xmlns:th",MYBATIS3_THYMELEAF_XMLNS_TH));
        answer.addAttribute(new Attribute("xmlns:sec",MYBATIS3_THYMELEAF_XMLNS_SEC));
        context.getCommentGenerator().addRootComment(answer);
        return answer;
    }

    protected void initializeAndExecuteGenerator(AbstractThymeleafHtmlElementGenerator elementGenerator, HtmlElement parentElement) {
        elementGenerator.setContext(context);
        elementGenerator.setIntrospectedTable(introspectedTable);
        elementGenerator.setProgressCallback(progressCallback);
        elementGenerator.setWarnings(warnings);
    }

    @Override
    public Document getDocument(HtmlGeneratorConfiguration htmlGeneratorConfiguration) {
        Document document = new Document(
                MYBATIS3_THYMELEAF_XMLNS_TH,
                MYBATIS3_THYMELEAF_XMLNS_SEC);
        document.setRootElement(getHtmlMapElement(htmlGeneratorConfiguration));
        if (!context.getPlugins().htmlMapDocumentGenerated(document,introspectedTable, htmlGeneratorConfiguration)) {
            document = null;
        }
        return document;
    }
}
