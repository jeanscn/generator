/**
 *    Copyright 2006-2020 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.generator.codegen.mybatis3.htmlmapper;

import org.mybatis.generator.api.FullyQualifiedTable;
import org.mybatis.generator.api.dom.html.Attribute;
import org.mybatis.generator.api.dom.html.Document;
import org.mybatis.generator.api.dom.html.HtmlElement;
import org.mybatis.generator.codegen.AbstractHtmlGenerator;
import org.mybatis.generator.codegen.HtmlConstants;
import org.mybatis.generator.codegen.mybatis3.htmlmapper.elements.AbstractHtmlElementGenerator;
import org.mybatis.generator.config.HtmlMapGeneratorConfiguration;

import static org.mybatis.generator.internal.util.messages.Messages.getString;

public class HTMLMapperGenerator extends AbstractHtmlGenerator {

    public HTMLMapperGenerator() {
        super();
    }

    protected HtmlElement getHtmlMapElement() {
        FullyQualifiedTable table = introspectedTable.getFullyQualifiedTable();
        progressCallback.startTask(getString("Progress.121", table.toString())); //$NON-NLS-1$
        HtmlElement answer = new HtmlElement("html"); //$NON-NLS-1$
        answer.addAttribute(new Attribute("xmlns:th",HtmlConstants.MYBATIS3_THYEMLEAF_XMLNS_TH));
        answer.addAttribute(new Attribute("xmlns:sec",HtmlConstants.MYBATIS3_THYEMLEAF_XMLNS_SEC));
        context.getCommentGenerator().addRootComment(answer);
        return answer;
    }

    protected void initializeAndExecuteGenerator(
            AbstractHtmlElementGenerator elementGenerator,
            HtmlElement parentElement) {
        elementGenerator.setContext(context);
        elementGenerator.setIntrospectedTable(introspectedTable);
        elementGenerator.setProgressCallback(progressCallback);
        elementGenerator.setWarnings(warnings);
        elementGenerator.addElements(parentElement);
    }

    @Override
    public Document getDocument(HtmlMapGeneratorConfiguration htmlMapGeneratorConfiguration) {
        Document document = new Document(
                HtmlConstants.MYBATIS3_THYEMLEAF_XMLNS_TH,
                HtmlConstants.MYBATIS3_THYEMLEAF_XMLNS_SEC);
        document.setRootElement(getHtmlMapElement());

        if (!context.getPlugins().htmlMapDocumentGenerated(document,introspectedTable,htmlMapGeneratorConfiguration)) {
            document = null;
        }
        return document;
    }
}
