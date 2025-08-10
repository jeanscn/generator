package org.mybatis.generator.runtime.kotlin.elements;

import org.mybatis.generator.api.dom.kotlin.FullyQualifiedKotlinType;
import org.mybatis.generator.api.dom.kotlin.KotlinArg;
import org.mybatis.generator.api.dom.kotlin.KotlinFile;
import org.mybatis.generator.api.dom.kotlin.KotlinFunction;

/**
 * @author cen_c
 */
public class BasicSelectOneMethodGenerator extends AbstractKotlinFunctionGenerator {

    private final FullyQualifiedKotlinType recordType;
    private final String resultMapId;
    private final KotlinFragmentGenerator fragmentGenerator;
    private final boolean reuseResultMap;

    private BasicSelectOneMethodGenerator(Builder builder) {
        super(builder);
        recordType = builder.recordType;
        resultMapId = builder.resultMapId;
        fragmentGenerator = builder.fragmentGenerator;
        reuseResultMap = builder.reuseResultMap;
    }

    @Override
    public KotlinFunctionAndImports generateMethodAndImports() {
        KotlinFunctionAndImports functionAndImports = KotlinFunctionAndImports
                .withFunction(KotlinFunction.newOneLineFunction("selectOne")
                        .withExplicitReturnType(recordType.getShortNameWithTypeArguments() + "?")
                        .withArgument(KotlinArg.newArg("selectStatement")
                                .withDataType("SelectStatementProvider")
                                .build())
                        .withAnnotation("@SelectProvider(type=SqlProviderAdapter::class,"
                                + " method=\"select\")")
                        .build())
                .withImport("org.mybatis.dynamic.sql.select.render.SelectStatementProvider")
                .withImport("org.mybatis.dynamic.sql.util.SqlProviderAdapter")
                .withImport("org.apache.ibatis.annotations.SelectProvider")
                .withImports(recordType.getImportList())
                .build();

        addFunctionComment(functionAndImports);

        if (reuseResultMap) {
            functionAndImports.getImports().add("org.apache.ibatis.annotations.ResultMap");
            functionAndImports.getFunction().addAnnotation("@ResultMap(\""
                    + resultMapId
                    + "\")");
        } else {
            KotlinFunctionParts functionParts = fragmentGenerator.getAnnotatedResults();
            acceptParts(functionAndImports, functionParts);
        }

        return functionAndImports;
    }

    @Override
    public boolean callPlugins(KotlinFunction kotlinFunction, KotlinFile kotlinFile) {
        return context.getPlugins().clientBasicSelectOneMethodGenerated(kotlinFunction, kotlinFile, introspectedTable);
    }

    public static class Builder extends BaseBuilder<Builder> {

        private FullyQualifiedKotlinType recordType;
        private String resultMapId;
        private KotlinFragmentGenerator fragmentGenerator;
        private boolean reuseResultMap;

        public Builder withRecordType(FullyQualifiedKotlinType recordType) {
            this.recordType = recordType;
            return this;
        }

        public Builder withResultMapId(String resultMapId) {
            this.resultMapId = resultMapId;
            return this;
        }

        public Builder withFragmentGenerator(KotlinFragmentGenerator fragmentGenerator) {
            this.fragmentGenerator = fragmentGenerator;
            return this;
        }

        public Builder withReuseResultMap(boolean reuseResultMap) {
            this.reuseResultMap = reuseResultMap;
            return this;
        }

        @Override
        public Builder getThis() {
            return this;
        }

        public BasicSelectOneMethodGenerator build() {
            return new BasicSelectOneMethodGenerator(this);
        }
    }
}
