package org.mybatis.generator.custom.annotations;

import lombok.Getter;
import lombok.Setter;

import static com.vgosoft.tool.core.VStringUtil.*;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-10-10 14:34
 * @version 3.0
 */
@Setter
@Getter
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
        if (stringHasValue(this.value)) {
            this.items.add(format("value = \"{0}\"", this.value));
        }
        if (stringHasValue(this.source)) {
            this.items.add(format("source = \"{0}\"", this.source));
        }
        if (!this.items.isEmpty()) return "@"+clazz.getSimpleName()+"("+ String.join(", ",items.toArray(new String[0])) +")";
        else return "@"+clazz.getSimpleName()+"()";
    }
}
