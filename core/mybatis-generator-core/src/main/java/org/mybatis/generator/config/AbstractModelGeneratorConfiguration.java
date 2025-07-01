package org.mybatis.generator.config;

import com.vgosoft.tool.core.VCollectionUtil;
import lombok.Getter;
import lombok.Setter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.internal.util.StringUtility;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
public abstract class AbstractModelGeneratorConfiguration extends AbstractGeneratorConfiguration {

    protected Set<String> excludeColumns = new HashSet<>();

    protected List<OverridePropertyValueGeneratorConfiguration> overridePropertyConfigurations = new ArrayList<>();

    protected List<VoAdditionalPropertyGeneratorConfiguration> additionalPropertyConfigurations = new ArrayList<>();

    protected final List<VoNameFragmentGeneratorConfiguration> voNameFragmentGeneratorConfigurations = new ArrayList<>();

    protected final List<VoColumnRenderFunGeneratorConfiguration> voColumnRenderFunGeneratorConfigurations = new ArrayList<>();

    protected List<String> equalsAndHashCodeColumns = new ArrayList<>();

    protected FullyQualifiedJavaType fullyQualifiedJavaType;

    public AbstractModelGeneratorConfiguration() {
        super();
    }

    public AbstractModelGeneratorConfiguration(Context context) {
        super();
        targetProject = context.getJavaModelGeneratorConfiguration().getTargetProject();
        baseTargetPackage = StringUtility.substringBeforeLast(context.getJavaModelGeneratorConfiguration().getTargetPackage(), ".")+".pojo";
    }

    public void addOverrideColumnConfigurations(OverridePropertyValueGeneratorConfiguration overridePropertyConfiguration) {
        overridePropertyConfigurations.stream()
                .filter(item -> item.getSourceColumnName().equals(overridePropertyConfiguration.getSourceColumnName())).findFirst()
                .ifPresent(item -> overridePropertyConfigurations.remove(item));
        this.overridePropertyConfigurations.add(overridePropertyConfiguration );
    }

    public void addAdditionalPropertyConfigurations(VoAdditionalPropertyGeneratorConfiguration additionalPropertyConfiguration) {
        VCollectionUtil.addIfNotContains(additionalPropertyConfigurations, additionalPropertyConfiguration);
    }

    public void addVoNameFragmentGeneratorConfiguration(VoNameFragmentGeneratorConfiguration voNameFragmentGeneratorConfiguration) {
        this.voNameFragmentGeneratorConfigurations.add(voNameFragmentGeneratorConfiguration);
    }

    @Override
    abstract void validate(List<String> errors, String contextId);

    public void addVoColumnRenderFunGeneratorConfiguration(VoColumnRenderFunGeneratorConfiguration voColumnRenderFunGeneratorConfiguration) {
        this.voColumnRenderFunGeneratorConfigurations.add(voColumnRenderFunGeneratorConfiguration);
    }

}
