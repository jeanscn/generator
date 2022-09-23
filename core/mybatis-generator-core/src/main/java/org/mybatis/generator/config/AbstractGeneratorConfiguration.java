package org.mybatis.generator.config;

import java.util.List;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;
import static org.mybatis.generator.internal.util.messages.Messages.getString;

public abstract class AbstractGeneratorConfiguration  extends TypedPropertyHolder{

    protected boolean generate;
    protected String targetPackage;
    protected String targetPackageGen;
    protected String targetProject;
    protected String baseTargetPackage;
    protected String subTargetPackage;

    public AbstractGeneratorConfiguration() {
        super();
        generate = false;
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

    public String getSubTargetPackage() {
        return subTargetPackage;
    }

    public void setSubTargetPackage(String subTargetPackage) {
        this.subTargetPackage = subTargetPackage;
    }

    public String getBaseTargetPackage() {
        return baseTargetPackage;
    }

    public void setBaseTargetPackage(String baseTargetPackage) {
        this.baseTargetPackage = baseTargetPackage;
    }

    public String getTargetPackageGen() {
        return targetPackageGen;
    }

    public void setTargetPackageGen(String targetPackageGen) {
        this.targetPackageGen = targetPackageGen;
    }

    public void validate(List<String> errors, String contextId, String funcKey){
        if (!stringHasValue(targetProject)) {
            errors.add(getString("ValidationError.0", contextId));
        }
        if (!stringHasValue(targetPackage)) {
            errors.add(getString("ValidationError.12",funcKey, contextId));
        }
    }

    abstract void validate(List<String> errors, String contextId);
}
