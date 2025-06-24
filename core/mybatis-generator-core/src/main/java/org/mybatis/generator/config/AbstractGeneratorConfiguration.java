package org.mybatis.generator.config;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;
import static org.mybatis.generator.internal.util.messages.Messages.getString;

@EqualsAndHashCode(callSuper = true)
@Data
public abstract class AbstractGeneratorConfiguration  extends TypedPropertyHolder{

    protected boolean generate;
    protected String targetPackage;
    protected String targetPackageGen;
    protected String targetProject;
    protected String baseTargetPackage;
    protected String subTargetPackage;

    protected Context context;

    public AbstractGeneratorConfiguration() {
        super();
        generate = false;
    }

    public AbstractGeneratorConfiguration(Context context) {
       this();
        this.context = context;
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
