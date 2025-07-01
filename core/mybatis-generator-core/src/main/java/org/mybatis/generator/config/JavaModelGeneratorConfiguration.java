package org.mybatis.generator.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
public class JavaModelGeneratorConfiguration extends AbstractModelGeneratorConfiguration {

    private boolean noMetaAnnotation;

    private boolean enableChildren;

    private boolean ignoreTenant = false;

    public JavaModelGeneratorConfiguration(Context context) {
        super();
        noMetaAnnotation = false;
    }

    @Override
    public void validate(List<String> errors, String contextId) {
        super.validate(errors, contextId, "JavaModelGeneratorConfiguration");
    }
}
