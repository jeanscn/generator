package org.mybatis.generator.plugins;

import com.vgosoft.core.db.enums.JDBCTypeTypeEnum;
import org.mybatis.generator.api.GeneratedFile;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.codegen.mybatis3.sqlschema.GeneratedSqlSchemaFile;
import org.mybatis.generator.codegen.mybatis3.sqlschema.SqlDataSysMenuScriptGenerator;
import org.mybatis.generator.custom.db.DatabaseDDLDialects;

import java.sql.JDBCType;
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
        if (this.context.getSysMenuDataScriptLines().size() == 0) {
            return answer;
        }
        String fileName = "data-menu-"+this.context.getModuleKeyword().toLowerCase()+".sql";
        GeneratedSqlSchemaFile generatedSqlSchemaFile = new GeneratedSqlSchemaFile(fileName,
                "init",
                "src/main/resources/sql",
                null,
                new SqlDataSysMenuScriptGenerator(this.context, DatabaseDDLDialects.getDatabaseDialect("MYSQL")));
        answer.add(generatedSqlSchemaFile);
        return answer;
    }

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }



}
