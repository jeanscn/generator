package org.mybatis.generator.api.dom;

import org.mybatis.generator.api.KotlinFormatter;
import org.mybatis.generator.api.dom.kotlin.KotlinFile;
import org.mybatis.generator.api.dom.kotlin.render.KotlinFileRenderer;
import org.mybatis.generator.config.Context;

/**
 * This class is the default formatter for generated Kotlin.  This class will use the
 * built in DOM renderers.
 *
 * @author Jeff Butler
 *
 */
public class DefaultKotlinFormatter implements KotlinFormatter {
    protected Context context;

    @Override
    public String getFormattedContent(KotlinFile kotlinFile) {
        return new KotlinFileRenderer().render(kotlinFile);
    }

    @Override
    public void setContext(Context context) {
        this.context = context;
    }
}
