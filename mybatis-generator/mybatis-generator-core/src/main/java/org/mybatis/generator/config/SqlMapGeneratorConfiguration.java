package org.mybatis.generator.config;

import java.util.List;

public class SqlMapGeneratorConfiguration extends AbstractGeneratorConfiguration {

    public SqlMapGeneratorConfiguration() {
        super();
    }

    public SqlMapGeneratorConfiguration(Context context) {
        super();
        targetProject = context.getSqlMapGeneratorConfiguration().getTargetProject();
        targetPackage = context.getSqlMapGeneratorConfiguration().getTargetPackage();
        baseTargetPackage = "";
    }


    @Override
    public void validate(List<String> errors, String contextId) {
        super.validate(errors, contextId, "SqlMapGenerator");
    }
}
