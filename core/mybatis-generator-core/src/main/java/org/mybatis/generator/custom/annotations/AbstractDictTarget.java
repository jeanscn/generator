package org.mybatis.generator.custom.annotations;

import com.vgosoft.tool.core.VStringUtil;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-10-10 14:34
 * @version 3.0
 */
public abstract class AbstractDictTarget<T> extends AbstractAnnotation{

    private final Class<T> clazz;

    protected String value;

    protected String source;

    protected AbstractDictTarget(Class<T> clazz) {
        super();
        this.clazz = clazz;
        this.addImports(clazz.getCanonicalName());
    }

    @Override
    public String toAnnotation() {
        if (VStringUtil.isNotBlank(this.value)) {
            this.items.add(VStringUtil.format("value = \"{0}\"", this.value));
        }
        if (VStringUtil.isNotBlank(this.source)) {
            this.items.add(VStringUtil.format("source = \"{0}\"", this.source));
        }
        if (this.items.size()>0) return "@"+clazz.getSimpleName()+"("+ String.join(", ",items.toArray(new String[0])) +")";
        else return "@"+clazz.getSimpleName()+"()";
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
