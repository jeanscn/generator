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

import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;
import org.mybatis.generator.custom.pojo.CustomMethodProperty;

import java.util.Map;

public class SelectTreeByParentIdElementGenerator extends AbstractXmlElementGenerator {

    public SelectTreeByParentIdElementGenerator() {
        super();
    }

    @Override
    public void addElements(XmlElement parentElement) {
        final String selectTreeByParentIdStatementId = introspectedTable.getSelectTreeByParentIdStatementId();
        Map<String, CustomMethodProperty> customMethodName = introspectedTable.getCustomAddtionalSelectMethods();
        if (customMethodName.isEmpty() || !customMethodName.containsKey(selectTreeByParentIdStatementId)) {
            return;
        }
        for (Map.Entry<String, CustomMethodProperty> methodPropertiesEntry : customMethodName.entrySet()) {
            CustomMethodProperty methodProperties = methodPropertiesEntry.getValue();
            XmlElement answer = new XmlElement("select");
            answer.addAttribute(new Attribute("id", methodProperties.getMethodName()));
            if (introspectedTable.getRules().generateResultMapWithBLOBs()) {
                answer.addAttribute(new Attribute("resultMap", introspectedTable.getResultMapWithBLOBsId()));
            } else {
                if (introspectedTable.getRelationProperties().size() > 0) {
                    answer.addAttribute(new Attribute("resultMap", introspectedTable.getRelationResultMapId()));
                } else {
                    answer.addAttribute(new Attribute("resultMap", introspectedTable.getBaseResultMapId()));
                }
            }
            answer.addAttribute(new Attribute("parameterType", methodProperties.getParentIdColumn().getFullyQualifiedJavaType().getFullyQualifiedName()));
            context.getCommentGenerator().addComment(answer);
            answer.addElement(new TextElement("select "));
            answer.addElement(getBaseColumnListElement());
            if (introspectedTable.hasBLOBColumns()) {
                answer.addElement(new TextElement(","));
                answer.addElement(getBlobColumnListElement());
            }
            //form
            StringBuilder sb = new StringBuilder();
            sb.append("from ");
            sb.append(introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime());
            answer.addElement(new TextElement(sb.toString()));
            //条件
            sb.setLength(0);
            sb.append("where ");
            if (methodPropertiesEntry.getKey().equals(introspectedTable.getSelectTreeByParentIdStatementId())) {
                sb.append("FIND_IN_SET(");
                sb.append(MyBatis3FormattingUtilities.getAliasedEscapedColumnName(methodProperties.getPrimaryKeyColumn()));
                sb.append(",");
                sb.append(methodProperties.getSqlMethod());
                sb.append("(");
                sb.append(MyBatis3FormattingUtilities.getParameterClause(methodProperties.getParentIdColumn()));
                sb.append("))");
                answer.addElement(new TextElement(sb.toString()));
                parentElement.addElement(answer);
            }
        }
    }
}
