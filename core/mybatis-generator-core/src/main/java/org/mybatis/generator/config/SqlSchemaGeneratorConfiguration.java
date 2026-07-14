package org.mybatis.generator.config;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

/**
 * Sql脚本文件生成配置类
 *
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-06-17 16:16
 * @version 3.0
 */
@Setter
@Getter
public class SqlSchemaGeneratorConfiguration extends AbstractGeneratorConfiguration{

    public String filePrefix;

    public Set<String> additionalDbTypes;

    public SqlSchemaGeneratorConfiguration(Context context, TableConfiguration tc) {
        super();
        this.targetProject = "src/main/resources/sql";
        this.filePrefix = "schema_";
        this.targetPackage = "schema";
        this.additionalDbTypes = Set.of();
    }


    @Override
    void validate(List<String> errors, String contextId) {
        super.validate(errors, contextId, "SqlSchemaGeneratorConfiguration");
    }
}
