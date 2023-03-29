package org.mybatis.generator.plugins;

import org.mybatis.generator.api.GeneratedFile;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.codegen.mybatis3.sqlschema.GeneratedSqlSchemaFile;
import org.mybatis.generator.codegen.mybatis3.sqlschema.SqlDataSysMenuScriptGenerator;
import org.mybatis.generator.codegen.mybatis3.sqlschema.SqlDataSysModuleScriptGenerator;
import org.mybatis.generator.custom.db.DatabaseDDLDialects;

import java.util.ArrayList;
import java.util.List;

/**
 * 生成附件文件的插件
 *
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-04-19 14:51
 * @version 3.0
 */
public class GenerateAdditionalFilesPlugin extends PluginAdapter {

    public GenerateAdditionalFilesPlugin() {
        super();
    }

    /**
     * 生成菜单期初数据sql脚本文件
     *
     */
    @Override
    public List<GeneratedFile> contextGenerateAdditionalFiles() {
        List<GeneratedFile> answer = new ArrayList<>();

        //menu data
        if (this.context.getSysMenuDataScriptLines().size()>0 && context.isUpdateMenuData()) {
            String menuDataFileName = context.getMenuDataFileName();
            GeneratedSqlSchemaFile generatedMenuDataFile = new GeneratedSqlSchemaFile(menuDataFileName,
                    "init",
                    "src/main/resources/sql",
                    null,
                    new SqlDataSysMenuScriptGenerator(this.context, DatabaseDDLDialects.getDatabaseDialect("MYSQL")));
            answer.add(generatedMenuDataFile);
        }

        //module data
        if (this.context.getModuleDataScriptLines().size()>0 && context.isUpdateModuleData()) {
            String moduleDataFileName = context.getModuleDataFileName();
            GeneratedSqlSchemaFile generatedModuleDataFile = new GeneratedSqlSchemaFile(moduleDataFileName,
                    "init",
                    "src/main/resources/sql",
                    null,
                    new SqlDataSysModuleScriptGenerator(this.context, DatabaseDDLDialects.getDatabaseDialect("MYSQL")));
            answer.add(generatedModuleDataFile);
        }
        return answer;
    }

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }



}
