/*
 *    Copyright 2006-2020 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.generator.config;

import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.internal.util.StringUtility;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class AbstractModelGeneratorConfiguration extends AbstractGeneratorConfiguration {

    protected VOGeneratorConfiguration voGeneratorConfiguration;

    protected List<String> excludeColumns = new ArrayList<>();

    private final List<OverridePropertyValueGeneratorConfiguration> overridePropertyConfigurations = new ArrayList<>();

    private List<VoAdditionalPropertyGeneratorConfiguration> additionalPropertyConfigurations = new ArrayList<>();

    private final List<VoNameFragmentGeneratorConfiguration> voNameFragmentGeneratorConfigurations = new ArrayList<>();

    private final List<VoColumnRenderFunGeneratorConfiguration> voColumnRenderFunGeneratorConfigurations = new ArrayList<>();

    private List<String> equalsAndHashCodeColumns = new ArrayList<>();

    protected FullyQualifiedJavaType fullyQualifiedJavaType;

    public AbstractModelGeneratorConfiguration() {
        super();
    }

    public AbstractModelGeneratorConfiguration(Context context) {
        super();
        targetProject = context.getJavaModelGeneratorConfiguration().getTargetProject();
        baseTargetPackage = StringUtility.substringBeforeLast(context.getJavaModelGeneratorConfiguration().getTargetPackage(), ".")+".pojo";
    }

    public List<String> getExcludeColumns() {
        return excludeColumns;
    }

    public void setExcludeColumns(List<String> excludeColumns) {
        this.excludeColumns = excludeColumns;
    }

    public FullyQualifiedJavaType getFullyQualifiedJavaType() {
        return fullyQualifiedJavaType;
    }

    public List<OverridePropertyValueGeneratorConfiguration> getOverridePropertyConfigurations() {
        return overridePropertyConfigurations;
    }

    public void addOverrideColumnConfigurations(OverridePropertyValueGeneratorConfiguration overridePropertyConfiguration) {
        this.overridePropertyConfigurations.add(overridePropertyConfiguration );
    }

    public VOGeneratorConfiguration getVoGeneratorConfiguration() {
        return voGeneratorConfiguration;
    }

    public void setVoGeneratorConfiguration(VOGeneratorConfiguration voGeneratorConfiguration) {
        this.voGeneratorConfiguration = voGeneratorConfiguration;
    }

    public List<VoAdditionalPropertyGeneratorConfiguration> getAdditionalPropertyConfigurations() {
        return additionalPropertyConfigurations;
    }

    public void setAdditionalPropertyConfigurations(List<VoAdditionalPropertyGeneratorConfiguration> additionalPropertyConfigurations) {
        this.additionalPropertyConfigurations = additionalPropertyConfigurations;
    }

    public void addAdditionalPropertyConfigurations(VoAdditionalPropertyGeneratorConfiguration additionalPropertyConfiguration) {
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
