package org.mybatis.generator.config;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
public class VoGeneratorConfiguration extends AbstractModelGeneratorConfiguration {

    private VoModelGeneratorConfiguration voModelConfiguration;

    private VoCreateGeneratorConfiguration voCreateConfiguration;

    private VoUpdateGeneratorConfiguration voUpdateConfiguration;

    private VoViewGeneratorConfiguration voViewConfiguration;

    private VoExcelGeneratorConfiguration voExcelConfiguration;

    private VoRequestGeneratorConfiguration voRequestConfiguration;

    private VoCacheGeneratorConfiguration voCacheConfiguration;

    private final List<MapstructMappingConfiguration> mappingConfigurations = new ArrayList<>();

    private Set<String> validateIgnoreColumns = new HashSet<>();

    public VoGeneratorConfiguration(Context context, TableConfiguration tc) {
        super(context);
        targetPackage = String.join(".", baseTargetPackage,"vo");
    }

    @Override
    public void validate(List<String> errors, String contextId) {
        super.validate(errors, contextId, "VoGeneratorConfiguration");
    }

    public void addMappingConfigurations(MapstructMappingConfiguration mappingConfiguration) {
        this.mappingConfigurations.add(mappingConfiguration);
    }

}
