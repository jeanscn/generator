package org.mybatis.generator.custom.annotations;

import com.vgosoft.tool.core.VStringUtil;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-10-03 16:24
 * @version 3.0
 */
public class PreAuthorize extends AbstractAnnotation{

    public static final String ANNOTATION_NAME = "@PreAuthorize";

    private final String value;

    public static PreAuthorize create(String value){
        return new PreAuthorize(value);
    }

    public PreAuthorize(String value) {
        super();
        this.value = value;
        this.addImports("org.springframework.security.access.prepost.PreAuthorize");
    }

    @Override
    public String toAnnotation() {
        if (VStringUtil.isNotBlank(value)) {
            this.items.add(VStringUtil.format("value = \"{0}\"", this.value));
        }
        return ANNOTATION_NAME+"("+ String.join(", ",items.toArray(new String[0])) +")";
    }
}
