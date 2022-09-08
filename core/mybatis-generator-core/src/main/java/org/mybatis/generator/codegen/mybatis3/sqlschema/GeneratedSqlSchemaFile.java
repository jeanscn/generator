package org.mybatis.generator.codegen.mybatis3.sqlschema;

import org.mybatis.generator.api.GeneratedFile;
import org.mybatis.generator.api.IntrospectedTable;

/**
 * 生成sql脚本文件
 *
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-06-19 23:32
 * @version 3.0
 */
public class GeneratedSqlSchemaFile extends GeneratedFile {

    private final String fileName;
    private final String targetPackage;
    public final IntrospectedTable introspectedTable;
    public final AbstractSqlScriptGenerator sqlScriptGenerator;

    public GeneratedSqlSchemaFile(String fileName,
                                  String targetPackage,
                                  String targetProject,
                                  IntrospectedTable introspectedTable,
                                  AbstractSqlScriptGenerator sqlScriptGenerator) {
        super(targetProject);
        this.fileName = fileName;
        this.targetPackage = targetPackage;
        this.introspectedTable = introspectedTable;
        this.sqlScriptGenerator = sqlScriptGenerator;
    }

    @Override
    public String getFormattedContent() {
        return sqlScriptGenerator.getSqlScript();
    }


    @Override
    public String getFileName() {
        return fileName;
    }


    @Override
    public String getTargetPackage() {
        return targetPackage;
    }


    @Override
    public boolean isMergeable() {
        return false;
    }

    @Override
    public String getFileEncoding() {
        return "UTF-8";
    }
}
