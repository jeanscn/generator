package org.mybatis.generator.config;

import com.vgosoft.tool.core.VCollectionUtil;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.internal.util.StringUtility;

import java.util.*;
import java.util.stream.Collectors;

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

    public Set<String> getExcludeColumns() {
        return excludeColumns;
    }

    public void setExcludeColumns(Set<String> excludeColumns) {
        this.excludeColumns = excludeColumns;
    }

    public FullyQualifiedJavaType getFullyQualifiedJavaType() {
        return fullyQualifiedJavaType;
    }

    public List<OverridePropertyValueGeneratorConfiguration> getOverridePropertyConfigurations() {
        return overridePropertyConfigurations;
    }

    public void addOverrideColumnConfigurations(OverridePropertyValueGeneratorConfiguration overridePropertyConfiguration) {
        overridePropertyConfigurations.stream()
                .filter(item -> item.getSourceColumnName().equals(overridePropertyConfiguration.getSourceColumnName())).findFirst()
                .ifPresent(item -> overridePropertyConfigurations.remove(item));
        this.overridePropertyConfigurations.add(overridePropertyConfiguration );
    }

    public void setOverridePropertyConfigurations(List<OverridePropertyValueGeneratorConfiguration> overridePropertyConfigurations) {
        this.overridePropertyConfigurations = overridePropertyConfigurations;
    }

    public List<VoAdditionalPropertyGeneratorConfiguration> getAdditionalPropertyConfigurations() {
        return additionalPropertyConfigurations;
    }

    public void setAdditionalPropertyConfigurations(List<VoAdditionalPropertyGeneratorConfiguration> additionalPropertyConfigurations) {
        this.additionalPropertyConfigurations = additionalPropertyConfigurations;
    }

    public void addAdditionalPropertyConfigurations(VoAdditionalPropertyGeneratorConfiguration additionalPropertyConfiguration) {
        VCollectionUtil.addIfNotContains(additionalPropertyConfigurations, additionalPropertyConfiguration);
    }

    public List<VoNameFragmentGeneratorConfiguration> getVoNameFragmentGeneratorConfigurations() {
        return voNameFragmentGeneratorConfigurations;
    }

    public void addVoNameFragmentGeneratorConfiguration(VoNameFragmentGeneratorConfiguration voNameFragmentGeneratorConfiguration) {
        this.voNameFragmentGeneratorConfigurations.add(voNameFragmentGeneratorConfiguration);
    }

    public List<String> getEqualsAndHashCodeColumns() {
        return equalsAndHashCodeColumns;
    }

    public void setEqualsAndHashCodeColumns(List<String> equalsAndHashCodeColumns) {
        this.equalsAndHashCodeColumns = equalsAndHashCodeColumns;
    }

    @Override
    abstract void validate(List<String> errors, String contextId);

    public void addVoColumnRenderFunGeneratorConfiguration(VoColumnRenderFunGeneratorConfiguration voColumnRenderFunGeneratorConfiguration) {
        this.voColumnRenderFunGeneratorConfigurations.add(voColumnRenderFunGeneratorConfiguration);
    }

    public List<VoColumnRenderFunGeneratorConfiguration> getVoColumnRenderFunGeneratorConfigurations() {
        return voColumnRenderFunGeneratorConfigurations;
    }
}
