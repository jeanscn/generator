package org.mybatis.generator.custom.annotations;

import com.vgosoft.tool.core.VStringUtil;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-10-10 14:34
 * @version 3.0
 */
public class DictSys extends AbstractAnnotation{

    public static final String ANNOTATION_NAME = "@DictSys";

    private String value;

    public static DictSys create(){
        return new DictSys();
    }

    public DictSys() {
        super();
        this.addImports("com.vgosoft.core.annotation.DictSys");
    }

    public DictSys(String value) {
        this();
        this.value = value;
    }

    @Override
    public String toAnnotation() {
        if (VStringUtil.isNotBlank(value)) {
            this.items.add(VStringUtil.format("value = \"{0}\"", this.value));
        }
        if (this.items.size()>0) return ANNOTATION_NAME+"("+ String.join(", ",items.toArray(new String[0])) +")";
        else return ANNOTATION_NAME;
    }
}
