package org.mybatis.generator.config;

import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;

import java.util.List;

public class VORequestGeneratorConfiguration extends AbstractModelGeneratorConfiguration {

    private boolean includePageParam = true;

    public VORequestGeneratorConfiguration(Context context,TableConfiguration tc) {
        super(context);
        this.generate = true;
        targetPackage = String.join(".", baseTargetPackage,"vo");
        fullyQualifiedJavaType = new FullyQualifiedJavaType(String.join(".",targetPackage,tc.getDomainObjectName()+"RequestVO"));
    }

    @Override
    public void validate(List<String> errors, String contextId) {
        super.validate(errors, contextId, "VORequestGeneratorConfiguration");
    }

    public boolean isIncludePageParam() {
        return includePageParam;
    }

    public void setIncludePageParam(boolean includePageParam) {
        this.includePageParam = includePageParam;
    }


}
