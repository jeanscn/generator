package org.mybatis.generator.config;

import org.mybatis.generator.internal.util.StringUtility;

import java.util.List;

public class JavaClientGeneratorConfiguration  extends AbstractGeneratorConfiguration {

    public JavaClientGeneratorConfiguration() {
        super();
    }

    public JavaClientGeneratorConfiguration(Context context) {
        super();
        targetProject = context.getJavaClientGeneratorConfiguration().getTargetProject();
        baseTargetPackage = StringUtility.substringBeforeLast(context.getJavaClientGeneratorConfiguration().getTargetPackage(), ".");
        targetPackage = String.join(".", baseTargetPackage,"dao");
        targetPackageGen = String.join(".", baseTargetPackage,"codegen.dao");
    }

    @Override
    public void validate(List<String> errors, String contextId) {
        super.validate(errors, contextId, "JavaClientGeneratorConfiguration");
    }
}
