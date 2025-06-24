package org.mybatis.generator.config;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class HtmlElementInnerListConfiguration extends AbstractHtmlElementDescriptor {

    private String elementKey;
    private String label = "";
    private boolean showTitle;
    private String moduleKeyword;
    private String sourceViewPath = "";
    private String sourceBeanName;
    private String sourceBeanNameKebabCase;
    private String relationField;
    private String sourceListViewClass;
    private String relationKey = "id";
    private String tagId;
    private String dataField;
    private int span = 24;
    private String afterColumn;
    private String containerType;
    private int order = 10;
    private String editMode = "row";
    private Set<String> editableFields = new HashSet<>();
    private Set<String> batchUpdateColumns = new HashSet<>();
    private boolean showRowNumber = true;
    private boolean totalRow;
    private Set<String> totalFields = new HashSet<>();
    private String totalText = "合计";
    private String restBasePath;
    private String editFormIn;
    private String detailFormIn;
    private String defaultSort;
    private String showActionColumn = "default";
    private String enableEdit = "default";
    private String printMode = "table";
    private int printFormColumnsNum = 2;
    private List<String> printFields = new ArrayList<>();
    private List<String> verify = new ArrayList<>();

    private String actionColumnWidth;

    public String getSourceViewPath() {
        if (sourceViewPath.contains("/")) {
            sourceViewPath = sourceViewPath.substring(sourceViewPath.lastIndexOf("/") + 1);
        }
        return sourceViewPath;
    }

    public List<String> getPrintFields() {
        return printFields.stream().distinct().collect(Collectors.toList());
    }

    public void addPrintField(String printField) {
        this.printFields.add(printField);
    }
}
