package org.mybatis.generator.custom.annotations;

import com.vgosoft.core.annotation.*;
import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.internal.util.Mb3GenUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ElementPlus表单元数据描述注解
 *
 * @version 6.0
 */
public class VueFormMetaDesc extends AbstractAnnotation {

    public static final String ANNOTATION_NAME = "@" + VueFormMeta.class.getSimpleName();

    private final String value;

    private String labelPosition = "right";
    private String labelWidth = "100px";
    private String size = "default";

    private String popSize = "default";

    private boolean popDraggable = true;
    private final String tableName;
    private final String appKeyword;

    private String restBasePath;

    private Set<String> readonlyFields;
    private Set<String> displayOnlyFields;
    private Set<String> hiddenFields;

    private List<VueFormUploadMetaDesc> uploadMeta = new ArrayList<>();

    private List<VueFormInnerListMetaDesc> innerListMeta = new ArrayList<>();

    private List<VueFormContainerMetaDesc> containerMeta = new ArrayList<>();

    private List<VueFormApprovalMetaDesc> approvalMeta = new ArrayList<>();

    public static VueFormMetaDesc create(IntrospectedTable introspectedTable) {
        return new VueFormMetaDesc(introspectedTable);
    }

    public VueFormMetaDesc(IntrospectedTable introspectedTable) {
        super();
        this.value = Mb3GenUtil.getDefaultHtmlKey(introspectedTable);
        items.add(VStringUtil.format("value = \"{0}\"", this.getValue()));
        this.appKeyword = introspectedTable.getContext().getAppKeyword();
        items.add(VStringUtil.format("appKeyword = \"{0}\"", this.getAppKeyword()));
        this.tableName = introspectedTable.getTableConfiguration().getTableName();
        items.add(VStringUtil.format("tableName = \"{0}\"", this.tableName));
        this.addImports(VueFormMeta.class.getCanonicalName());
    }

    @Override
    public String toAnnotation() {
        if (VStringUtil.isNotBlank(this.getLabelPosition()) && !"right".equals(this.getLabelPosition())) {
            items.add(VStringUtil.format("labelPosition = \"{0}\"", this.getLabelPosition()));
        }
        if (VStringUtil.isNotBlank(this.getLabelWidth()) && !"100px".equals(this.getLabelWidth())) {
            items.add(VStringUtil.format("labelWidth = \"{0}\"", this.getLabelWidth()));
        }
        if (VStringUtil.isNotBlank(this.getSize()) && !"default".equals(this.getSize())) {
            items.add(VStringUtil.format("size = \"{0}\"", this.getSize()));
        }
        if (VStringUtil.isNotBlank(this.getRestBasePath())) {
            items.add(VStringUtil.format("restBasePath = \"{0}\"", this.getRestBasePath()));
        }
        if (VStringUtil.isNotBlank(this.getPopSize()) && !"default".equals(this.getPopSize())) {
            items.add(VStringUtil.format("popSize = \"{0}\"", this.getPopSize()));
        }
        if (!popDraggable) {
            items.add("popDraggable = false");
        }
        if (!uploadMeta.isEmpty()) {
            items.add("\n        uploadMeta = {\n                " + uploadMeta.stream().map(VueFormUploadMetaDesc::toAnnotation).collect(Collectors.joining(",\n                ")) + "\n        }");
            this.addImports(VueFormUploadMeta.class.getCanonicalName());
        }
        if (!innerListMeta.isEmpty()) {
            items.add("\n        innerListMeta = {\n                " + innerListMeta.stream().map(VueFormInnerListMetaDesc::toAnnotation).collect(Collectors.joining(",\n                ")) + "\n        }");
            this.addImports(VueFormInnerListMeta.class.getCanonicalName());
        }
        if (!containerMeta.isEmpty()) {
            items.add("\n        containerMeta = {\n                " + containerMeta.stream().map(VueFormContainerMetaDesc::toAnnotation).collect(Collectors.joining(",\n                ")) + "\n        }");
            this.addImports(VueFormContainerMeta.class.getCanonicalName());
        }
        if (!approvalMeta.isEmpty()) {
            items.add("\n        approvalMeta = {\n                " + approvalMeta.stream().map(VueFormApprovalMetaDesc::toAnnotation).collect(Collectors.joining(",\n                ")) + "\n        }");
            this.addImports(VueFormApprovalMeta.class.getCanonicalName());
        }
        if (!this.getReadonlyFields().isEmpty()) {
            items.add(VStringUtil.format("readonlyFields = \"{0}\"", String.join(",", this.getReadonlyFields())));
        }
        if (!this.getDisplayOnlyFields().isEmpty()) {
            items.add(VStringUtil.format("displayOnlyFields = \"{0}\"", String.join(",", this.getDisplayOnlyFields())));
        }
        if (!this.getHiddenFields().isEmpty()) {
            items.add(VStringUtil.format("hiddenFields = \"{0}\"", String.join(",", this.getHiddenFields())));
        }
        return ANNOTATION_NAME + "(" + String.join(", ", items.toArray(new String[0])) + "\n)";
    }

    public String getValue() {
        return value;
    }

    public String getLabelPosition() {
        return labelPosition;
    }

    public void setLabelPosition(String labelPosition) {
        this.labelPosition = labelPosition;
    }

    public String getLabelWidth() {
        return labelWidth;
    }

    public void setLabelWidth(String labelWidth) {
        this.labelWidth = labelWidth;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getTableName() {
        return tableName;
    }

    public String getAppKeyword() {
        return appKeyword;
    }

    public String getRestBasePath() {
        return restBasePath;
    }

    public void setRestBasePath(String restBasePath) {
        this.restBasePath = restBasePath;
    }

    public String getPopSize() {
        return popSize;
    }

    public void setPopSize(String popSize) {
        this.popSize = popSize;
    }

    public List<VueFormUploadMetaDesc> getUploadMeta() {
        return uploadMeta;
    }

    public void setUploadMeta(List<VueFormUploadMetaDesc> uploadMeta) {
        this.uploadMeta = uploadMeta;
    }

    public List<VueFormInnerListMetaDesc> getInnerListMeta() {
        return innerListMeta;
    }

    public void setInnerListMeta(List<VueFormInnerListMetaDesc> innerListMeta) {
        this.innerListMeta = innerListMeta;
    }

    public List<VueFormContainerMetaDesc> getContainerMeta() {
        return containerMeta;
    }

    public void setContainerMeta(List<VueFormContainerMetaDesc> containerMeta) {
        this.containerMeta = containerMeta;
    }

    public boolean isPopDraggable() {
        return popDraggable;
    }

    public void setPopDraggable(boolean popDraggable) {
        this.popDraggable = popDraggable;
    }

    public Set<String> getReadonlyFields() {
        return readonlyFields;
    }

    public void setReadonlyFields(Set<String> readonlyFields) {
        this.readonlyFields = readonlyFields;
    }

    public Set<String> getDisplayOnlyFields() {
        return displayOnlyFields;
    }

    public void setDisplayOnlyFields(Set<String> displayOnlyFields) {
        this.displayOnlyFields = displayOnlyFields;
    }

    public Set<String> getHiddenFields() {
        return hiddenFields;
    }

    public void setHiddenFields(Set<String> hiddenFields) {
        this.hiddenFields = hiddenFields;
    }

    public List<VueFormApprovalMetaDesc> getApprovalMeta() {
        return approvalMeta;
    }

    public void setApprovalMeta(List<VueFormApprovalMetaDesc> approvalMeta) {
        this.approvalMeta = approvalMeta;
    }
}
