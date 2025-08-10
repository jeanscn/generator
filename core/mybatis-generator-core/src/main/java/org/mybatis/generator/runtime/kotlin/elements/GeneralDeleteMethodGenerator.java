package org.mybatis.generator.runtime.kotlin.elements;

import org.mybatis.generator.api.dom.kotlin.KotlinArg;
import org.mybatis.generator.api.dom.kotlin.KotlinFile;
import org.mybatis.generator.api.dom.kotlin.KotlinFunction;

/**
 * @author cen_c
 */
public class GeneralDeleteMethodGenerator extends AbstractKotlinFunctionGenerator {

    private final String mapperName;

    private GeneralDeleteMethodGenerator(Builder builder) {
        super(builder);
        mapperName = builder.mapperName;
    }

    @Override
    public KotlinFunctionAndImports generateMethodAndImports() {
        KotlinFunctionAndImports functionAndImports = KotlinFunctionAndImports.withFunction(
                KotlinFunction.newOneLineFunction(mapperName + ".delete")
                .withArgument(KotlinArg.newArg("completer")
                        .withDataType("DeleteCompleter")
                        .build())
                .withCodeLine("deleteFrom(this::delete, " + tableFieldName + ", completer)")  
                .build())
                .withImport("org.mybatis.dynamic.sql.util.kotlin.DeleteCompleter")
                .withImport("org.mybatis.dynamic.sql.util.kotlin.mybatis3.deleteFrom")
                .build();

        addFunctionComment(functionAndImports);
        return functionAndImports;
    }

    @Override
    public boolean callPlugins(KotlinFunction kotlinFunction, KotlinFile kotlinFile) {
        return context.getPlugins().clientGeneralDeleteMethodGenerated(kotlinFunction, kotlinFile, introspectedTable);
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

        public GeneralDeleteMethodGenerator build() {
            return new GeneralDeleteMethodGenerator(this);
        }
    }
}
