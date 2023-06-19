package org.mybatis.generator.custom.annotations;

import com.vgosoft.tool.core.VStringUtil;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-05-17 23:10
 * @version 3.0
 */
public class DictEnumDesc extends AbstractAnnotation {

    public static final String ANNOTATION_NAME = "@DictEnum";

    private String value;

    private String generic;

    private String source;

    public static DictEnumDesc create() {
        return new DictEnumDesc();
    }

    public DictEnumDesc() {
        super();
        this.addImports("com.vgosoft.core.annotation.DictEnum");
    }

    public DictEnumDesc(String value) {
        this();
        this.value = value;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getGeneric() {
        return generic;
    }

    public void setGeneric(String generic) {
        this.generic = generic;
    }

    @Override
    public String toAnnotation() {
        if (VStringUtil.isNotBlank(value)) {
            this.items.add(VStringUtil.format("value = \"{0}\"", this.value));
        }
        if (VStringUtil.isNotBlank(source)) {
            this.items.add(VStringUtil.format("source = \"{0}\"", this.source));
        }
        if (VStringUtil.isNotBlank(generic)) {
            this.items.add(VStringUtil.format("generic = \"{0}\"", this.generic));
        }
        if (this.items.size() > 0) return ANNOTATION_NAME + "(" + String.join(", ", items.toArray(new String[0])) + ")";
        else return ANNOTATION_NAME;
    }
}
