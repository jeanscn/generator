package org.mybatis.generator.config;

import org.mybatis.generator.internal.util.StringUtility;

import java.util.List;

public class JavaServiceImplGeneratorConfiguration extends JavaServiceGeneratorConfiguration {

    private boolean noServiceAnnotation;

    private boolean generateUnitTest;

    public JavaServiceImplGeneratorConfiguration(Context context) {
        super(context);
        noServiceAnnotation = false;
        generateUnitTest = true;
        String modelTargetPackage= context.getJavaModelGeneratorConfiguration().getTargetPackage();
        baseTargetPackage = StringUtility.substringBeforeLast(modelTargetPackage, ".");
        targetPackage = String.join(".",baseTargetPackage,"service.impl");
        targetPackageGen = String.join(".",baseTargetPackage,"codegen.service.impl");
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
