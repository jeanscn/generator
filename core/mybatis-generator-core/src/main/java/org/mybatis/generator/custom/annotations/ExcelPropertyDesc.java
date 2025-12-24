package org.mybatis.generator.custom.annotations;

import com.vgosoft.tool.core.VStringUtil;
import lombok.Getter;
import lombok.Setter;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-10-03 16:24
 * @version 3.0
 */
@Getter
public class ExcelPropertyDesc extends AbstractAnnotation {

    public static final String ANNOTATION_NAME = "@ExcelProperty";
    public static final String EXCEL_PROPERTY = "com.alibaba.excel.annotation.ExcelProperty";

    private final String[] value;

    @Setter
    private int index = -1;

    @Setter
    private int order;
    @Setter
    String converter;

    public static ExcelPropertyDesc create(String... value) {
        return new ExcelPropertyDesc(value);
    }

    public ExcelPropertyDesc(String... value) {
        super();
        this.value = value;
        this.addImports(EXCEL_PROPERTY);
    }


    @Override
    public String toAnnotation() {
        if (value.length == 1) {
            this.items.add(VStringUtil.format("value = \"{0}\"", this.value[0]));
        } else if (value.length > 1) {
            for (int i = 0; i < value.length; i++) {
                value[i] = VStringUtil.format("\"{0}\"", value[i]);
            }
            this.items.add(VStringUtil.format("value = '{'{0}'}'", String.join(",", value)));
        }
        if (VStringUtil.isNotBlank(converter)) {
            this.items.add(VStringUtil.format("converter = {0}", this.converter));
        }
        if (index != -1) {
            this.items.add(VStringUtil.format("index = {0}", this.index));
        }
        if (order != 0) {
            this.items.add(VStringUtil.format("order = {0}", this.order));
        }
        return ANNOTATION_NAME + "(" + String.join(", ", items.toArray(new String[0])) + ")";
    }
}
