package org.mybatis.generator.config;

import lombok.Getter;
import lombok.Setter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.internal.util.StringUtility;

import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author cen_c
 */
@Setter
@Getter
public class VoCacheGeneratorConfiguration extends AbstractGeneratorConfiguration {

    private Set<String> includeColumns = new HashSet<>();

    private FullyQualifiedJavaType fullyQualifiedJavaType;

    private String typeColumn;

    private String keyColumn;

    private String valueColumn;

    private List<String> dictCacheOrderColumns = new ArrayList<>();

    public VoCacheGeneratorConfiguration(Context context, TableConfiguration tc) {
        super();
        this.generate = false;
        targetProject = context.getJavaModelGeneratorConfiguration().getTargetProject();
        baseTargetPackage = StringUtility.substringBeforeLast(context.getJavaModelGeneratorConfiguration().getTargetPackage(), ".")+".pojo";
        targetPackage = String.join(".", baseTargetPackage,"po");
        fullyQualifiedJavaType = new FullyQualifiedJavaType(String.join(".",targetPackage,tc.getDomainObjectName()+"CachePo"));
    }

    @Override
    public void validate(List<String> errors, String contextId) {
        if (!dictCacheOrderColumns.isEmpty()) {
            if (!generate) {
                errors.add("The dictCacheOrderColumns attribute requires generate=\"true\" in context " + contextId);
            }
            if (dictCacheOrderColumns.stream().distinct().count() != dictCacheOrderColumns.size()) {
                errors.add("The dictCacheOrderColumns attribute must not contain duplicate columns in context " + contextId);
            }
        }
        super.validate(errors, contextId, "VoCacheGeneratorConfiguration");
    }
}
