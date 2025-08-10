package org.mybatis.generator.runtime.kotlin.elements;

import org.mybatis.generator.api.dom.kotlin.FullyQualifiedKotlinType;
import org.mybatis.generator.api.dom.kotlin.KotlinArg;
import org.mybatis.generator.api.dom.kotlin.KotlinFile;
import org.mybatis.generator.api.dom.kotlin.KotlinFunction;

public class BasicSelectManyMethodGenerator extends AbstractKotlinFunctionGenerator {
    private final FullyQualifiedKotlinType recordType;
    private final KotlinFragmentGenerator fragmentGenerator;

    private BasicSelectManyMethodGenerator(Builder builder) {
        super(builder);
        recordType = builder.recordType;
        fragmentGenerator = builder.fragmentGenerator;
    }

    @Override
    public KotlinFunctionAndImports generateMethodAndImports() {
        KotlinFunctionAndImports functionAndImports = KotlinFunctionAndImports.withFunction(
                KotlinFunction.newOneLineFunction("selectMany")
                .withExplicitReturnType("List<"
                        + recordType.getShortNameWithTypeArguments()
                        + ">")
                .withArgument(KotlinArg.newArg("selectStatement")
                        .withDataType("SelectStatementProvider")
                        .build())
                .withAnnotation("@SelectProvider(type=SqlProviderAdapter::class, method=\"select\")")
                .build())
                .withImport("org.mybatis.dynamic.sql.select.render.SelectStatementProvider")
                .withImport("org.mybatis.dynamic.sql.util.SqlProviderAdapter")
                .withImport("org.apache.ibatis.annotations.SelectProvider")
                .withImports(recordType.getImportList())
                .build();

        addFunctionComment(functionAndImports);

        KotlinFunctionParts functionParts = fragmentGenerator.getAnnotatedResults();
        acceptParts(functionAndImports, functionParts);

        return functionAndImports;
    }

    @Override
    public boolean callPlugins(KotlinFunction kotlinFunction, KotlinFile kotlinFile) {
        return context.getPlugins().clientBasicSelectManyMethodGenerated(kotlinFunction, kotlinFile, introspectedTable);
    }

    public static class Builder extends BaseBuilder<Builder> {
        private FullyQualifiedKotlinType recordType;
        private KotlinFragmentGenerator fragmentGenerator;

        public Builder withRecordType(FullyQualifiedKotlinType recordType) {
            this.recordType = recordType;
            return this;
        }

        public Builder withFragmentGenerator(KotlinFragmentGenerator fragmentGenerator) {
            this.fragmentGenerator = fragmentGenerator;
            return this;
        }

        @Override
        public Builder getThis() {
            return this;
        }

        public BasicSelectManyMethodGenerator build() {
            return new BasicSelectManyMethodGenerator(this);
        }
    }
}
