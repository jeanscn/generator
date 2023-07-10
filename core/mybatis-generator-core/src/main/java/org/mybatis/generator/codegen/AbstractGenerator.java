package org.mybatis.generator.codegen;

import java.util.List;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.ProgressCallback;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.internal.util.StringUtility;

public abstract class AbstractGenerator {
    protected Context context;
    protected IntrospectedTable introspectedTable;
    protected List<String> warnings;
    protected ProgressCallback progressCallback;

    protected AbstractGenerator() {
    }

    protected AbstractGenerator(Context context, IntrospectedTable introspectedTable, List<String> warnings, ProgressCallback progressCallback) {
        this.context = context;
        this.introspectedTable = introspectedTable;
        this.warnings = warnings;
        this.progressCallback = progressCallback;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public IntrospectedTable getIntrospectedTable() {
        return introspectedTable;
    }

    public void setIntrospectedTable(IntrospectedTable introspectedTable) {
        this.introspectedTable = introspectedTable;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }

    public ProgressCallback getProgressCallback() {
        return progressCallback;
    }

    public void setProgressCallback(ProgressCallback progressCallback) {
        this.progressCallback = progressCallback;
    }
}
