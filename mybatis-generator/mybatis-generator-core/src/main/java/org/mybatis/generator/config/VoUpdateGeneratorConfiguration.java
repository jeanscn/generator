package org.mybatis.generator.config;

import lombok.Getter;
import lombok.Setter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;

import java.util.*;

/**
 * @author cen_c
 */
@Setter
@Getter
public class VoUpdateGeneratorConfiguration extends AbstractModelGeneratorConfiguration {

    private List<String> includeColumns = new ArrayList<>();

    private List<String> requiredColumns = new ArrayList<>();

    private Set<String> validateIgnoreColumns = new HashSet<>();

    private boolean isEnableSelective = true;

    public VoUpdateGeneratorConfiguration(Context context, TableConfiguration tc) {
        super(context);
        this.generate = true;
        targetPackage = String.join(".", baseTargetPackage,"vo");
        fullyQualifiedJavaType = new FullyQualifiedJavaType(String.join(".",targetPackage,tc.getDomainObjectName()+"UpdateVo"));
    }

    @Override
    public void validate(List<String> errors, String contextId) {
        super.validate(errors, contextId, "VoUpdateGeneratorConfiguration");
    }
}
