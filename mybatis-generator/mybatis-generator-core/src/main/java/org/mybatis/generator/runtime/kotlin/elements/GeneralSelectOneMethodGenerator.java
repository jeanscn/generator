package org.mybatis.generator.runtime.kotlin.elements;

import java.util.Objects;

import org.mybatis.generator.api.dom.kotlin.KotlinArg;
import org.mybatis.generator.api.dom.kotlin.KotlinFile;
import org.mybatis.generator.api.dom.kotlin.KotlinFunction;

/**
 * @author cen_c
 */
public class GeneralSelectOneMethodGenerator extends AbstractKotlinFunctionGenerator {
    private final String mapperName;

    private GeneralSelectOneMethodGenerator(Builder builder) {
        super(builder);
        mapperName = Objects.requireNonNull(builder.mapperName);
    }

    @Override
    public KotlinFunctionAndImports generateMethodAndImports() {
        KotlinFunctionAndImports functionAndImports = KotlinFunctionAndImports.withFunction(
                KotlinFunction.newOneLineFunction(mapperName + ".selectOne")
                .withArgument(KotlinArg.newArg("completer")
                        .withDataType("SelectCompleter")
                        .build())
                .withCodeLine("selectOne(this::selectOne, columnList, " + tableFieldName
                        + ", completer)")
                .build())
                .withImport("org.mybatis.dynamic.sql.util.kotlin.SelectCompleter")
                .withImport("org.mybatis.dynamic.sql.util.kotlin.mybatis3.selectOne")
                .build();

        addFunctionComment(functionAndImports);
        return functionAndImports;
    }

    @Override
    public boolean callPlugins(KotlinFunction kotlinFunction, KotlinFile kotlinFile) {
        return context.getPlugins().clientSelectOneMethodGenerated(kotlinFunction, kotlinFile, introspectedTable);
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

        public GeneralSelectOneMethodGenerator build() {
            return new GeneralSelectOneMethodGenerator(this);
        }
    }
}
