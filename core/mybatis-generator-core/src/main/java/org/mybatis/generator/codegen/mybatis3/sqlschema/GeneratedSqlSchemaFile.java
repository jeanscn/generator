package org.mybatis.generator.codegen.mybatis3.sqlschema;

import org.mybatis.generator.api.GeneratedFile;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.codegen.mybatis3.AbstractGeneratedFile;

/**
 * 生成sql脚本文件
 *
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-06-19 23:32
 * @version 3.0
 */
public class GeneratedSqlSchemaFile extends AbstractGeneratedFile {
    public final AbstractSqlScriptGenerator sqlScriptGenerator;

    public GeneratedSqlSchemaFile(String fileName,
                                  String targetPackage,
                                  String targetProject,
                                  IntrospectedTable introspectedTable,
                                  AbstractSqlScriptGenerator sqlScriptGenerator) {
        super(targetProject,targetPackage,fileName,introspectedTable);
        this.sqlScriptGenerator = sqlScriptGenerator;
    }

    @Override
    public String getFormattedContent() {
        return sqlScriptGenerator.getSqlScript();
    }
}
