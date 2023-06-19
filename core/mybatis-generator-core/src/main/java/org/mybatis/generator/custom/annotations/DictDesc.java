package org.mybatis.generator.custom.annotations;

import com.vgosoft.core.constant.GlobalConstant;
import com.vgosoft.tool.core.VStringUtil;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-10-10 14:34
 * @version 3.0
 */

public class DictDesc extends AbstractAnnotation{

    public static final String ANNOTATION_NAME = "@Dict";

    private String value;

    private final String beanName;

    private String applyProperty;

    private String source;

    public static DictDesc create(String beanName){
        return new DictDesc(beanName);
    }

    public DictDesc(String beanName) {
        super();
        this.beanName = beanName;
        this.addImports("com.vgosoft.core.annotation.Dict");
    }

    public DictDesc(String value, String beanName) {
        this(beanName);
        this.value = value;
    }

    @Override
    public String toAnnotation() {
        if (VStringUtil.isNotBlank(value)) {
            this.items.add(VStringUtil.format("value = \"{0}\"", this.value));
        }
        if (VStringUtil.isNotBlank(beanName)) {
            this.items.add(VStringUtil.format("beanName = \"{0}\"", this.beanName));
        }
        if (VStringUtil.isNotBlank(applyProperty) && !applyProperty.equalsIgnoreCase(GlobalConstant.CACHE_PO_DEFAULT_VALUE_TEXT)) {
            this.items.add(VStringUtil.format("applyProperty = \"{0}\"", this.applyProperty));
        }
        if (VStringUtil.isNotBlank(source)) {
            this.items.add(VStringUtil.format("source = \"{0}\"", this.source));
        }
        if (this.items.size()>0) return ANNOTATION_NAME+"("+ String.join(", ",items.toArray(new String[0])) +")";
        else return ANNOTATION_NAME;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getBeanName() {
        return beanName;
    }

    public String getApplyProperty() {
        return applyProperty;
    }

    public void setApplyProperty(String applyProperty) {
        this.applyProperty = applyProperty;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
