package org.mybatis.generator.config;

import org.mybatis.generator.internal.util.StringUtility;

import java.util.List;
import java.util.ArrayList;

/**
 * @author cen_c
 */
public class JavaServiceGeneratorConfiguration extends AbstractGeneratorConfiguration {
    private final List<DisabledServiceMethodConfiguration> disabledMethods = new ArrayList<>();

    public List<DisabledServiceMethodConfiguration> getDisabledMethods() {
        return java.util.Collections.unmodifiableList(disabledMethods);
    }

    public void addDisabledMethod(DisabledServiceMethodConfiguration method) {
        if (disabledMethods.stream().anyMatch(existing -> existing.signature().equals(method.signature()))) {
            throw new IllegalArgumentException("disabledMethod signature 重复：" + method.signature());
        }
        disabledMethods.add(method);
    }

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
