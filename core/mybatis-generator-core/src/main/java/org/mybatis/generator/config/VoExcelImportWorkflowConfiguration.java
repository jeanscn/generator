package org.mybatis.generator.config;

import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 配置独立的 Excel 两阶段导入工作流及其资源限制。
 *
 * @author vgosoft-agent
 * @author vgosoft
 */
public class VoExcelImportWorkflowConfiguration extends AbstractModelGeneratorConfiguration {

    private List<String> importIncludeColumns = new ArrayList<>();

    private Set<String> importExcludeColumns = new HashSet<>();

    private Set<String> importIgnoreFields = new HashSet<>();

    private long maxInputBytes;

    private long maxExpandedBytes;

    private long maxEntryBytes;

    private int maxZipEntries;

    private int maxRows;

    private int maxErrorMessages;

    private final FullyQualifiedJavaType excelImportType;

    public VoExcelImportWorkflowConfiguration(Context context, TableConfiguration tableConfiguration) {
        super(context);
        generate = false;
        targetPackage = String.join(".", baseTargetPackage, "vo");
        excelImportType = new FullyQualifiedJavaType(
                String.join(".", targetPackage, tableConfiguration.getDomainObjectName() + "ExcelImportVo"));
        fullyQualifiedJavaType = excelImportType;
    }

    public List<String> getImportIncludeColumns() {
        return importIncludeColumns;
    }

    public void setImportIncludeColumns(List<String> importIncludeColumns) {
        this.importIncludeColumns = importIncludeColumns;
    }

    public Set<String> getImportExcludeColumns() {
        return importExcludeColumns;
    }

    public void setImportExcludeColumns(Set<String> importExcludeColumns) {
        this.importExcludeColumns = importExcludeColumns;
    }

    public Set<String> getImportIgnoreFields() {
        return importIgnoreFields;
    }

    public void setImportIgnoreFields(Set<String> importIgnoreFields) {
        this.importIgnoreFields = importIgnoreFields;
    }

    public long getMaxInputBytes() {
        return maxInputBytes;
    }

    public void setMaxInputBytes(long maxInputBytes) {
        this.maxInputBytes = maxInputBytes;
    }

    public long getMaxExpandedBytes() {
        return maxExpandedBytes;
    }

    public void setMaxExpandedBytes(long maxExpandedBytes) {
        this.maxExpandedBytes = maxExpandedBytes;
    }

    public long getMaxEntryBytes() {
        return maxEntryBytes;
    }

    public void setMaxEntryBytes(long maxEntryBytes) {
        this.maxEntryBytes = maxEntryBytes;
    }

    public int getMaxZipEntries() {
        return maxZipEntries;
    }

    public void setMaxZipEntries(int maxZipEntries) {
        this.maxZipEntries = maxZipEntries;
    }

    public int getMaxRows() {
        return maxRows;
    }

    public void setMaxRows(int maxRows) {
        this.maxRows = maxRows;
    }

    public int getMaxErrorMessages() {
        return maxErrorMessages;
    }

    public void setMaxErrorMessages(int maxErrorMessages) {
        this.maxErrorMessages = maxErrorMessages;
    }

    public FullyQualifiedJavaType getExcelImportType() {
        return excelImportType;
    }

    @Override
    public void validate(List<String> errors, String contextId) {
        super.validate(errors, contextId, "VoExcelImportWorkflowConfiguration");
    }
}
