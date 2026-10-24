package org.mybatis.generator.runtime.kotlin.elements;

import org.mybatis.generator.api.dom.kotlin.FullyQualifiedKotlinType;
import org.mybatis.generator.api.dom.kotlin.KotlinArg;
import org.mybatis.generator.api.dom.kotlin.KotlinFile;
import org.mybatis.generator.api.dom.kotlin.KotlinFunction;
import org.mybatis.generator.config.GeneratedKey;
import org.mybatis.generator.runtime.dynamic.sql.elements.Utils;

/**
 * @author cen_c
 */
public class BasicMultipleInsertMethodGenerator extends AbstractKotlinFunctionGenerator {

    private final FullyQualifiedKotlinType recordType;

    private BasicMultipleInsertMethodGenerator(Builder builder) {
        super(builder);
        recordType = builder.recordType;
    }

    @Override
    public KotlinFunctionAndImports generateMethodAndImports() {
        if (!Utils.generateMultipleRowInsert(introspectedTable)) {
            return null;
        }

        return introspectedTable.getGeneratedKey().map(this::generateMethodWithGeneratedKeys).orElse(null);
    }

    private KotlinFunctionAndImports generateMethodWithGeneratedKeys(GeneratedKey gk) {

        KotlinFunctionAndImports functionAndImports = KotlinFunctionAndImports.withFunction(
                KotlinFunction.newOneLineFunction("insertMultiple")
                .withExplicitReturnType("Int")
                .withAnnotation("@InsertProvider(type = SqlProviderAdapter::class,"
                        + " method = \"insertMultipleWithGeneratedKeys\")")
                .withArgument(KotlinArg.newArg("insertStatement")
                        .withAnnotation("@Param(\"insertStatement\")")
                        .withDataType("String")
                        .build())
                .withArgument(KotlinArg.newArg("records")
                        .withAnnotation("@Param(\"records\")")
                        .withDataType("List<"
                                + recordType.getShortNameWithTypeArguments()
                                + ">")
                        .build())
                .build())
                .withImport("org.mybatis.dynamic.sql.util.SqlProviderAdapter")
                .withImport("org.apache.ibatis.annotations.InsertProvider")
                .withImport("org.apache.ibatis.annotations.Param")
                .withImports(recordType.getImportList())
                .build();

        addFunctionComment(functionAndImports);

        KotlinFunctionParts functionParts = getGeneratedKeyAnnotation(gk);
        acceptParts(functionAndImports, functionParts);

        return functionAndImports;
    }

    private KotlinFunctionParts getGeneratedKeyAnnotation(GeneratedKey gk) {
        KotlinFunctionParts.Builder builder = new KotlinFunctionParts.Builder();

        StringBuilder sb = new StringBuilder();
        introspectedTable.getColumn(gk.getColumn()).ifPresent(introspectedColumn -> {
            if (gk.isJdbcStandard()) {
                // only jdbc standard keys are supported for multiple insert
                builder.withImport("org.apache.ibatis.annotations.Options");
                sb.append("@Options(useGeneratedKeys=true,keyProperty=\"records.");
                sb.append(introspectedColumn.getJavaProperty());
                sb.append("\")");
                builder.withAnnotation(sb.toString());
            }
        });

        return builder.build();
    }

    @Override
    public boolean callPlugins(KotlinFunction kotlinFunction, KotlinFile kotlinFile) {
        return context.getPlugins().clientBasicInsertMultipleMethodGenerated(kotlinFunction, kotlinFile,
                introspectedTable);
    }

    public static class Builder extends BaseBuilder<Builder> {

        private FullyQualifiedKotlinType recordType;

        public Builder withRecordType(FullyQualifiedKotlinType recordType) {
            this.recordType = recordType;
            return this;
        }

        @Override
        public Builder getThis() {
            return this;
        }

        public BasicMultipleInsertMethodGenerator build() {
            return new BasicMultipleInsertMethodGenerator(this);
        }
    }
}
