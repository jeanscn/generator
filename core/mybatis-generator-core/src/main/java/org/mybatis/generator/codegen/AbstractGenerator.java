package org.mybatis.generator.codegen;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.ProgressCallback;
import org.mybatis.generator.config.Context;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public abstract class AbstractGenerator {

    public static final String PUBLISHER_FIELD_NAME = "publisher";

    protected Context context;
    protected IntrospectedTable introspectedTable;
    protected List<String> warnings;
    protected ProgressCallback progressCallback;

    protected AbstractGenerator(Context context, IntrospectedTable introspectedTable, List<String> warnings, ProgressCallback progressCallback) {
        this.context = context;
        this.introspectedTable = introspectedTable;
        this.warnings = warnings;
        this.progressCallback = progressCallback;
    }

}
