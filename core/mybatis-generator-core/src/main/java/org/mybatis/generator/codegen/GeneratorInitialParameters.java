package org.mybatis.generator.codegen;

import lombok.Getter;
import lombok.Setter;
import org.mybatis.generator.api.AbstractIntrospectedTable;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.ProgressCallback;
import org.mybatis.generator.config.Context;

import java.util.List;

/**
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2023-04-13 12:56
 * @version 3.0
 */
@Setter
@Getter
public class GeneratorInitialParameters {
    private Context context;
    private AbstractIntrospectedTable introspectedTable;
    private List<String> warnings;
    private ProgressCallback progressCallback;

    private IntrospectedColumn introspectedColumn;

    public GeneratorInitialParameters(Context context, AbstractIntrospectedTable introspectedTable, List<String> warnings, ProgressCallback progressCallback) {
        this.context = context;
        this.introspectedTable = introspectedTable;
        this.warnings = warnings;
        this.progressCallback = progressCallback;
    }

}
