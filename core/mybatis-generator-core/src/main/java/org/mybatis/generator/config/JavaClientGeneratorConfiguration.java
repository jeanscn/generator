package org.mybatis.generator.config;

import java.util.List;

public class JavaClientGeneratorConfiguration  extends AbstractGeneratorConfiguration {

    public JavaClientGeneratorConfiguration() {
        super();
    }

    @Override
    public void validate(List<String> errors, String contextId) {
        super.validate(errors, contextId, "JavaClientGeneratorConfiguration");
    }
}
