package org.mybatis.generator.runtime.kotlin;

import java.util.List;

import org.mybatis.generator.api.ProgressCallback;
import org.mybatis.generator.codegen.AbstractKotlinGenerator;
import org.mybatis.generator.codegen.mybatis3.IntrospectedTableMyBatis3Impl;

public class IntrospectedTableKotlinImpl extends IntrospectedTableMyBatis3Impl {

    public IntrospectedTableKotlinImpl() {
        super();
        targetRuntime = TargetRuntime.MYBATIS3_DSQL;
    }

    @Override
    public void calculateCustom(List<String> warnings, ProgressCallback callback) {

    }

    @Override
    public void calculateGenerators(List<String> warnings, ProgressCallback progressCallback) {
        calculateKotlinDataClassGenerator(warnings, progressCallback);
        if (contextHasClientConfiguration() && rules.generateJavaClient()) {
            calculateKotlinMapperAndExtensionsGenerator(warnings, progressCallback);
        }
    }

    private boolean contextHasClientConfiguration() {
        return context.getJavaClientGeneratorConfiguration() != null;
    }

    protected void calculateKotlinMapperAndExtensionsGenerator(List<String> warnings,
            ProgressCallback progressCallback) {
        AbstractKotlinGenerator kotlinGenerator = new KotlinMapperAndExtensionsGenerator(getClientProject());
        initializeAbstractGenerator(kotlinGenerator, warnings,
                progressCallback);
        kotlinGenerators.add(kotlinGenerator);
    }

    protected void calculateKotlinDataClassGenerator(List<String> warnings,
            ProgressCallback progressCallback) {
        AbstractKotlinGenerator kotlinGenerator = new KotlinDataClassGenerator(getModelProject());
        initializeAbstractGenerator(kotlinGenerator, warnings, progressCallback);
        kotlinGenerators.add(kotlinGenerator);
    }

    @Override
    public boolean requiresXMLGenerator() {
        return false;
    }
}
