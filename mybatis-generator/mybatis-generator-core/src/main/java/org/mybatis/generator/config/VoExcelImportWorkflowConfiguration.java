package org.mybatis.generator.config;

import lombok.Getter;
import lombok.Setter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 配置独立的 Excel 两阶段导入工作流及其资源限制。
 *
 * @author vgosoft-agent
 */
@Getter
@Setter
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

    @Override
    public void validate(List<String> errors, String contextId) {
        super.validate(errors, contextId, "VoExcelImportWorkflowConfiguration");
        if (!isGenerate()) {
            return;
        }
        if (maxInputBytes < 1 || maxInputBytes > Integer.MAX_VALUE) {
            errors.add("excelImport.maxInputBytes 必须在 1 至 Integer.MAX_VALUE 之间，context " + contextId);
        }
        if (maxExpandedBytes < 1) {
            errors.add("excelImport.maxExpandedBytes 必须大于 0，context " + contextId);
        }
        if (maxEntryBytes < 1 || maxEntryBytes > maxExpandedBytes) {
            errors.add("excelImport.maxEntryBytes 必须大于 0 且不超过 maxExpandedBytes，context " + contextId);
        }
        if (maxZipEntries < 1) {
            errors.add("excelImport.maxZipEntries 必须大于 0，context " + contextId);
        }
        if (maxRows < 1) {
            errors.add("excelImport.maxRows 必须大于 0，context " + contextId);
        }
        if (maxErrorMessages < 1) {
            errors.add("excelImport.maxErrorMessages 必须大于 0，context " + contextId);
        }
    }
}
