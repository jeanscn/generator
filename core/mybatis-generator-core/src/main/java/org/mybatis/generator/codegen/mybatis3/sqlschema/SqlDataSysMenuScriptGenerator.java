package org.mybatis.generator.codegen.mybatis3.sqlschema;

import org.mybatis.generator.config.Context;
import org.mybatis.generator.custom.db.DatabaseDDLDialects;

import com.vgosoft.tool.core.VStringUtil;

/**
 * sql脚本生成器
 *
 * @author <a href="mailto:TechCenter@vgosoft.com">vgosoft</a>
 * 2022-06-21 18:53
 * @version 3.0
 */
public class SqlDataSysMenuScriptGenerator extends AbstractSqlScriptGenerator {

    public SqlDataSysMenuScriptGenerator(Context context, DatabaseDDLDialects databaseDdlDialects) {
        this.context = context;
        lines.add("-- ----------------------------");
        lines.add(VStringUtil.format("-- SysMenu Data for {0}", this.context.getModuleKeyword()));
        lines.add("-- ----------------------------");
        lines.add("");
    }

    @Override
    public String getSqlScript() {
        if (!this.context.getSysMenuDataScriptLines().isEmpty()) {
            lines.addAll(this.context.getSysMenuDataScriptLines().values());
        }
        return String.join("\n", getLines());
    }

}
