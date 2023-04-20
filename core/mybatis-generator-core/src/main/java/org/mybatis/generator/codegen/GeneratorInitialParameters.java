package org.mybatis.generator.codegen;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.ProgressCallback;
import org.mybatis.generator.config.Context;

import java.util.List;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-04-13 12:56
 * @version 3.0
 */
public class GeneratorInitialParameters {
    private Context context;
    private IntrospectedTable introspectedTable;
    private List<String> warnings;
    private ProgressCallback progressCallback;

    public GeneratorInitialParameters(Context context, IntrospectedTable introspectedTable, List<String> warnings, ProgressCallback progressCallback) {
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
