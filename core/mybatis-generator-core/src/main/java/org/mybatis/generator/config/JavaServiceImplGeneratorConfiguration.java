package org.mybatis.generator.config;

import java.util.List;

public class JavaServiceImplGeneratorConfiguration extends JavaServiceGeneratorConfiguration {

    private boolean noServiceAnnotation;

    public JavaServiceImplGeneratorConfiguration(Context context) {
        super(context);
        noServiceAnnotation = false;
        baseTargetPackage = targetPackage;
        targetPackage = String.join(".",getTargetPackage(),"impl");
    }

    public boolean isNoServiceAnnotation() {
        return noServiceAnnotation;
    }

    public void setNoServiceAnnotation(boolean noServiceAnnotation) {
        this.noServiceAnnotation = noServiceAnnotation;
    }

    @Override
    public void validate(List<String> errors, String contextId) {
        super.validate(errors, contextId, "ServiceImplGenerator");
    }
}
