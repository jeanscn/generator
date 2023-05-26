package org.mybatis.generator.config;

import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VOExcelGeneratorConfiguration extends AbstractModelGeneratorConfiguration {

    private Set<String> includeColumns = new HashSet<>();

    private Set<String> importIncludeColumns = new HashSet<>();

    private Set<String> importExcludeColumns = new HashSet<>();

    private Set<String> ignoreFields = new HashSet<>();

    private Set<String> importIgnoreFields = new HashSet<>();

    private final FullyQualifiedJavaType excelImportType;

    public VOExcelGeneratorConfiguration(Context context,TableConfiguration tc) {
        super(context);
        this.generate = true;
        targetPackage = String.join(".", baseTargetPackage,"vo");
        fullyQualifiedJavaType = new FullyQualifiedJavaType(String.join(".",targetPackage,tc.getDomainObjectName()+"ExcelVO"));
        excelImportType = new FullyQualifiedJavaType(String.join(".",targetPackage,tc.getDomainObjectName()+"ExcelImportVO"));
    }

    public FullyQualifiedJavaType getExcelImportType() {
        return excelImportType;
    }

    public Set<String> getIncludeColumns() {
        return includeColumns;
    }

    public void setIncludeColumns(Set<String> includeColumns) {
        this.includeColumns = includeColumns;
    }


    public Set<String> getImportIncludeColumns() {
        return importIncludeColumns;
    }

    public void setImportIncludeColumns(Set<String> importIncludeColumns) {
        this.importIncludeColumns = importIncludeColumns;
    }

    public Set<String> getImportExcludeColumns() {
        return importExcludeColumns;
    }

    public void setImportExcludeColumns(Set<String> importExcludeColumns) {
        this.importExcludeColumns = importExcludeColumns;
    }

    public Set<String> getIgnoreFields() {
        return ignoreFields;
    }

    public void setIgnoreFields(Set<String> ignoreFields) {
        this.ignoreFields = ignoreFields;
    }

    public Set<String> getImportIgnoreFields() {
        return importIgnoreFields;
    }

    public void setImportIgnoreFields(Set<String> importIgnoreFields) {
        this.importIgnoreFields = importIgnoreFields;
    }

    @Override
    public void validate(List<String> errors, String contextId) {
        super.validate(errors, contextId, "VOExcelGeneratorConfiguration");
    }


}
