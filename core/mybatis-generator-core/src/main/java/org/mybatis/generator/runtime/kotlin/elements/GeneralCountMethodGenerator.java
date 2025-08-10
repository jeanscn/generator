package org.mybatis.generator.runtime.kotlin.elements;

import java.util.Objects;

import org.mybatis.generator.api.dom.kotlin.KotlinArg;
import org.mybatis.generator.api.dom.kotlin.KotlinFile;
import org.mybatis.generator.api.dom.kotlin.KotlinFunction;

/**
 * @author cen_c
 */
public class GeneralCountMethodGenerator extends AbstractKotlinFunctionGenerator {

    private final String mapperName;
    private final String tableFieldImport;

    private GeneralCountMethodGenerator(Builder builder) {
        super(builder);
        mapperName = Objects.requireNonNull(builder.mapperName);
        tableFieldImport = Objects.requireNonNull(builder.tableFieldImport);
    }

    @Override
    public KotlinFunctionAndImports generateMethodAndImports() {
        KotlinFunctionAndImports functionAndImports = KotlinFunctionAndImports.withFunction(
                KotlinFunction.newOneLineFunction(mapperName + ".count")
                .withArgument(KotlinArg.newArg("completer")
                        .withDataType("CountCompleter")
                        .build())
                .withCodeLine("countFrom(this::count, " + tableFieldName + ", completer)")  
                .build())
                .withImport("org.mybatis.dynamic.sql.util.kotlin.CountCompleter")
                .withImport("org.mybatis.dynamic.sql.util.kotlin.mybatis3.countFrom")
                .withImport(tableFieldImport)
                .build();

        addFunctionComment(functionAndImports);
        return functionAndImports;
    }

    @Override
    public boolean callPlugins(KotlinFunction kotlinFunction, KotlinFile kotlinFile) {
        return context.getPlugins().clientGeneralCountMethodGenerated(kotlinFunction, kotlinFile, introspectedTable);
    }

    public static class Builder extends BaseBuilder<Builder> {
        private String mapperName;
        private String tableFieldImport;

        public Builder withTableFieldImport(String tableFieldImport) {
            this.tableFieldImport = tableFieldImport;
            return this;
        }

        public Builder withMapperName(String mapperName) {
            this.mapperName = mapperName;
            return this;
        }

        @Override
        public Builder getThis() {
            return this;
        }

        public GeneralCountMethodGenerator build() {
            return new GeneralCountMethodGenerator(this);
        }
    }
}
