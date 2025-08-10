package org.mybatis.generator.runtime.kotlin.elements;

import org.mybatis.generator.api.dom.kotlin.FullyQualifiedKotlinType;
import org.mybatis.generator.api.dom.kotlin.KotlinArg;
import org.mybatis.generator.api.dom.kotlin.KotlinFile;
import org.mybatis.generator.api.dom.kotlin.KotlinFunction;

public class BasicInsertMethodGenerator extends AbstractKotlinFunctionGenerator {

    private final FullyQualifiedKotlinType recordType;
    private final KotlinFragmentGenerator fragmentGenerator;
    private final KotlinFile kotlinFile;

    private BasicInsertMethodGenerator(Builder builder) {
        super(builder);
        recordType = builder.recordType;
        fragmentGenerator = builder.fragmentGenerator;
        kotlinFile = builder.kotlinFile;
    }

    @Override
    public KotlinFunctionAndImports generateMethodAndImports() {
        String parameterType = "InsertStatementProvider<"
                + recordType.getShortNameWithTypeArguments()
                + ">";

        KotlinFunctionAndImports functionAndImports = KotlinFunctionAndImports.withFunction(
                KotlinFunction.newOneLineFunction("insert")
                .withExplicitReturnType("Int")
                .withArgument(KotlinArg.newArg("insertStatement")
                        .withDataType(parameterType)
                        .build())
                .withAnnotation("@InsertProvider(type=SqlProviderAdapter::class, method=\"insert\")")
                .build())
                .withImport("org.mybatis.dynamic.sql.util.SqlProviderAdapter")
                .withImport("org.apache.ibatis.annotations.InsertProvider")
                .withImport("org.mybatis.dynamic.sql.insert.render.InsertStatementProvider")
                .withImports(recordType.getImportList())
                .build();


        addFunctionComment(functionAndImports);

        introspectedTable.getGeneratedKey().ifPresent(gk -> {
            KotlinFunctionParts functionParts = fragmentGenerator.getGeneratedKeyAnnotation(gk);
            acceptParts(kotlinFile, functionAndImports.getFunction(), functionParts);
        });

        return functionAndImports;
    }

    @Override
    public boolean callPlugins(KotlinFunction function, KotlinFile kotlinFile) {
        return context.getPlugins().clientBasicInsertMethodGenerated(function, kotlinFile, introspectedTable);
    }

    public static class Builder extends BaseBuilder<Builder> {

        private FullyQualifiedKotlinType recordType;
        private KotlinFragmentGenerator fragmentGenerator;
        private KotlinFile kotlinFile;

        public Builder withRecordType(FullyQualifiedKotlinType recordType) {
            this.recordType = recordType;
            return this;
        }

        public Builder withFragmentGenerator(KotlinFragmentGenerator fragmentGenerator) {
            this.fragmentGenerator = fragmentGenerator;
            return this;
        }

        public Builder withKotlinFile(KotlinFile kotlinFile) {
            this.kotlinFile = kotlinFile;
            return this;
        }

        @Override
        public Builder getThis() {
            return this;
        }

        public BasicInsertMethodGenerator build() {
            return new BasicInsertMethodGenerator(this);
        }
    }
}
