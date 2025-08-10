package org.mybatis.generator.runtime.kotlin.elements;

import org.mybatis.generator.api.dom.kotlin.KotlinArg;
import org.mybatis.generator.api.dom.kotlin.KotlinFile;
import org.mybatis.generator.api.dom.kotlin.KotlinFunction;

public class GeneralSelectMethodGenerator extends AbstractKotlinFunctionGenerator {
    private final String mapperName;

    private GeneralSelectMethodGenerator(Builder builder) {
        super(builder);
        mapperName = builder.mapperName;
    }

    @Override
    public KotlinFunctionAndImports generateMethodAndImports() {
        KotlinFunctionAndImports functionAndImports = KotlinFunctionAndImports.withFunction(
                KotlinFunction.newOneLineFunction(mapperName + ".select")
                .withArgument(KotlinArg.newArg("completer")
                        .withDataType("SelectCompleter")
                        .build())
                .withCodeLine("selectList(this::selectMany, columnList, " + tableFieldName
                        + ", completer)")
                .build())
                .withImport("org.mybatis.dynamic.sql.util.kotlin.SelectCompleter")
                .withImport("org.mybatis.dynamic.sql.util.kotlin.mybatis3.selectList")
                .build();

        addFunctionComment(functionAndImports);
        return functionAndImports;
    }

    @Override
    public boolean callPlugins(KotlinFunction kotlinFunction, KotlinFile kotlinFile) {
        return context.getPlugins().clientGeneralSelectMethodGenerated(kotlinFunction, kotlinFile, introspectedTable);
    }

    public static class Builder extends BaseBuilder<Builder> {
        private String mapperName;

        public Builder withMapperName(String mapperName) {
            this.mapperName = mapperName;
            return this;
        }

        @Override
        public Builder getThis() {
            return this;
        }

        public GeneralSelectMethodGenerator build() {
            return new GeneralSelectMethodGenerator(this);
        }
    }
}
