package org.mybatis.generator.config;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
public class VOGeneratorConfiguration extends AbstractModelGeneratorConfiguration {

    private VOModelGeneratorConfiguration voModelConfiguration;

    private VOCreateGeneratorConfiguration voCreateConfiguration;

    private VOUpdateGeneratorConfiguration voUpdateConfiguration;

    private VOViewGeneratorConfiguration voViewConfiguration;

    private VOExcelGeneratorConfiguration voExcelConfiguration;

    private VORequestGeneratorConfiguration voRequestConfiguration;

    private VOCacheGeneratorConfiguration voCacheConfiguration;

    private final List<MapstructMappingConfiguration> mappingConfigurations = new ArrayList<>();

    private Set<String> validateIgnoreColumns = new HashSet<>();

    public VOGeneratorConfiguration(Context context,TableConfiguration tc) {
        super(context);
        targetPackage = String.join(".", baseTargetPackage,"vo");
    }

    @Override
    public void validate(List<String> errors, String contextId) {
        super.validate(errors, contextId, "VOGeneratorConfiguration");
    }

    public void addMappingConfigurations(MapstructMappingConfiguration mappingConfiguration) {
        this.mappingConfigurations.add(mappingConfiguration);
    }

}
