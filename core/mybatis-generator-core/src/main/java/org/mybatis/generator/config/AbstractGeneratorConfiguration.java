package org.mybatis.generator.config;

import java.util.List;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;
import static org.mybatis.generator.internal.util.messages.Messages.getString;

public abstract class AbstractGeneratorConfiguration  extends TypedPropertyHolder{

    protected String targetPackage;
    protected String targetProject;
    protected boolean generate;

    public AbstractGeneratorConfiguration() {
        super();
    }

    public String getTargetPackage() {
        return targetPackage;
    }

    public void setTargetPackage(String targetPackage) {
        this.targetPackage = targetPackage;
    }

    public String getTargetProject() {
        return targetProject;
    }

    public void setTargetProject(String targetProject) {
        this.targetProject = targetProject;
    }

    public boolean isGenerate() {
        return generate;
    }

    public void setGenerate(boolean generate) {
        this.generate = generate;
    }

    public void validate(List<String> errors, String contextId,String funcKey){
        if (!stringHasValue(targetProject)) {
            errors.add(getString("ValidationError.0", contextId));
        }
        if (!stringHasValue(targetPackage)) {
            errors.add(getString("ValidationError.12",funcKey, contextId));
        }
    };

    protected void validate(List<String> errors, String contextId) {
        validate(errors,contextId,"GeneratorConfiguration");
    }
}
