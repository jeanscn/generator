package org.mybatis.generator.config;

import lombok.Getter;
import lombok.Setter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.internal.util.StringUtility;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Setter
@Getter
public class VOCacheGeneratorConfiguration extends AbstractGeneratorConfiguration {

    private Set<String> includeColumns = new HashSet<>();

    private FullyQualifiedJavaType fullyQualifiedJavaType;

    private String typeColumn;

    private String keyColumn;

    private String valueColumn;

    public VOCacheGeneratorConfiguration(Context context, TableConfiguration tc) {
        super();
        this.generate = false;
        targetProject = context.getJavaModelGeneratorConfiguration().getTargetProject();
        baseTargetPackage = StringUtility.substringBeforeLast(context.getJavaModelGeneratorConfiguration().getTargetPackage(), ".")+".pojo";
        targetPackage = String.join(".", baseTargetPackage,"po");
        fullyQualifiedJavaType = new FullyQualifiedJavaType(String.join(".",targetPackage,tc.getDomainObjectName()+"CachePO"));
    }

    @Override
    public void validate(List<String> errors, String contextId) {
        super.validate(errors, contextId, "VOCacheGeneratorConfiguration");
    }
}
