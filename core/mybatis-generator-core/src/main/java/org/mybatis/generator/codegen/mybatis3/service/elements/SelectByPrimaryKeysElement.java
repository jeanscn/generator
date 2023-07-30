package org.mybatis.generator.codegen.mybatis3.service.elements;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.service.AbstractServiceElementGenerator;

import java.util.List;
import java.util.stream.Collectors;

import static org.mybatis.generator.custom.ConstantsUtil.*;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-04-26 14:38
 * @version 3.0
 */
public class SelectByPrimaryKeysElement extends AbstractServiceElementGenerator {


    public SelectByPrimaryKeysElement() {
        super();
    }

    @Override
    public void addElements(TopLevelClass parentElement) {
        Method method = serviceMethods.getSelectByMultiStringIdsMethod(parentElement, false);
        parentElement.addImportedType(new FullyQualifiedJavaType(ANNOTATION_NULLABLE));
        method.addAnnotation("@Override");
        //方法体
        method.addBodyLine("if (!VStringUtil.stringHasValue(ids)) {");
        method.addBodyLine("return this.selectByExample(new {0}Example()).getResult();",entityType.getShortName());
        method.addBodyLine("} else {");
        method.addBodyLine("return Arrays.stream(ids.split(\",\"))\n" +
                "                    .map(this::selectByPrimaryKey)\n" +
                "                    .filter(ServiceResult::hasResult)\n" +
                "                    .map(ServiceResult::getResult)\n" +
                "                    .collect(Collectors.toList());");
        method.addBodyLine("}");
        parentElement.addImportedType(FullyQualifiedJavaType.getNewListInstance());
        parentElement.addMethod(method);
        parentElement.addImportedType(SERVICE_RESULT);
        parentElement.addImportedType(V_STRING_UTIL);
        parentElement.addImportedType(new FullyQualifiedJavaType("java.util.Arrays"));
        parentElement.addImportedType(new FullyQualifiedJavaType("java.util.stream.Collectors"));
    }
}
