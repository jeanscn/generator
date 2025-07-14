package org.mybatis.generator.config;

import lombok.Getter;
import lombok.Setter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;

import java.util.*;

public class VoUpdateGeneratorConfiguration extends AbstractModelGeneratorConfiguration {

    @Setter
    @Getter
    private List<String> includeColumns = new ArrayList<>();

    @Setter
    @Getter
    private List<String> requiredColumns = new ArrayList<>();

    @Setter
    @Getter
    private Set<String> validateIgnoreColumns = new HashSet<>();

    private boolean isEnableSelective = true;

    public VoUpdateGeneratorConfiguration(Context context, TableConfiguration tc) {
        super(context);
        this.generate = true;
        targetPackage = String.join(".", baseTargetPackage,"vo");
        fullyQualifiedJavaType = new FullyQualifiedJavaType(String.join(".",targetPackage,tc.getDomainObjectName()+"UpdateVo"));
    }

    public boolean isEnableSelective() {
        return isEnableSelective;
    }

    public void setEnableSelective(boolean enableSelective) {
        isEnableSelective = enableSelective;
    }

    @Override
    public void validate(List<String> errors, String contextId) {
        super.validate(errors, contextId, "VoUpdateGeneratorConfiguration");
    }
}
