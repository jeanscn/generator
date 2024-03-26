package org.mybatis.generator.custom.annotations;

import com.vgosoft.core.annotation.*;
import com.vgosoft.core.constant.GlobalConstant;
import com.vgosoft.core.constant.enums.view.ViewIndexColumnEnum;
import com.vgosoft.tool.core.VMD5Util;
import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.internal.util.Mb3GenUtil;

import java.util.Arrays;
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
    private String labelWidth = "120px";
    private String size = "medium";
    private final String tableName;
    private final String appKeyword;

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
        if (VStringUtil.isNotBlank(this.getAppKeyword())) {
            items.add(VStringUtil.format("labelPosition = \"{0}\"", this.getLabelPosition()));
        }
        if (VStringUtil.isNotBlank(this.getLabelWidth())) {
            items.add(VStringUtil.format("labelWidth = \"{0}\"", this.getLabelWidth()));
        }
        if (VStringUtil.isNotBlank(this.getSize())) {
            items.add(VStringUtil.format("size = \"{0}\"", this.getSize()));
        }
        return ANNOTATION_NAME + "(" + String.join("\n       ,", items.toArray(new String[0])) + ")";
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
}
