package org.mybatis.generator.custom.annotations;

import com.vgosoft.core.annotation.Dict;
import com.vgosoft.core.constant.GlobalConstant;
import com.vgosoft.tool.core.VStringUtil;
import lombok.Getter;
import lombok.Setter;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-10-10 14:34
 * @version 3.0
 */

@Getter
public class DictDesc extends AbstractAnnotation{

    public static final String ANNOTATION_NAME = "@"+Dict.class.getSimpleName();

    @Setter
    private String value;

    private final String beanName;

    @Setter
    private String applyProperty;

    @Setter
    private String source;

    public static DictDesc create(String beanName){
        return new DictDesc(beanName);
    }

    public DictDesc(String beanName) {
        super();
        this.beanName = beanName;
        this.addImports(Dict.class.getCanonicalName());
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
        if (!this.items.isEmpty()) { return ANNOTATION_NAME+"("+ String.join(", ",items.toArray(new String[0])) +")";}
        else {return ANNOTATION_NAME;}
    }
}
