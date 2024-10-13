package org.mybatis.generator.config;

import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.internal.util.StringUtility;

import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractModelGeneratorConfiguration extends AbstractGeneratorConfiguration {

    protected Set<String> excludeColumns = new HashSet<>();

    protected Set<OverridePropertyValueGeneratorConfiguration> overridePropertyConfigurations = new HashSet<>();

    protected TreeSet<VoAdditionalPropertyGeneratorConfiguration> additionalPropertyConfigurations = new TreeSet<>(Comparator.comparing(VoAdditionalPropertyGeneratorConfiguration::getName));

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

    public Set<OverridePropertyValueGeneratorConfiguration> getOverridePropertyConfigurations() {
        return overridePropertyConfigurations;
    }

    public void addOverrideColumnConfigurations(OverridePropertyValueGeneratorConfiguration overridePropertyConfiguration) {
        this.overridePropertyConfigurations.add(overridePropertyConfiguration );
    }

    public void setOverridePropertyConfigurations(Set<OverridePropertyValueGeneratorConfiguration> overridePropertyConfigurations) {
        this.overridePropertyConfigurations = overridePropertyConfigurations;
    }

    public TreeSet<VoAdditionalPropertyGeneratorConfiguration> getAdditionalPropertyConfigurations() {
        //去重
        //additionalPropertyConfigurations = additionalPropertyConfigurations.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(VoAdditionalPropertyGeneratorConfiguration::getName))), ArrayList::new));
        return additionalPropertyConfigurations;
    }

    public void setAdditionalPropertyConfigurations(TreeSet<VoAdditionalPropertyGeneratorConfiguration> additionalPropertyConfigurations) {
        this.additionalPropertyConfigurations = additionalPropertyConfigurations;
    }

    public void addAdditionalPropertyConfigurations(VoAdditionalPropertyGeneratorConfiguration additionalPropertyConfiguration) {
//        Optional<VoAdditionalPropertyGeneratorConfiguration> first = additionalPropertyConfigurations.stream().filter(item -> item.getName().equals(additionalPropertyConfiguration.getName())).findFirst();
//        if (!first.isPresent()) {
//            additionalPropertyConfigurations.remove(additionalPropertyConfiguration);
//        }
        this.additionalPropertyConfigurations.add(additionalPropertyConfiguration);
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
