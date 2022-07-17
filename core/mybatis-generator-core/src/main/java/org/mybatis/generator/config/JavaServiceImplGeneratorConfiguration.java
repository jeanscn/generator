package org.mybatis.generator.config;

import java.util.List;

public class JavaServiceImplGeneratorConfiguration extends JavaServiceGeneratorConfiguration {

    private boolean noServiceAnnotation;

    private boolean generateUnitTest;

    public JavaServiceImplGeneratorConfiguration(Context context) {
        super(context);
        noServiceAnnotation = false;
        generateUnitTest = true;
        baseTargetPackage = targetPackage;
        targetPackage = String.join(".",getTargetPackage(),"impl");
    }

    public boolean isNoServiceAnnotation() {
        return noServiceAnnotation;
    }

    public void setNoServiceAnnotation(boolean noServiceAnnotation) {
        this.noServiceAnnotation = noServiceAnnotation;
    }

    public boolean isGenerateUnitTest() {
        return generateUnitTest;
    }

    public void setGenerateUnitTest(boolean generateUnitTest) {
        this.generateUnitTest = generateUnitTest;
    }

    @Override
    public void validate(List<String> errors, String contextId) {
        super.validate(errors, contextId, "ServiceImplGenerator");
    }
}
