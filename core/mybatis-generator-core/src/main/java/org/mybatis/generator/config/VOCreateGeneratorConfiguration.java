package org.mybatis.generator.config;

import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;

import java.util.*;

public class VOCreateGeneratorConfiguration extends AbstractModelGeneratorConfiguration {

    private List<String> includeColumns = new ArrayList<>();

    private Set<String> requiredColumns = new HashSet<>();

    private Set<String> validateIgnoreColumns = new HashSet<>();

    private boolean isEnableSelective = true;

    public VOCreateGeneratorConfiguration(Context context, TableConfiguration tc) {
        super(context);
        this.generate = true;
        targetPackage = String.join(".", baseTargetPackage,"vo");
        fullyQualifiedJavaType = new FullyQualifiedJavaType(String.join(".",targetPackage,tc.getDomainObjectName()+"CreateVO"));
    }

    public List<String> getIncludeColumns() {
        return includeColumns;
    }

    public void setIncludeColumns(List<String> includeColumns) {
        this.includeColumns = includeColumns;
    }

    public Set<String> getRequiredColumns() {
        return requiredColumns;
    }

    public void setRequiredColumns(Set<String> requiredColumns) {
        this.requiredColumns = requiredColumns;
    }

    public Set<String> getValidateIgnoreColumns() {
        return validateIgnoreColumns;
    }

    public void setValidateIgnoreColumns(Set<String> validateIgnoreColumns) {
        this.validateIgnoreColumns = validateIgnoreColumns;
    }

    public boolean isEnableSelective() {
        return isEnableSelective;
    }

    public void setEnableSelective(boolean enableSelective) {
        isEnableSelective = enableSelective;
    }

    @Override
    public void validate(List<String> errors, String contextId) {
        super.validate(errors, contextId, "VOCreateGeneratorConfiguration");
    }
}
