package org.mybatis.generator.config;

import lombok.Getter;
import lombok.Setter;
import org.mybatis.generator.internal.util.StringUtility;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author cen_c
 */
@Setter
@Getter
public class JavaServiceImplGeneratorConfiguration extends JavaServiceGeneratorConfiguration {

    private boolean noServiceAnnotation;

    private boolean generateUnitTest;

    private Set<String> entityEvent = new HashSet<>();

    private Set<StatisticsDateDimGeneratorConfiguration> statisticsConfigurations = new HashSet<>();

    private Set<StatisticsCustomGeneratorConfiguration> statisticsCustomConfigurations = new HashSet<>();

    public JavaServiceImplGeneratorConfiguration(Context context) {
        super(context);
        noServiceAnnotation = false;
        generateUnitTest = true;
        String modelTargetPackage= context.getJavaModelGeneratorConfiguration().getTargetPackage();
        baseTargetPackage = StringUtility.substringBeforeLast(modelTargetPackage, ".");
        targetPackage = String.join(".",baseTargetPackage,"service.impl");
        targetPackageGen = String.join(".",baseTargetPackage,"codegen.service.impl");
    }

    public void addStatisticsConfiguration(StatisticsDateDimGeneratorConfiguration statisticsConfiguration) {
        if (statisticsConfiguration != null) {
            statisticsConfigurations.add(statisticsConfiguration);
        }
    }

    public void addStatisticsCustomConfiguration(StatisticsCustomGeneratorConfiguration statisticsCustomConfiguration) {
        if (statisticsCustomConfiguration != null) {
            statisticsCustomConfigurations.add(statisticsCustomConfiguration);
        }
    }

    @Override
    public void validate(List<String> errors, String contextId) {
        super.validate(errors, contextId, "ServiceImplGenerator");
    }
}
