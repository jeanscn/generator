package org.mybatis.generator.runtime.dynamic.sql.elements;

import org.mybatis.generator.api.AbstractIntrospectedTable;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.config.Context;

/**
 * @author cen_c
 */
public abstract class AbstractMethodGenerator {
    protected final Context context;
    protected final AbstractIntrospectedTable introspectedTable;
    protected final String tableFieldName;

    protected AbstractMethodGenerator(BaseBuilder<?> builder) {
        context = builder.context;
        introspectedTable = builder.introspectedTable;
        tableFieldName = builder.tableFieldName;
    }

    protected String calculateFieldName(IntrospectedColumn column) {
        return calculateFieldName(tableFieldName, column);
    }

    public static String calculateFieldName(String tableFieldName, IntrospectedColumn column) {
        String fieldName = column.getJavaProperty();
        if (fieldName.equals(tableFieldName)) {
            // name collision, no shortcut generated
            fieldName = tableFieldName + "." + fieldName;
        }
        return fieldName;
    }

    protected void acceptParts(MethodAndImports.Builder builder, Method method, MethodParts methodParts) {
        for (Parameter parameter : methodParts.getParameters()) {
            method.addParameter(parameter);
        }

        for (String annotation : methodParts.getAnnotations()) {
            method.addAnnotation(annotation);
        }

        method.addBodyLines(methodParts.getBodyLines());
        builder.withImports(methodParts.getImports());
    }

    /**
     * 生成方法和所需的import
     * @return MethodAndImports
     */
    public abstract MethodAndImports generateMethodAndImports();

    public abstract boolean callPlugins(Method method, Interface interfaze);

    public abstract static class BaseBuilder<T extends BaseBuilder<T>> {
        private Context context;
        private AbstractIntrospectedTable introspectedTable;
        private String tableFieldName;

        public T withContext(Context context) {
            this.context = context;
            return getThis();
        }

        public T withIntrospectedTable(AbstractIntrospectedTable introspectedTable) {
            this.introspectedTable = introspectedTable;
            return getThis();
        }

        public T withTableFieldName(String tableFieldName) {
            this.tableFieldName = tableFieldName;
            return getThis();
        }

        /**
         * 子类返回自己
         * @return 子类实例
         */
        public abstract T getThis();
    }
}
