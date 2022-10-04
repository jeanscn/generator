package org.mybatis.generator.custom.annotations;

import com.vgosoft.tool.core.VStringUtil;

import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-10-03 16:24
 * @version 3.0
 */
public class PreAuthorize extends AbstractAnnotation{

    public static final String ANNOTATION_NAME = "@PreAuthorize";

    private final String value;

    public PreAuthorize(String value) {
        super();
        this.value = value;
        this.addImports("org.springframework.security.access.prepost.PreAuthorize");
    }

    @Override
    public String toAnnotation() {
        StringBuilder sb = new StringBuilder();
        if (VStringUtil.isNotBlank(value)) {
            sb.append("value=\"").append(value).append("\"");
        }
        return ANNOTATION_NAME+"("+ sb +")";
    }
}
