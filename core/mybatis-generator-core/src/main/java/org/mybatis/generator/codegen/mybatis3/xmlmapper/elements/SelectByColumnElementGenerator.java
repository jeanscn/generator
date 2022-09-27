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
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;
import org.mybatis.generator.custom.pojo.SelectByColumnGeneratorConfiguration;
import org.mybatis.generator.internal.util.StringUtility;

import java.util.stream.Collectors;

public class SelectByColumnElementGenerator extends
        AbstractXmlElementGenerator {

    public SelectByColumnElementGenerator() {
        super();
    }

    @Override
    public void addElements(XmlElement parentElement) {
        if (introspectedTable.getTableConfiguration().getSelectByColumnGeneratorConfigurations().size() == 0) {
            return;
        }
        for (SelectByColumnGeneratorConfiguration selectByColumnGeneratorConfiguration : introspectedTable.getTableConfiguration().getSelectByColumnGeneratorConfigurations()) {
            String columnName = selectByColumnGeneratorConfiguration.getColumnName();
            long count = introspectedTable.getAllColumns().stream().filter(c -> c.getActualColumnName().equalsIgnoreCase(columnName)).count();
            if (count==0) {
                continue;
            }
            XmlElement answer = new XmlElement("select");
            answer.addAttribute(new Attribute("id", selectByColumnGeneratorConfiguration.getMethodName()));
            if (selectByColumnGeneratorConfiguration.isReturnPrimaryKey()) {
                answer.addAttribute(new Attribute("resultType", FullyQualifiedJavaType.getStringInstance().getFullyQualifiedName()));
            }else{
                if(introspectedTable.getRules().generateResultMapWithBLOBs()){
                    answer.addAttribute(new Attribute("resultMap", introspectedTable.getResultMapWithBLOBsId()));
                }else if (introspectedTable.getRelationGeneratorConfigurations().size() > 0) {
                    answer.addAttribute(new Attribute("resultMap",introspectedTable.getRelationResultMapId()));
                }else{
                    answer.addAttribute(new Attribute("resultMap", introspectedTable.getBaseResultMapId()));
                }
            }
            answer.addAttribute(new Attribute("parameterType", selectByColumnGeneratorConfiguration.getColumn().getFullyQualifiedJavaType().getFullyQualifiedName()));
            context.getCommentGenerator().addComment(answer);

            answer.addElement(new TextElement("select "));
            if (selectByColumnGeneratorConfiguration.isReturnPrimaryKey()) {
                String collect = introspectedTable.getPrimaryKeyColumns().stream().map(IntrospectedColumn::getActualColumnName).collect(Collectors.joining(","));
                answer.addElement(new TextElement(collect));
            }else{
                answer.addElement(getBaseColumnListElement());
                if (introspectedTable.hasBLOBColumns()) {
                    answer.addElement(new TextElement(","));
                    answer.addElement(getBlobColumnListElement());
                }
            }
            //form
            StringBuilder sb = new StringBuilder();
            sb.append("from ");
            sb.append(introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime());
            answer.addElement(new TextElement(sb.toString()));

            //where
            sb.setLength(0);
            sb.append("where ");
            sb.append(MyBatis3FormattingUtilities.getAliasedEscapedColumnName(selectByColumnGeneratorConfiguration.getColumn()));
            if ("list".equals(selectByColumnGeneratorConfiguration.getParameterType())) {
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
                sb.append(MyBatis3FormattingUtilities.getParameterClause(selectByColumnGeneratorConfiguration.getColumn()));
                answer.addElement(new TextElement(sb.toString()));
            }

            //order by
            if (StringUtility.propertyValueValid(selectByColumnGeneratorConfiguration.getOrderByClause())) {
                answer.addElement(new TextElement("order by "+ selectByColumnGeneratorConfiguration.getOrderByClause()));
            }else{
                TextElement defaultOrderBy = buildOrderByDefault();
                if (defaultOrderBy != null) {
                    answer.addElement(defaultOrderBy);
                }
            }
            parentElement.addElement(answer);
        }
    }
}
