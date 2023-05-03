package org.mybatis.generator.config;

import org.mybatis.generator.internal.util.StringUtility;

import java.util.List;

public class JavaServiceGeneratorConfiguration extends AbstractGeneratorConfiguration {

    public JavaServiceGeneratorConfiguration(Context context) {
        super();
        targetProject = context.getJavaModelGeneratorConfiguration().getTargetProject();
        String modelTargetPackage= context.getJavaModelGeneratorConfiguration().getTargetPackage();
        baseTargetPackage = StringUtility.substringBeforeLast(modelTargetPackage, ".");
        targetPackage = String.join(".", baseTargetPackage, "service");
        targetPackageGen = String.join(".", baseTargetPackage, "codegen.service");
    }

    @Override
    public void validate(List<String> errors, String contextId) {
        super.validate(errors, contextId, "ServiceGenerator");
    }
}
