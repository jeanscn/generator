/**
 * Copyright 2006-2019 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mybatis.generator.codegen.mybatis3.xmlmapper.elements;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.custom.SelectByTableProperties;

public class SelectByTableElementGenerator extends
        AbstractXmlElementGenerator {

    public SelectByTableElementGenerator() {
        super();
    }

    @Override
    public void addElements(XmlElement parentElement) {
        if (introspectedTable.getSelectByTableProperties().size() == 0) {
            return;
        }
        for (SelectByTableProperties selectByTableProperty : introspectedTable.getSelectByTableProperties()) {
            XmlElement answer = new XmlElement("select");
            answer.addAttribute(new Attribute("id", selectByTableProperty.getMethodName()));
            answer.addAttribute(new Attribute("resultMap", introspectedTable.getBaseResultMapId()));
            answer.addAttribute(new Attribute("parameterType", "java.lang.String"));
            context.getCommentGenerator().addComment(answer);
            answer.addElement(new TextElement("select "));
            answer.addElement(getBaseColumnListElement());
            //form
            StringBuilder sb = new StringBuilder();
            sb.append("from ");
            sb.append(introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime());
            answer.addElement(new TextElement(sb.toString()));
            sb.setLength(0);
            sb.append("left join ");
            sb.append(selectByTableProperty.getTableName()).append(" RT on RT.");
            sb.append(selectByTableProperty.getPrimaryKeyColumn());
            sb.append(" = ");
            IntrospectedColumn introspectedColumn = introspectedTable.getPrimaryKeyColumns().get(0);
            sb.append(introspectedColumn.getTableAlias()).append(".");
            sb.append(introspectedColumn.getActualColumnName());
            answer.addElement(new TextElement(sb.toString()));
            sb.setLength(0);
            sb.append("where ").append("RT.").append(selectByTableProperty.getOtherPrimaryKeyColumn());
            sb.append(" = #{");
            sb.append(selectByTableProperty.getParameterName());
            sb.append(",jdbcType=VARCHAR} ");
            answer.addElement(new TextElement(sb.toString()));
            parentElement.addElement(answer);
        }
    }
}
