package org.mybatis.generator.codegen.mybatis3.javamapper.elements.sqlprovider;

import java.util.Set;
import java.util.TreeSet;

import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.AbstractGenerator;

public abstract class AbstractJavaProviderMethodGenerator extends AbstractGenerator {

    protected static final FullyQualifiedJavaType BUILDER_IMPORT =
            new FullyQualifiedJavaType("org.apache.ibatis.jdbc.SQL"); //$NON-NLS-1$

    protected AbstractJavaProviderMethodGenerator() {
        super();
    }

    protected Set<FullyQualifiedJavaType> initializeImportedTypes() {
        Set<FullyQualifiedJavaType> importedTypes = new TreeSet<>();

        importedTypes.add(BUILDER_IMPORT);

        return importedTypes;
    }

    protected Set<FullyQualifiedJavaType> initializeImportedTypes(String extraType) {
        return initializeImportedTypes(new FullyQualifiedJavaType(extraType));
    }

    protected Set<FullyQualifiedJavaType> initializeImportedTypes(FullyQualifiedJavaType extraType) {
        Set<FullyQualifiedJavaType> importedTypes = initializeImportedTypes();

        importedTypes.add(extraType);

        return importedTypes;
    }

    public abstract void addClassElements(TopLevelClass topLevelClass);
}
