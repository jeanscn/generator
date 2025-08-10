package org.mybatis.generator.runtime.dynamic.sql.elements;

import java.util.HashSet;
import java.util.Set;

import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;

/**
 * @author cen_c
 */
public class UpdateByPrimaryKeySelectiveMethodGenerator extends AbstractMethodGenerator {
    private final FullyQualifiedJavaType recordType;
    private final FragmentGenerator fragmentGenerator;

    private UpdateByPrimaryKeySelectiveMethodGenerator(Builder builder) {
        super(builder);
        recordType = builder.recordType;
        fragmentGenerator = builder.fragmentGenerator;
    }

    @Override
    public MethodAndImports generateMethodAndImports() {
        if (!Utils.generateUpdateByPrimaryKey(introspectedTable)) {
            return null;
        }

        Set<FullyQualifiedJavaType> imports = new HashSet<>();

        imports.add(recordType);

        Method method = new Method("updateByPrimaryKeySelective");
        method.setDefault(true);
        context.getCommentGenerator().addGeneralMethodAnnotation(method, introspectedTable, imports);

        method.setReturnType(FullyQualifiedJavaType.getIntInstance());
        method.addParameter(new Parameter(recordType, "row"));

        method.addBodyLine("return update(c ->");

        method.addBodyLines(
                fragmentGenerator.getSetEqualWhenPresentLines(introspectedTable.getNonPrimaryKeyColumns(),
                        "    c", "    ", false));
        method.addBodyLines(fragmentGenerator.getPrimaryKeyWhereClauseForUpdate("    "));

        method.addBodyLine(");");
        return MethodAndImports.withMethod(method)
                .withImports(imports)
                .build();
    }

    @Override
    public boolean callPlugins(Method method, Interface interfaze) {
        return context.getPlugins()
                .clientUpdateByPrimaryKeySelectiveMethodGenerated(method, interfaze, introspectedTable);
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

        public UpdateByPrimaryKeySelectiveMethodGenerator build() {
            return new UpdateByPrimaryKeySelectiveMethodGenerator(this);
        }
    }
}
