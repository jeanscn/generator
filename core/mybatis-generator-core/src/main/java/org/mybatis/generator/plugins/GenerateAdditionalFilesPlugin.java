package org.mybatis.generator.plugins;

import org.mybatis.generator.api.GeneratedFile;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.codegen.mybatis3.sqlschema.GeneratedSqlSchemaFile;
import org.mybatis.generator.codegen.mybatis3.sqlschema.SqlDataSysMenuScriptGenerator;
import org.mybatis.generator.codegen.mybatis3.sqlschema.SqlDataSysModuleScriptGenerator;
import org.mybatis.generator.codegen.mybatis3.sqlschema.SqlDataWfProcTypeScriptGenerator;
import org.mybatis.generator.custom.db.DatabaseDDLDialects;
import org.mybatis.generator.dbstrategy.DatabaseIntrospectorFactory;

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
        DatabaseDDLDialects currentDialect = resolveContextDialect();

        //menu data
        if (!this.context.getSysMenuDataScriptLines().isEmpty() && context.isUpdateMenuData()) {
            String menuDataFileName = context.getMenuDataFileName();
            GeneratedSqlSchemaFile generatedMenuDataFile = new GeneratedSqlSchemaFile(menuDataFileName,
                    "init",
                    "src/main/resources/sql",
                    null,
                    new SqlDataSysMenuScriptGenerator(this.context, currentDialect));
            answer.add(generatedMenuDataFile);
        }

        //module data
        if (!this.context.getModuleDataScriptLines().isEmpty() && context.isUpdateModuleData()) {
            String moduleDataFileName = context.getModuleDataFileName();
            GeneratedSqlSchemaFile generatedModuleDataFile = new GeneratedSqlSchemaFile(moduleDataFileName,
                    "init",
                    "src/main/resources/sql",
                    null,
                    new SqlDataSysModuleScriptGenerator(this.context, currentDialect));
            answer.add(generatedModuleDataFile);
        }

        //WF ProcType data
        if (!this.context.getWfProcTypeDataScriptLines().isEmpty()) {
            String wfProcTypeDataFileName = "data-wf-proc-type-" + this.context.getModuleKeyword().toLowerCase() + ".sql";
            GeneratedSqlSchemaFile generatedWfProcTypeDataFile = new GeneratedSqlSchemaFile(wfProcTypeDataFileName,
                    "init",
                    "src/main/resources/sql",
                    null,
                    new SqlDataWfProcTypeScriptGenerator(this.context, currentDialect));
            answer.add(generatedWfProcTypeDataFile);
        }
        return answer;
    }

    private DatabaseDDLDialects resolveContextDialect() {
        String jdbcUrl = context.getJdbcConnectionConfiguration() == null
                ? null
                : context.getJdbcConnectionConfiguration().getConnectionURL();
        DatabaseDDLDialects dialect = DatabaseIntrospectorFactory.createProfile(null, jdbcUrl).ddlDialect();
        return dialect == null ? DatabaseDDLDialects.OTHER : dialect;
    }

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }



}
