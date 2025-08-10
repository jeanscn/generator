package org.mybatis.generator.runtime.kotlin.elements;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.kotlin.KotlinArg;
import org.mybatis.generator.api.dom.kotlin.KotlinFile;
import org.mybatis.generator.api.dom.kotlin.KotlinFunction;
import org.mybatis.generator.config.Context;

/**
 * @author cen_c
 */
public abstract class AbstractKotlinFunctionGenerator {
    protected final Context context;
    protected final IntrospectedTable introspectedTable;
    protected final String tableFieldName;

    protected AbstractKotlinFunctionGenerator(BaseBuilder<?> builder) {
        context = builder.context;
        introspectedTable = builder.introspectedTable;
        tableFieldName = builder.tableFieldName;
    }

    protected void acceptParts(KotlinFile kotlinFile, KotlinFunction kotlinFunction,
            KotlinFunctionParts functionParts) {
        for (KotlinArg argument : functionParts.getArguments()) {
            kotlinFunction.addArgument(argument);
        }

        for (String annotation : functionParts.getAnnotations()) {
            kotlinFunction.addAnnotation(annotation);
        }

        kotlinFunction.addCodeLines(functionParts.getCodeLines());
        kotlinFile.addImports(functionParts.getImports());
    }

    protected void acceptParts(KotlinFunctionAndImports functionAndImports, KotlinFunctionParts functionParts) {
        for (KotlinArg argument : functionParts.getArguments()) {
            functionAndImports.getFunction().addArgument(argument);
        }

        for (String annotation : functionParts.getAnnotations()) {
            functionAndImports.getFunction().addAnnotation(annotation);
        }

        functionAndImports.getFunction().addCodeLines(functionParts.getCodeLines());
        functionAndImports.getImports().addAll(functionParts.getImports());
    }

    protected void addFunctionComment(KotlinFunctionAndImports functionAndImports) {
        context.getCommentGenerator().addGeneralFunctionComment(functionAndImports.getFunction(), introspectedTable,
                functionAndImports.getImports());
    }

    public static FieldNameAndImport calculateFieldNameAndImport(String tableFieldName, String supportObjectImport,
                                                     IntrospectedColumn column) {
        FieldNameAndImport answer = new FieldNameAndImport();
        answer.fieldName = column.getJavaProperty();
        if (answer.fieldName.equals(tableFieldName)) {
            // name collision, no shortcut generated
            answer.fieldName = tableFieldName + "." + answer.fieldName;
            answer.importString = supportObjectImport + "." + tableFieldName;
        } else {
            answer.importString = supportObjectImport + "." + answer.fieldName;
        }
        return answer;
    }

    public static class FieldNameAndImport {
        private String fieldName;
        private String importString;

        public String fieldName() {
            return fieldName;
        }

        public String importString() {
            return importString;
        }
    }

    /**
     * 生成方法和导入
     * @return KotlinFunctionAndImports 对象，包含生成的方法和需要导入的包
     */
    public abstract KotlinFunctionAndImports generateMethodAndImports();

    public abstract boolean callPlugins(KotlinFunction method, KotlinFile kotlinFile);

    public abstract static class BaseBuilder<T extends BaseBuilder<T>> {
        private Context context;
        private IntrospectedTable introspectedTable;
        private String tableFieldName;

        public T withContext(Context context) {
            this.context = context;
            return getThis();
        }

        public T withTableFieldName(String tableFieldName) {
            this.tableFieldName = tableFieldName;
            return getThis();
        }

        public T withIntrospectedTable(IntrospectedTable introspectedTable) {
            this.introspectedTable = introspectedTable;
            return getThis();
        }

        /**
         * 返回当前构建器的实例
         * @return 当前构建器的实例
         */
        public abstract T getThis();
    }
}
