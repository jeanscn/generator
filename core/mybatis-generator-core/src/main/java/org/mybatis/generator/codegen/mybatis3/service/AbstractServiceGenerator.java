package org.mybatis.generator.codegen.mybatis3.service;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.codegen.AbstractJavaGenerator;

import java.util.*;

public abstract class AbstractServiceGenerator extends AbstractJavaGenerator {

    protected FullyQualifiedJavaType entityType;

    protected FullyQualifiedJavaType exampleType;

    public AbstractServiceGenerator(String project) {
        super(project);
    }

    @Override
    public abstract List<CompilationUnit> getCompilationUnits();

    @Override
    public void setIntrospectedTable(IntrospectedTable introspectedTable) {
        super.setIntrospectedTable(introspectedTable);
        entityType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        exampleType = new FullyQualifiedJavaType(introspectedTable.getExampleType());
    }

    protected String getInterfaceClassShortName(String targetPackage, String entityTypeShortName) {
        return targetPackage +
                "." + "I" + entityTypeShortName;
    }

    protected String getGenInterfaceClassShortName(String targetPackage, String entityTypeShortName) {
        return targetPackage +
                "." + "IGen" + entityTypeShortName;
    }
}
