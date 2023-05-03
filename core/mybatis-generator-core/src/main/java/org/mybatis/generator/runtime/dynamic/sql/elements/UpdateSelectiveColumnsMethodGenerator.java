package org.mybatis.generator.runtime.dynamic.sql.elements;

import java.util.HashSet;
import java.util.Set;

import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;

public class UpdateSelectiveColumnsMethodGenerator extends AbstractMethodGenerator {
    private final FullyQualifiedJavaType recordType;
    private final FragmentGenerator fragmentGenerator;

    private UpdateSelectiveColumnsMethodGenerator(Builder builder) {
        super(builder);
        recordType = builder.recordType;
        fragmentGenerator = builder.fragmentGenerator;
    }

    @Override
    public MethodAndImports generateMethodAndImports() {
        Set<FullyQualifiedJavaType> imports = new HashSet<>();

        FullyQualifiedJavaType parameterAndReturnType = new FullyQualifiedJavaType(
                "org.mybatis.dynamic.sql.update.UpdateDSL"); //$NON-NLS-1$
        parameterAndReturnType.addTypeArgument(new FullyQualifiedJavaType(
                "org.mybatis.dynamic.sql.update.UpdateModel")); //$NON-NLS-1$
        imports.add(parameterAndReturnType);

        imports.add(recordType);

        Method method = new Method("updateSelectiveColumns"); //$NON-NLS-1$
        method.setStatic(true);
        context.getCommentGenerator().addGeneralMethodAnnotation(method, introspectedTable, imports);

        method.setReturnType(parameterAndReturnType);
        method.addParameter(new Parameter(recordType, "row")); //$NON-NLS-1$
        method.addParameter(new Parameter(parameterAndReturnType, "dsl")); //$NON-NLS-1$

        method.addBodyLines(fragmentGenerator.getSetEqualWhenPresentLines(introspectedTable.getAllColumns(),
                "return dsl", "        ", true)); //$NON-NLS-1$ //$NON-NLS-2$

        return MethodAndImports.withMethod(method)
                .withImports(imports)
                .build();
    }

    @Override
    public boolean callPlugins(Method method, Interface interfaze) {
        return context.getPlugins().clientUpdateSelectiveColumnsMethodGenerated(method, interfaze, introspectedTable);
    }

    public static class Builder extends BaseBuilder<Builder> {
        private FullyQualifiedJavaType recordType;
        private FragmentGenerator fragmentGenerator;

        public Builder withRecordType(FullyQualifiedJavaType recordType) {
            this.recordType = recordType;
            return this;
        }

        public Builder withFragmentGenerator(FragmentGenerator fragmentGenerator) {
            this.fragmentGenerator = fragmentGenerator;
            return this;
        }

        @Override
        public Builder getThis() {
            return this;
        }

        public UpdateSelectiveColumnsMethodGenerator build() {
            return new UpdateSelectiveColumnsMethodGenerator(this);
        }
    }
}
