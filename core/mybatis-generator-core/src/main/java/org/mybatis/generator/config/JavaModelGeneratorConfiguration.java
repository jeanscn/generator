package org.mybatis.generator.config;

import org.mybatis.generator.internal.util.StringUtility;

import java.util.ArrayList;
import java.util.List;

public class JavaModelGeneratorConfiguration extends AbstractModelGeneratorConfiguration {

    private boolean noMetaAnnotation;

    private boolean enableChildren;

    private boolean ignoreTenant = false;

    public JavaModelGeneratorConfiguration() {
        super();
    }

    public JavaModelGeneratorConfiguration(Context context) {
        super();
        noMetaAnnotation = false;
    }

    public boolean isNoMetaAnnotation() {
        return noMetaAnnotation;
    }

    public void setNoMetaAnnotation(boolean noMetaAnnotation) {
        this.noMetaAnnotation = noMetaAnnotation;
    }

    public boolean isGenerateChildren() {
        return enableChildren;
    }

    public void setGenerateChildren(boolean enableChildren) {
        this.enableChildren = enableChildren;
    }

    @Override
    public void validate(List<String> errors, String contextId) {
        super.validate(errors, contextId, "JavaModelGeneratorConfiguration");
    }

    public boolean isIgnoreTenant() {
        return ignoreTenant;
    }

    public void setIgnoreTenant(boolean ignoreTenant) {
        this.ignoreTenant = ignoreTenant;
    }
}
