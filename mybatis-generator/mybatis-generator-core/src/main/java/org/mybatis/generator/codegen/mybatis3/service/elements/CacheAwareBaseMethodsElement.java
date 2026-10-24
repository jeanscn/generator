package org.mybatis.generator.codegen.mybatis3.service.elements;

import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.service.AbstractServiceElementGenerator;
import org.mybatis.generator.codegen.mybatis3.service.CacheConfigurationResolver;

public class CacheAwareBaseMethodsElement extends AbstractServiceElementGenerator {

    @Override
    public void addElements(TopLevelClass parentElement) {
        addInsertBySql(parentElement);
        addInsertOrUpdateSelective(parentElement);
        addUpdateByExampleSelectiveAndSelect(parentElement);
    }

    private void addInsertBySql(TopLevelClass parentElement) {
        Method method = new Method("insertBySql");
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(serviceResult(FullyQualifiedJavaType.getIntegerInstance()));
        method.addParameter(new Parameter(new FullyQualifiedJavaType("com.vgosoft.mybatis.sqlbuilder.InsertSqlBuilder"), "insertSqlBuilder"));
        method.addAnnotation("@Override");
        addMutationCacheEviction(method, parentElement,
                CacheConfigurationResolver.sqlMutationCondition(tc));
        method.addBodyLine("return super.insertBySql(insertSqlBuilder);");
        parentElement.addMethod(method);
        parentElement.addImportedType(new FullyQualifiedJavaType("com.vgosoft.core.adapter.ServiceResult"));
        parentElement.addImportedType(method.getParameters().get(0).getType());
    }

    private void addInsertOrUpdateSelective(TopLevelClass parentElement) {
        Method method = new Method("insertOrUpdateSelective");
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(serviceResult(entityType));
        method.addParameter(new Parameter(entityType, "record"));
        method.addAnnotation("@Override");
        addMutationCacheEviction(method, parentElement);
        method.addBodyLine("return super.insertOrUpdateSelective(record);");
        parentElement.addMethod(method);
        parentElement.addImportedType(new FullyQualifiedJavaType("com.vgosoft.core.adapter.ServiceResult"));
        parentElement.addImportedType(entityType);
    }

    private void addUpdateByExampleSelectiveAndSelect(TopLevelClass parentElement) {
        FullyQualifiedJavaType entityList = FullyQualifiedJavaType.getNewListInstance();
        entityList.addTypeArgument(entityType);
        Method method = new Method("updateByExampleSelectiveAndSelect");
        method.setVisibility(JavaVisibility.PUBLIC);
        method.setReturnType(serviceResult(entityList));
        method.addParameter(new Parameter(entityType, "record"));
        method.addParameter(new Parameter(exampleType, "example"));
        method.addAnnotation("@Override");
        addMutationCacheEviction(method, parentElement);
        method.addBodyLine("return super.updateByExampleSelectiveAndSelect(record, example);");
        parentElement.addMethod(method);
        parentElement.addImportedType(new FullyQualifiedJavaType("com.vgosoft.core.adapter.ServiceResult"));
        parentElement.addImportedType(new FullyQualifiedJavaType("java.util.List"));
        parentElement.addImportedType(entityList);
        parentElement.addImportedType(entityType);
        parentElement.addImportedType(exampleType);
    }

    private FullyQualifiedJavaType serviceResult(FullyQualifiedJavaType resultType) {
        FullyQualifiedJavaType serviceResult = new FullyQualifiedJavaType("com.vgosoft.core.adapter.ServiceResult");
        serviceResult.addTypeArgument(resultType);
        return serviceResult;
    }

}
