package org.mybatis.generator.api.dom;

import org.mybatis.generator.api.JavaFormatter;
import org.mybatis.generator.api.dom.java.CompilationUnit;
import org.mybatis.generator.api.dom.java.CompilationUnitVisitor;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.java.TopLevelEnumeration;
import org.mybatis.generator.api.dom.java.render.TopLevelClassRenderer;
import org.mybatis.generator.api.dom.java.render.TopLevelEnumerationRenderer;
import org.mybatis.generator.api.dom.java.render.TopLevelInterfaceRenderer;
import org.mybatis.generator.config.Context;

/**
 * This class is the default formatter for generated Java.  This class will use the
 * built in DOM renderers.
 *
 * @author Jeff Butler
 *
 */
public class DefaultJavaFormatter implements JavaFormatter, CompilationUnitVisitor<String> {
    protected Context context;

    @Override
    public String getFormattedContent(CompilationUnit compilationUnit) {
        return compilationUnit.accept(this);
    }

    @Override
    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public String visit(TopLevelClass topLevelClass) {
        return new TopLevelClassRenderer().render(topLevelClass);
    }

    @Override
    public String visit(TopLevelEnumeration topLevelEnumeration) {
        return new TopLevelEnumerationRenderer().render(topLevelEnumeration);
    }

    @Override
    public String visit(Interface topLevelInterface) {
        return new TopLevelInterfaceRenderer().render(topLevelInterface);
    }
}
