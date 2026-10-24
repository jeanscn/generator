package org.mybatis.generator.codegen;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.mybatis.generator.api.AbstractIntrospectedTable;
import org.mybatis.generator.api.ProgressCallback;
import org.mybatis.generator.config.Context;

import java.util.List;

/**
 * @author cen_c
 */
@Getter
@Setter
@NoArgsConstructor
public abstract class AbstractGenerator {

    public static final String PUBLISHER_FIELD_NAME = "publisher";

    protected Context context;
    protected AbstractIntrospectedTable introspectedTable;
    protected List<String> warnings;
    protected ProgressCallback progressCallback;

    protected AbstractGenerator(Context context, AbstractIntrospectedTable introspectedTable, List<String> warnings, ProgressCallback progressCallback) {
        this.context = context;
        this.introspectedTable = introspectedTable;
        this.warnings = warnings;
        this.progressCallback = progressCallback;
    }

}
