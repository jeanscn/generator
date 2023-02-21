package org.mybatis.generator.config;

import java.util.ArrayList;
import java.util.List;

public class VOGeneratorConfiguration extends AbstractModelGeneratorConfiguration {

    private VOModelGeneratorConfiguration voModelConfiguration;

    private VOCreateGeneratorConfiguration voCreateConfiguration;

    private VOUpdateGeneratorConfiguration voUpdateConfiguration;

    private VOViewGeneratorConfiguration voViewConfiguration;

    private VOExcelGeneratorConfiguration voExcelConfiguration;

    private VORequestGeneratorConfiguration voRequestConfiguration;

    private VOCacheGeneratorConfiguration voCacheConfiguration;

    private List<MapstructMappingConfiguration> mappingConfigurations = new ArrayList<>();

    public VOGeneratorConfiguration(Context context,TableConfiguration tc) {
        super(context);
        targetPackage = String.join(".", baseTargetPackage,"vo");
    }

    @Override
    public void validate(List<String> errors, String contextId) {
        super.validate(errors, contextId, "VOGeneratorConfiguration");
    }

    public VOModelGeneratorConfiguration getVoModelConfiguration() {
        return voModelConfiguration;
    }

    public void setVoModelConfiguration(VOModelGeneratorConfiguration voModelConfiguration) {
        this.voModelConfiguration = voModelConfiguration;
    }

    public VOCreateGeneratorConfiguration getVoCreateConfiguration() {
        return voCreateConfiguration;
    }

    public void setVoCreateConfiguration(VOCreateGeneratorConfiguration voCreateConfiguration) {
        this.voCreateConfiguration = voCreateConfiguration;
    }

    public VOUpdateGeneratorConfiguration getVoUpdateConfiguration() {
        return voUpdateConfiguration;
    }

    public void setVoUpdateConfiguration(VOUpdateGeneratorConfiguration voUpdateConfiguration) {
        this.voUpdateConfiguration = voUpdateConfiguration;
    }

    public VOViewGeneratorConfiguration getVoViewConfiguration() {
        return voViewConfiguration;
    }

    public void setVoViewConfiguration(VOViewGeneratorConfiguration voViewConfiguration) {
        this.voViewConfiguration = voViewConfiguration;
    }

    public VOExcelGeneratorConfiguration getVoExcelConfiguration() {
        return voExcelConfiguration;
    }

    public void setVoExcelConfiguration(VOExcelGeneratorConfiguration voExcelConfiguration) {
        this.voExcelConfiguration = voExcelConfiguration;
    }

    public VORequestGeneratorConfiguration getVoRequestConfiguration() {
        return voRequestConfiguration;
    }

    public void setVoRequestConfiguration(VORequestGeneratorConfiguration voRequestConfiguration) {
        this.voRequestConfiguration = voRequestConfiguration;
    }

    public VOCacheGeneratorConfiguration getVoCacheConfiguration() {
        return voCacheConfiguration;
    }

    public void setVoCacheConfiguration(VOCacheGeneratorConfiguration voCacheConfiguration) {
        this.voCacheConfiguration = voCacheConfiguration;
    }

    public List<MapstructMappingConfiguration> getMappingConfigurations() {
        return mappingConfigurations;
    }

    public void addMappingConfigurations(MapstructMappingConfiguration mappingConfiguration) {
        this.mappingConfigurations.add(mappingConfiguration);
    }
}
