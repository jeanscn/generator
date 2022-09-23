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

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;
import org.mybatis.generator.config.VOCacheGeneratorConfiguration;
import org.mybatis.generator.internal.util.JavaBeansUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

public class SelectByKeysDictElementGenerator extends AbstractXmlElementGenerator {

    public SelectByKeysDictElementGenerator() {
        super();
    }

    @Override
    public void addElements(XmlElement parentElement) {
        XmlElement answer = new XmlElement("select");
        answer.addAttribute(new Attribute("id", introspectedTable.getSelectByKeysDictStatementId()));
        answer.addAttribute(new Attribute("resultMap",introspectedTable.getBaseResultMapId()));

        answer.addAttribute(new Attribute("parameterType", "map")); //$NON-NLS-1$
        context.getCommentGenerator().addComment(answer);

        answer.addElement(new TextElement("select "));
        answer.addElement(getBaseColumnListElement());
        StringBuilder sb = new StringBuilder();
        sb.append("from ");
        sb.append(introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime());
        answer.addElement(new TextElement(sb.toString()));
        VOCacheGeneratorConfiguration config = introspectedTable.getTableConfiguration().getVoCacheGeneratorConfiguration();

        List<IntrospectedColumn> keysColumns = new ArrayList<>();
        if (config.getTypeColumn() != null && config.getCodeColumn()!=null) {
            List<IntrospectedColumn> keysColumn = Stream.of(config.getTypeColumn(), config.getCodeColumn())
                    .map(n -> introspectedTable.getColumn(n).orElse(null))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            keysColumns.addAll(keysColumn);
        }else{
            introspectedTable.getColumn(config.getCodeColumn()).ifPresent(keysColumns::add);
        }
        XmlElement where = new XmlElement("where");
        answer.addElement(where);
        for (IntrospectedColumn keysColumn : keysColumns) {
            XmlElement anIf = new XmlElement("if");
           sb.setLength(0);
           sb.append(keysColumn.getJavaProperty()).append(" != null");
            if (keysColumn.isStringColumn()) {
                sb.append(" and ").append(keysColumn.getJavaProperty()).append(" != ''");
            }
            anIf.addAttribute(new Attribute("test", sb.toString()));
            String line = " and ";
            line += MyBatis3FormattingUtilities.getEscapedColumnName(keysColumn);
            line += " = "; //$NON-NLS-1$
            line += MyBatis3FormattingUtilities.getParameterClause(keysColumn);
            anIf.addElement(new TextElement(line));
            where.addElement(anIf);
        }
        TextElement defaultOrderBy = buildOrderByDefault();
        if (defaultOrderBy != null) {
            answer.addElement(defaultOrderBy);
        }
        if (context.getPlugins().sqlMapSelectByKeysDictElementGenerated(answer, introspectedTable)) {
            parentElement.addElement(answer);
        }
    }
}
