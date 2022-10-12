package org.mybatis.generator.custom.annotations;

import com.vgosoft.tool.core.VStringUtil;
import org.mybatis.generator.custom.ConstantsUtil;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-10-03 16:24
 * @version 3.0
 */
public class ExcelProperty extends AbstractAnnotation{

    public static final String ANNOTATION_NAME = "@ExcelProperty";

    private final String value;

    public static ExcelProperty create(String value){
        return new ExcelProperty(value);
    }

    public ExcelProperty(String value) {
        super();
        this.value = value;
        this.addImports(ConstantsUtil.EXCEL_PROPERTY);
    }

    @Override
    public String toAnnotation() {
        if (VStringUtil.isNotBlank(value)) {
            this.items.add(VStringUtil.format("value = \"{0}\"", this.value));
        }
        return ANNOTATION_NAME+"("+ String.join(", ",items.toArray(new String[0])) +")";
    }
}
