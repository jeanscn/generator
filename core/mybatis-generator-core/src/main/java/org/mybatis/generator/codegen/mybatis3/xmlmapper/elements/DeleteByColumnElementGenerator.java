/*
 *    Copyright 2006-2021 the original author or authors.
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
package org.mybatis.generator.codegen.mybatis3.xmlmapper.elements;

import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;
import org.mybatis.generator.custom.pojo.SelectByColumnGeneratorConfiguration;
import org.mybatis.generator.internal.util.StringUtility;

public class DeleteByColumnElementGenerator extends AbstractXmlElementGenerator {

    private final SelectByColumnGeneratorConfiguration configuration;

    public DeleteByColumnElementGenerator(SelectByColumnGeneratorConfiguration configuration) {
        super();
        this.configuration = configuration;
    }

    @Override
    public void addElements(XmlElement parentElement) {
        XmlElement answer = new XmlElement("delete");
        answer.addAttribute(new Attribute("id", configuration.getDeleteMethodName()));
        answer.addAttribute(new Attribute("parameterType", configuration.getColumn().getFullyQualifiedJavaType().getFullyQualifiedName()));
        context.getCommentGenerator().addComment(answer);
        StringBuilder sb = new StringBuilder("delete ");
        if (StringUtility.stringHasValue(introspectedTable.getTableConfiguration().getAlias())) {
            sb.append(introspectedTable.getTableConfiguration().getAlias());
        }
        sb.append(" ").append(introspectedTable.getFullyQualifiedTableNameAtRuntime());
        answer.addElement(new TextElement(sb.toString()));
        //where
        sb.setLength(0);
        sb.append("where ");
        sb.append(MyBatis3FormattingUtilities.getAliasedEscapedColumnName(configuration.getColumn()));
        if ("list".equals(configuration.getParameterType())) {
            sb.append(" in ");
            answer.addElement(new TextElement(sb.toString()));
            XmlElement foreachListField = new XmlElement("foreach");
            foreachListField.addAttribute(new Attribute("collection", "list"));
            foreachListField.addAttribute(new Attribute("item", "item"));
            foreachListField.addAttribute(new Attribute("index", "index"));
            foreachListField.addAttribute(new Attribute("separator", ","));
            foreachListField.addAttribute(new Attribute("open", "("));
            foreachListField.addAttribute(new Attribute("close", ")"));
            answer.addElement(foreachListField);
            foreachListField.addElement(new TextElement("#{item}"));
        }else{
            sb.append(" = ");
            sb.append(MyBatis3FormattingUtilities.getParameterClause(configuration.getColumn()));
            answer.addElement(new TextElement(sb.toString()));
        }
        parentElement.addElement(answer);
    }
}
