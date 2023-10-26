package org.mybatis.generator.config;

import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VOExcelGeneratorConfiguration extends AbstractModelGeneratorConfiguration {

    private Set<String> exportIncludeColumns = new HashSet<>();
    private Set<String> exportExcludeColumns = new HashSet<>();
    private Set<String> exportIgnoreFields = new HashSet<>();

    private Set<String> importIncludeColumns = new HashSet<>();
    private Set<String> importExcludeColumns = new HashSet<>();
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

    public Set<String> getExportIncludeColumns() {
        return exportIncludeColumns;
    }

    public void setExportIncludeColumns(Set<String> exportIncludeColumns) {
        this.exportIncludeColumns = exportIncludeColumns;
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

    public Set<String> getExportIgnoreFields() {
        return exportIgnoreFields;
    }

    public void setExportIgnoreFields(Set<String> exportIgnoreFields) {
        this.exportIgnoreFields = exportIgnoreFields;
    }

    public Set<String> getImportIgnoreFields() {
        return importIgnoreFields;
    }

    public void setImportIgnoreFields(Set<String> importIgnoreFields) {
        this.importIgnoreFields = importIgnoreFields;
    }

    public Set<String> getExportExcludeColumns() {
        return exportExcludeColumns;
    }

    public void setExportExcludeColumns(Set<String> exportExcludeColumns) {
        this.exportExcludeColumns = exportExcludeColumns;
    }

    @Override
    public void validate(List<String> errors, String contextId) {
        super.validate(errors, contextId, "VOExcelGeneratorConfiguration");
    }


}
