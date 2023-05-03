package org.mybatis.generator.config;

import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VOExcelGeneratorConfiguration extends AbstractModelGeneratorConfiguration {

    private Set<String> includeColumns = new HashSet<>();

    public VOExcelGeneratorConfiguration(Context context,TableConfiguration tc) {
        super(context);
        this.generate = true;
        targetPackage = String.join(".", baseTargetPackage,"vo");
        fullyQualifiedJavaType = new FullyQualifiedJavaType(String.join(".",targetPackage,tc.getDomainObjectName()+"ExcelVO"));
    }

    public Set<String> getIncludeColumns() {
        return includeColumns;
    }

    public void setIncludeColumns(Set<String> includeColumns) {
        this.includeColumns = includeColumns;
    }

    @Override
    public void validate(List<String> errors, String contextId) {
        super.validate(errors, contextId, "VOExcelGeneratorConfiguration");
    }
}
