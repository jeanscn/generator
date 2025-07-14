package org.mybatis.generator.config;

import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;

import java.util.*;

public class VoModelGeneratorConfiguration extends AbstractModelGeneratorConfiguration {

    private List<String> includeColumns = new ArrayList<>();

    private Set<String> validateIgnoreColumns = new HashSet<>();

    public VoModelGeneratorConfiguration(Context context, TableConfiguration tc) {
        super(context);
        this.generate = true;
        targetPackage = String.join(".", baseTargetPackage,"vo");
        fullyQualifiedJavaType = new FullyQualifiedJavaType(String.join(".",targetPackage,tc.getDomainObjectName()+"Vo"));
    }

    public List<String> getIncludeColumns() {
        return includeColumns;
    }

    public void setIncludeColumns(List<String> includeColumns) {
        this.includeColumns = includeColumns;
    }

    public Set<String> getValidateIgnoreColumns() {
        return validateIgnoreColumns;
    }

    public void setValidateIgnoreColumns(Set<String> validateIgnoreColumns) {
        this.validateIgnoreColumns = validateIgnoreColumns;
    }

    @Override
    public void validate(List<String> errors, String contextId) {
        super.validate(errors, contextId, "VoModelGeneratorConfiguration");
    }
}
