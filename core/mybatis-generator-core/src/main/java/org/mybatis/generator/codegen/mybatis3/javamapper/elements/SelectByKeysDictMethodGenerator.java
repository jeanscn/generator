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
package org.mybatis.generator.codegen.mybatis3.javamapper.elements;

import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.config.VOCacheGeneratorConfiguration;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SelectByKeysDictMethodGenerator extends AbstractJavaMapperMethodGenerator {

    public SelectByKeysDictMethodGenerator() {
        super();
    }

    @Override
    public void addInterfaceElements(Interface interfaze) {
        VOCacheGeneratorConfiguration config = introspectedTable.getTableConfiguration().getVoCacheGeneratorConfiguration();
        Method method = new Method(introspectedTable.getSelectByKeysDictStatementId());
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setAbstract(true);

        FullyQualifiedJavaType returnType = FullyQualifiedJavaType.getNewListInstance();
        returnType.addTypeArgument(new FullyQualifiedJavaType(introspectedTable.getBaseRecordType()));
        method.setReturnType(returnType);

        Set<FullyQualifiedJavaType> importedTypes = new TreeSet<>();
        importedTypes.add(returnType);

        List<IntrospectedColumn> introspectedColumns = Stream.of(config.getTypeColumn(), config.getCodeColumn())
                .map(n -> introspectedTable.getColumn(n).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        importedTypes.add(new FullyQualifiedJavaType("org.apache.ibatis.annotations.Param"));

        StringBuilder sb = new StringBuilder();
        for (IntrospectedColumn introspectedColumn : introspectedColumns) {
            FullyQualifiedJavaType type = introspectedColumn.getFullyQualifiedJavaType();
            importedTypes.add(type);
            Parameter parameter = new Parameter(type, introspectedColumn.getJavaProperty());
            sb.setLength(0);
            sb.append("@Param(\""); //$NON-NLS-1$
            sb.append(introspectedColumn.getJavaProperty());
            sb.append("\")"); //$NON-NLS-1$
            parameter.addAnnotation(sb.toString());
            method.addParameter(parameter);
        }

        addMapperAnnotations(interfaze, method);

        List<String> collect = introspectedColumns.stream()
                .map(c -> "@param " + c.getJavaProperty() + " " + c.getRemarks(false))
                .collect(Collectors.toList());
        collect.add(0,  "基于表的字典查询方法的实现。");
        collect.add(0,"这个抽象方法通过定制版Mybatis Generator自动生成");
        collect.add(0,"提示 - @mbg.generated");
        context.getCommentGenerator().addMethodJavaDocLine(method, false, collect.toArray(new String[0]));

        if (context.getPlugins().clientSelectByKeysDicMethodGenerated(method, interfaze, introspectedTable)) {
            addExtraImports(interfaze);
            interfaze.addImportedTypes(importedTypes);
            interfaze.addMethod(method);
        }
    }

    public void addMapperAnnotations(Interface interfaze, Method method) {
        // extension point for subclasses
    }

    public void addExtraImports(Interface interfaze) {
        // extension point for subclasses
    }
}
